package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.internal.command.*
import me.markhc.hangoutbot.locale.Messages

@Precondition
fun produceIsDirectMessagePrecondition() = precondition {
    val command = it.container[it.commandStruct.commandName] ?: return@precondition Fail()

    return@precondition when {
        it.guild != null -> Pass
        else -> Fail(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)
    }
}