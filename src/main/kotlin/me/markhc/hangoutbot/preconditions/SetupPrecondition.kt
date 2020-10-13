package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.services.*

class SetupPrecondition(private val persistentData: PersistentData) : Precondition() {

    override suspend fun evaluate(event: CommandEvent<*>): PreconditionResult {

        val command = event.command ?: return Fail()

        val guild = event.guild ?: return Fail()
        if (persistentData.hasGuildConfig(guild.id.value))
            return Pass

        if (!command.names.contains("setup"))
            return Fail("This guild is not setup. You must run `${event.prefix().plus("setup")}` first.")

        return Pass
    }
}