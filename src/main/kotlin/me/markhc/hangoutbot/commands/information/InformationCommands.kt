package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.arguments.CommandArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.arguments.UserArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.utilities.buildGuildInfoEmbed
import me.markhc.hangoutbot.utilities.buildMemberInfoEmbed
import me.markhc.hangoutbot.utilities.buildRoleInfoEmbed
import me.markhc.hangoutbot.utilities.buildUserInfoEmbed

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
            //TODO this wont be needed after 0.7.0 kord
            try {
                val invite = guild.getVanityUrl() ?: throw Exception()

                respond(invite)
            } catch (ex: Exception) {
                val guildChannel = guild.rulesChannel ?: guild.systemChannel ?: guild.publicUpdatesChannel ?: channel

                // TODO: Cache these invites so we don't generate a new one every time
                val invite = guildChannel.asChannel().createInvite {
                    age = 86400
                }

                respond("Here's your invite! It will expire in 24 hours!\nhttps://discord.gg/${invite.code}")
            }


//            if (guild.getVanityUrl() != null) {
//                respond(guild.getVanityUrl()!!)
//            } else {
//                val guildChannel = guild.rulesChannel ?: guild.systemChannel ?: guild.publicUpdatesChannel ?: channel
//
//                // TODO: Cache these invites so we don't generate a new one every time
//                val invite = guildChannel.asChannel().createInvite {
//                    age = 86400
//                }
//
//                respond("Here's your invite! It will expire in 24 hours!\n${invite.code}")
//            }
        }
    }

    guildCommand("serverinfo") {
        description = "Display a message giving basic server information."
        execute {
            buildGuildInfoEmbed(guild)
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

    command("spotlight") {
        description = "Gets relevant link to spotlights."
        execute(AnyArg("name").makeOptional("")) {
            val (name) = args

            if (name.isBlank()) {
                respond("Spotlights are occasional, temporary channels that cover a piece of technology that might be unknown to part of our users. Past tech spotlight write-ups are available here https://theprogrammershangout.com/archives. If you have a suggestion, please post in server-meta or contact ModMail.")
                return@execute
            }

            // TODO: check if status 404
            respond("Looking up ${name}... https://theprogrammershangout.com/archives/what-is-${name}.md/")
        }
    }
}
