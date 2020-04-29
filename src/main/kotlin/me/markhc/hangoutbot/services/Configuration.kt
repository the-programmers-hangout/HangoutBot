package me.markhc.hangoutbot.services

import com.beust.klaxon.Klaxon
import java.io.File

data class Configuration(val token: String = "",
                         val ownerId: String = "",
                         val logLevel: String = "WARN");

data class Properties(val version: String, val kutils: String, val repository: String)

fun loadConfig(onFinishedLoading: (Configuration?) -> Unit) {
    val configFile = File("config/config.json")

    if(!configFile.exists()) {
        return onFinishedLoading(null)
    }

    return onFinishedLoading(Klaxon().parse(configFile))

}