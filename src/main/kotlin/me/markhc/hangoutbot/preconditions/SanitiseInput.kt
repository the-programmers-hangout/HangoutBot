package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition

@Precondition
fun produceCommandSanitizerPrecondition() = precondition { event ->
    if(event.rawInputs.commandArgs.any { it.matches(""".*discord\.gg.*""".toRegex()) })
        return@precondition Fail()

    return@precondition Pass
}