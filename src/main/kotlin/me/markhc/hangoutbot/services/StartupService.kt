package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.commands.utilities.services.MuteService
import me.markhc.hangoutbot.commands.utilities.services.ReminderService

@Service
class StartupService(properties: Properties,
                     config: BotConfiguration,
                     botStats: BotStatsService,
                     discord: Discord,
                     permissionsService: PermissionsService,
                     persistentData: PersistentData,
                     muteService: MuteService,
                     reminderService: ReminderService) {
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
                    value = "markhc#0001"
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
                return@predicate if(it.guild == null && it.command.requiresGuild) {
                    false
                } else {
                    permissionsService.isCommandVisible(it.guild, it.user, it.command)
                }
            }
        }
    }
}