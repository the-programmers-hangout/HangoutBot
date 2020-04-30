package me.markhc.hangoutbot.services

import com.google.gson.Gson
import java.io.File

data class BotConfiguration(val token: String = "",
                         val ownerId: String = "",
                         val logLevel: String = "WARN");

data class Properties(val version: String = "",
                      val kutils: String = "",
                      val repository: String = "")

fun loadConfig(onFinishedLoading: (BotConfiguration?) -> Unit) {
    val configFile = File("config/config.json")

    if(!configFile.exists()) {
        return onFinishedLoading(null)
    }

    return onFinishedLoading(Gson().fromJson(configFile.readText(), BotConfiguration::class.java))
}