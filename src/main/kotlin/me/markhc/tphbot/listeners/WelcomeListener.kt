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

typealias MessageID = String
typealias UserID = String

class WelcomeListener(private val logger: KLogger) {
    private val welcomeMessages = ConcurrentHashMap<UserID, MessageID>()

    @Subscribe
    fun onGuildMemberJoinEvent(event: GuildMemberJoinEvent) {
        val guild = transaction {
            GuildConfiguration.findOrCreate(event.guild.id)
        }

        val secsSinceCreation = (System.currentTimeMillis() / 1000) - event.user.timeCreated.toEpochSecond()
        val numOfDays = TimeUnit.DAYS.convert(secsSinceCreation, TimeUnit.SECONDS).toInt()
        val user = "${event.user.fullName()} :: ${event.user.asMention}"
        val date = event.user.timeCreated.toString().formatJdaDate()

        logger.info { "$user created $numOfDays days ago ($date) -- joined the server" }

        if(guild.welcomeChannel == null) {
            logger.debug { "Welcome channel not set" }
        }

        val welcomeChannel = event.guild.textChannels.find  {
            it.id == guild.welcomeChannel
        }

        val userImage = event.user.effectiveAvatarUrl

        welcomeChannel?.sendMessage(buildJoinMessage(
                title = "Welcome",
                response = "Aww yeeee it\u0027s ${event.user.asMention}(${event.user.fullName()}",
                image = userImage)
        )?.queue{msg ->
            msg.addReaction("\uD83D\uDC4B").queue {
                welcomeMessages[event.user.id] = msg.id
                Timer().schedule(1000 * 10) {
                    welcomeMessages.remove(event.user.id)
                    logger.debug { "Removed welcome message from list" }
                }
            }
        }
    }

    private fun buildJoinMessage(response: String, image: String, title: String) =
            EmbedBuilder()
                    .setTitle(title)
                    .setDescription(response)
                    .setColor(Color.red)
                    .setThumbnail(image)
                    .build()

}