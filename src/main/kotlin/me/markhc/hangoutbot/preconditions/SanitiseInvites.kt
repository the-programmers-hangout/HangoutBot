package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition

fun sanitiseInvites() = precondition {
    if (args.toList().any { it.matches(""".*discord\.gg.*""".toRegex()) })
        return@precondition fail()

    return@precondition
}
