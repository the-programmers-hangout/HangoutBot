package me.markhc.hangoutbot.modules.`fun`

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.arguments.EveryArg
import me.markhc.hangoutbot.modules.`fun`.services.XKCDService

@CommandSet("XKCD")
fun xkcdCommands(xkcd: XKCDService) = commands {
    command("xkcd") {
        description = "Returns the XKCD comic number specified, or a random comic if you don't supply a number."
        execute(IntegerArg("Comic Number").makeNullableOptional()) {
            val (id) = it.args

            val latest = xkcd.getLatest()
                    ?: return@execute it.respond("Sorry, failed to get a comic.")

            if(id == null) {
                it.respond(xkcd.getUrl(kotlin.random.Random.nextInt(1, latest)))
            } else if(id < 1 || id > latest) {
                it.respond("Please enter a valid comic number between 1 and $latest")
            } else {
                it.respond(xkcd.getUrl(id))
            }
        }
    }

    command("xkcd-latest") {
        description = "Grabs the latest XKCD comic."
        execute {
            val latest = xkcd.getLatest()
                    ?: return@execute it.respond("Sorry, failed to get latest comic.")

            it.respond(xkcd.getUrl(latest))
        }
    }

    command("xkcd-search") {
        description = "Returns a XKCD comic that most closely matches your query."
        execute(EveryArg("Query")) {
            val (what) = it.args

            val result = xkcd.search(what) ?: return@execute it.respond("Sorry, the search failed.")

            it.respond(xkcd.getUrl(result))
        }
    }
}