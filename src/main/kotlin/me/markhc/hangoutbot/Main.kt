package me.markhc.hangoutbot

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.extensions.addInlineField
import me.markhc.hangoutbot.dataclasses.*
import me.markhc.hangoutbot.services.*
import java.awt.Color

suspend fun main() {
    loadConfig {
        val configuration = it ?: throw Exception("Failed to parse configuration")
        val propFile = Properties::class.java.getResource("/hangoutbot_properties.json").readText()
        val properties = Json.decodeFromString<Properties>(propFile)

        bot(configuration.token) {
            inject(configuration)
            inject(properties)

            prefix {
                val persistentData = discord.getInjectionObjects(PersistentData::class)
                guild?.let { persistentData.getGuildProperty(it) { prefix() } } ?: "++"
            }

            configure {
                allowMentionPrefix = true
                requiresGuild = false
                generateCommandDocs = false
                commandReaction = null
                theme = Color.CYAN
            }

            mentionEmbed {
                val botStats = it.discord.getInjectionObjects(BotStatsService::class)

                val channel = it.channel
                val self = channel.kord.getSelf()
                color = it.discord.configuration.theme

                thumbnail {
                    url = self.avatar.url
                }

                field {
                    name = self.tag
                    value = "A bot to manage utility commands and functionality that does not warrant its own bot"
                }

                addInlineField("Prefix", it.prefix())
                addInlineField("Contributors", "markhc#8366")

                with(properties) {
                    val kotlinVersion = KotlinVersion.CURRENT

                    field {
                        name = "Build Info"
                        value = "```" +
                            "Version:   $version\n" +
                            "DiscordKt: $version\n" +
                            "Kotlin:    $kotlinVersion" +
                            "```"
                    }

                    field {
                        name = "Uptime"
                        value = botStats.uptime
                    }

                    field {
                        name = "Source"
                        value = "[[GitHub]](${repository})"
                    }
                }
            }

            permissions {
                val permissionsService = discord.getInjectionObjects(PermissionsService::class)

                if (guild != null || !(command.requiresGuild ?: discord.configuration.requiresGuild))
                    permissionsService.isCommandVisible(guild!!, user, command)
                else
                    false
            }
        }
    }
}