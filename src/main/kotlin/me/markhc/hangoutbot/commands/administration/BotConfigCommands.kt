package me.markhc.hangoutbot.commands.administration

import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.commands.administration.services.GreetingService
import me.markhc.hangoutbot.conversations.ConfigurationConversation
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel

fun botConfigCommands(persistentData: PersistentData, greetingService: GreetingService) = commands("Configuration") {
    text("setup") {
        description = "Sets the guild up for first use."
        requiredPermissionLevel = PermissionLevel.GuildOwner
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
        requiredPermissionLevel = PermissionLevel.Administrator
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
            requiredPermissionLevel = PermissionLevel.Administrator
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
            requiredPermissionLevel = PermissionLevel.Administrator
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

        text("botchannel") {
            description = "Sets the bot channel. If set, the bot channel will be the only channel where the bot will accept commands from."
            requiredPermissionLevel = PermissionLevel.Administrator
            execute(ChannelArg.optionalNullable(null)) {
                val channel = args.first

                persistentData.setGuildProperty(guild) {
                    botChannel = channel?.id?.toString() ?: ""
                }

                if (channel != null)
                    respond("Bot channel set to #${channel.name}. The bot will now ignore commands from anywhere else.")
                else
                    respond("Bot channel cleared. Now accepting commands in any channel.")
            }
        }

        text("greetchannel") {
            description = "Gets or sets the channel used for welcome greetings."
            requiredPermissionLevel = PermissionLevel.Administrator
            execute(ChannelArg.optionalNullable(null)) {
                val (textChannel) = args

                if (textChannel != null) {
                    greetingService.setChannel(guild, textChannel)
                    respond("Greeting channel set to **#${textChannel.name}**")
                } else {
                    val channel = greetingService.getChannel(guild)?.mention ?: "not set"
                    respond("Greeting channel is $channel")
                }
            }
        }
    }
}
