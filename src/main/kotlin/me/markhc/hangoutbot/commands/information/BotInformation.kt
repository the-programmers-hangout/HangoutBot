package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.commands.commands
import me.markhc.hangoutbot.dataclasses.*
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.utilities.createBotStatsEmbed

fun botInformationCommands(botStats: BotStatsService, config: Configuration) = commands("Bot Information") {
    command("source") {
        description = "Get the url for the bot source code."
        execute {
            respond("https://github.com/the-programmers-hangout/HangoutBot/")
        }
    }

    command("botstats", "ping", "uptime") {
        description = "Displays miscellaneous information about the bot."
        execute {
            createBotStatsEmbed(botStats, config)
        }
    }
}

