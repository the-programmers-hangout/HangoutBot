package me.markhc.hangoutbot.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.Role

open class GuildRoleArg(override val name: String = "Role")
    : RoleArg(name) {
    companion object : GuildRoleArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Role> {
        with(super.convert(arg, args, event)) {
            return when(this) {
                is ArgumentResult.Error -> this
                is ArgumentResult.Success -> {
                    if(event.guild == result.guild) {
                        this
                    } else {
                        ArgumentResult.Error("Could not resolve any roles")
                    }
                }
            }
        }

    }
}
