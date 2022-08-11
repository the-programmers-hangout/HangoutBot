import java.util.*

group = "me.markhc"
version = "4.0.0"
description = "hangoutbot"

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.23.3")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"

        Properties().apply {
            setProperty("name", project.name)
            setProperty("description", project.description)
            setProperty("version", version.toString())
            setProperty("url", "https://github.com/the-programmers-hangout/HangoutBot")

            store(file("src/main/resources/bot.properties").outputStream(), null)
        }
    }

    shadowJar {
        archiveFileName.set("HangoutBot.jar")
        manifest {
            attributes("Main-Class" to "me.markhc.hangoutbot.MainKt")
        }
    }
}