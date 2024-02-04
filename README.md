# Coders Squad - Discord Bot
**Discord bot with some helpful things for our (private, lol) discord server**

## Language/Build System

[![Kotlin](https://img.shields.io/badge/kotlin-1.8.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-7.5.1-blue?logo=gradle)](https://gradle.org)

## Dependencies

![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=Kord&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fdev%2Fkord%2Fkord-core%2Fmaven-metadata.xml)
![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=KordEx&metadataUrl=https%3A%2F%2Fmaven.kotlindiscord.com%2Frepository%2Fmaven-public%2Fcom%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions%2Fmaven-metadata.xml&versionPrefix=1.7.1&versionSuffix=-SNAPSHOT)
![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=Time4J&metadataUrl=https%3A%2F%2Fmaven.kotlindiscord.com%2Frepository%2Fmaven-public%2Fcom%2Fkotlindiscord%2Fkord%2Fextensions%2Ftime4j%2Fmaven-metadata.xml&versionPrefix=1.7.1&versionSuffix=-SNAPSHOT)
![Maven Central](https://img.shields.io/maven-central/v/io.sentry/sentry?label=Sentry&versionPrefix=6.11.0)
![Maven Central](https://img.shields.io/maven-central/v/org.slf4j/slf4j-log4j12?label=Log4J&versionPrefix=2.0.5)
![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.kotlin/kotlin-scripting-jsr223?label=Kotlin%20Scripting%20Jsr223)
![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.kotlin/kotlin-script-runtime?label=Kotlin%20Script%20Runtime)
![Maven Central](https://img.shields.io/maven-central/v/io.github.config4k/config4k?label=Config4K&versionPrefix=0.5.0)
![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.exposed/exposed-core?label=Exposed%20Core&versionPrefix=0.39.2)
![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.exposed/exposed-dao?label=Exposed%20DAO&versionPrefix=0.39.2)
![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.exposed/exposed-jdbc?label=Exposed%20JDBC&versionPrefix=0.39.2)
![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.exposed/exposed-kotlin-datetime?label=Exposed%20Kotlin%20DateTime&versionPrefix=0.39.2)
![Maven Central](https://img.shields.io/maven-central/v/org.postgresql/postgresql?label=PostgreSQL&versionPrefix=42.3.5)
![Maven Central](https://img.shields.io/maven-central/v/org.knowm.xchart/xchart?label=XChart&versionPrefix=3.8.3)
![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=LavaPlayer&metadataUrl=https%3A%2F%2Fm2.dv8tion.net%2Freleases%2Fcom%2Fsedmelluq%2Flavaplayer%2Fmaven-metadata.xml&versionPrefix=1.3.77)
![JitPack](https://img.shields.io/jitpack/v/github/aikaterna/lavaplayer-natives?label=LavaPlayer%20Natives)

## Start project

1. Run in console from `cs_dsbot` dir:
```bash
./gradlew build
./gradlew run
```
2. Configure created `config.yml` file
3. Run bot again:
```bash
./gradlew run
```

## Building for deploying to prod
1. Run in console from `cs_dsbot` dir:
```bash
./gradlew shadowJar
```
2. Find compiled JAR file in the `build/libs`
3. Run it with `java -jar FILENAME.jar` command