import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val ktormVersion: String by project
val exposedVersion: String by project

plugins {
	kotlin("jvm") version "1.6.20"
	kotlin("plugin.serialization") version "1.6.20"
	application
}

repositories {
	mavenCentral()
	maven("https://oss.sonatype.org/content/repositories/snapshots")
	maven("https://maven.kotlindiscord.com/repository/maven-public/")
	maven("https://m2.dv8tion.net/releases")
	maven("https://jitpack.io")
	maven("https://repo.kotlin.link")
}

dependencies {
	implementation("dev.kord:kord-core:0.8.x-SNAPSHOT") {
		capabilities {
			requireCapability("dev.kord:core-voice:0.8.x-SNAPSHOT")
		}
	}
	implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.4-SNAPSHOT")
	implementation("com.kotlindiscord.kord.extensions:time4j:1.5.3-SNAPSHOT")

	implementation("io.sentry:sentry:5.7.4")
	implementation("org.slf4j:slf4j-log4j12:2.0.0-alpha1")

	implementation(kotlin("scripting-jsr223"))
	runtimeOnly(kotlin("script-runtime"))

	implementation("com.charleskorn.kaml:kaml:0.46.0")

	implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
	implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
	implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
	implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")
	implementation("com.impossibl.pgjdbc-ng", "pgjdbc-ng", "0.8.3")

	implementation("com.sedmelluq:lavaplayer:1.3.77")
	implementation("com.github.aikaterna:lavaplayer-natives:original-SNAPSHOT")
}

application {
	mainClass.set("bot.AppKt")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "17"
	}
}