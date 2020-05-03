package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.dataclasses.Configuration
import java.awt.Color

@Service
class StartupService(properties: Properties,
                     config: Configuration,
                     discord: Discord,
                     permissionsService: PermissionsService,
                     muteService: MuteService,
                     reminderService: ReminderService) {
    init {
        muteService.launchTimers()
        reminderService.launchTimers()

        with(discord.configuration) {
            prefix = config.prefix
            mentionEmbed {
                val channel = it.channel
                val self = channel.jda.selfUser

                color = Color(0x00bfff)
                thumbnail = self.effectiveAvatarUrl

                field {
                    name = self.fullName()
                    value = "A bot to manage utility commands and funcionaility that does not warrant its own bot"
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

                    addField("Source", repository)
                }
            }
            visibilityPredicate predicate@{
                return@predicate permissionsService.isCommandVisible(it.guild!!, it.user, it.command)
            }
        }
    }
}