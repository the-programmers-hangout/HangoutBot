package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.utilities.toLongDurationString
import java.util.*

private val startTime = Date()

@Service
class BotStatsService(private val config: Configuration,
                      private val persistenceService: PersistenceService,
                      private val discord: Discord,
                      var totalCommands: Int = 0) {
    fun commandExecuted(event: CommandEvent<*>) {
        totalCommands++
        config.totalCommandsExecuted++

        if(event.guild != null) {
            config.getGuildConfig(event.guild!!).apply {
                totalCommandsExecuted++
            }
        }
        persistenceService.save(config)
    }

    val uptime: String
        get() = (Date().time - startTime.time).toLongDurationString()

    val ping: Long
        get() = discord.jda.gatewayPing
}