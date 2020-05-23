package me.markhc.hangoutbot.commands.`fun`

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.arguments.EveryArg
import me.markhc.hangoutbot.utilities.XKCD

@CommandSet("XKCD")
fun xkcdCommands() = commands {
    command("xkcd") {
        description = "Returns the XKCD comic number specified, or a random comic if you don't supply a number."
        execute(IntegerArg("Comic Number").makeNullableOptional()) {
            val (id) = it.args

            val latest = XKCD.getLatest()
                    ?: return@execute it.respond("Sorry, failed to get a comic.")

            if(id == null) {
                it.respond(XKCD.getUrl(kotlin.random.Random.nextInt(1, latest)))
            } else if(id < 1 || id > latest) {
                it.respond("Please enter a valid comic number between 1 and $latest")
            } else {
                it.respond(XKCD.getUrl(id))
            }
        }
    }

    command("xkcd-latest") {
        description = "Grabs the latest XKCD comic."
        execute {
            val latest = XKCD.getLatest()
                    ?: return@execute it.respond("Sorry, failed to get latest comic.")

            it.respond(XKCD.getUrl(latest))
        }
    }

    command("xkcd-search") {
        description = "Returns a XKCD comic that most closely matches your query."
        execute(EveryArg("Query")) {
            val (what) = it.args

            val result = XKCD.search(what) ?: return@execute it.respond("Sorry, the search failed.")

            it.respond(XKCD.getUrl(result))
        }
    }
}