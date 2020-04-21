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
        data class Properties(val version: String, val kutils: String, val repository: String)
        val propFile = Properties::class.java.getResource("/properties.json").readText()
        val project = Klaxon().parse<Properties>(propFile) ?: throw Exception("Failed to parse properties");

        loadConfig {
            val configuration = it ?: throw Exception("Failed to parse configuration");

            startBot(configuration.token) {
                registerInjectionObject(it);

                configure {
                    prefix = configuration.prefix
                    reactToCommands = configuration.reactToCommands
                    deleteMode = PrefixDeleteMode.None
                    mentionEmbed = {
                        embed {
                            val channel = it.channel
                            val self = channel.jda.selfUser

                            color = Color(0x00bfff)
                            thumbnail = self.effectiveAvatarUrl
                            addField(self.fullName(), "It's a bot!")
                            addInlineField("Prefix", configuration.prefix)

                            with (project) {
                                val kotlinVersion = KotlinVersion.CURRENT

                                addField("Build Info", "```" +
                                        "Version: $version\n" +
                                        "KUtils: $kutils\n" +
                                        "Kotlin: $kotlinVersion" +
                                        "```")

                                addField("Source", repository)
                            }

                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        println(e.message)
        exitProcess(-1)
    }
}