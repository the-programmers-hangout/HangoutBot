package me.markhc.hangoutbot.modules.information

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.runLoggedCommand

@CommandSet("Bot Information")
fun botInformationCommands(helpService: HelpService, botStats: BotStatsService, config: Configuration) = commands {
    command("source") {
        description = "Get the url for the bot source code."
        execute {
            runLoggedCommand(it) {
                val properties = it.discord.getInjectionObjects(Properties::class)

                it.respond(properties.repository)
            }
        }
    }

    command("botstats") {
        description = "Displays miscellaneous information about the bot."
        execute {
            runLoggedCommand(it) {
                it.respond(embed {
                    title = "Stats"
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
        }

        command("debugstats") {
            description = "Displays some debugging information"
            requiredPermissionLevel = PermissionLevel.BotOwner
            execute {
                runLoggedCommand(it) {
                    it.respond(embed {
                        title = "Debug"
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
}
