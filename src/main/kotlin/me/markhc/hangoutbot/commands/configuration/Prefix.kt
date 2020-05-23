package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.AnyArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData

@CommandSet("Prefix")
fun prefixCommands(persistentData: PersistentData) = commands {
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