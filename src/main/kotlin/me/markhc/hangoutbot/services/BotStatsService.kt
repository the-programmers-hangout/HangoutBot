package me.markhc.hangoutbot.services

import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.annotations.Service
import me.jakejmattson.kutils.api.dsl.command.Command
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.markhc.hangoutbot.utilities.toLongDurationString
import java.util.*

@Service
class BotStatsService(private val persistentData: PersistentData,
                      private val discord: Discord) {

    private var startTime: Date = Date()
    private var averageExecutionTime = mutableMapOf<Command, Pair<Int, Double>>()

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

    fun commandExecutionTime(command: Command, it: Long) {
        val (count, average) = averageExecutionTime[command] ?: (0 to 0.0)

        val newAverage = average + (it.toDouble() - average) / (count + 1)

        averageExecutionTime[command] = (count+1) to newAverage
    }

    val uptime: String
        get() = (Date().time - startTime.time).toLongDurationString()

    val avgCommandTimes: Map<String, Double>
        get() = averageExecutionTime.map { it.key.names.first() to it.value.second }.toMap()

    val avgResponseTime: Double
        get() = averageExecutionTime.map { it.value.second }.average()

    val ping: String
        get() = "${discord.jda.gatewayPing} ms"

    private var _totalCommands: Int = 0
    var totalCommands: Int
        get() = _totalCommands
        private set(value) {
            _totalCommands = value
        }
}