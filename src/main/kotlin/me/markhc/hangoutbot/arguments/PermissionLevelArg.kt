package me.markhc.hangoutbot.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.markhc.hangoutbot.services.PermissionLevel

open class PermissionLevelArg(override val name : String = "Permission Level") : ArgumentType<PermissionLevel>() {
    companion object : PermissionLevelArg()

    override val consumptionType = ConsumptionType.Single
    override fun generateExamples(event: CommandEvent<*>)
            = mutableListOf("BotOwner", "GuildOwner", "Administrator", "Staff", "Everyone")

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<PermissionLevel> {
        val level = PermissionLevel.values().firstOrNull {
            it.name.equals(arg, true)
        } ?: return ArgumentResult.Error("Could not retrieve permission level: $arg")

        return ArgumentResult.Success(level)
    }
}
