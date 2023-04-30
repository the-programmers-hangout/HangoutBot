package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition
import me.markhc.hangoutbot.dataclasses.Configuration

fun setupPrecondition(configuration: Configuration) = precondition {
    val guild = guild ?: return@precondition fail()

    if (configuration.hasGuildConfig(guild)) return@precondition

    if (command.name.lowercase() != "configure")
        fail("This guild is not setup. You must run `/configure` first.")
}