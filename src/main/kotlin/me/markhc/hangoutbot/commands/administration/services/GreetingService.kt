package me.markhc.hangoutbot.commands.administration.services

import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.annotations.Service
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PersistentData
import java.util.*

@Service
class GreetingService(private val persistentData: PersistentData) {
    companion object {
        private val welcomeMessages: MutableMap<Long, Queue<Pair<Long, Long>>> = mutableMapOf()
    }

    @Subscribe
    fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val (embeds, channel) = persistentData.getGuildProperty(event.guild) {
            welcomeEmbeds to welcomeChannel
        }

        if (!embeds || channel.isEmpty()) return

        val welcomeChannel = event.guild.getTextChannelById(channel)

        try {
            val message = welcomeChannel.createEmbed {
                title { text = "Welcome" }
                description = Messages.getRandomJoinMessage("${event.user.asMention} (${event.user.fullName()})")
                thumbnail = event.user.effectiveAvatarUrl
                color = infoColor
                field {
                    name = "How do I start?"
                    value = Messages.welcomeDescription
                }
            }

            addMessageToCache(event.user, message)
            message.addReaction("\uD83D\uDC4B")
        } catch (ex: Exception) {
            System.err.println(ex.message ?: "Failed to send message.")
        }
    }

    @Subscribe
    fun onGuildMemberLeave(event: GuildMemberRemoveEvent) {
        val channel = persistentData.getGuildProperty(event.guild) {
            welcomeChannel
        }

        if (channel.isEmpty()) return

        val welcomeChannel = event.guild.getTextChannelById(channel) ?: return

        val message = getCachedMessage(event.guild, event.user) ?: return

        welcomeChannel.deleteMessageById(message).queue()

        removeMessagesFromCache(event.guild, event.user)
    }

    private fun addMessageToCache(user: User, msg: Message) {
        if (welcomeMessages.containsKey(msg.guild.id.longValue)) {
            welcomeMessages[msg.guild.id.longValue]!!.add(user.idLong to msg.idLong)
        } else {
            welcomeMessages[msg.guild.id.longValue] = EvictingQueue.create(200)
            welcomeMessages[msg.guild.id.longValue]!!.add(user.idLong to msg.idLong)
        }
    }

    private fun getCachedMessage(guild: Guild, user: User) =
        welcomeMessages[guild.id.longValue]?.find { it.first == user.id.longValue }?.second

    private fun removeMessagesFromCache(guild: Guild, user: User) =
        welcomeMessages[guild.id.longValue]?.removeIf { it.first == user.id.longValue }

    fun setEnabled(guild: Guild, state: Boolean) = persistentData.setGuildProperty(guild) {
        welcomeEmbeds = state
    }

    fun isEnabled(guild: Guild) = persistentData.getGuildProperty(guild) {
        welcomeEmbeds
    }

    fun setChannel(guild: Guild, textChannel: TextChannel) = persistentData.setGuildProperty(guild) {
        welcomeChannel = textChannel.id.value
    }

    fun getChannel(guild: Guild) = persistentData.getGuildProperty(guild) { welcomeChannel }.let {
        it.ifBlank { null }.let { id ->
            guild.getGuildChannelById(id) as TextChannel?
        }
    }
}