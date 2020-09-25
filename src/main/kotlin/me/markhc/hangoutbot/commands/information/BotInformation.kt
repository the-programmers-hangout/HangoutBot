package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.dataclasses.*
import me.markhc.hangoutbot.services.*


fun botInformationCommands(helpService: HelpService, botStats: BotStatsService, config: Configuration) = commands("Bot Information") {
    command("source") {
        description = "Get the url for the bot source code."
        execute {
            val properties = discord.getInjectionObjects(Properties::class)
            respond(properties.repository)
        }
    }

    command("botstats", "ping", "uptime") {
        description = "Displays miscellaneous information about the bot."
        execute {
            respond {
                title = "Stats"
                color = discord.configuration.theme

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
            }

            respond(embedService.botStats())
        }

        command("debugstats") {
            description = "Displays some debugging information"
            requiredPermissionLevel = PermissionLevel.BotOwner
            execute {
                respond {
                    title = "Debug"
                    color = discord.configuration.theme

                    field {
                        name = "Command times"
                        value = "```\n" + botStats.avgCommandTimes.toList()
                            .sortedBy { it.second }
                            .joinToString("\n") {
                                "${it.first.padEnd(15)} ${it.second.toString().padStart(8)} ms"
                            } + "\n```"
                    }
                }

            respond(embedService.debugStats())
            }
        }
    }
}
