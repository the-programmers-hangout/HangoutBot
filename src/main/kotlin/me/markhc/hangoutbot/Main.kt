package me.markhc.hangoutbot;

import com.google.gson.Gson
import me.jakejmattson.kutils.api.dsl.configuration.startBot
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.dataclasses.loadConfig
import java.awt.Color

fun main(args: Array<String>) {
    loadConfig {
        System.setProperty("logback.configurationFile", "classpath:logback.xml");
        val configuration = it ?: throw Exception("Failed to parse configuration")
        val propFile = Properties::class.java.getResource("/hangoutbot_properties.json").readText()
        val properties = Gson().fromJson(propFile, Properties::class.java)
                ?: throw Exception("Failed to parse properties")

        startBot(configuration.token) {
            registerInjectionObjects(configuration, properties)

            configure {
                colors {
                    infoColor = Color.CYAN
                    failureColor = Color.RED
                    successColor = Color.GREEN
                }
                commandReaction = null
                allowMentionPrefix = true
                requiresGuild = false
            }
        }
    }
}