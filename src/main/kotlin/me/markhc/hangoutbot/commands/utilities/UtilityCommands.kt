package me.markhc.hangoutbot.commands.utilities

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.TimeArg
import me.jakejmattson.discordkt.api.dsl.command.commands
import me.jakejmattson.discordkt.api.dsl.embed.embed
import me.jakejmattson.discordkt.api.extensions.jda.fullName
import me.jakejmattson.discordkt.api.extensions.jda.sendPrivateMessage
import me.markhc.hangoutbot.commands.utilities.services.MuteService
import me.markhc.hangoutbot.commands.utilities.services.ReminderService
import me.markhc.hangoutbot.utilities.executeLogged
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@CommandSet("Selfmute")
fun produceUtilityCommands(muteService: MuteService) = commands {
    command("selfmute") {
        requiresGuild = true
        description = "Mute yourself for the given amount of time. A mute will stop you from talking in any channel. Default is 1 hour. Max is 24 hours."
        executeLogged(TimeArg.makeOptional(3600.0)) {
            val (timeInSeconds) = it.args

            if(timeInSeconds < 5) {
                return@executeLogged it.respond("You cannot mute yourself for less than 5 seconds.")
            }
            if(timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                return@executeLogged it.respond("You cannot mute yourself for more than 24 hours.")
            }

            val guild = it.guild!!
            val member = guild.getMember(it.author)!!
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(member, millis, soft = false).fold(
                    success = { embed ->
                        it.message.addReaction("\uD83D\uDD07").queue()
                        it.author.sendPrivateMessage(embed)
                    },
                    failure = { ex -> it.respond(ex.message!!) }
            )
        }
    }

    command("productivemute") {
        requiresGuild = true
        description = "Trying to be productive? Mute yourself for the specified amount of time. " +
                "A productive mute will prevent you from talking in the social channels while still allowing " +
                "the use of the language channels. Default is 1 hour. Max is 24 hours."
        executeLogged(TimeArg.makeOptional(3600.0)) {
            val (timeInSeconds) = it.args

            if(timeInSeconds < 5) {
                return@executeLogged it.respond("You cannot mute yourself for less than 5 seconds.")
            }
            if(timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                return@executeLogged it.respond("You cannot mute yourself for more than 24 hours.")
            }

            val guild = it.guild!!
            val member = guild.getMember(it.author)!!
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(member, millis, soft = true).fold(
                    success = { embed ->
                        it.message.addReaction("\uD83D\uDD07").queue()
                        it.author.sendPrivateMessage(embed)
                    },
                    failure = { ex -> it.respond(ex.message!!) }
            )
        }
    }
}

@CommandSet("Reminders")
fun reminderCommands(muteService: MuteService,
                     reminderService: ReminderService) = commands {
    command("remindme") {
        description = "A command that'll remind you about something after the specified time."
        executeLogged(TimeArg, EveryArg) {
            val (timeInSeconds, sentence) = it.args

            if(timeInSeconds < 5) {
                return@executeLogged it.respond("You cannot set a reminder for less than 5 seconds.")
            }
            if(timeInSeconds > TimeUnit.DAYS.toSeconds(90)) {
                return@executeLogged it.respond("You cannot set a reminder more than 90 days into the future.")
            }

            val millis = timeInSeconds.roundToLong() * 1000

            reminderService.addReminder(it.author, millis, sentence).fold(
                    success = { msg -> it.respond(msg) },
                    failure = { ex -> it.respond(ex.message!!) }
            )
        }
    }

    command("listreminders") {
        description = "List your active reminders"
        executeLogged { event ->
            val messageEmbed = embed {
                title { text = "Active reminders for ${event.author.fullName()}" }

                val count = reminderService.listReminders(event.author) {
                    field {
                        name = it.timeUntil
                        value = "```\n${if(it.what.length < 100) it.what else "${it.what.take(100)}..."}\n```"
                    }
                }

                if(count == 0) {
                    description = "There doesn't seem to be anything here."
                }
            }

            event.respond(messageEmbed)
        }
    }
}