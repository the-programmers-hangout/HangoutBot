package me.markhc.hangoutbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import mu.KLogger
import java.awt.Color

@Suppress("unused")
class WelcomeListener(private val logger: KLogger, private val guildConfigs: GuildConfigurations) {
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
                description = "Aww yea it\u0027s ${event.user.asMention}(${event.user.fullName()})"
                thumbnail = event.user.effectiveAvatarUrl
                color = Color.CYAN
        })?.queue{
                it.addReaction("\uD83D\uDC4B").queue()
        }
    }
}