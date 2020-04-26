package me.markhc.hangoutbot.commands.serverutilities

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.extensions.stdlib.convertToTimeString
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.arguments.TimeStringArg
import me.aberrantfox.kjdautils.internal.arguments.UserArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.dataclasses.MuteEntry
import me.markhc.hangoutbot.extensions.availableThroughDMs
import me.markhc.hangoutbot.utilities.*
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.SimpleFormatter
import kotlin.math.roundToLong
import kotlin.time.hours
import kotlin.time.milliseconds

private val dateFormatter = DateTimeFormat.fullDateTime()

@CommandSet("Utility")
fun utilityCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    command("ping") {
        description = "pong"
        availableThroughDMs = true
        execute {
            it.respond("Gateway ping: ${it.discord.jda.gatewayPing}")
        }
    }

    command("serverinfo") {
        description = "Display a message giving basic server information"
        execute {
            val guild = it.guild!!

            it.respond(buildServerInfoEmbed(guild))
        }
    }

    command("viewjoindate") {
        description = "Displays when a user joined the guild"
        execute(MemberArg) {
            val member = it.args.first

            val joinTime = DateTime(member.timeJoined.toInstant().toEpochMilli(), DateTimeZone.UTC)

            it.respond("${member.fullName()}'s join date: ${joinTime.toString(dateFormatter)}")
        }
    }

    command("viewcreationdate") {
        description = "Displays when a user was created"
        availableThroughDMs = true
        execute(UserArg) {
            val user = it.args.first

            val createdTime = DateTime(user.timeCreated.toInstant().toEpochMilli(), DateTimeZone.UTC)

            it.respond("${user.fullName()}'s creation date: ${createdTime.toString(dateFormatter)}")
        }
    }

    command("avatar") {
        description = "Gets the avatar from the given user"
        availableThroughDMs = true
        execute(UserArg) {
            val user = it.args.first

            it.respond("${user.avatarUrl}")
        }
    }

    command("selfmute") {
        description = "Mute yourself for an amout of time. Default is 1 hour. Max is 24 hours."
        execute(TimeStringArg.makeOptional { 3600.0 }) {
            val (timeInSeconds) = it.args
            val guild = it.guild!!
            val guildConfig = config.getGuildConfig(guild.id)

            if(guildConfig.muteRole.isEmpty()) {
                return@execute it.respond("Sorry, this guild does not have a mute role.")
            }

            val role = guild.getRoleById(guildConfig.muteRole)
                    ?: return@execute it.respond("Sorry, this guild does not have a mute role.")

            val member = guild.getMember(it.author)!!

            if(guildConfig.muteRole in member.roles.map { r -> r.id }.toList()) {
                return@execute it.respond("Nice try, but you're already muted!")
            }

            val millis = timeInSeconds.roundToLong() * 1000
            guildConfig.addMutedMember(member, millis)
            persistence.save(config)

            muteMemberWithTimer(member, role, millis) {
                guildConfig.removeMutedMember(this)
                persistence.save(config)
                unmuteMember(this, role)
            }

            it.author.sendPrivateMessage(buildSelfMuteEmbed(member, millis))
        }
    }
}