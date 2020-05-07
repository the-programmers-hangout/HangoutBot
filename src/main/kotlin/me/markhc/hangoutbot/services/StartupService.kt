package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.dataclasses.Configuration
import net.dv8tion.jda.api.entities.Activity
import java.awt.Color

@Service
class StartupService(properties: Properties,
                     config: BotConfiguration,
                     botStats: BotStatsService,
                     discord: Discord,
                     permissionsService: PermissionsService,
                     muteService: MuteService,
                     reminderService: ReminderService) {
    init {
        muteService.launchTimers()
        reminderService.launchTimers()

        discord.jda.presence.activity = Activity.playing("${config.prefix}help for more information")

        with(discord.configuration) {
            prefix = config.prefix
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
                    value = config.prefix
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
                return@predicate permissionsService.isCommandVisible(it.guild!!, it.user, it.command)
            }
        }
    }
}