package me.markhc.hangoutbot.commands.utilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.commands.utilities.services.ColorService
import me.markhc.hangoutbot.extensions.addRole
import me.markhc.hangoutbot.extensions.removeRole
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.*
import net.dv8tion.jda.api.entities.*
import java.awt.Color

@CommandSet("Moderation")
fun moderationCommands(persistentData: PersistentData,
                       colorService: ColorService) = commands {
    command("echo") {
        description = "Echo a message to a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(TextChannelArg.makeOptional { it.channel as TextChannel }, EveryArg) {
            val (target, message) = it.args

            target.sendMessage(message).queue()
        }
    }

    command("nuke") {
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(TextChannelArg.makeOptional { it.channel as TextChannel },
                IntegerArg) {
            val (channel, amount) = it.args

            if (amount !in 2..99) {
                return@execute it.respond("You can only nuke between 2 and 99 messages")
            }

            val sameChannel = it.channel.id == channel.id

            channel.history.retrievePast(amount + if (sameChannel) 1 else 0).queue { past ->
                safeDeleteMessages(channel, past)

                channel.sendMessage("Be nice. No spam.").queue()

                if (!sameChannel) it.respond("$amount messages deleted.")
            }
        }
    }
}

@CommandSet("Slowmode")
fun slowModeCommands() = commands {
    command("setslowmode") {
        description = "Set slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(TextChannelArg, TimeArg) {
            val (channel, interval) = it.args

            if (interval > 21600 || interval < 0) {
                return@execute it.respond("Invalid time element passed.")
            }

            val res = it

            channel.manager.setSlowmode(interval.toInt()).queue {
                res.respond("Successfully set slow-mode in channel ${channel
                        .asMention} to ${interval.toInt()} seconds.")
            }
        }
    }
}

private fun safeDeleteMessages(channel: TextChannel,
                               messages: List<Message>) {
    try {
        channel.deleteMessages(messages).queue()
    } catch (e: IllegalArgumentException) { // some messages older than 2 weeks => can't mass delete
        messages.forEach { it.delete().queue() }
    }
}
