package me.markhc.hangoutbot.commands.`fun`

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.EveryArg
import me.jakejmattson.kutils.api.dsl.command.*
import me.markhc.hangoutbot.commands.`fun`.services.AnilistService
import me.markhc.hangoutbot.commands.`fun`.services.MediaType
import me.markhc.hangoutbot.utilities.executeLogged

@CommandSet("Anime")
fun animeCommands(anilistService: AnilistService) = commands {
    command("anime") {
        description = "Searches for an anime on Anilist based on the given terms."
        executeLogged(EveryArg("Search terms")) { event ->
            runCatching {
                anilistService.searchMedia(MediaType.ANIME, event.args.first)
            }.onSuccess { media ->
                event.respond(media.buildEmbed())
            }.onFailure {
                event.respond(it.message!!)
            }
        }
    }

    command("manga") {
        description = "Searches for a manga on Anilist based on the given terms."
        executeLogged(EveryArg("Search terms")) { event ->
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
