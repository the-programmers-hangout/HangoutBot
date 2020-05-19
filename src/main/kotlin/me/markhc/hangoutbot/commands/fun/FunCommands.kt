package me.markhc.hangoutbot.commands.`fun`

import com.github.ricksbrown.cowsay.Cowsay
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.arguments.EveryArg
import me.aberrantfox.kjdautils.internal.arguments.SplitterArg
import me.aberrantfox.kjdautils.internal.arguments.AnyArg
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.utilities.XKCD
import kotlin.random.Random

private object CowsayData {
    val validCows = Cowsay.say(arrayOf("-l")).split(System.lineSeparator()).filterNot { listOf("sodomized", "head-in", "telebears").contains(it) }
}

@Suppress("unused")
@CommandSet("Fun")
fun produceFunCommands() = commands {
    command("coin") {
        description = "Flip a coin (or coins)."
        execute(IntegerArg("Coins").makeOptional(1)) {
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
        execute(SplitterArg("Choice 1 | Choice 2 | ...")) {
            val (args) = it.args
            val choice = args[Random.nextInt(args.size)]
            it.respond(Messages.getRandomFlipMessage(choice))
        }
    }

    command("roll") {
        description = "Rolls a number in a range (default 1-100)"
        execute(IntegerArg("Min").makeOptional(1), IntegerArg("Max").makeOptional(100)) {
            val (a, b) = it.args
            if (a == b) return@execute it.respond("$a")
            val result = if (a > b) Random.nextInt(b, a) else Random.nextInt(a, b)

            it.respond("$result")
        }
    }

    command("cowsay") {
        description = "Displays a cowsay with a given message. Run with no arguments to get a list of valid cows."
        execute(AnyArg("Cow").makeOptional(""), EveryArg("Message").makeOptional("")) {
            val (arg0, arg1) = it.args

            it.respond(when {
                arg0.isBlank() && arg1.isBlank() -> CowsayData.validCows.joinToString(", ")
                arg1.isBlank() -> "```${Cowsay.say(arrayOf(arg0))}```"
                CowsayData.validCows.contains(arg0) -> "```${Cowsay.say(arrayOf("-f $arg0", arg1))}```"
                else -> "```${Cowsay.say(arrayOf("$arg0 $arg1"))}```"
            })
        }
    }

    command("xkcd") {
        description = "Returns the XKCD comic number specified, or a random comic if you don't supply a number."
        execute(IntegerArg("Comic Number").makeNullableOptional()) {
            val (id) = it.args

            val latest = XKCD.getLatest()
                    ?: return@execute it.respond("Sorry, failed to get a comic.")

            if(id == null) {
                it.respond(XKCD.getUrl(Random.nextInt(1, latest)))
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

