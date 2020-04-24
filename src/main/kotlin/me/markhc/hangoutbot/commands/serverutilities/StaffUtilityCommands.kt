package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.UserArg
import me.markhc.hangoutbot.arguments.MyTextChannelArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import java.text.SimpleDateFormat

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

    command("nuke") {
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        execute(MyTextChannelArg.makeOptional { it.channel as TextChannel },
                IntegerArg) {
            val (channel, amount) = it.args

            if (amount !in 2..99) {
                return@execute it.respond("You can only nuke between 2 and 99 messages")
            }

            val sameChannel = it.channel.id == channel.id
            val singlePrefixInvocationDeleted = it.stealthInvocation

            channel.history.retrievePast(amount + if (sameChannel) 1 else 0).queue { past ->
                val noSinglePrefixMsg = past.drop(if (sameChannel && singlePrefixInvocationDeleted) 1 else 0)

                safeDeleteMessages(channel, noSinglePrefixMsg)

                channel.sendMessage("Be nice. No spam.").queue()

                if (!sameChannel) it.respond("$amount messages deleted.")
            }
        }
    }
}

fun safeDeleteMessages(channel: TextChannel,
                       messages: List<Message>) {
    try {
        channel.deleteMessages(messages).queue()
    } catch (e: IllegalArgumentException) { // some messages older than 2 weeks => can't mass delete
        messages.forEach { it.delete().queue() }
    }
}
