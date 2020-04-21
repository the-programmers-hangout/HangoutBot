package me.markhc.tphbot;

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.dsl.PrefixDeleteMode
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.api.startBot;
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.tphbot.services.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        loadConfig {
            val configuration = it ?: throw Exception("Failed to parse configuration");

            startBot(configuration.token) {
                createDatabaseSchema(configuration)
                registerInjectionObject(configuration)

                configure {
                    prefix = "++"
                    deleteMode = PrefixDeleteMode.None
                }
            }
        }
    } catch (e: Exception) {
        println(e.message)
        exitProcess(-1)
    }
}