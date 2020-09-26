package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.dsl.command.commands
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.services.*

@CommandSet("Bot Information")
fun botInformationCommands(helpService: HelpService, embedService: EmbedService) = commands {
    command("source") {
        description = "Get the url for the bot source code."
        execute {
            val properties = it.discord.getInjectionObjects(Properties::class)

            it.respond(properties.repository)
        }
    }

    command("botstats", "ping", "uptime") {
        description = "Displays miscellaneous information about the bot."
        execute {
            it.respond(embedService.botStats())
        }
    }
}
