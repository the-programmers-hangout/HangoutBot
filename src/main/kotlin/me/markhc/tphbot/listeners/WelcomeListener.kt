package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
import me.markhc.tphbot.services.Configuration
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class WelcomeListener(private val configuration: Configuration) {
    @Subscribe
    fun onGuildMemberJoinEvent(event: GuildMemberJoinEvent) {
//        val guild = event.guild;
//        val welcomeChannel = event.guild.textChannels.find {
//            it.id == configuration.welcomeChannel
//        }
//
//        welcomeChannel?.sendMessage("Welcome ${event.user.name}! Have a cookie.")
    }
}