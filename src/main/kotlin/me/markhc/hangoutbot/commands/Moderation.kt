package me.markhc.hangoutbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.commands.commands

fun moderationCommands() = commands("Moderation", Permissions(Permission.ManageMessages)) {
    slash("echo") {
        description = "Echo a message to a channel."
        execute(EveryArg("Message"), ChannelArg.optional { it.channel as TextChannel }) {
            val (message, channel) = args
            channel.createMessage(message)
        }
    }

    message("Nuke Messages After", "NukeAfter", "Delete all message following this one") {
        val delete = channel.getMessagesAfter(arg.id).map { it.id }.toList()
        (channel as GuildMessageChannel).bulkDelete(delete)
        respondPublic("Deleted ${delete.size} messages.")
    }
}
