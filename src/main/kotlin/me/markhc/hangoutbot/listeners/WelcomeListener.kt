package me.markhc.hangoutbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import me.markhc.hangoutbot.services.GuildConfiguration
import me.markhc.hangoutbot.services.findOrCreate
import mu.KLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class WelcomeListener(private val logger: KLogger) {
    @Subscribe
    fun onGuildMemberJoinEvent(event: GuildMemberJoinEvent) {
        logger.debug { "Join event on guild ${event.guild.id}" }

        val guild = transaction {
            GuildConfiguration.findOrCreate(event.guild.id)
        }

        if(!guild.welcomeEmbeds || guild.welcomeChannel == null) return;

        val welcomeChannel = event.guild.textChannels.find  {
            it.id == guild.welcomeChannel
        }

        welcomeChannel?.sendMessage(
            embed {
                title = "Welcome"
                description = "Aww yea it\u0027s ${event.user.asMention}(${event.user.fullName()})"
                thumbnail = event.user.effectiveAvatarUrl
                color = Color.red
        })?.queue{
                it.addReaction("\uD83D\uDC4B").queue()
        }
    }
}