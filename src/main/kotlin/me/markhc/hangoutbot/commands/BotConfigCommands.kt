package me.markhc.hangoutbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.arguments.ChannelArg
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
}
