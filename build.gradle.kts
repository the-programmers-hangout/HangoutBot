import org.jetbrains.kotlin.config.KotlinCompilerVersion

group = "me.markhc"
version = "2.1.0"
description = "hangoutbot"

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.33.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(kotlin("scripting-compiler-embeddable", KotlinCompilerVersion.VERSION))
    implementation(kotlin("compiler-embeddable", KotlinCompilerVersion.VERSION))
    implementation(kotlin("script-runtime", KotlinCompilerVersion.VERSION))
    implementation(kotlin("script-util", KotlinCompilerVersion.VERSION))

    implementation("me.jakejmattson:DiscordKt:${Versions.DISCORDKT}")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.0")
    implementation("com.github.kittinunf.fuel:fuel:2.3.0")
    implementation("joda-time:joda-time:2.10.6")
    implementation("com.github.ricksbrown:cowsay:1.1.0")

    testImplementation("io.mockk:mockk:1.10.2")
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
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

    copy {
        val resourcePath = "src/main/resources"
        from(file("$resourcePath/properties-template.json"))
        into(file(resourcePath))
        rename { "hangoutbot_properties.json" }
        expand(
            "version" to version,
            "discordkt" to Versions.DISCORDKT,
            "kotlin" to KotlinCompilerVersion.VERSION,
            "repository" to "https://github.com/the-programmers-hangout/HangoutBot/"
        )
    }
}

object Versions {
    const val DISCORDKT: String = "0.21.0-SNAPSHOT"
}