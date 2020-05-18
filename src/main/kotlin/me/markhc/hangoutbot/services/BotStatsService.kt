package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.discord.Discord
import me.markhc.hangoutbot.utilities.toLongDurationString
import java.util.*

@Service
class BotStatsService(private val persistentData: PersistentData,
                      private val discord: Discord) {

    var startTime: Date = Date()
    var totalCommands: Int = 0

    fun commandExecuted(event: CommandEvent<*>) {
        totalCommands++
        persistentData.setGlobalProperty {
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

    val ping: String
        get() = "${discord.jda.gatewayPing} ms"
}