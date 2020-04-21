package me.markhc.tphbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.markhc.tphbot.services.Configuration
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class MessageListener(private val configuration: Configuration, private val logger: BotLogger) {
    @Subscribe
    fun onGuildMessageEvent(event: GuildMessageReceivedEvent) {
        logger.info("Message received.")
    }
}