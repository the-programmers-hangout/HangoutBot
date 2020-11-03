package me.markhc.hangoutbot.commands.utilities

import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.TextChannel
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.requiredPermissionLevel

fun moderationCommands() = commands("Moderation") {
    guildCommand("echo") {
        description = "Echo a message to a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(ChannelArg.makeOptional { it.channel as TextChannel }, EveryArg) {
            val (target, message) = args

            target.createMessage(message)
        }
    }

    guildCommand("nuke") {
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        requiredPermissionLevel = PermissionLevel.Staff
        execute(ChannelArg.makeNullableOptional(), IntegerArg) {
            val targetChannel = args.first ?: channel
            val amount = args.second

            if (amount !in 2..99) {
                respond("You can only nuke between 2 and 99 messages")
                return@execute
            }

            val sameChannel = targetChannel.id == channel.id

            val messages = targetChannel.getMessagesBefore(message.id, amount).toList()

            if (sameChannel)
                message.delete()

            safeDeleteMessages(targetChannel, messages)

            targetChannel.createMessage("Be nice. No spam.")

            if (!sameChannel) respond("$amount messages deleted.")
        }
    }
}

private suspend fun safeDeleteMessages(channel: TextChannel, messages: List<Message>) = channel.bulkDelete(messages.map { it.id })
