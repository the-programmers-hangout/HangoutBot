package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.*

fun produceInformationCommands(helpService: HelpService, embedService: EmbedService) = commands("Information") {
    command("help") {
        description = "Display help information."
        requiresGuild = true
        execute(CommandArg.makeNullableOptional { null }) {
            val (command) = args

            if (command == null) {
                respond(helpService.buildHelpEmbed(it))
            } else {
                respond(helpService.buildHelpEmbedForCommand(it, command))
            }
        }
    }

    command("invite") {
        description = "Generates an invite link to this server."
        requiresGuild = true
        execute {
            val guild = guild!!

            if (guild.vanityUrl != null) {
                respond(guild.vanityUrl!!)
            } else {
                val guildChannel = guild.getGuildChannelById(guild.defaultChannel!!.id)!!

                // TODO: Cache these invites so we don't generate a new one every time
                guildChannel.createInvite().setMaxAge(86400).queue { invite ->
                    respond("Here's your invite! It will expire in 24 hours!\n${invite.url}")
                }
            }
        }
    }

    command("serverinfo") {
        description = "Display a message giving basic server information."
        requiresGuild = true
        execute {
            respond(embedService.guildInfo(guild!!))
        }
    }

    command("userinfo") {
        description = "Displays information about the given user."
        execute(UserArg("user").makeOptional { it.author }) {
            val (user) = args
            val member = guild?.getMember(user.id)

            if (member != null)
                respond(embedService.memberInfo(member))
            else
                respond(embedService.userInfo(user))
        }
    }

    command("roleinfo") {
        description = "Displays information about the given role."
        requiresGuild = true
        execute(RoleArg) {
            respond(embedService.roleInfo(args.first))
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
