package me.markhc.hangoutbot.commands.utilities

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage
import me.markhc.hangoutbot.commands.utilities.services.*

import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

fun produceUtilityCommands(muteService: MuteService) = commands("Selfmute") {
    command("selfmute") {
        requiresGuild = true
        description = "Mute yourself for the given amount of time. A mute will stop you from talking in any channel. Default is 1 hour. Max is 24 hours."
        execute(TimeArg.makeOptional(3600.0)) {
            val (timeInSeconds) = args

            if (timeInSeconds < 5) {
                return@execute respond("You cannot mute yourself for less than 5 seconds.")
            }
            if (timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                return@execute respond("You cannot mute yourself for more than 24 hours.")
            }

            val guild = guild!!
            val member = author.asMember(guild.id)
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(member, millis, soft = false).fold(
                success = { embed ->
                    message.addReaction("\uD83D\uDD07")
                    author.sendPrivateMessage(embed)
                },
                failure = { ex -> respond(ex.message!!) }
            )
        }
    }

    command("productivemute") {
        requiresGuild = true
        description = "Trying to be productive? Mute yourself for the specified amount of time. " +
            "A productive mute will prevent you from talking in the social channels while still allowing " +
            "the use of the language channels. Default is 1 hour. Max is 24 hours."
        execute(TimeArg.makeOptional(3600.0)) {
            val (timeInSeconds) = args

            if (timeInSeconds < 5) {
                return@execute respond("You cannot mute yourself for less than 5 seconds.")
            }
            if (timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                return@execute respond("You cannot mute yourself for more than 24 hours.")
            }

            val guild = guild!!
            val member = author.asMember(guild.id)
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(member, millis, soft = true).fold(
                success = { embed ->
                    message.addReaction("\uD83D\uDD07").queue()
                    author.sendPrivateMessage(embed)
                },
                failure = { ex -> respond(ex.message!!) }
            )
        }
    }
}

fun reminderCommands(muteService: MuteService,
                     reminderService: ReminderService) = commands("Reminders") {
    command("remindme") {
        description = "A command that'll remind you about something after the specified time."
        execute(TimeArg, EveryArg) {
            val (timeInSeconds, sentence) = args

            if (timeInSeconds < 5) {
                return@execute respond("You cannot set a reminder for less than 5 seconds.")
            }
            if (timeInSeconds > TimeUnit.DAYS.toSeconds(90)) {
                return@execute respond("You cannot set a reminder more than 90 days into the future.")
            }

            val millis = timeInSeconds.roundToLong() * 1000

            reminderService.addReminder(author, millis, sentence).fold(
                success = { msg -> respond(msg) },
                failure = { ex -> respond(ex.message!!) }
            )
        }
    }

    command("listreminders") {
        description = "List your active reminders"
        execute {
            val authorTag = author.tag

            respond {
                title = "Active reminders for $authorTag"

                val count = reminderService.listReminders(this@execute.author) {
                    field {
                        name = timeUntil
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