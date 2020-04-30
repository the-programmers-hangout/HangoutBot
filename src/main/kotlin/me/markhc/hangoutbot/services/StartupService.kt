package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.launchMuteTimers
import java.awt.Color

@Service
class StartupService(properties: Properties, config: Configuration, persistenceService: PersistenceService, discord: Discord, permissionsService: PermissionsService) {
    init {
        launchMuteTimers(config, persistenceService, discord)

        with(discord.configuration) {
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
                    value = config.getGuildConfig(it.guild.id).prefix
                    inline = true
                }
                field {
                    name = "Contributors"
                    value = "markhc#0001"
                    inline = true
                }

                with (properties) {
                    val kotlinVersion = kotlin.KotlinVersion.CURRENT

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
                it.guild ?: return@predicate false

                val member = it.user.toMember(it.guild!!)!!
                val permission = it.command.requiredPermissionLevel

                permissionsService.hasClearance(member, permission)
            }
        }
    }
}