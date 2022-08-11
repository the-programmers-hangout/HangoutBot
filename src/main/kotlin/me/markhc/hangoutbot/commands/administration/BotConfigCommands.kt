package me.markhc.hangoutbot.commands.administration

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.conversations.ConfigurationConversation
import me.markhc.hangoutbot.services.PersistentData

fun botConfigCommands(persistentData: PersistentData) = commands("Configuration", Permissions(Permission.Administrator)) {
    text("setup") {
        description = "Sets the guild up for first use."
        requiredPermissions = Permissions(Permission.ManageGuild)
        execute {
            if (persistentData.hasGuildConfig(guild.id.toString())) {
                respond("This guild is already setup.")
                return@execute
            }

            ConfigurationConversation(persistentData)
                .createConfigurationConversation(guild)
                .startPublicly(discord, author, channel.asChannel())

            respond("${guild.name} has been setup")
        }
    }

    text("muterole") {
        description = "Gets or sets the role used to mute an user."
        execute(RoleArg.optionalNullable(null)) {
            val (role) = args

            if (role != null) {
                persistentData.setGuildProperty(guild) { muteRole = role.id.toString() }
                respond("Mute role set to **${role.name}**")
            } else {
                val roleId = persistentData.getGuildProperty(guild) { muteRole }.toSnowflakeOrNull()

                if (roleId != null) {
                    respond("Mute role is **${guild.getRole(roleId).name}**")
                } else {
                    respond("Mute role is not set")
                }
            }
        }

        text("softmuterole") {
            description = "Gets or sets the role used to soft mute an user"
            execute(RoleArg.optionalNullable(null)) {
                val (role) = args

                if (role != null) {
                    persistentData.setGuildProperty(guild) { softMuteRole = role.id.toString() }

                    respond("Soft mute role set to **${role.name}**")
                } else {
                    val roleId = persistentData.getGuildProperty(guild) { softMuteRole }.toSnowflakeOrNull()

                    if (roleId != null) {
                        respond("Soft mute role is **${guild.getRole(roleId).name}**")
                    } else {
                        respond("Soft mute role is not set")
                    }
                }
            }
        }

        text("logchannel") {
            description = "Sets the channel used to log executed commands"
            execute(ChannelArg.optionalNullable(null)) {
                val (textChannel) = args

                if (textChannel != null) {
                    persistentData.setGuildProperty(guild) { loggingChannel = textChannel.id.toString() }

                    respond("Logging channel set to **#${textChannel.name}**")
                } else {
                    val channelId = persistentData.getGuildProperty(guild) { loggingChannel }.toSnowflakeOrNull()

                    if (channelId != null) {
                        val channel = guild.getChannelOf<TextChannel>(channelId)

                        respond("Logging channel is ${channel.mention}")
                    } else {
                        respond("Logging channel is not set")
                    }
                }
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

                    persistentData.setGuildProperty(guild) {
                        cooldown = cd.toInt()
                    }

                    respond("Command cooldown set to $cd seconds")
                } else {
                    val value = persistentData.getGuildProperty(guild) {
                        cooldown
                    }
                    respond("Command cooldown is $value seconds")
                }
            }
        }
    }
}
