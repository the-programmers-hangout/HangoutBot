package me.markhc.hangoutbot.commands.`fun`

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.github.ricksbrown.cowsay.Cowsay
import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.AnyArg
import me.jakejmattson.kutils.api.arguments.EveryArg
import me.jakejmattson.kutils.api.arguments.IntegerArg
import me.jakejmattson.kutils.api.arguments.SplitterArg
import me.jakejmattson.kutils.api.dsl.command.commands
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.utilities.executeLogged
import kotlin.random.Random

private object CowsayData {
    val validCows = Cowsay.say(arrayOf("-l")).split(System.lineSeparator()).filterNot { listOf("sodomized", "head-in", "telebears").contains(it) }
}
private data class JokeResponse(val id: String = "", val joke: String = "", val status: Int = 500)

@CommandSet("Fun")
fun produceFunCommands() = commands {
    command("coin") {
        description = "Flip a coin (or coins)."
        executeLogged(IntegerArg("Coins").makeOptional(1)) {
            val response = when (val coins = it.args.first) {
                1 -> if (Random.nextDouble() > 0.5) "Heads!" else "Tails!"
                in 2..100000 -> {
                    val heads = (0..coins).sumBy { if (Random.nextDouble() > 0.5) 1 else 0 }
                    val tails = coins - heads
                    "Flipped $coins coins. Result: $heads heads, $tails tails!"
                }
                else -> "Sorry, cannot flip that many coins"
            }

            it.respond(response)
        }
    }

    command("flip") {
        description = "Choose one of the given choices."
        executeLogged(SplitterArg("Choices", ";")) {
            val (args) = it.args
            val choice = args[Random.nextInt(args.size)]
            it.respond(Messages.getRandomFlipMessage(choice))
        }

        command("roll") {
            description = "Rolls a number in a range (default 1-100)"
            executeLogged(IntegerArg("Min").makeOptional(1), IntegerArg("Max").makeOptional(100)) {
                val (a, b) = it.args
                if (a == b) return@executeLogged it.respond("$a")
                val result = if (a > b) Random.nextInt(b, a) else Random.nextInt(a, b)

                it.respond("$result")
            }
        }

        command("cowsay") {
            description = "Displays a cowsay with a given message. Run with no arguments to get a list of valid cows."
            executeLogged(AnyArg("Cow").makeOptional(""), EveryArg("Message").makeOptional("")) {
                val (arg0, arg1) = it.args

                it.respond(when {
                    arg0.isBlank() && arg1.isBlank() -> CowsayData.validCows.joinToString(", ")
                    arg1.isBlank() -> "```${Cowsay.say(arrayOf(arg0))}```"
                    CowsayData.validCows.contains(arg0) -> "```${Cowsay.say(arrayOf("-f $arg0", arg1))}```"
                    else -> "```${Cowsay.say(arrayOf("$arg0 $arg1"))}```"
                })
            }
        }

        command("dadjoke") {
            description = "Returns a random dad joke."
            executeLogged { event ->
                val (_, _, result) = Fuel
                        .get("https://icanhazdadjoke.com/")
                        .set("User-Agent", "HangoutBot (https://github.com/the-programmers-hangout/HangoutBot/)")
                        .set("Accept", "application/json")
                        .responseObject<JokeResponse>()

                result.fold(
                        success = {
                            if (it.status == 200) {
                                event.respond(it.joke)
                            } else {
                                event.respond("Failed to fetch joke. Status: ${it.status}")
                            }
                        },
                        failure = {
                            event.respond("Error trying to fetch joke")
                        })
            }
        }
    }
}

