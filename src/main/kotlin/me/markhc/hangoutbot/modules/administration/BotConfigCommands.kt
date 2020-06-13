package me.markhc.hangoutbot.modules.administration

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.RoleArg
import me.jakejmattson.kutils.api.arguments.TextChannelArg
import me.jakejmattson.kutils.api.dsl.command.commands
import me.markhc.hangoutbot.modules.administration.services.GreetingService
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.runLoggedCommand
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Configuration")
fun botConfigCommands(persistentData: PersistentData, greetingService: GreetingService) = commands {
    command("muterole") {
        description = "Gets or sets the role used to mute an user."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(RoleArg.makeNullableOptional(null)) {
            runLoggedCommand(it) {
                val (role) = it.args

                if(role != null) {
                    persistentData.setGuildProperty(it.guild!!) { muteRole = role.id }

                    it.respond("Mute role set to **${role.name}**")
                } else {
                    val roleId = persistentData.getGuildProperty(it.guild!!) { muteRole }

                    if(roleId.isNotEmpty()) {
                        it.respond("Mute role is **${it.guild!!.getRoleById(roleId)?.name}**")
                    } else {
                        it.respond("Mute role is not set")
                    }
                }
            }
        }
    }

    command("softmuterole") {
        description = "Gets or sets the role used to soft mute an user"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(RoleArg.makeNullableOptional(null)) {
            runLoggedCommand(it) {
                val (role) = it.args

                if (role != null) {
                    persistentData.setGuildProperty(it.guild!!) { softMuteRole = role.id }

                    it.respond("Soft mute role set to **${role.name}**")
                } else {
                    val roleId = persistentData.getGuildProperty(it.guild!!) { softMuteRole }

                    if (roleId.isNotEmpty()) {
                        it.respond("Soft mute role is **${it.guild!!.getRoleById(roleId)?.name}**")
                    } else {
                        it.respond("Soft mute role is not set")
                    }
                }
            }
        }
    }

    command("logchannel") {
        description = "Sets the channel used to log executed commands"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(TextChannelArg.makeNullableOptional(null)) {
            runLoggedCommand(it) {
                val (textChannel) = it.args

                if (textChannel != null) {
                    persistentData.setGuildProperty(it.guild!!) { loggingChannel = textChannel.id }

                    it.respond("Logging channel set to **#${textChannel.name}**")
                } else {
                    val channelId = persistentData.getGuildProperty(it.guild!!) { loggingChannel }

                    if (channelId.isNotEmpty()) {
                        val channel = it.guild!!.getGuildChannelById(channelId) as TextChannel?

                        it.respond("Logging channel is ${channel?.asMention}")
                    } else {
                        it.respond("Logging channel is not set")
                    }
                }
            }
        }
    }

    command("botchannel") {
        description = "Sets the bot channel. If set, the bot channel will be the only channel where the bot will accept commands from."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(TextChannelArg.makeNullableOptional(null)) {
            runLoggedCommand(it) {
                val channel = it.args.first

                persistentData.setGuildProperty(it.guild!!) {
                    botChannel = channel?.id ?: ""
                }

                if (channel != null)
                    it.respond("Bot channel set to #${channel.name}. The bot will now ignore commands from anywhere else.")
                else
                    it.respond("Bot channel cleared. Now accepting commands in any channel.")
            }
        }
    }

    command("greetchannel") {
        description = "Gets or sets the channel used for welcome greetings."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(TextChannelArg.makeNullableOptional(null)) {
            runLoggedCommand(it) {
                val (textChannel) = it.args
                val guild = it.guild!!

                if (textChannel != null) {
                    greetingService.setChannel(guild, textChannel)

                    it.respond("Greeting channel set to **#${textChannel.name}**")
                } else {
                    val channel = greetingService.getChannel(guild)?.asMention ?: "not set"

                    it.respond("Greeting channel is $channel")
                }
            }
        }
    }
}
