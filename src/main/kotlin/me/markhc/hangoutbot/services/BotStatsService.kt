package me.markhc.hangoutbot.services

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.command.Command
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.markhc.hangoutbot.utilities.TimeFormatter
import java.util.*

@Service
class BotStatsService(private val persistentData: PersistentData,
                      private val discord: Discord) {

    private var startTime: Date = Date()

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
        get() = TimeFormatter.toLongDurationString(Date().time - startTime.time)

    val ping: String
        get() = "${discord.jda.gatewayPing} ms"

    private var _totalCommands: Int = 0
    var totalCommands: Int
        get() = _totalCommands
        private set(value) {
            _totalCommands = value
        }
}