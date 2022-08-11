package me.markhc.hangoutbot.commands

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import me.jakejmattson.discordkt.arguments.SplitterArg
import me.jakejmattson.discordkt.commands.commands
import me.markhc.hangoutbot.locale.Messages
import kotlin.random.Random

private data class JokeResponse(val id: String = "", val joke: String = "", val status: Int = 500)

fun produceFunCommands() = commands("Fun") {
    command("flip") {
        description = "Choose one of the given choices."
        execute(SplitterArg("Choices", ";")) {
            val (args) = args
            val choice = args[Random.nextInt(args.size)]
            respond(Messages.getRandomFlipMessage(choice))
        }
    }

    command("dadjoke") {
        description = "Returns a random dad joke."
        execute {
            val (_, _, result) = Fuel
                .get("https://icanhazdadjoke.com/")
                .set("User-Agent", "HangoutBot (https://github.com/the-programmers-hangout/HangoutBot/)")
                .set("Accept", "application/json")
                .responseObject<JokeResponse>()

            result.fold(
                success = {
                    if (it.status == 200) {
                        respond(it.joke)
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