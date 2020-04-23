package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.stdlib.formatJdaDate
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.findOrCreate
import mu.KLogger
import net.dv8tion.jda.api.EmbedBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

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