package me.markhc.hangoutbot.commands.administration

import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.commands.administration.services.GreetingService
import me.markhc.hangoutbot.services.*
import me.markhc.hangoutbot.utilities.executeLogged

fun botConfigCommands(persistentData: PersistentData, greetingService: GreetingService) = commands("Configuration") {
    command("muterole") {
        description = "Gets or sets the role used to mute an user."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        executeLogged(RoleArg.makeNullableOptional(null)) {
            val (role) = args

            if (role != null) {
                persistentData.setGuildProperty(guild!!) { muteRole = role.id.value }

                respond("Mute role set to **${role.name}**")
            } else {
                val roleId = persistentData.getGuildProperty(guild!!) { muteRole }

                if (roleId.isNotEmpty()) {
                    respond("Mute role is **${guild!!.getRole(roleId).name}**")
                } else {
                    respond("Mute role is not set")
                }
            }
        }
    }

    command("softmuterole") {
        description = "Gets or sets the role used to soft mute an user"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        executeLogged(RoleArg.makeNullableOptional(null)) {
            val (role) = args

            if (role != null) {
                persistentData.setGuildProperty(guild!!) { softMuteRole = role.id.value }

                respond("Soft mute role set to **${role.name}**")
            } else {
                val roleId = persistentData.getGuildProperty(guild!!) { softMuteRole }

                if (roleId.isNotEmpty()) {
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
        executeLogged(ChannelArg.makeNullableOptional(null)) {
            val (textChannel) = args

            if (textChannel != null) {
                persistentData.setGuildProperty(guild!!) { loggingChannel = textChannel.id.value }

                respond("Logging channel set to **#${textChannel.name}**")
            } else {
                val channelId = persistentData.getGuildProperty(guild!!) { loggingChannel }

                if (channelId.isNotEmpty()) {
                    val channel = guild!!.getGuildChannelById(channelId) as TextChannel?

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
        executeLogged(ChannelArg.makeNullableOptional(null)) {
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
        executeLogged(ChannelArg.makeNullableOptional(null)) {
            val (textChannel) = args
            val guild = guild!!

            if (textChannel != null) {
                greetingService.setChannel(guild, textChannel)
                respond("Greeting channel set to **#${textChannel.name}**")
            } else {
                val channel = greetingService.getChannel(guild).asMention ?: "not set"
                respond("Greeting channel is $channel")
            }
        }
    }
}
