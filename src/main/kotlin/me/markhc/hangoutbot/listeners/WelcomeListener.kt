package me.markhc.hangoutbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PersistentData
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import java.awt.Color

@Suppress("unused")
class WelcomeListener(private val persistentData: PersistentData) {
    @Subscribe
    fun onGuildMemberJoinEvent(event: GuildMemberJoinEvent) {
        val (embeds, channel) = persistentData.getGuildProperty(event.guild) {
            welcomeEmbeds to welcomeChannel
        }

        if(!embeds || channel.isEmpty()) return;

        val welcomeChannel = event.guild.getTextChannelById(channel)

        welcomeChannel?.sendMessage(
            embed {
                title = "Welcome"
                description = "${Messages.getRandomJoinMessage(event.user.asMention)}${event.user.fullName()})"
                thumbnail = event.user.effectiveAvatarUrl
                color = infoColor
                field  {
                    name = "How do I start?"
                    value = Messages.welcomeDescription
                }

            })?.queue{
                it.addReaction("\uD83D\uDC4B").queue()
        }
    }
}