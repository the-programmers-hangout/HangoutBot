package me.markhc.hangoutbot.commands.information

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.executeLogged

@CommandSet("Bot Information")
fun botInformationCommands(helpService: HelpService, botStats: BotStatsService, config: Configuration) = commands {
    command("source") {
        description = "Get the url for the bot source code."
        executeLogged {
            val properties = it.discord.getInjectionObjects(Properties::class)

            it.respond(properties.repository)
        }
    }

    command("botstats", "ping") {
        description = "Displays miscellaneous information about the bot."
        executeLogged {
            it.respond(embed {
                title { text = "Stats" }
                color = infoColor

                field {
                    name = "Commands"
                    value = """
                        ```
                        Commands executed:      ${String.format("%6d", config.totalCommandsExecuted)}
                        Commands since restart: ${String.format("%6d", botStats.totalCommands)}
                        Average execution time: ${String.format("%6.1f", botStats.avgResponseTime)} ms
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
                    value = botStats.ping
                    inline = true
                }

                field {
                    name = "Uptime"
                    value = botStats.uptime
                }
            })
        }

        command("debugstats") {
            description = "Displays some debugging information"
            requiredPermissionLevel = PermissionLevel.BotOwner
            executeLogged {
                it.respond(embed {
                    title { text = "Debug" }
                    color = infoColor

                    field {
                        name = "Command times"
                        value = "```\n" + botStats.avgCommandTimes.toList()
                                .sortedBy { it.second }
                                .joinToString("\n") {
                                    "${it.first.padEnd(15)} ${it.second.toString().padStart(8)} ms"
                                } + "\n```";
                    }
                })
            }
        }
    }
}
