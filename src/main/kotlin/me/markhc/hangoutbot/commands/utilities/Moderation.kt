package me.markhc.hangoutbot.commands.utilities

import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.TextChannel
import kotlinx.coroutines.flow.*
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.*

fun moderationCommands() = commands("Moderation") {
    command("echo") {
        description = "Echo a message to a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(ChannelArg.makeOptional { it.channel as TextChannel }, EveryArg) {
            val (target, message) = args

            target.createMessage(message)
        }
    }

    command("nuke") {
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(ChannelArg.makeOptional { it.channel as TextChannel },
            IntegerArg) {
            val (channel, amount) = args

            if (amount !in 2..99) {
                return@execute respond("You can only nuke between 2 and 99 messages")
            }

            val sameChannel = channel.id == channel.id
            val messages = channel.messages.take(amount + if (sameChannel) 1 else 0).toList()

            safeDeleteMessages(channel, messages)

            channel.createMessage("Be nice. No spam.")

            if (!sameChannel) respond("$amount messages deleted.")
        }
    }
}

private suspend fun safeDeleteMessages(channel: TextChannel, messages: List<Message>) = channel.bulkDelete(messages.map { it.id })
