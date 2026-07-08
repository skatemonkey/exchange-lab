# AGENTS.md

## Overview

Exchange Lab is an exchange simulator built with Java, Spring Boot, Gradle, and Kafka to practice trading platform backend engineering.

## Tech Stack

- Java 26
- Spring Boot
- Gradle
- Kafka

## Local Testing

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-26.0.1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test
```

## Project Layout

- `_support/docs/`: project documentation and design notes
- `_support/database/`: database schema files
- `_support/load-test/`: k6 scripts, seed data, and verification SQL
- `src/`: application source code and tests
- `build.gradle`: Gradle build configuration
