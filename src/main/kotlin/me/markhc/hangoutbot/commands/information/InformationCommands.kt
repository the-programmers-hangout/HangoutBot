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
import me.markhc.hangoutbot.services.Properties
import me.markhc.hangoutbot.utilities.*
import java.util.Date

@Suppress("unused")
@CommandSet("Information")
fun produceInformationCommands(botStats: BotStatsService, config: Configuration) = commands {
    command("help") {
        description = "Display help information."
        execute(CommandArg.makeNullableOptional { null }) {
            val (command) = it.args

            if(command == null) {
                it.respond(buildHelpEmbed("+", it.container))
            } else {
                it.respond(buildHelpEmbedForCommand(it, "+", command))
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
                    name = "# Commands"
                    value = "${config.totalCommandsExecuted}"
                    inline = true
                }

                field {
                    name = "# Commands this session"
                    value = "${botStats.totalCommands}"
                    inline = true
                }

                field {
                    name = "# Commands in this guild"
                    value = "${config.getGuildConfig(it.guild!!).totalCommandsExecuted}"
                    inline = true
                }

                field {
                    name = "Ping"
                    value = "${botStats.ping}"
                    inline = true
                }

                val runtime = Runtime.getRuntime()

                fun Long.toMB() = (this/1024/1024)

                field {
                    name = "Memory Used"
                    value = "${(runtime.totalMemory() - runtime.freeMemory()).toMB()}/${runtime.totalMemory().toMB()} MiB"
                    inline = true
                }

                field {
                    name = "Uptime"
                    value = botStats.uptime
                    inline = true
                }
            })
        }
    }
}
