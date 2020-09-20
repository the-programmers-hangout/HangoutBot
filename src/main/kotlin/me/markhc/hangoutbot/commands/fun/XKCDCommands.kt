package me.markhc.hangoutbot.commands.`fun`

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.commands.`fun`.services.XKCDService
import me.markhc.hangoutbot.utilities.executeLogged
import kotlin.random.Random

fun xkcdCommands(xkcd: XKCDService) = commands("XKCD") {
    command("xkcd") {
        description = "Returns the XKCD comic number specified, or a random comic if you don't supply a number."
        executeLogged(IntegerArg("Comic Number").makeNullableOptional()) {
            val (id) = it.args

            val latest = xkcd.getLatest()
                ?: return@executeLogged it.respond("Sorry, failed to get a comic.")

            if (id == null) {
                it.respond(xkcd.getUrl(Random.nextInt(1, latest)))
            } else if (id < 1 || id > latest) {
                it.respond("Please enter a valid comic number between 1 and $latest")
            } else {
                it.respond(xkcd.getUrl(id))
            }
        }
    }

    command("xkcd-latest") {
        description = "Grabs the latest XKCD comic."
        executeLogged {
            val latest = xkcd.getLatest()
                ?: return@executeLogged it.respond("Sorry, failed to get latest comic.")

            it.respond(xkcd.getUrl(latest))
        }
    }

    command("xkcd-search") {
        description = "Returns a XKCD comic that most closely matches your query."
        executeLogged(EveryArg("Query")) {
            val (what) = it.args

            val result = xkcd.search(what) ?: return@executeLogged it.respond("Sorry, the search failed.")

            it.respond(xkcd.getUrl(result))
        }
    }
}