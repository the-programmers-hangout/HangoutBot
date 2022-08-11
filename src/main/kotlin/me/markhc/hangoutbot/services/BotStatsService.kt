package me.markhc.hangoutbot.services

import dev.kord.core.entity.Guild
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.markhc.hangoutbot.utilities.TimeFormatter
import java.util.*

@Service
class BotStatsService(private val persistentData: PersistentData, private val discord: Discord) {
    private var startTime: Date = Date()

    suspend fun commandExecuted(guild: Guild?) {
        totalCommands++
        persistentData.setGlobalProperty {
            totalCommandsExecuted++
        }

        if (guild != null) {
            persistentData.setGuildProperty(guild) {
                totalCommandsExecuted++
            }
        }
    }

    val uptime: String
        get() = TimeFormatter.toLongDurationString(Date().time - startTime.time)

    val ping: String
        get() = "${discord.kord.gateway.averagePing}"

    private var _totalCommands: Int = 0
    var totalCommands: Int
        get() = _totalCommands
        private set(value) {
            _totalCommands = value
        }
}