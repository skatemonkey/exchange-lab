param(
    [string]$MySqlContainer = "exchange-lab-mysql",
    [string]$RedisContainer = "exchange-lab-redis"
)

$ErrorActionPreference = "Stop"

function Invoke-MySqlQuery([string]$Query) {
    $rows = & docker exec -e MYSQL_PWD=exchange_lab $MySqlContainer `
        mysql -uexchange_lab --batch --skip-column-names exchange_lab -e $Query
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL verification query failed"
    }
    return @($rows)
}

function Read-RedisKeys([string]$Pattern) {
    $keys = & docker exec $RedisContainer redis-cli --raw --scan --pattern $Pattern
    if ($LASTEXITCODE -ne 0) {
        throw "Redis key scan failed"
    }
    return @($keys | Where-Object { $_ })
}

function Read-RedisValues([string[]]$Keys) {
    if ($Keys.Count -eq 0) {
        return @()
    }

    $values = & docker exec $RedisContainer redis-cli --raw MGET @Keys
    if ($LASTEXITCODE -ne 0) {
        throw "Redis value read failed"
    }
    return @($values)
}

$cashByTrader = @{}
Invoke-MySqlQuery @"
select trader_id,
       cast(round((cash_balance - reserved_cash) * 100000000, 0) as char)
from trader_accounts
"@ | ForEach-Object {
    $parts = $_ -split "`t"
    $cashByTrader[$parts[0].ToLowerInvariant()] = $parts[1]
}

$stockByTraderAndSymbol = @{}
Invoke-MySqlQuery @"
select trader_id,
       symbol,
       cast(round((quantity - reserved_quantity) * 100000000, 0) as char)
from stock_positions
"@ | ForEach-Object {
    $parts = $_ -split "`t"
    $stockByTraderAndSymbol["$($parts[0].ToLowerInvariant()):$($parts[1])"] = $parts[2]
}

$cashKeys = Read-RedisKeys "cash:available:*"
$stockKeys = Read-RedisKeys "stock:available:*"
$cashValues = Read-RedisValues $cashKeys
$stockValues = Read-RedisValues $stockKeys
$cashMismatches = 0
$stockMismatches = 0
$negativeValues = 0

for ($index = 0; $index -lt $cashKeys.Count; $index++) {
    $key = $cashKeys[$index]
    $traderId = $key.Substring("cash:available:".Length).ToLowerInvariant()
    $actual = [string]$cashValues[$index]
    if (-not $cashByTrader.ContainsKey($traderId) -or $cashByTrader[$traderId] -ne $actual) {
        $cashMismatches++
    }
    if ([decimal]$actual -lt 0) {
        $negativeValues++
    }
}

for ($index = 0; $index -lt $stockKeys.Count; $index++) {
    $key = $stockKeys[$index]
    $identity = $key.Substring("stock:available:".Length)
    $separator = $identity.IndexOf(":")
    if ($separator -lt 1) {
        $stockMismatches++
        continue
    }

    $traderId = $identity.Substring(0, $separator).ToLowerInvariant()
    $symbol = $identity.Substring($separator + 1)
    $actual = [string]$stockValues[$index]
    $lookupKey = "$traderId`:$symbol"
    if (-not $stockByTraderAndSymbol.ContainsKey($lookupKey) -or $stockByTraderAndSymbol[$lookupKey] -ne $actual) {
        $stockMismatches++
    }
    if ([decimal]$actual -lt 0) {
        $negativeValues++
    }
}

$checks = @(
    [pscustomobject]@{ Check = "cash_key_count"; Expected = $cashByTrader.Count; Actual = $cashKeys.Count },
    [pscustomobject]@{ Check = "stock_key_count"; Expected = $stockByTraderAndSymbol.Count; Actual = $stockKeys.Count },
    [pscustomobject]@{ Check = "cash_value_mismatches"; Expected = 0; Actual = $cashMismatches },
    [pscustomobject]@{ Check = "stock_value_mismatches"; Expected = 0; Actual = $stockMismatches },
    [pscustomobject]@{ Check = "negative_redis_values"; Expected = 0; Actual = $negativeValues }
)

"check_name`texpected`tactual`tpass"
foreach ($check in $checks) {
    $pass = [int]($check.Actual -eq $check.Expected)
    "$($check.Check)`t$($check.Expected)`t$($check.Actual)`t$pass"
}

if ($checks.Where({ $_.Actual -ne $_.Expected }).Count -gt 0) {
    exit 1
}
