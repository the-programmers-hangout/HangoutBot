package me.markhc.hangoutbot.commands

import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.arguments.UserArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.pfpUrl
import me.markhc.hangoutbot.utilities.*

fun produceInformationCommands() = commands("Information") {
    text("serverinfo") {
        description = "Display a message giving basic server information."
        execute {
            buildGuildInfoEmbed(guild)
        }
    }

    text("userinfo") {
        description = "Displays information about the given user."
        execute(UserArg("user").optional { it.author }) {
            val (user) = args
            val member = guild.getMemberOrNull(user.id)

            if (member != null)
                buildMemberInfoEmbed(member)
            else
                buildUserInfoEmbed(user)
        }
    }

    text("roleinfo") {
        description = "Displays information about the given role."
        execute(RoleArg) {
            buildRoleInfoEmbed(args.first)
        }
    }

    text("avatar") {
        description = "Gets the avatar from the given user"
        execute(UserArg("user").optional { it.author }) {
            val user = args.first

            respond("${user.pfpUrl}?size=512")
        }
    }
}
