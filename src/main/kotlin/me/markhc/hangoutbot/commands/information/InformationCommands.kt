package me.markhc.hangoutbot.commands.information

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.api.getInjectionObject
import me.aberrantfox.kjdautils.internal.arguments.CommandArg
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.UserArg
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.services.Properties
import me.markhc.hangoutbot.utilities.*
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.util.Date

@Suppress("unused")
@CommandSet("Information")
fun produceInformationCommands(botStats: BotStatsService, config: Configuration, helpService: HelpService) = commands {
    command("help") {
        description = "Display help information."
        requiresGuild = true
        execute(CommandArg.makeNullableOptional { null }) {
            val (command) = it.args

            if(command == null) {
                it.respond(helpService.buildHelpEmbed(it))
            } else {
                it.respond(helpService.buildHelpEmbedForCommand(it, command))
            }
        }
    }

    command("invite") {
        description = "Generates an invite link to this server."
        requiresGuild = true
        execute {
            val guild = it.guild!!

            if(guild.vanityUrl != null) {
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

    command("ping") {
        description = "pong."
        execute {
            it.respond("${botStats.ping}")
        }
    }

    command("serverinfo") {
        description = "Display a message giving basic server information."
        requiresGuild = true
        execute {
            val guild = it.guild!!

            it.respond(buildServerInfoEmbed(guild))
        }
    }

    command("userinfo") {
        description = "Displays information about the given user."
        execute(UserArg) {
            val (user) = it.args
            val member = it.guild?.getMember(user)
            if(member != null)
                it.respond(buildMemberInfoEmbed(member))
            else
                it.respond(buildUserInfoEmbed(user))
        }
    }

    command("roleinfo") {
        description = "Displays information about the given role."
        requiresGuild = true
        execute(RoleArg) {
            it.respond(buildRoleInfoEmbed(it.args.first))
        }
    }

    command("source") {
        description = "Get the url for the bot source code."
        execute {
            val properties = it.discord.getInjectionObject<Properties>()

            it.respond(properties?.repository ?: "None")
        }
    }

    command("uptime") {
        description = "Displays how long the bot has been running for."
        execute {
            it.respond("I have been running for ${botStats.uptime}")
        }
    }

    command("botstats") {
        description = "Displays miscellaneous information about the bot."
        execute {
            it.respond(embed {
                title = "Stats"
                color = infoColor

                field {
                    name = "Commands"
                    value = """
                        ```
                        Commands executed:      ${String.format("%6d", config.totalCommandsExecuted)}
                        Commands since restart: ${String.format("%6d", botStats.totalCommands)}
                        ```
                    """.trimIndent()
                }

                val runtime = Runtime.getRuntime()
                val usedMemory = runtime.totalMemory() - runtime.freeMemory()

                field {
                    name = "Memory"
                    value = "${usedMemory / 1000000}/${runtime.totalMemory() / 1000000} MiB"
                    inline = true
                }

                field {
                    name = "Ping"
                    value = "${botStats.ping} ms"
                    inline = true
                }

                field {
                    name = "Uptime"
                    value = botStats.uptime
                }
            })
        }
    }
}
