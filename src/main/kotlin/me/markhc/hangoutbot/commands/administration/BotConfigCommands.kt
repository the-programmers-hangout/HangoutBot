package me.markhc.hangoutbot.commands.administration

import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.markhc.hangoutbot.commands.administration.services.GreetingService
import me.markhc.hangoutbot.services.*

fun botConfigCommands(persistentData: PersistentData, greetingService: GreetingService) = commands("Configuration") {
    command("muterole") {
        description = "Gets or sets the role used to mute an user."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(RoleArg.makeNullableOptional(null)) {
            val (role) = args

            if (role != null) {
                persistentData.setGuildProperty(guild!!) { muteRole = role.id.value }
                respond("Mute role set to **${role.name}**")
            } else {
                val roleId = persistentData.getGuildProperty(guild!!) { muteRole }.toSnowflake()

                if (roleId != null) {
                    respond("Mute role is **${guild!!.getRole(roleId).name}**")
                } else {
                    respond("Mute role is not set")
                }
            }
        }

        command("softmuterole") {
            description = "Gets or sets the role used to soft mute an user"
            requiredPermissionLevel = PermissionLevel.Administrator
            requiresGuild = true
            execute(RoleArg.makeNullableOptional(null)) {
                val (role) = args

                if (role != null) {
                    persistentData.setGuildProperty(guild!!) { softMuteRole = role.id.value }

                    respond("Soft mute role set to **${role.name}**")
                } else {
                    val roleId = persistentData.getGuildProperty(guild!!) { softMuteRole }.toSnowflake()

                    if (roleId != null) {
                        respond("Soft mute role is **${guild!!.getRole(roleId).name}**")
                    } else {
                        respond("Soft mute role is not set")
                    }
                }
            }
        }

        command("logchannel") {
            description = "Sets the channel used to log executed commands"
            requiredPermissionLevel = PermissionLevel.Administrator
            requiresGuild = true
            execute(ChannelArg.makeNullableOptional(null)) {
                val (textChannel) = args

                if (textChannel != null) {
                    persistentData.setGuildProperty(guild!!) { loggingChannel = textChannel.id.value }

                    respond("Logging channel set to **#${textChannel.name}**")
                } else {
                    val channelId = persistentData.getGuildProperty(guild!!) { loggingChannel }.toSnowflake()

                    if (channelId != null) {
                        val channel = guild!!.getChannelOf<TextChannel>(channelId)

                        respond("Logging channel is ${channel.mention}")
                    } else {
                        respond("Logging channel is not set")
                    }
                }
            }
        }

        command("botchannel") {
            description = "Sets the bot channel. If set, the bot channel will be the only channel where the bot will accept commands from."
            requiredPermissionLevel = PermissionLevel.Administrator
            requiresGuild = true
            execute(ChannelArg.makeNullableOptional(null)) {
                val channel = args.first

                persistentData.setGuildProperty(guild!!) {
                    botChannel = channel?.id?.value ?: ""
                }

                if (channel != null)
                    respond("Bot channel set to #${channel.name}. The bot will now ignore commands from anywhere else.")
                else
                    respond("Bot channel cleared. Now accepting commands in any channel.")
            }
        }

        command("greetchannel") {
            description = "Gets or sets the channel used for welcome greetings."
            requiredPermissionLevel = PermissionLevel.Administrator
            requiresGuild = true
            execute(ChannelArg.makeNullableOptional(null)) {
                val (textChannel) = args
                val guild = guild!!

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
