package me.markhc.hangoutbot.modules.utilities

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.arguments.*
import me.markhc.hangoutbot.modules.utilities.services.ColorService
import me.markhc.hangoutbot.services.*
import me.markhc.hangoutbot.utilities.runLoggedCommand
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

@CommandSet("Moderation")
fun moderationCommands(persistentData: PersistentData,
                       colorService: ColorService) = commands {
    command("echo") {
        description = "Echo a message to a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(TextChannelArg.makeOptional { it.channel as TextChannel }, EveryArg) {
            runLoggedCommand(it) {
                val (target, message) = it.args

                target.sendMessage(message).queue()
            }
        }
    }

    command("nuke") {
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(TextChannelArg.makeOptional { it.channel as TextChannel },
                IntegerArg) {
            runLoggedCommand(it) {
                val (channel, amount) = it.args

                if (amount !in 2..99) {
                    return@execute it.respond("You can only nuke between 2 and 99 messages")
                }

                val sameChannel = it.channel.id == channel.id

                try {
                    channel.history.retrievePast(amount + if (sameChannel) 1 else 0).queue { past ->
                        safeDeleteMessages(channel, past)

                        channel.sendMessage("Be nice. No spam.").queue()

                        if (!sameChannel) it.respond("$amount messages deleted.")
                    }
                } catch (e: InsufficientPermissionException) {
                    it.respond(e.message!!)
                }
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
