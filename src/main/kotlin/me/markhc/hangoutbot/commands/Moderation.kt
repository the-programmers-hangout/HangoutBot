package me.markhc.hangoutbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands

fun moderationCommands() = commands("Moderation", Permissions(Permission.ManageMessages)) {
    text("echo") {
        description = "Echo a message to a channel."
        execute(ChannelArg.optional { it.channel as TextChannel }, EveryArg) {
            val (target, message) = args

            target.createMessage(message)
        }
    }

    text("nuke") {
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        execute(ChannelArg.optionalNullable(), IntegerArg) {
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

private suspend fun safeDeleteMessages(channel: GuildMessageChannel, messages: List<Message>) = channel.bulkDelete(messages.map { it.id })
