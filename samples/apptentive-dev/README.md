# Apptentive Dev App
This app is for internal development and testing purposes.

# Setup
Modify gradle.properties in this directory. Fill in the Apptentive API Key for each build flavor you plan to use.

# Build Process

## Clean
1. ./gradlew clean

## Dev build
1. ./gradlew installDevDebug

## Create SNAPSHOT build
1. ./gradlew uploadArchives

## QA SNAPSHOT Build
1. ./gradlew installQaDebug

## Create RELEASE build
1. ./gradlew -PMAVEN_RELEASE_BUILD uploadArchives

## QA RELEASE Build
1. ./gradlew installRcDebug