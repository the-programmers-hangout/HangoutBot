package me.markhc.hangoutbot.commands.administration

import dev.kord.core.behavior.channel.edit
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.commands.commands
import me.markhc.hangoutbot.services.*

fun channelCommands() = commands("Channel") {
    text("slowmode") {
        description = "Set the slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(ChannelArg, TimeArg) {
            val (channel, interval) = args

            if (interval > 21600 || interval < 0) {
                respond("Invalid time element passed.")
                return@execute
            }

            channel.edit {
                //rateLimitPerUser = interval
            }

            respond("Successfully set slow-mode in channel ${channel.mention} to ${interval.toInt()} seconds.")
        }
    }
}