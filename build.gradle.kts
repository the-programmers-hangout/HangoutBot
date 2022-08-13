import java.util.*

group = "me.markhc"
version = "4.0.0"
description = "An misc bot for TheProgrammersHangout"

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