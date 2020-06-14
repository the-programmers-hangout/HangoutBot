package me.markhc.hangoutbot.modules.`fun`

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.EveryArg
import me.jakejmattson.kutils.api.dsl.command.commands
import me.markhc.hangoutbot.modules.`fun`.services.AnilistService
import me.markhc.hangoutbot.modules.`fun`.services.MediaType
import me.markhc.hangoutbot.utilities.runLoggedCommand

@CommandSet("Anime")
fun animeCommands(anilistService: AnilistService/*, loggingService: LoggingService*/) = commands {
    command("anime") {
        description = "Searches for an anime on Anilist based on the given terms."
        execute(EveryArg("Search terms")) { event ->
            runLoggedCommand(event) {
                runCatching {
                    anilistService.searchMedia(MediaType.ANIME, event.args.first)
                }.onSuccess { media ->
                    event.respond(media.buildEmbed())
                }.onFailure {
                    event.respond(it.message!!)
                }
            }
        }
    }

    command("manga") {
        description = "Searches for a manga on Anilist based on the given terms."
        execute(EveryArg("Search terms")) { event ->
            runLoggedCommand(event) {
                runCatching {
                    anilistService.searchMedia(MediaType.MANGA, event.args.first)
                }.onSuccess { media ->
                    event.respond(media.buildEmbed())
                }.onFailure {
                    event.respond(it.message!!)
                }
            }
        }
    }
}
