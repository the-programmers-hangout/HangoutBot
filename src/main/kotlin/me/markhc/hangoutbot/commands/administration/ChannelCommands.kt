package me.markhc.hangoutbot.commands.administration

import com.gitlab.kordlib.core.behavior.channel.edit
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.arguments.TimeArg
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.executeLogged

fun channelCommands() = commands("Channel") {
    command("slowmode") {
        description = "Set the slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        executeLogged(ChannelArg, TimeArg) {
            val (channel, interval) = args

            if (interval > 21600 || interval < 0) {
                return@executeLogged respond("Invalid time element passed.")
            }
            try {
                channel.asChannel()

                channel.manager.setSlowmode(interval.toInt()).queue {
                    respond("Successfully set slow-mode in channel ${channel.mention} to ${interval.toInt()} seconds.")
                }
            } catch (e: InsufficientPermissionException) {
                respond(e.message!!)
            }
        }
    }
}