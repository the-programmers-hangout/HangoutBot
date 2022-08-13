group = "me.markhc"
version = "4.0.0"
description = "A misc feature bot for TheProgrammersHangout"

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
        dependsOn("writeProperties")
    }

    register<WriteProperties>("writeProperties") {
        property("name", project.name)
        property("description", project.description.toString())
        property("version", version.toString())
        property("url", "https://github.com/the-programmers-hangout/HangoutBot")
        setOutputFile("src/main/resources/bot.properties")
    }

    shadowJar {
        archiveFileName.set("HangoutBot.jar")
        manifest {
            attributes("Main-Class" to "me.markhc.hangoutbot.MainKt")
        }
    }
}