package me.markhc.hangoutbot.modules.information

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.api.getInjectionObject
import me.markhc.hangoutbot.configuration.Configuration
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.HelpService
import me.markhc.hangoutbot.configuration.Properties

@CommandSet("Bot Information")
fun botInformationCommands(helpService: HelpService, botStats: BotStatsService, config: Configuration) = commands {
    command("source") {
        description = "Get the url for the bot source code."
        execute {
            val properties = it.discord.getInjectionObject<Properties>()

            it.respond(properties?.repository ?: "None")
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
}
