package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.utilities.*

fun produceInformationCommands(helpService: HelpService) = commands("Information") {
    command("help") {
        description = "Display help information."
        execute(CommandArg.makeNullableOptional { null }) {
            val (command) = args

            if (command == null) {
                helpService.buildHelpEmbed(this)
            } else {
                helpService.buildHelpEmbedForCommand(this, command)
            }
        }
    }

    guildCommand("invite") {
        description = "Generates an invite link to this server."
        execute {

            if (guild.getVanityUrl() != null) {
                respond(guild.getVanityUrl()!!)
            } else {
                val guildChannel = guild.rulesChannel ?: guild.systemChannel ?: guild.publicUpdatesChannel ?: channel

                // TODO: Cache these invites so we don't generate a new one every time
                val invite = guildChannel.asChannel().createInvite {
                    age = 86400
                }

                respond("Here's your invite! It will expire in 24 hours!\n${invite.code}")
            }
        }
    }

    guildCommand("serverinfo") {
        description = "Display a message giving basic server information."
        execute {
            respond {
                buildGuildInfoEmbed(guild)
            }
        }
    }

    command("userinfo") {
        description = "Displays information about the given user."
        execute(UserArg("user").makeOptional { it.author }) {
            val (user) = args
            val member = guild?.getMember(user.id)

            if (member != null)
                buildMemberInfoEmbed(member)
            else
                buildUserInfoEmbed(user)
        }
    }

    guildCommand("roleinfo") {
        description = "Displays information about the given role."
        execute(RoleArg) {
            buildRoleInfoEmbed(args.first)
        }
    }

    command("avatar") {
        description = "Gets the avatar from the given user"
        execute(UserArg("user").makeOptional { it.author }) {
            val user = args.first

            respond("${user.avatar.url}?size=512")
        }
    }
}
