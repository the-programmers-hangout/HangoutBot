package me.markhc.hangoutbot.commands

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import kotlin.random.Random

@CommandSet("Fun")
fun funCommands() = commands {
    command("flip") {
        description = "Flip a coin (or coins)"
        execute(IntegerArg("Coins").makeOptional { 1 }) {
            val (coins) = it.args

            if(coins <= 0 || coins > 10000) {
                return@execute it.respond("Sorry, cannot flip that many coins")
            }

            if(coins == 1) {
                if(Random.nextDouble() > 0.5) {
                    return@execute it.respond("Heads!")
                } else {
                    return@execute it.respond("Tails!")
                }
            }

            var heads = 0
            var tails = 0

            for(i in 0..coins) {
                if(Random.nextDouble() > 0.5) {
                    heads++
                } else {
                    tails++
                }
            }

            it.respond("Fliiped $coins coins. Result: $heads heads, $tails tails!")
        }
    }

    command("roll") {
        description = "Rolls a number in a range (default 1-100)"
        execute(IntegerArg("Min"), IntegerArg("Max")) {
            val (a, b) = it.args
            val result = if(a > b) Random.nextInt(b, a) else Random.nextInt(a, b)

            it.respond("$result!")
        }
    }
}