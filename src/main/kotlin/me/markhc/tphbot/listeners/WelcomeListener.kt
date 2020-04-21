package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
import me.markhc.tphbot.services.Configuration
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.findOrCreate
import org.jetbrains.exposed.sql.transactions.transaction

class WelcomeListener(configuration: Configuration, persistenceService: PersistenceService) {
    @Subscribe
    fun onGuildMemberJoinEvent(event: GuildMemberJoinEvent) {
        val guild = transaction {
            GuildConfiguration.findOrCreate(event.guild.id)
        }

        if(guild.welcomeChannel == null) {
            println("Welcome channel not set")
        }

        val welcomeChannel = event.guild.textChannels.find {
            it.id == guild.welcomeChannel
        }

        if(welcomeChannel == null) {
            println("Failed to find welcome channel")
        }

        welcomeChannel?.sendMessage("Welcome ${event.user.name}! Have a cookie.")
    }
}