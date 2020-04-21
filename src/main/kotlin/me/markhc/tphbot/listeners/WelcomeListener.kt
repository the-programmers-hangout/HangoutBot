package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.stdlib.formatJdaDate
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.findOrCreate
import mu.KLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class WelcomeListener(private val logger: KLogger) {
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
            logger.error { "Welcome channel not set" }
        }

        val welcomeChannel = event.guild.textChannels.find {
            it.id == guild.welcomeChannel
        }

        welcomeChannel?.sendMessage("Welcome ${event.user.name}! Have a cookie.")
    }
}