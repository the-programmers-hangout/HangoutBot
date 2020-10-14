package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.precondition

fun sanitiseInvites() = precondition {
    if (rawInputs.commandArgs.any { it.matches(""".*discord\.gg.*""".toRegex()) })
        fail()
}