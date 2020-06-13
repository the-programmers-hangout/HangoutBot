package me.markhc.hangoutbot.modules.information

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.arguments.CommandArg
import me.jakejmattson.kutils.api.arguments.RoleArg
import me.jakejmattson.kutils.api.arguments.UserArg
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.utilities.*
import org.joda.time.DateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun formatOffsetTime(time: OffsetDateTime): String {
    val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toInstant().toEpochMilli())
    return "$days days ago, on ${time.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
}

@CommandSet("Information")
fun produceInformationCommands(helpService: HelpService, botStats: BotStatsService, config: Configuration) = commands {
    command("help") {
        description = "Display help information."
        requiresGuild = true
        execute(CommandArg.makeNullableOptional { null }) {
            runLoggedCommand(it) {
                val (command) = it.args

                if (command == null) {
                    it.respond(helpService.buildHelpEmbed(it))
                } else {
                    it.respond(helpService.buildHelpEmbedForCommand(it, command))
                }
            }
        }
    }

    command("invite") {
        description = "Generates an invite link to this server."
        requiresGuild = true
        execute {
            runLoggedCommand(it) {
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
    }

    command("serverinfo") {
        description = "Display a message giving basic server information."
        requiresGuild = true
        execute {
            runLoggedCommand(it) {
                val guild = it.guild!!

                it.respond(buildServerInfoEmbed(guild))
            }
        }
    }

    command("userinfo") {
        description = "Displays information about the given user."
        execute(UserArg("user", allowsBot = true).makeOptional { it.author }) {
            runLoggedCommand(it) {
                val (user) = it.args
                val member = it.guild?.getMember(user)
                if (member != null)
                    it.respond(buildMemberInfoEmbed(member))
                else
                    it.respond(buildUserInfoEmbed(user))
            }
        }
    }

    command("roleinfo") {
        description = "Displays information about the given role."
        requiresGuild = true
        execute(RoleArg) {
            runLoggedCommand(it) {
                it.respond(buildRoleInfoEmbed(it.args.first))
            }
        }
    }

    command("avatar") {
        description = "Gets the avatar from the given user"
        execute(UserArg("user", allowsBot = true).makeOptional { it.author }) {
            runLoggedCommand(it) {
                val user = it.args.first

                it.respond("${user.effectiveAvatarUrl}?size=512")
            }
        }
    }
}
