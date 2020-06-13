package me.markhc.hangoutbot.modules.`fun`

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.github.ricksbrown.cowsay.Cowsay
import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.arguments.IntegerArg
import me.jakejmattson.kutils.api.arguments.EveryArg
import me.jakejmattson.kutils.api.arguments.SplitterArg
import me.jakejmattson.kutils.api.arguments.AnyArg
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.utilities.runLoggedCommand
import kotlin.random.Random

private object CowsayData {
    val validCows = Cowsay.say(arrayOf("-l")).split(System.lineSeparator()).filterNot { listOf("sodomized", "head-in", "telebears").contains(it) }
}
private data class JokeResponse(val id: String = "", val joke: String = "", val status: Int = 500)

@CommandSet("Fun")
fun produceFunCommands() = commands {
    command("coin") {
        description = "Flip a coin (or coins)."
        execute(IntegerArg("Coins").makeOptional(1)) {
            runLoggedCommand(it) {
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
    }

    command("flip") {
        description = "Choose one of the given choices."
        execute(SplitterArg(name = "Choice 1;Choice 2;...", splitter = ";")) {
            runLoggedCommand(it) {
                val (args) = it.args
                val choice = args[Random.nextInt(args.size)].trim()
                it.respond(Messages.getRandomFlipMessage(choice))
            }
        }
    }

    command("roll") {
        description = "Rolls a number in a range (default 1-100)"
        execute(IntegerArg("Min").makeOptional(1), IntegerArg("Max").makeOptional(100)) {
            runLoggedCommand(it) {
                val (a, b) = it.args
                if (a == b) return@execute it.respond("$a")
                val result = if (a > b) Random.nextInt(b, a) else Random.nextInt(a, b)

                it.respond("$result")
            }
        }
    }

    command("cowsay") {
        description = "Displays a cowsay with a given message. Run with no arguments to get a list of valid cows."
        execute(AnyArg("Cow").makeOptional(""), EveryArg("Message").makeOptional("")) {
            runLoggedCommand(it) {
                val (arg0, arg1) = it.args

                it.respond(when {
                    arg0.isBlank() && arg1.isBlank() -> CowsayData.validCows.joinToString(", ")
                    arg1.isBlank() -> "```${Cowsay.say(arrayOf(arg0))}```"
                    CowsayData.validCows.contains(arg0) -> "```${Cowsay.say(arrayOf("-f $arg0", arg1))}```"
                    else -> "```${Cowsay.say(arrayOf("$arg0 $arg1"))}```"
                })
            }
        }
    }

    command("dadjoke") {
        description = "Returns a random dad joke."
        execute { event ->
            runLoggedCommand(event) {
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

