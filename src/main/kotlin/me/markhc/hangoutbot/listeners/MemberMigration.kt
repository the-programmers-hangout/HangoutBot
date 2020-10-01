package me.markhc.hangoutbot.listeners

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.*
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.channel.TextChannel
import com.gitlab.kordlib.core.event.guild.*
import com.gitlab.kordlib.kordx.emoji.*
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.commands.administration.services.GreetingService
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PersistentData

fun migrationListeners(persistentData: PersistentData, guildService: GreetingService) = listeners {
    on<MemberJoinEvent> {
        val (embeds, channel) = persistentData.getGuildProperty(guild.asGuild()) {
            welcomeEmbeds to welcomeChannel
        }

        if (!embeds || channel.isEmpty) return@on

        val welcomeChannel = channel.toSnowflakeOrNull()?.let { guild.getChannelOf<TextChannel>(it) } ?: return@on

        try {
            val message = welcomeChannel.createEmbed {
                title = "Welcome"
                description = Messages.getRandomJoinMessage("${member.mention} (${member.tag})")
                color = discord.configuration.theme

                thumbnail {
                    url = member.avatar.url
                }

                field {
                    name = "How do I start?"
                    value = Messages.welcomeDescription
                }
            }

            guildService.addMessageToCache(member.asUser(), message)
            message.addReaction(Emojis.wave.toReaction())
        } catch (ex: Exception) {
            System.err.println(ex.message ?: "Failed to send message.")
        }
    }

    on<MemberLeaveEvent> {
        val guild = guild.asGuild()

        val channel = persistentData.getGuildProperty(guild) {
            welcomeChannel
        }.toSnowflakeOrNull() ?: return@on

        val welcomeChannel = guild.getChannelOfOrNull<TextChannel>(channel) ?: return@on

        val message = guildService.getCachedMessage(guild, user)?.let { Snowflake(it) } ?: return@on

        welcomeChannel.deleteMessage(message)
        guildService.removeMessagesFromCache(guild, user)
    }
}