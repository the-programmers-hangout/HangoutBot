package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.dsl.preconditions.Fail
import me.jakejmattson.discordkt.api.dsl.preconditions.Pass
import me.jakejmattson.discordkt.api.dsl.preconditions.Precondition
import me.jakejmattson.discordkt.api.dsl.preconditions.PreconditionResult

class SanitiseInvites : Precondition() {
    override fun evaluate(event: CommandEvent<*>): PreconditionResult {
        if (event.rawInputs.commandArgs.any { it.matches(""".*discord\.gg.*""".toRegex()) })
            return Fail()

        return Pass
    }
}