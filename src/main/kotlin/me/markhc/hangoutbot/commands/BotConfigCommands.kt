package me.markhc.hangoutbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.GuildConfiguration

fun botConfigCommands(configuration: Configuration) = commands("Configuration", Permissions(Permission.Administrator)) {
    slash("Configure") {
        description = "Configure guild properties."
        requiredPermissions = Permissions(Permission.ManageGuild)
        execute(RoleArg("Mute", "The role applied when someone is muted"),
            RoleArg("SoftMute", "The role applied to soft-mute a user"),
            ChannelArg("Logging", "The channel where logging messages will be sent")) {

            val (muteRole, softMuteRole, loggingChannel) = args
            configuration[guild.id] = GuildConfiguration(muteRole.id, softMuteRole.id, loggingChannel.id)
            respond("${guild.name} configured!")
        }
    }

    text("cooldown") {
        description = "Gets or sets the command cooldown period (in seconds)."
        execute(IntegerArg.optionalNullable(null)) {
            val (cd) = args

            if (cd != null) {
                if (cd < 1) {
                    respond("Cooldown cannot be less than 1 second!")
                    return@execute
                }
                if (cd > 3600) {
                    respond("Cooldown cannot be more than 1 hour!")
                    return@execute
                }

                configuration[guild].cooldown = cd.toInt()

                respond("Command cooldown set to $cd seconds")
            } else {
                val value = configuration[guild].cooldown
                respond("Command cooldown is $value seconds")
            }
        }
    }
}
