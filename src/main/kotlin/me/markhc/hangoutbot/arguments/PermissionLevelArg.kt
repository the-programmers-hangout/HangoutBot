package me.markhc.hangoutbot.arguments

import me.jakejmattson.kutils.api.dsl.arguments.ArgumentResult
import me.jakejmattson.kutils.api.dsl.arguments.ArgumentType
import me.jakejmattson.kutils.api.dsl.arguments.Error
import me.jakejmattson.kutils.api.dsl.arguments.Success
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.markhc.hangoutbot.services.PermissionLevel

open class PermissionLevelArg(override val name : String = "Permission Level") : ArgumentType<PermissionLevel>() {
    companion object : PermissionLevelArg()

    override fun generateExamples(event: CommandEvent<*>)
            = mutableListOf("BotOwner", "GuildOwner", "Administrator", "Staff", "Everyone")

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<PermissionLevel> {
        val level = PermissionLevel.values().firstOrNull {
            it.name.equals(arg, true)
        } ?: return Error("Could not retrieve permission level: $arg")

        return Success(level, 1)
    }
}
