package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
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

        welcomeChannel?.sendMessage(buildJoinMessage(
                title = "Welcome",
                response = "Aww yeeee it\u0027s ${event.user.asMention}(${event.user.fullName()})",
                image = event.user.effectiveAvatarUrl)
        )?.queue{msg -> msg.addReaction("\uD83D\uDC4B").queue()}
    }

    private fun buildJoinMessage(response: String, image: String, title: String) =
            EmbedBuilder()
                    .setTitle(title)
                    .setDescription(response)
                    .setColor(Color.red)
                    .setThumbnail(image)
                    .build()

}