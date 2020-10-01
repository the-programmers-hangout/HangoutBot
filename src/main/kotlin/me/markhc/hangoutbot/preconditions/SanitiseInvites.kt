package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.*

class SanitiseInvites : Precondition() {
    override suspend fun evaluate(event: CommandEvent<*>): PreconditionResult {
        if (event.rawInputs.commandArgs.any { it.matches(""".*discord\.gg.*""".toRegex()) })
            return Fail()

        return Pass
    }
}