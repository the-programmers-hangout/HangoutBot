package me.markhc.tphbot.services

import com.beust.klaxon.JsonBase
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import java.io.File

data class Configuration(val token: String = "<insert-token>",
                         val developmentMode: Boolean = false,
                         val prefix: String = "++",
                         val reactToCommands: Boolean = false);


fun loadConfig(onFinishedLoading: (Configuration?) -> Unit) {
    val configFile = File("config/config.json")

    if(!configFile.exists()) {
        val builder = StringBuilder(Klaxon().toJsonString(Configuration()))
        val jsonData = (Parser().parse(builder) as JsonBase).toJsonString(true)
        configFile.printWriter().use { it.print(jsonData) }

        return onFinishedLoading(null)
    }

    return onFinishedLoading(Klaxon().parse(configFile))

}