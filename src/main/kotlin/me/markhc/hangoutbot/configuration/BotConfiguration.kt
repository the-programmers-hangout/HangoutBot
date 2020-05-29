package me.markhc.hangoutbot.configuration

import com.google.gson.Gson
import me.aberrantfox.kjdautils.api.annotation.Data
import java.io.File

@Data("config/config.json")
data class BotConfiguration(val token: String = "",
                            var prefix: String = "++",
                            val ownerId: String = "");

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