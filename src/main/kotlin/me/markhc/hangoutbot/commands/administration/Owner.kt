package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.services.*

fun ownerCommands(persistentData: PersistentData) = commands("Owner Commands") {
    guildCommand("cooldown") {
        description = "Gets or sets the command cooldown period (in seconds)."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        execute(IntegerArg.makeNullableOptional(null)) {
            val (cd) = args

            if (cd != null) {
                if (cd < 1) {
                    respond("Cooldown cannot be less than 1 second!")
                    return@execute
                }
                if (cd > 3600) {
                    respond("Cooldown cannot be more than 1 hour!")
                    return@execute
                }

                persistentData.setGuildProperty(guild) {
                    cooldown = cd.toInt()
                }

                respond("Command cooldown set to $cd seconds")
            } else {
                val value = persistentData.getGuildProperty(guild) {
                    cooldown
                }
                respond("Command cooldown is $value seconds")
            }
        }
    }

    guildCommand("setprefix") {
        description = "Sets the bot prefix."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        execute(AnyArg("Prefix")) {
            val newPrefix = args.first

            persistentData.setGuildProperty(guild) {
                prefix = newPrefix
            }

            respond("Bot prefix set to **$newPrefix**")
        }
    }
}