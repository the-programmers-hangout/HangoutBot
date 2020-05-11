package me.markhc.hangoutbot.listeners

import com.google.common.collect.EvictingQueue
import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PersistentData
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import java.util.*

@Suppress("unused")
class WelcomeEmbedListeners(private val persistentData: PersistentData) {
    private val welcomeMessages: Queue<Pair<Long, Message>> = EvictingQueue.create(50)

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
                welcomeMessages.add(event.user.idLong to message)
                message.addReaction("\uD83D\uDC4B").queue()
            }
        } catch (ex: Exception) {
            System.err.println(ex.message ?: "Failed to send message.")
        }
    }

    @Subscribe
    fun onGuildMemberLeave(event: GuildMemberRemoveEvent) {
        val member = event.member ?: return

        val message = welcomeMessages.find { it.first == member.idLong }?.second

        message?.delete()?.queue()
    }
}