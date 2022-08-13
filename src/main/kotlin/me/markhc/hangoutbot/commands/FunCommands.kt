package me.markhc.hangoutbot.commands

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import me.jakejmattson.discordkt.arguments.SplitterArg
import me.jakejmattson.discordkt.commands.commands

private data class JokeResponse(val id: String = "", val joke: String = "", val status: Int = 500)

fun produceFunCommands() = commands("Fun") {
    slash("flip") {
        description = "Choose one of the given choices."
        execute(SplitterArg(";", "Choices")) {
            val response = listOf(
                "Hmm, I'd say %choice%.",
                "%choice%, no doubt.",
                "Perhaps... %choice%.",
                "%choice% sounds good to me.",
                "If it were up to me, I'd go with %choice%",
                "East or west, %choice% is the best."
            ).random().replace("%choice%", args.first.random())

            respondPublic(response)
        }
    }

    slash("dadjoke") {
        description = "Returns a random dad joke."
        execute {
            val (_, _, result) = Fuel
                .get("https://icanhazdadjoke.com/")
                .set("User-Agent", "HangoutBot (https://github.com/the-programmers-hangout/HangoutBot/)")
                .set("Accept", "application/json")
                .responseObject<JokeResponse>()

            result.fold<Unit>(
                success = {
                    if (it.status == 200) {
                        respondPublic(it.joke)
                    } else {
                        respond("Failed to fetch joke. Status: ${it.status}")
                    }
                },
                failure = {
                    respond("Error trying to fetch joke")
                })
        }
    }
}