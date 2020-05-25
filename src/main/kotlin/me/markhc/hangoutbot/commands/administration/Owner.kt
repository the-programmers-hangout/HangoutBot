package me.markhc.hangoutbot.commands.administration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.AnyArg
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData

@CommandSet("Owner Commands")
fun ownerCommands(persistentData: PersistentData) = commands {
    command("cooldown") {
        description = "Gets or sets the cooldown (in seconds) after a user executes a command before he is able to execute another."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(IntegerArg.makeNullableOptional(null)) {
            val (cd) = it.args

            if(cd != null) {
                if(cd < 1) {
                    return@execute it.respond("Cooldown cannot be less than 1 second!")
                }

                persistentData.getGuildProperty(it.guild!!) {
                    cooldown = cd.toInt()
                }

                it.respond("Command cooldown set to $cd seconds")
            } else {
                val value = persistentData.getGuildProperty(it.guild!!) {
                    cooldown
                }
                it.respond("Command cooldown is $value seconds")
            }
        }
    }

    command("setprefix") {
        description = "Sets the bot prefix."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(AnyArg("Prefix")) {
            persistentData.setGuildProperty(it.guild!!) {
                prefix = it.args.first!!
            }

            it.respond("Bot prefix set to **${it.args.first}**")
        }
    }
}