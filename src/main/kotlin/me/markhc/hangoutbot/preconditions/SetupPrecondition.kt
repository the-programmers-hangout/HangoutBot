package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition
import me.markhc.hangoutbot.services.PersistentData
import java.util.*

fun setupPrecondition(persistentData: PersistentData) = precondition {
    val command = command ?: return@precondition fail()
    val guild = guild ?: return@precondition fail()

    if (persistentData.hasGuildConfig(guild.id.toString())) return@precondition

    if (!command.names.any { it.lowercase(Locale.getDefault()) == "setup" })
        fail("This guild is not setup. You must run `${prefix().plus("setup")}` first.")

    return@precondition
}