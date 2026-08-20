param(
    [Parameter(Mandatory = $true)]
    [int] $Rate,

    [Parameter(Mandatory = $true)]
    [int] $Run,

    [string] $ResultDirectory = "$env:TEMP\exchange-lab-c3-results"
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$resultPath = Join-Path $ResultDirectory "rate-$Rate-run-$Run"
$gradleProcess = $null

function Stop-ApplicationProcesses {
    $processIds = Get-NetTCPConnection -LocalPort 8080, 8081, 8082 -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($processId in $processIds) {
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
}

function Get-ConsumerGroupState([string] $groupName) {
    $output = @(& docker exec exchange-lab-kafka /opt/kafka/bin/kafka-consumer-groups.sh `
        --bootstrap-server localhost:9092 --describe --group $groupName 2>&1)
    $active = $false
    $lag = 0L

    foreach ($line in $output) {
        $columns = ($line.ToString().Trim() -split '\s+')
        if ($columns.Length -ge 7 -and $columns[0] -eq $groupName -and $columns[5] -match '^\d+$') {
            $lag += [long] $columns[5]
            if ($columns[6] -ne '-') {
                $active = $true
            }
        }
    }

    return [pscustomobject]@{ Active = $active; Lag = $lag; Output = $output }
}

function Wait-ForGroupsInactive([int] $timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    do {
        $orders = Get-ConsumerGroupState 'exchange-lab-order-matcher'
        $trades = Get-ConsumerGroupState 'exchange-lab-trade-settler'
        if (-not $orders.Active -and -not $trades.Active) {
            return
        }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)

    throw 'Old Kafka consumer-group members did not become inactive.'
}

function Wait-ForGroupsReady([int] $timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    do {
        $orders = Get-ConsumerGroupState 'exchange-lab-order-matcher'
        $trades = Get-ConsumerGroupState 'exchange-lab-trade-settler'
        if ($orders.Active -and $trades.Active -and $orders.Lag -eq 0 -and $trades.Lag -eq 0) {
            return
        }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)

    throw "Kafka consumers not ready: order active=$($orders.Active), lag=$($orders.Lag); trade active=$($trades.Active), lag=$($trades.Lag)"
}

function Wait-ForZeroLag([int] $timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    do {
        $orders = Get-ConsumerGroupState 'exchange-lab-order-matcher'
        $trades = Get-ConsumerGroupState 'exchange-lab-trade-settler'
        if ($orders.Lag -eq 0 -and $trades.Lag -eq 0) {
            return
        }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)

    throw "Kafka backlog did not drain: order lag=$($orders.Lag), trade lag=$($trades.Lag)"
}

function Wait-ForHealth([int] $port) {
    $deadline = (Get-Date).AddSeconds(120)
    do {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:$port/actuator/health" -TimeoutSec 2
            if ($response.StatusCode -eq 200) {
                return
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    } while ((Get-Date) -lt $deadline)

    throw "Service on port $port did not become healthy."
}

function Wait-ForContainerHealth([string] $containerName) {
    $deadline = (Get-Date).AddSeconds(120)
    do {
        $status = (& docker inspect --format '{{.State.Health.Status}}' $containerName 2>$null).Trim()
        if ($status -eq 'healthy') {
            return
        }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)

    throw "$containerName did not become healthy."
}

function Invoke-MySqlFile([string] $filePath) {
    Get-Content -Raw $filePath |
        & docker exec -i exchange-lab-mysql mysql -u exchange_lab -pexchange_lab exchange_lab
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL command failed for $filePath."
    }
}

Set-Location $repositoryRoot
New-Item -ItemType Directory -Force -Path $ResultDirectory | Out-Null
$env:JAVA_HOME = Join-Path $env:USERPROFILE '.jdks\openjdk-26.0.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

try {
    Stop-ApplicationProcesses
    Wait-ForGroupsInactive 90
    Wait-ForContainerHealth 'exchange-lab-mysql'
    Wait-ForContainerHealth 'exchange-lab-kafka'
    Wait-ForContainerHealth 'exchange-lab-redis'
    Wait-ForZeroLag 5

    & docker exec exchange-lab-redis redis-cli FLUSHDB | Out-File "$resultPath-redis-flush.txt"
    if ($LASTEXITCODE -ne 0) {
        throw 'Redis flush failed.'
    }
    Invoke-MySqlFile (Join-Path $repositoryRoot '_support\load-test\seed.sql') | Out-File "$resultPath-seed.txt"

    $gradleProcess = Start-Process -FilePath (Join-Path $repositoryRoot 'gradlew.bat') `
        -ArgumentList '--parallel', '--max-workers=3', ':finance-service:bootRun', ':match-service:bootRun', ':exchange-service:bootRun' `
        -WorkingDirectory $repositoryRoot `
        -RedirectStandardOutput "$resultPath-app.stdout.log" `
        -RedirectStandardError "$resultPath-app.stderr.log" `
        -WindowStyle Hidden `
        -PassThru

    Wait-ForHealth 8081
    Wait-ForHealth 8082
    Wait-ForHealth 8080
    Wait-ForGroupsReady 90
    "Kafka consumers ready at $((Get-Date).ToString('o'))" | Out-File "$resultPath-readiness.txt"
    Start-Sleep -Seconds 10

    & k6 run --quiet `
        -e "RATE=$Rate" `
        -e 'DURATION=30s' `
        -e 'DRAIN_SECONDS=5' `
        --summary-export "$resultPath-summary.json" `
        (Join-Path $repositoryRoot '_support\load-test\k6\02-tps-benchmark.js') 2>&1 |
        Tee-Object -FilePath "$resultPath-console.txt"

    Invoke-MySqlFile (Join-Path $repositoryRoot '_support\load-test\verify.sql') |
        Tee-Object -FilePath "$resultPath-verify-sql.txt"

    $redisOutput = @(& (Join-Path $repositoryRoot '_support\load-test\verify-redis.ps1') 2>&1)
    $redisExitCode = $LASTEXITCODE
    $redisOutput | Tee-Object -FilePath "$resultPath-verify-redis.txt"

    $orders = Get-ConsumerGroupState 'exchange-lab-order-matcher'
    $trades = Get-ConsumerGroupState 'exchange-lab-trade-settler'
    $orders.Output | Tee-Object -FilePath "$resultPath-order-lag.txt"
    $trades.Output | Tee-Object -FilePath "$resultPath-trade-lag.txt"
    "fixed_order_lag=$($orders.Lag) fixed_trade_lag=$($trades.Lag)" |
        Tee-Object -FilePath "$resultPath-fixed-lag.txt"

    if ($redisExitCode -ne 0) {
        throw 'Redis verification failed.'
    }
} finally {
    try {
        Wait-ForZeroLag 120
    } catch {
        Write-Warning $_
    }
    Stop-ApplicationProcesses
    if ($null -ne $gradleProcess -and -not $gradleProcess.HasExited) {
        Stop-Process -Id $gradleProcess.Id -Force -ErrorAction SilentlyContinue
    }
    try {
        Wait-ForGroupsInactive 90
    } catch {
        Write-Warning $_
    }
}
