package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.arguments.UserArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.availableThroughDMs
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.utilities.buildServerInfoEmbed
import java.text.SimpleDateFormat

@CommandSet("Utility")
fun utilityCommands(guildConfigs: GuildConfigurations) = commands {
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

            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val joinDateParsed = dateFormat.parse(member.timeJoined.toString())
            val joinDate = dateFormat.format(joinDateParsed)

            it.respond("${member.fullName()}'s join date: $joinDate")
        }
    }

    command("viewcreationdate") {
        description = "Displays when a user was created"
        availableThroughDMs = true
        execute(UserArg) {
            val member = it.args.first

            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val joinDateParsed = dateFormat.parse(member.timeCreated.toString())
            val joinDate = dateFormat.format(joinDateParsed)

            it.respond("${member.fullName()}'s creation date: $joinDate")
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
}
