package me.markhc.hangoutbot.services

import dev.kord.core.entity.Guild
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service

@Service
class BotStatsService(private val persistentData: PersistentData, private val discord: Discord) {
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

    private var _totalCommands: Int = 0
    var totalCommands: Int
        get() = _totalCommands
        private set(value) {
            _totalCommands = value
        }
}