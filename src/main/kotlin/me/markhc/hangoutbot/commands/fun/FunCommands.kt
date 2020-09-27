package me.markhc.hangoutbot.commands.`fun`

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.github.ricksbrown.cowsay.Cowsay
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.locale.Messages
import kotlin.random.Random

private object CowsayData {
    val validCows = Cowsay.say(arrayOf("-l")).split(System.lineSeparator()).filterNot { listOf("sodomized", "head-in", "telebears").contains(it) }
}

private data class JokeResponse(val id: String = "", val joke: String = "", val status: Int = 500)

fun produceFunCommands() = commands("Fun") {
    command("coin") {
        description = "Flip a coin (or coins)."
        execute(IntegerArg("Coins").makeOptional(1)) {
            val response = when (val coins = args.first) {
                1 -> if (Random.nextDouble() > 0.5) "Heads!" else "Tails!"
                in 2..100000 -> {
                    val heads = (0..coins).sumBy { if (Random.nextDouble() > 0.5) 1 else 0 }
                    val tails = coins - heads
                    "Flipped $coins coins. Result: $heads heads, $tails tails!"
                }
                else -> "Sorry, cannot flip that many coins"
            }

            respond(response)
        }
    }

    command("flip") {
        description = "Choose one of the given choices."
        execute(SplitterArg("Choices", ";")) {
            val (args) = args
            val choice = args[Random.nextInt(args.size)]
            respond(Messages.getRandomFlipMessage(choice))
        }
    }

    command("roll") {
        description = "Rolls a number in a range (default 1-100)"
        execute(IntegerArg("Min").makeOptional(1), IntegerArg("Max").makeOptional(100)) {
            val (a, b) = args

            val result =
                when {
                    a > b -> Random.nextInt(b, a)
                    a < b -> Random.nextInt(a, b)
                    else -> a
                }

            respond(result)
        }
    }

    command("cowsay") {
        description = "Displays a cowsay with a given message. Run with no arguments to get a list of valid cows."
        execute(AnyArg("Cow").makeOptional(""), EveryArg("Message").makeOptional("")) {
            val (arg0, arg1) = args

            respond(when {
                arg0.isBlank() && arg1.isBlank() -> CowsayData.validCows.joinToString(", ")
                arg1.isBlank() -> "```${Cowsay.say(arrayOf(arg0))}```"
                CowsayData.validCows.contains(arg0) -> "```${Cowsay.say(arrayOf("-f $arg0", arg1))}```"
                else -> "```${Cowsay.say(arrayOf("$arg0 $arg1"))}```"
            })
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