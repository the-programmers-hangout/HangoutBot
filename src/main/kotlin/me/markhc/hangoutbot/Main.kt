package me.markhc.hangoutbot;

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.dsl.PrefixDeleteMode
import me.aberrantfox.kjdautils.api.startBot
import me.markhc.hangoutbot.services.Properties
import me.markhc.hangoutbot.services.loadConfig
import mu.KotlinLogging

fun main(args: Array<String>) {
    loadConfig {
        val configuration = it ?: throw Exception("Failed to parse configuration");
        val propFile = Properties::class.java.getResource("/properties.json").readText()
        val properties = Klaxon().parse<Properties>(propFile) ?: throw Exception("Failed to parse properties");

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, configuration.logLevel);

        val logger = KotlinLogging.logger {}

        startBot(configuration.token) {
            registerInjectionObject(configuration, logger, properties)

            configure {
                prefix = "+"
                deleteMode = PrefixDeleteMode.None
                allowPrivateMessages = false
            }
        }
    }
}