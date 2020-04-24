package me.markhc.hangoutbot;

import me.aberrantfox.kjdautils.api.dsl.PrefixDeleteMode
import me.aberrantfox.kjdautils.api.startBot;
import me.markhc.hangoutbot.services.*
import mu.KotlinLogging
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        loadConfig {
            val configuration = it ?: throw Exception("Failed to parse configuration");

            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, configuration.logLevel);

            val logger = KotlinLogging.logger {}

            startBot(configuration.token) {
                createDatabaseSchema(configuration)
                registerInjectionObject(configuration, logger)

                configure {
                    prefix = "+"
                    deleteMode = PrefixDeleteMode.None
                    allowPrivateMessages = false
                }
            }
        }
    } catch (e: Exception) {
        println(e.message)
        exitProcess(-1)
    }
}