package me.markhc.hangoutbot.commands

import com.github.ricksbrown.cowsay.Cowsay
import kotlin.random.Random
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.SplitterArg
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.markhc.hangoutbot.locale.Messages

@CommandSet("Fun")
@Suppress("unused")
class FunCommands() {
    object CowsayData {
        val validCows = Cowsay.say(arrayOf("-l")).split("\n").filterNot { listOf("sodomized", "head-in", "telebears").contains(it) }
    }

    fun produce() = commands {
        command("coin") {
            description = "Flip a coin (or coins)."
            execute(IntegerArg("Coins").makeOptional { 1 }) {
                val (coins) = it.args

                if (coins <= 0 || coins > 100000) {
                    return@execute it.respond("Sorry, cannot flip that many coins")
                }

                if (coins == 1) {
                    if (Random.nextDouble() > 0.5) {
                        return@execute it.respond("Heads!")
                    } else {
                        return@execute it.respond("Tails!")
                    }
                }

                var heads = 0
                var tails = 0

                for (i in 0..coins) {
                    if (Random.nextDouble() > 0.5) {
                        heads++
                    } else {
                        tails++
                    }
                }

                it.respond("Fliiped $coins coins. Result: $heads heads, $tails tails!")
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
            execute(IntegerArg("Min").makeOptional { 1 }, IntegerArg("Max").makeOptional { 100 }) {
                val (a, b) = it.args
                if (a == b) return@execute it.respond("$a")
                val result = if (a > b) Random.nextInt(b, a) else Random.nextInt(a, b)

                it.respond("$result")
            }
        }

        command("cowsay") {
            description = "Displays a cowsay with a given message. Run with no arguments to get a list of valid cows."
            execute(WordArg("Cow").makeOptional { "" }, SentenceArg("Message").makeOptional { "" }) {
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
}