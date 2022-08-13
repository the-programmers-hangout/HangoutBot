package me.markhc.hangoutbot.commands

import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.TimeArg
import me.jakejmattson.discordkt.commands.commands
import me.markhc.hangoutbot.services.MuteService
import me.markhc.hangoutbot.services.ReminderService
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

fun produceUtilityCommands(muteService: MuteService) = commands("Selfmute") {
    slash("selfmute") {
        description = "Mute yourself for the given amount of time. A mute will stop you from talking in any channel. Default is 1 hour. Max is 24 hours."
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

    slash("productivemute") {
        description = "Trying to be productive? Mute yourself for the specified amount of time. " +
            "A productive mute will prevent you from talking in the social channels while still allowing " +
            "the use of the language channels. Default is 1 hour. Max is 24 hours."
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

            val guild = guild
            val member = author.asMember(guild.id)
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(this, member, millis, soft = true)
        }
    }
}

fun reminderCommands(reminderService: ReminderService) = commands("Reminders") {
    slash("remindme") {
        description = "A command that'll remind you about something after the specified time."
        execute(TimeArg, EveryArg) {
            val (seconds, reminder) = args

            if (seconds < 5) {
                respond("You cannot set a reminder for less than 5 seconds.")
                return@execute
            }
            if (seconds > TimeUnit.DAYS.toSeconds(90)) {
                respond("You cannot set a reminder more than 90 days into the future.")
                return@execute
            }

            val millis = seconds.roundToLong() * 1000

            reminderService.addReminder(author, millis, reminder).fold<Unit>(
                success = { msg -> respond(msg) },
                failure = { ex -> respond(ex.message!!) }
            )
        }
    }

    slash("listreminders") {
        description = "List your active reminders"
        execute {
            val authorTag = author.tag

            respond {
                title = "Active reminders for $authorTag"

                val count = reminderService.listReminders(this@execute.author) {
                    field {
                        name = it.timeUntil
                        value = "```\n${if (it.what.length < 100) it.what else "${it.what.take(100)}..."}\n```"
                    }
                }

                if (count == 0) {
                    description = "There doesn't seem to be anything here."
                }
            }
        }
    }
}