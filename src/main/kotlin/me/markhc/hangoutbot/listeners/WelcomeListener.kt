package me.markhc.hangoutbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.locale.Messages
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import java.awt.Color

@Suppress("unused")
class WelcomeListener(private val guildConfigs: GuildConfigurations) {
    @Subscribe
    fun onGuildMemberJoinEvent(event: GuildMemberJoinEvent) {
        val guild = guildConfigs.getGuildConfig(event.guild.id)

        if(!guild.welcomeEmbeds || guild.welcomeChannel.isEmpty()) return;

        val welcomeChannel = event.guild.textChannels.find  {
            it.id == guild.welcomeChannel
        }

        welcomeChannel?.sendMessage(
            embed {
                title = "Welcome"
                description = "${Messages.getRandomJoinMessage(event.user.asMention)}${event.user.fullName()})"
                thumbnail = event.user.effectiveAvatarUrl
                color = Color.CYAN
                field  {
                    name = "How do I start?"
                    value = Messages.welcomeDescription
                }

            })?.queue{
                it.addReaction("\uD83D\uDC4B").queue()
        }
    }
}