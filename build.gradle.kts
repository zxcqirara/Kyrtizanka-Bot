import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val ktormVersion: String by project
val exposedVersion: String by project

plugins {
	kotlin("jvm") version "1.8.0"
	kotlin("plugin.serialization") version "1.8.0"
	application
	id("com.palantir.git-version") version "0.15.0"
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()

val versionFile = file("src/main/resources/version.txt")
if (!versionFile.exists()) versionFile.createNewFile()
versionFile.writeText(version.toString())

val ktorVersion = "2.2.3"

repositories {
	mavenCentral()
	maven("https://europe-west3-maven.pkg.dev/mik-music/kord")
	maven("https://oss.sonatype.org/content/repositories/snapshots/")
	maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
	maven("https://m2.dv8tion.net/releases")
	maven("https://jitpack.io")
	maven("https://repo.kotlin.link")
}

dependencies {
	implementation("dev.kord:kord-core:0.14.0-SNAPSHOT")
	implementation("dev.kord:kord-voice:0.14.0-SNAPSHOT")
	implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.7.1-SNAPSHOT")
	implementation("com.kotlindiscord.kord.extensions:time4j:1.7.1-SNAPSHOT")

	implementation("io.sentry:sentry:6.11.0")
	implementation("ch.qos.logback:logback-classic:1.4.12")

	implementation(kotlin("scripting-jsr223"))
	runtimeOnly(kotlin("script-runtime"))

	implementation("io.github.config4k:config4k:0.5.0")
	implementation("com.squareup.okio:okio:3.5.0")

	implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
	implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
	implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
	implementation("org.jetbrains.exposed:exposed-kotlin-datetime:${exposedVersion}")
	implementation("com.impossibl.pgjdbc-ng", "pgjdbc-ng", "0.8.3")

	implementation("com.sedmelluq:lavaplayer:1.3.77")
	implementation("com.github.aikaterna:lavaplayer-natives:original-SNAPSHOT")

	implementation("org.knowm.xchart:xchart:3.8.3")

	implementation("io.ktor:ktor-client-core:$ktorVersion")
	implementation("io.ktor:ktor-client-cio:$ktorVersion")
	implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}

application {
	mainClass.set("bot.AppKt")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "17"
	}
}