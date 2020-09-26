package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.*
import me.markhc.hangoutbot.utilities.*

fun produceInformationCommands(helpService: HelpService) = commands("Information") {
    command("help") {
        description = "Display help information."
        requiresGuild = true
        execute(CommandArg.makeNullableOptional { null }) {
            val (command) = args

            if (command == null) {
                respond(helpService.buildHelpEmbed(this))
            } else {
                respond(helpService.buildHelpEmbedForCommand(this, command))
            }
        }
    }

    command("invite") {
        description = "Generates an invite link to this server."
        requiresGuild = true
        execute {
            val guild = guild!!

            if (guild.getVanityUrl() != null) {
                respond(guild.getVanityUrl()!!)
            } else {
                val guildChannel = guild.rulesChannel ?: guild.systemChannel ?: guild.publicUpdatesChannel
                    ?: return@execute respond("Couldn't find a channel to create an invite for")

                // TODO: Cache these invites so we don't generate a new one every time
                val invite = guildChannel.asChannel().createInvite {
                    age = 86400
                }

                respond("Here's your invite! It will expire in 24 hours!\n${invite.code}")
            }
        }
    }

    command("serverinfo") {
        description = "Display a message giving basic server information."
        requiresGuild = true
        execute {
            respond {
                buildGuildInfoEmbed(guild!!)
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

    command("roleinfo") {
        description = "Displays information about the given role."
        requiresGuild = true
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
