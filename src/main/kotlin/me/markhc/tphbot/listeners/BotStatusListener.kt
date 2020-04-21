package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
import me.markhc.tphbot.services.Configuration
import net.dv8tion.jda.api.events.DisconnectEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ReconnectedEvent

class BotStatusListener(private val configuration: Configuration) {
    @Subscribe
    fun onBotReadyEvent(event: ReadyEvent) {
        println("Bot successfully logged in with token " +
                "${configuration.token}" +
                "${if(configuration.developmentMode) " (DEV)." else "."}")
    }

    @Subscribe
    fun onBotDisconnect(event: DisconnectEvent) {
        println("Bot disconnected.")
    }

    @Subscribe
    fun onBotReconnect(event: ReconnectedEvent) {
        println("Bot reconnected.")
    }
}