package me.markhc.hangoutbot.preconditions

import me.jakejmattson.kutils.api.annotations.Precondition
import me.jakejmattson.kutils.api.dsl.preconditions.Fail
import me.jakejmattson.kutils.api.dsl.preconditions.Pass
import me.jakejmattson.kutils.api.dsl.preconditions.precondition

@Precondition(1)
fun produceCommandSanitizerPrecondition() = precondition { event ->
    if(event.rawInputs.commandArgs.any { it.matches(""".*discord\.gg.*""".toRegex()) })
        return@precondition Fail()

    return@precondition Pass
}