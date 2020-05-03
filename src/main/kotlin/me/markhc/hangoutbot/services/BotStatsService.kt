package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.utilities.toLongDurationString
import org.joda.time.DateTime
import java.util.*

@Service
class BotStatsService(private val persistentData: PersistentData,
                      private val discord: Discord) {

    var startTime: Date = Date()
    var totalCommands: Int = 0

    fun commandExecuted(event: CommandEvent<*>) {
        totalCommands++
        persistentData.setProperty {
            totalCommandsExecuted++
        }

        if(event.guild != null) {
            persistentData.setGuildProperty(event.guild!!) {
                totalCommandsExecuted++
            }
        }
    }

    val uptime: String
        get() = (Date().time - startTime.time).toLongDurationString()

    val ping: Long
        get() = discord.jda.gatewayPing
}