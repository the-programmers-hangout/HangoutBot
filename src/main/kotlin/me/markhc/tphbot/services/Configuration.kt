package me.markhc.tphbot.services

import com.beust.klaxon.Klaxon
import java.io.File

data class Configuration(val token: String = "",
                         val ownerId: String = "");


fun loadConfig(onFinishedLoading: (Configuration?) -> Unit) {
    val configFile = File("config/config.json")

    if(!configFile.exists()) {
        return onFinishedLoading(null)
    }

    return onFinishedLoading(Klaxon().parse(configFile))

}