# Apptentive Dev App
This app is for internal development and testing purposes.

# Setup
Modify gradle.properties in this directory. Fill in the Apptentive API Key for each build flavor you plan to use.

# Build Process

## Dev build
1. ./gradlew installDevDebug

## Create SNAPSHOT build
1. ./gradlew clean uploadArchives

## QA SNAPSHOT Build
1. ./gradlew --refresh-dependencies clean installQaDebug

## Create RELEASE build
1. ./gradlew -DMAVEN_RELEASE_BUILD clean uploadArchives

## QA RELEASE Build
1. ./gradlew clean installRcRelease