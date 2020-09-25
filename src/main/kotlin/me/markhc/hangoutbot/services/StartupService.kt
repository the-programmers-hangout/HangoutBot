package me.markhc.hangoutbot.services

import me.jakejmattson.discordkt.api.annotations.Service
import me.markhc.hangoutbot.commands.utilities.services.MuteService
import me.markhc.hangoutbot.commands.utilities.services.ReminderService

@Service
class StartupService(muteService: MuteService, reminderService: ReminderService) {
    init {
        muteService.launchTimers()
        reminderService.launchTimers()
    }
}