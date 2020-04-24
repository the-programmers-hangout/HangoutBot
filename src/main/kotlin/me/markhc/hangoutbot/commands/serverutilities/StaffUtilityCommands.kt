package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.markhc.hangoutbot.arguments.MyTextChannelArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("StaffUtility")
fun staffUtilityCommands() = commands {
    requiredPermissionLevel = Permission.Staff

    command("echo") {
        description = "Echo a message to a channel."
        execute(MyTextChannelArg.makeOptional { it.channel as TextChannel }, SentenceArg) {
            val (target, message) = it.args

            target.sendMessage(message.sanitiseMentions()).queue()
        }
    }
}