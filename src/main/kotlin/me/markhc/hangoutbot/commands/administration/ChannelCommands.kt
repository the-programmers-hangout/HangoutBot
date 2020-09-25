package me.markhc.hangoutbot.commands.administration

import com.gitlab.kordlib.core.behavior.channel.edit
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.*

fun channelCommands() = commands("Channel") {
    command("slowmode") {
        description = "Set the slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(ChannelArg, TimeArg) {
            val (channel, interval) = args

            if (interval > 21600 || interval < 0) {
                return@execute respond("Invalid time element passed.")
            }

            channel.edit {
                rateLimitPerUser = interval.toInt()
            }

            respond("Successfully set slow-mode in channel ${channel.mention} to ${interval.toInt()} seconds.")
        }
    }
}