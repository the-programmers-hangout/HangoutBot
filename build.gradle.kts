import org.jetbrains.kotlin.config.KotlinCompilerVersion

group = "me.markhc"
version = Versions.BotVersion
description = "hangoutbot"

object Versions {
    const val BotVersion = "3.0.0"
    const val DiscordKt = "0.21.3"
    const val Fuel = "2.3.1"
    const val JodaTime = "2.10.10"
    const val Cowsay = "1.1.0"
    const val Mockk = "1.11.0"
    const val JUnit = "5.7.0"
}

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("com.github.ben-manes.versions") version "0.38.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("scripting-compiler-embeddable", KotlinCompilerVersion.VERSION))
    implementation(kotlin("compiler-embeddable", KotlinCompilerVersion.VERSION))
    implementation(kotlin("script-runtime", KotlinCompilerVersion.VERSION))
    implementation(kotlin("script-util", KotlinCompilerVersion.VERSION))

    implementation("me.jakejmattson:DiscordKt:${Versions.DiscordKt}")
    implementation("com.github.kittinunf.fuel:fuel-gson:${Versions.Fuel}")
    implementation("com.github.kittinunf.fuel:fuel:${Versions.Fuel}")
    implementation("joda-time:joda-time:${Versions.JodaTime}")
    implementation("com.github.ricksbrown:cowsay:${Versions.Cowsay}")

    testImplementation("io.mockk:mockk:${Versions.Mockk}")
    testImplementation(platform("org.junit:junit-bom:${Versions.JUnit}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    shadowJar {
        archiveFileName.set("HangoutBot-${Versions.BotVersion}.jar")
        manifest {
            attributes(
                "Main-Class" to "me.markhc.hangoutbot.MainKt"
            )
        }
    }
}