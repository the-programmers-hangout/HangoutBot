package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition
import me.markhc.hangoutbot.dataclasses.Configuration
import java.util.*

fun setupPrecondition(configuration: Configuration) = precondition {
    val command = command ?: return@precondition fail()
    val guild = guild ?: return@precondition fail()

    if (configuration.hasGuildConfig(guild)) return@precondition

    if (!command.names.any { it.lowercase(Locale.getDefault()) == "setup" })
        fail("This guild is not setup. You must run `${prefix().plus("setup")}` first.")

    return@precondition
}