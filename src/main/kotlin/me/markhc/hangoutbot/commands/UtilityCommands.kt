package me.markhc.hangoutbot.commands

import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.TimeArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.dsl.edit
import me.jakejmattson.discordkt.extensions.TimeStamp
import me.jakejmattson.discordkt.extensions.TimeStyle
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.Reminder
import me.markhc.hangoutbot.services.MuteService
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

fun produceUtilityCommands(muteService: MuteService, configuration: Configuration) = commands("Selfmute") {
    slash("selfmute", "Mute yourself for the given amount of time.") {
        execute(TimeArg.optional(3600.0)) {
            val (timeInSeconds) = args

            if (timeInSeconds < 5) {
                respond("You cannot mute yourself for less than 5 seconds.")
                return@execute
            }
            if (timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                respond("You cannot mute yourself for more than 24 hours.")
                return@execute
            }

            val member = author.asMember(guild.id)
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(this, member, millis, soft = false)
        }
    }

    slash("productivemute", "Hide social channels for a given amount of time.") {
        execute(TimeArg.optional(3600.0)) {
            val (timeInSeconds) = args

            if (timeInSeconds < 5) {
                respond("You cannot mute yourself for less than 5 seconds.")
                return@execute
            }
            if (timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                respond("You cannot mute yourself for more than 24 hours.")
                return@execute
            }

            val member = author.asMember(guild.id)
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(this, member, millis, soft = true)
        }
    }

    slash("remindme", "A command that'll remind you about something after the specified time.") {
        execute(TimeArg, EveryArg) {
            val (seconds, message) = args

            if (seconds < 5) {
                respond("You cannot set a reminder for less than 5 seconds.")
                return@execute
            }
            if (seconds > TimeUnit.DAYS.toSeconds(90)) {
                respond("You cannot set a reminder more than 90 days into the future.")
                return@execute
            }

            val ms = seconds.roundToLong() * 1000
            val until = Instant.now().plusMillis(ms)
            val reminder = Reminder(author.id, until.toEpochMilli(), message)

            configuration.edit { reminders.add(reminder) }
            reminder.launch(discord, configuration)

            respond("Got it, I'll remind you ${TimeStamp.at(until, TimeStyle.RELATIVE)}")
        }
    }
}