package me.markhc.hangoutbot.services

import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.annotations.Service
import me.markhc.hangoutbot.commands.utilities.services.MuteService
import me.markhc.hangoutbot.commands.utilities.services.ReminderService

@Service
class StartupService(muteService: MuteService, reminderService: ReminderService) {
    init {
        runBlocking {
            muteService.launchTimers()
            reminderService.launchTimers()
        }
    }
}