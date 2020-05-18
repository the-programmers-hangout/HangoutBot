package me.markhc.hangoutbot;

import com.google.gson.Gson
import me.aberrantfox.kjdautils.api.dsl.PrefixDeleteMode
import me.aberrantfox.kjdautils.api.startBot
import me.markhc.hangoutbot.services.Properties
import me.markhc.hangoutbot.services.loadConfig
import java.awt.Color

fun main(args: Array<String>) {
    loadConfig {
        val configuration = it ?: throw Exception("Failed to parse configuration");
        val propFile = Properties::class.java.getResource("/hangoutbot_properties.json").readText()
        val properties = Gson().fromJson(propFile, Properties::class.java) ?: throw Exception("Failed to parse properties");

        startBot(configuration.token) {
            registerInjectionObjects(configuration, properties)

            configure {
                colors {
                    infoColor = Color.CYAN
                    failureColor = Color.RED
                    successColor = Color.GREEN
                }
                commandReaction = null
                deleteMode = PrefixDeleteMode.None
                allowMentionPrefix = true
                allowPrivateMessages = true
            }
        }
    }
}