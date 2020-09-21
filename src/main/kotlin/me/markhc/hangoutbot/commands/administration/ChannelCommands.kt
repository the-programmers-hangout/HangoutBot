package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.TextChannelArg
import me.jakejmattson.discordkt.api.arguments.TimeArg
import me.jakejmattson.discordkt.api.dsl.command.commands
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.executeLogged
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

@CommandSet("Channel")
fun channelCommands() = commands {
    command("slowmode") {
        description = "Set the slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        executeLogged(TextChannelArg, TimeArg) { event ->
            val (channel, interval) = event.args

            if (interval > 21600 || interval < 0) {
                return@executeLogged event.respond("Invalid time element passed.")
            }
            try {
                channel.manager.setSlowmode(interval.toInt()).queue {
                    event.respond("Successfully set slow-mode in channel ${channel.asMention} to ${interval.toInt()} seconds.")
                }
            } catch (e: InsufficientPermissionException) {
                event.respond(e.message!!)
            }
        }
    }
}