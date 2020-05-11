package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.*
import net.dv8tion.jda.api.entities.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@Suppress("unused")
@CommandSet("Utility")
fun produceUtilityCommands(muteService: MuteService,
                           reminderService: ReminderService) = commands {
    val dateFormatter = DateTimeFormat.fullDateTime()

    fun formatOffsetTime(time: OffsetDateTime): String {
        val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toInstant().toEpochMilli())
        return "$days days ago. ${time.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    }

    command("viewjoindate") {
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
        description = "Mute yourself for an amout of time. Default is 1 hour. Max is 24 hours."
        execute(TimeStringArg.makeOptional(3600.0)) {
            val (timeInSeconds) = it.args

            if(timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                return@execute it.respond("You cannot mute yourself for that long.")
            }

            val guild = it.guild!!
            val member = guild.getMember(it.author)!!
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(member, millis).fold(
                    success = { embed -> it.author.sendPrivateMessage(embed) },
                    failure = { ex -> it.respond(ex.message!!) }
            )
        }
    }

    command("remindme") {
        description = "A command that'll remind you about something after the specified time."
        execute(TimeStringArg, SentenceArg) {
            val (timeInSeconds, sentence) = it.args

            if(timeInSeconds > TimeUnit.DAYS.toSeconds(30)) {
                return@execute it.respond("You cannot set a reminder that far into the future.")
            }

            val guild = it.guild!!
            val member = guild.getMember(it.author)!!
            val millis = timeInSeconds.roundToLong() * 1000

            reminderService.addReminder(member, millis, sentence).fold(
                    success = { msg -> it.respond(msg) },
                    failure = { ex -> it.respond(ex.message!!) }
            )
        }
    }
}