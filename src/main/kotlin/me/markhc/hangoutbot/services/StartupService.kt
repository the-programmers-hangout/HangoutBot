package me.markhc.hangoutbot.services

import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.annotations.Service
import me.jakejmattson.kutils.api.extensions.jda.fullName
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.commands.utilities.services.MuteService
import me.markhc.hangoutbot.commands.utilities.services.ReminderService

@Service
class StartupService(private val properties: Properties,
                     private val config: BotConfiguration,
                     private val botStats: BotStatsService,
                     private val discord: Discord,
                     private val permissionsService: PermissionsService,
                     private val persistentData: PersistentData,
                     private val muteService: MuteService,
                     private val reminderService: ReminderService) {
    init {
        muteService.launchTimers()
        reminderService.launchTimers()

        with(discord.configuration) {
            prefix {
                if(it.guild == null)
                    config.prefix
                else
                    persistentData.getGuildProperty(it.guild!!) { prefix }
            }
            mentionEmbed {
                val channel = it.channel
                val self = channel.jda.selfUser

                color = infoColor
                thumbnail = self.effectiveAvatarUrl

                field {
                    name = self.fullName()
                    value = "A bot to manage utility commands and functionality that does not warrant its own bot"
                }
                field {
                    name = "Prefix"
                    value = if(it.guild == null)
                        config.prefix
                    else
                        persistentData.getGuildProperty(it.guild!!) { prefix }
                    inline = true
                }
                field {
                    name = "Contributors"
                    value = "markhc#8366"
                    inline = true
                }

                with (properties) {
                    val kotlinVersion = KotlinVersion.CURRENT

                    field {
                        name = "Build Info"
                        value = "```"+
                                "Version: $version\n" +
                                "KUtils:  $kutils\n" +
                                "Kotlin:  $kotlinVersion" +
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

            visibilityPredicate predicate@{
                return@predicate if(it.guild == null && it.command.requiresGuild ?: discord.configuration.requiresGuild) {
                    false
                } else {
                    permissionsService.isCommandVisible(it.guild, it.user, it.command)
                }
            }
        }
    }
}