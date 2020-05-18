package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.TimeStringArg
import me.aberrantfox.kjdautils.internal.arguments.UserArg
import me.markhc.hangoutbot.services.MuteService
import me.markhc.hangoutbot.services.ReminderService
import org.joda.time.DateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@Suppress("unused")
@CommandSet("Utility")
fun produceUtilityCommands(muteService: MuteService,
                           reminderService: ReminderService) = commands {

    fun formatOffsetTime(time: OffsetDateTime): String {
        val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toInstant().toEpochMilli())
        return "$days days ago, on ${time.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    }

    command("viewjoindate") {
        requiresGuild = true
        description = "Displays when a user joined the guild"
        execute(MemberArg) {
            val member = it.args.first

            it.respond("${member.fullName()} joined ${formatOffsetTime(member.timeJoined)}")
        }
    }

    command("viewcreationdate") {
        description = "Displays when a user was created"
        execute(UserArg) {
            val user = it.args.first

            it.respond("${user.fullName()} created ${formatOffsetTime(user.timeCreated)}")
        }
    }

    command("avatar") {
        description = "Gets the avatar from the given user"
        execute(UserArg) {
            val user = it.args.first

            it.respond("${user.effectiveAvatarUrl}?size=512")
        }
    }

    command("selfmute") {
        requiresGuild = true
        description = "Mute yourself for the given amount of time. A mute will stop you from talking in any channel. Default is 1 hour. Max is 24 hours."
        execute(TimeStringArg.makeOptional(3600.0)) {
            val (timeInSeconds) = it.args

            if(timeInSeconds < 5) {
                return@execute it.respond("You cannot mute yourself for less than 5 seconds.")
            }
            if(timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
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

    command("productivemute") {
        requiresGuild = true
        description = "Trying to be productive? Mute yourself for the specified amount of time. " +
                "A productive mute will prevent you from talking in the social channels while still allowing " +
                "the use of the language channels. Default is 1 hour. Max is 24 hours."
        execute(TimeStringArg.makeOptional(3600.0)) {
            val (timeInSeconds) = it.args

            if(timeInSeconds < 5) {
                return@execute it.respond("You cannot mute yourself for less than 5 seconds.")
            }
            if(timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
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

    command("remindme") {
        description = "A command that'll remind you about something after the specified time."
        execute(TimeStringArg, SentenceArg) {
            val (timeInSeconds, sentence) = it.args

            if(timeInSeconds < 5) {
                return@execute it.respond("You cannot set a reminder for less than 5 seconds.")
            }
            if(timeInSeconds > TimeUnit.DAYS.toSeconds(30)) {
                return@execute it.respond("You cannot set a reminder more than 30 days into the future.")
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
        execute { event ->
            val messageEmbed = embed {
                title = "Active reminders for ${event.author.fullName()}"

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