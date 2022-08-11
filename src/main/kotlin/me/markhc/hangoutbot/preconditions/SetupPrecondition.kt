package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition
import me.markhc.hangoutbot.services.PersistentData

fun setupPrecondition(persistentData: PersistentData) = precondition {
    val command = command ?: return@precondition fail()
    val guild = guild ?: return@precondition fail()

    if (persistentData.hasGuildConfig(guild.id.toString())) return@precondition

    if (!command.names.any { it.toLowerCase() == "setup" })
        fail("This guild is not setup. You must run `${prefix().plus("setup")}` first.")

    return@precondition
}