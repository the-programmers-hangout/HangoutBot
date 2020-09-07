package me.markhc.hangoutbot.commands.information

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.dsl.command.commands
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.services.*
import me.markhc.hangoutbot.utilities.executeLogged

@CommandSet("Bot Information")
fun botInformationCommands(helpService: HelpService, embedService: EmbedService) = commands {
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
            it.respond(embedService.botStats())
        }

        command("debugstats") {
            description = "Displays some debugging information"
            requiredPermissionLevel = PermissionLevel.BotOwner
            executeLogged {
                it.respond(embedService.debugStats())
            }
        }
    }
}
