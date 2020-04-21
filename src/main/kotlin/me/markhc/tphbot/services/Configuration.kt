package me.markhc.tphbot.services

import com.beust.klaxon.JsonBase
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import java.io.File

data class Configuration(val token: String = "<insert-token>",
                         val developmentMode: Boolean = false,
                         val mysqlConfig: MySQLConfig = MySQLConfig());

data class MySQLConfig(val url: String = "localhost:3306",
                       val dbname: String = "tphbot",
                       val username: String = "<user>",
                       val password: String = "<pass>")

fun loadConfig(onFinishedLoading: (Configuration?) -> Unit) {
    val configFile = File("config/config.json")

    if(!configFile.exists()) {
        return onFinishedLoading(null)
    }

    return onFinishedLoading(Klaxon().parse(configFile))

}