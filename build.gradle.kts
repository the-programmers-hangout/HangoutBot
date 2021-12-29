group = "me.markhc"
version = Versions.BotVersion
description = "hangoutbot"

object Versions {
    const val BotVersion = "4.0.0"

    const val DiscordKt = "0.23.0-SNAPSHOT"
    const val Fuel = "2.3.1"
    const val Cowsay = "1.1.0"
}

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:${Versions.DiscordKt}")
    implementation("com.github.kittinunf.fuel:fuel-gson:${Versions.Fuel}")
    implementation("com.github.kittinunf.fuel:fuel:${Versions.Fuel}")
    implementation("com.github.ricksbrown:cowsay:${Versions.Cowsay}")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        archiveFileName.set("HangoutBot.jar")
        manifest {
            attributes(
                "Main-Class" to "me.markhc.hangoutbot.MainKt"
            )
        }
    }
}