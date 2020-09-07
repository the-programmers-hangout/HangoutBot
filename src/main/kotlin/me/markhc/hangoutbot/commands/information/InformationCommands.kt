package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.dsl.command.commands
import me.jakejmattson.discordkt.api.arguments.CommandArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.arguments.UserArg
import me.markhc.hangoutbot.services.EmbedService
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.utilities.*

@CommandSet("Information")
fun produceInformationCommands(helpService: HelpService, embedService: EmbedService) = commands {
    command("help") {
        description = "Display help information."
        requiresGuild = true
        executeLogged(CommandArg.makeNullableOptional { null }) {
            val (command) = it.args

            if (command == null) {
                it.respond(helpService.buildHelpEmbed(it))
            } else {
                it.respond(helpService.buildHelpEmbedForCommand(it, command))
            }
        }
    }

    command("invite") {
        description = "Generates an invite link to this server."
        requiresGuild = true
        executeLogged {
            val guild = it.guild!!

            if (guild.vanityUrl != null) {
                it.respond(guild.vanityUrl!!)
            } else {
                val guildChannel = guild.getGuildChannelById(guild.defaultChannel!!.id)!!

                // TODO: Cache these invites so we don't generate a new one every time
                guildChannel.createInvite().setMaxAge(86400).queue { invite ->
                    it.respond("Here's your invite! It will expire in 24 hours!\n${invite.url}")
                }
            }
        }
    }

    command("serverinfo") {
        description = "Display a message giving basic server information."
        requiresGuild = true
        executeLogged {
            it.respond(embedService.guildInfo(it.guild!!))
        }
    }

    command("userinfo") {
        description = "Displays information about the given user."
        executeLogged(UserArg("user").makeOptional { it.author }) {
            val (user) = it.args
            val member = it.guild?.getMember(user)
            if (member != null)
                it.respond(embedService.memberInfo(member))
            else
                it.respond(embedService.userInfo(user))
        }
    }

    command("roleinfo") {
        description = "Displays information about the given role."
        requiresGuild = true
        executeLogged(RoleArg) {
            it.respond(embedService.roleInfo(it.args.first))
        }
    }

    command("avatar") {
        description = "Gets the avatar from the given user"
        executeLogged(UserArg("user").makeOptional { it.author }) {
            val user = it.args.first

            it.respond("${user.effectiveAvatarUrl}?size=512")
        }
    }
}
