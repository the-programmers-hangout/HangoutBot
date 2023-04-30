group = "me.markhc"
version = "4.0.0-RC1"
description = "A misc feature bot for TheProgrammersHangout"

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.24.0-SNAPSHOT")
}

tasks {
    val jvmTarget = "11"

    compileJava {
        sourceCompatibility = jvmTarget
        targetCompatibility = jvmTarget
    }

    compileKotlin {
        kotlinOptions.jvmTarget = jvmTarget
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