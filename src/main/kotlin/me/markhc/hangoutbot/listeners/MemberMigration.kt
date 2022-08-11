package me.markhc.hangoutbot.listeners

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.toReaction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.dsl.listeners
import me.jakejmattson.discordkt.extensions.pfpUrl
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.commands.administration.services.GreetingService
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PersistentData

const val WELCOME_DELAY: Long = 5000

fun migrationListeners(persistentData: PersistentData, guildService: GreetingService) = listeners {
    on<MemberJoinEvent> {
        val (embeds, channel) = persistentData.getGuildProperty(guild.asGuild()) {
            welcomeEmbeds to welcomeChannel
        }

        if (!embeds || channel.isEmpty()) return@on

        val welcomeChannel = channel.toSnowflakeOrNull()?.let { guild.getChannelOf<TextChannel>(it) }
                ?: return@on

        GlobalScope.launch {
            delay(WELCOME_DELAY)

            if (guild.getMemberOrNull(member.id) != null) {
                val message = welcomeChannel.createEmbed {
                    title = "Welcome"
                    description = Messages.getRandomJoinMessage("**${member.username}**")
                    color = discord.configuration.theme

                    thumbnail {
                        url = member.pfpUrl
                    }
                }

                guildService.addMessageToCache(member.asUser(), message)
                message.addReaction(Emojis.wave.toReaction())
            }
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