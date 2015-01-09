# Development Process

## Dev build
1. ./gradlew clean
2. ./gradlew installDevDebug

## Create SNAPSHOT build
1. Make sure gradle.properties VERSION ends with -SNAPSHOT
2. ./gradlew clean
3. ./gradlew uploadArchives

## QA SNAPSHOT Build
1. ./gradlew clean
2. ./gradlew installQaDebug

## Create RELEASE build
1. Make sure gradle.properties VERSION does not end with -SNAPSHOT
2. ./gradlew clean
3. ./gradlew uploadArchives

## QA RELEASE Build
1. ./gradlew clean
2. ./gradlew installRcRelease
2. ./gradlew installRcRelease