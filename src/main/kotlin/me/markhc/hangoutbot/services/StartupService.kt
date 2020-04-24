package me.markhc.hangoutbot.services

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import java.awt.Color

@Service
class StartupService(guilds: GuildConfigurations, discord: Discord, permissionsService: PermissionsService) {
    private data class Properties(val version: String, val kutils: String, val repository: String)
    private val propFile = Properties::class.java.getResource("/properties.json").readText()
    private val project = Klaxon().parse<Properties>(propFile) ?: throw Exception("Failed to parse properties");

    init {
        with(discord.configuration) {
            mentionEmbed = {
                embed {
                    val channel = it.channel
                    val self = channel.jda.selfUser

                    color = Color(0x00bfff)
                    thumbnail = self.effectiveAvatarUrl
                    addField(self.fullName(), "It's a bot!")

                    addInlineField("Prefix", guilds.getGuildConfig(it.guild.id).prefix)

                    with (project) {
                        val kotlinVersion = kotlin.KotlinVersion.CURRENT

                        addField("Build Info", "```" +
                                "Version: $version\n" +
                                "KUtils: $kutils\n" +
                                "Kotlin: $kotlinVersion" +
                                "```")

                        addField("Source", repository)
                    }
                }
            }

            visibilityPredicate = predicate@{ command: Command, user: User, _: MessageChannel, guild: Guild? ->
                guild ?: return@predicate false

                val member = user.toMember(guild)!!
                val permission = command.requiredPermissionLevel

                permissionsService.hasClearance(member, permission)
            }

        }
    }
}