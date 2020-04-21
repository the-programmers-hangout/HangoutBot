package me.markhc.tphbot.services

import com.beust.klaxon.JsonBase
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import java.io.File

data class Configuration(val token: String = "<insert-token>",
                         val developmentMode: Boolean = false);


fun loadConfig(onFinishedLoading: (Configuration?) -> Unit) {
    val configFile = File("config/config.json")

    if(!configFile.exists()) {
        return onFinishedLoading(null)
    }

    return onFinishedLoading(Klaxon().parse(configFile))

}