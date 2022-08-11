package me.markhc.hangoutbot.commands.administration.services

import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.utilities.EvictingQueue

@Service
class GreetingService(private val persistentData: PersistentData) {
    companion object {
        private val welcomeMessages: MutableMap<Long, EvictingQueue<Pair<Long, Long>>> = mutableMapOf()
    }

    suspend fun addMessageToCache(user: User, msg: Message) {
        if (welcomeMessages.containsKey(msg.getGuild().id.value.toLong())) {
            welcomeMessages[msg.getGuild().id.value.toLong()]!!.add(user.id.value.toLong() to msg.id.value.toLong())
        } else {
            welcomeMessages[msg.getGuild().id.value.toLong()] = EvictingQueue.create(200)
            welcomeMessages[msg.getGuild().id.value.toLong()]!!.add(user.id.value.toLong() to msg.id.value.toLong())
        }
    }

    fun getCachedMessage(guild: Guild, user: User) =
        welcomeMessages[guild.id.value.toLong()]?.find { it.first == user.id.value.toLong() }?.second

    fun removeMessagesFromCache(guild: Guild, user: User) =
        welcomeMessages[guild.id.value.toLong()]?.removeIf { it.first == user.id.value.toLong() }

    suspend fun setEnabled(guild: Guild, state: Boolean) = persistentData.setGuildProperty(guild) {
        welcomeEmbeds = state
    }

    suspend fun isEnabled(guild: Guild) = persistentData.getGuildProperty(guild) {
        welcomeEmbeds
    }

    suspend fun setChannel(guild: Guild, textChannel: TextChannel) = persistentData.setGuildProperty(guild) {
        welcomeChannel = textChannel.id.toString()
    }

    suspend fun getChannel(guild: Guild) = persistentData.getGuildProperty(guild) { welcomeChannel }.let {
        it.ifBlank { null }.let { id ->
            id?.toSnowflakeOrNull()?.let { guild.getChannelOfOrNull<TextChannel>(it) }
        }
    }
}