package me.markhc.hangoutbot.listeners

import com.google.common.collect.EvictingQueue
import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PersistentData
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import java.util.*

@Suppress("unused")
class WelcomeEmbedListeners(private val persistentData: PersistentData) {
    private val welcomeMessages: MutableMap<Long, Queue<Pair<Long, Long>>> = mutableMapOf()

    @Subscribe
    fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val (embeds, channel) = persistentData.getGuildProperty(event.guild) {
            welcomeEmbeds to welcomeChannel
        }

        if(!embeds || channel.isEmpty()) return;

        val welcomeChannel = event.guild.getTextChannelById(channel)
        val welcomeEmbed = embed {
            title = "Welcome"
            description = Messages.getRandomJoinMessage("${event.user.asMention} (${event.user.fullName()})")
            thumbnail = event.user.effectiveAvatarUrl
            color = infoColor
            field  {
                name = "How do I start?"
                value = Messages.welcomeDescription
            }
        }

        try {
            welcomeChannel?.sendMessage(welcomeEmbed)?.queue { message ->
                addMessageToCache(event.user, message)
                message.addReaction("\uD83D\uDC4B").queue()
            }
        } catch (ex: Exception) {
            System.err.println(ex.message ?: "Failed to send message.")
        }
    }

    @Subscribe
    fun onGuildMemberLeave(event: GuildMemberRemoveEvent) {
        val channel = persistentData.getGuildProperty(event.guild) {
            welcomeChannel
        }

        if(channel.isEmpty()) return;

        val welcomeChannel = event.guild.getTextChannelById(channel) ?: return

        val message = getCachedMessage(event.guild, event.user) ?: return

        welcomeChannel.deleteMessageById(message).queue()
    }

    private fun addMessageToCache(user: User, msg: Message) {
        if(welcomeMessages.containsKey(msg.guild.idLong)) {
            welcomeMessages[msg.guild.idLong]!!.add(user.idLong to msg.idLong)
        } else {
            welcomeMessages[msg.guild.idLong] = EvictingQueue.create(200)
            welcomeMessages[msg.guild.idLong]!!.add(user.idLong to msg.idLong)
        }
    }

    private fun getCachedMessage(guild: Guild, user: User) =
        welcomeMessages[guild.idLong]?.find { it.first == user.idLong }?.second
}