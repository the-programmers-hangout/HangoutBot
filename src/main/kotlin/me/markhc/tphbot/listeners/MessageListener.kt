package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
import me.markhc.tphbot.services.Configuration
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class MessageListener(private val configuration: Configuration) {
    @Subscribe
    fun onGuildMessageEvent(event: GuildMessageReceivedEvent) {
        println("Message received.")
    }
}