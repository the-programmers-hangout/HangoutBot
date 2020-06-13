package me.markhc.hangoutbot.modules.utilities

import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.*
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.jakejmattson.kutils.api.extensions.jda.fullName
import me.jakejmattson.kutils.api.extensions.jda.sendPrivateMessage
import me.markhc.hangoutbot.dataclasses.CustomAlert
import me.markhc.hangoutbot.modules.utilities.services.CustomAlertsService
import me.markhc.hangoutbot.modules.utilities.services.MuteService
import me.markhc.hangoutbot.modules.utilities.services.ReminderService
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.runLoggedCommand
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@CommandSet("Selfmute")
fun produceUtilityCommands(muteService: MuteService,
                           reminderService: ReminderService) = commands {
    command("selfmute") {
        requiresGuild = true
        description = "Mute yourself for the given amount of time. A mute will stop you from talking in any channel. Default is 1 hour. Max is 24 hours."
        execute(TimeArg.makeOptional(3600.0)) {
            runLoggedCommand(it) {
                val (timeInSeconds) = it.args

                if (timeInSeconds < 5) {
                    return@execute it.respond("You cannot mute yourself for less than 5 seconds.")
                }
                if (timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                    return@execute it.respond("You cannot mute yourself for more than 24 hours.")
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
    }

    command("productivemute") {
        requiresGuild = true
        description = "Trying to be productive? Mute yourself for the specified amount of time. " +
                "A productive mute will prevent you from talking in the social channels while still allowing " +
                "the use of the language channels. Default is 1 hour. Max is 24 hours."
        execute(TimeArg.makeOptional(3600.0)) {
            runLoggedCommand(it) {
                val (timeInSeconds) = it.args

                if (timeInSeconds < 5) {
                    return@execute it.respond("You cannot mute yourself for less than 5 seconds.")
                }
                if (timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                    return@execute it.respond("You cannot mute yourself for more than 24 hours.")
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
}

@CommandSet("Reminders")
fun reminderCommands(muteService: MuteService,
                     reminderService: ReminderService) = commands {
    command("remindme") {
        description = "A command that'll remind you about something after the specified time."
        execute(TimeArg, EveryArg) {
            runLoggedCommand(it) {
                val (timeInSeconds, sentence) = it.args

                if (timeInSeconds < 5) {
                    return@execute it.respond("You cannot set a reminder for less than 5 seconds.")
                }
                if (timeInSeconds > TimeUnit.DAYS.toSeconds(90)) {
                    return@execute it.respond("You cannot set a reminder more than 90 days into the future.")
                }

                val millis = timeInSeconds.roundToLong() * 1000

                reminderService.addReminder(it.author, millis, sentence).fold(
                        success = { msg -> it.respond(msg) },
                        failure = { ex -> it.respond(ex.message!!) }
                )
            }
        }
    }

    command("listreminders") {
        description = "List your active reminders"
        execute { event ->
            runLoggedCommand(event) {
                val messageEmbed = embed {
                    title = "Active reminders for ${event.author.fullName()}"
                    color = infoColor

                    val count = reminderService.listReminders(event.author) {
                        field {
                            name = it.timeUntil
                            value = "```\n${if (it.what.length < 100) it.what else "${it.what.take(100)}..."}\n```"
                        }
                    }

                    if (count == 0) {
                        description = "There doesn't seem to be anything here."
                    }
                }

                event.respond(messageEmbed)
            }
        }
    }
}

@CommandSet("Custom Alerts")
fun produceAlertCommands(alerts: CustomAlertsService, discord: Discord) = commands {
    fun generateAlertsEmbed(guild: Guild, user: User) = embed {
        title = "Alerts for ${user.fullName()}"
        color = infoColor

        fun getChannel(alert: CustomAlert) = if(alert.channel != 0.toLong())
            "in ${discord.jda.getTextChannelById(alert.channel)?.asMention ?: alert.channel}"
        else ""


        val count = alerts.listAlerts(guild, user) {
            field {
                name = "**Id: ${it.id}**" + if(it.disabled) " (disabled)" else ""
                value = "Every message " + getChannel(it) + " that contains `${it.text}`"
            }
        }

        if (count == 0) {
            description = "There doesn't seem to be anything here."
        }
    }

    command("setupalert") {
        description = "Setup a custom alert. Alerts are notifications sent by the Bot via DM when " +
                "a message that matches the requirements is received. " +
                "You can setup an alert to be notified when someone says a specific word or sentence."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(TextChannelArg("Channel").makeNullableOptional(null),
                EveryArg("Text")) {
            runLoggedCommand(it) {
                val (channel, regex) = it.args;

                runCatching {
                    alerts.addAlert(it.guild!!, it.author, channel, regex)
                }.onSuccess { msg ->
                    it.respond("$msg\n\nIf you'd like to remove this alert, see `${it.relevantPrefix}help disablealert`");
                }.onFailure { ex ->
                    it.respond(ex.message ?: "An error occurred while setting the alert")
                }
            }
        }
    }

    command("disablealert") {
        description = "Disables a custom alert. Disabled alerts are not removed " +
                "unless you include `remove` after the alert id. **Run without args to get a list " +
                "of all your alerts.**"
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(IntegerArg("Id").makeNullableOptional(null),
                BooleanArg("Remove", "remove", "keep").makeOptional(false)) {
            runLoggedCommand(it) {
                val (id, remove) = it.args

                if (id == null) {
                    it.respond(generateAlertsEmbed(it.guild!!, it.author))
                } else {
                    runCatching {
                        if (remove) {
                            alerts.removeAlert(it.guild!!, it.author, id)
                        } else {
                            alerts.disableAlert(it.guild!!, it.author, id)
                        }
                    }.onSuccess { _ ->
                        if (remove) {
                            it.respond("Removed alert $id");
                        } else {
                            it.respond("Disabled alert $id");
                        }
                    }.onFailure { ex ->
                        it.respond(ex.message ?: "An error occurred while enabling the alert")
                    }
                }
            }
        }
    }

    command("enablealert") {
        description = "Enables a previously disabled alert."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(IntegerArg("Id").makeNullableOptional(null)) {
            runLoggedCommand(it) {
                val (id) = it.args

                if (id == null) {
                    it.respond(generateAlertsEmbed(it.guild!!, it.author))
                } else {
                    runCatching {
                        alerts.enableAlert(it.guild!!, it.author, id)
                    }.onSuccess { _ ->
                        it.respond("Enabled alert $id");
                    }.onFailure { ex ->
                        it.respond(ex.message ?: "An error occurred while enabling the alert")
                    }
                }
            }
        }
    }

    command("listalerts") {
        description = "Lists all your alerts."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute {
            runLoggedCommand(it) {
                it.respond(generateAlertsEmbed(it.guild!!, it.author))
            }
        }
    }
}