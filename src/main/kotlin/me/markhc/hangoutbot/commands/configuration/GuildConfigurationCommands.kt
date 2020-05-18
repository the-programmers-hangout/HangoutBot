package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData

@Suppress("unused")
@CommandSet("Guild")
fun produceGuildConfigurationCommands(persistentData: PersistentData) = commands {
    command("setprefix") {
        description = "Sets the bot prefix."
        requiredPermissionLevel = PermissionLevel.BotOwner
        requiresGuild = true
        execute(AnyArg("Prefix")) {
            persistentData.setGuildProperty(it.guild!!) {
                prefix = it.args.first
            }

            return@execute it.respond("Bot prefix in this guild set to ${it.args.first}")
        }
    }

    command("setmuterole") {
        description = "Sets the role used to mute an user"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(RoleArg) {
            val (role) = it.args

            persistentData.setGuildProperty(it.guild!!) { muteRole = role.id }

            return@execute it.respond("Mute role set to ${role.name}")
        }
    }

    command("setsoftmuterole") {
        description = "Sets the role used to soft mute an user"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(RoleArg) {
            val (role) = it.args

            persistentData.setGuildProperty(it.guild!!) { softMuteRole = role.id }

            return@execute it.respond("Soft mute role set to ${role.name}")
        }
    }

    command("setlogchannel") {
        description = "Sets the channel used to log executed commands"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(TextChannelArg) {
            val (channel) = it.args

            persistentData.setGuildProperty(it.guild!!) { loggingChannel = channel.id }

            return@execute it.respond("Logging channel set to #${channel.name}")
        }
    }

    command("togglewelcome") {
        description = "Toggles the display of welcome messages upon guild user join."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute {
            val enabled = persistentData.setGuildProperty(it.guild!!) {
                welcomeEmbeds = !welcomeEmbeds
                welcomeEmbeds
            }

            it.respond("Welcome embeds are now ${if(enabled) "enabled" else "disabled"}")
        }
    }

    command("setwelcomechannel") {
        description = "Sets the channel used for welcome embeds."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(TextChannelArg) {
            val (channel) = it.args

            persistentData.setGuildProperty(it.guild!!) { welcomeChannel = channel.id }

            it.respond("Welcome channel set to #${channel.name}")
        }
    }

    command("getwelcomechannel") {
        description = "Gets the channel used for welcome embeds."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute {
            persistentData.getGuildProperty(it.guild!!) {
                if(welcomeChannel.isEmpty())
                    it.respond("Welcome channel not set")
                else
                    it.respond("Welcome channel is #${it.guild!!.getGuildChannelById(welcomeChannel)!!.name}")
            }
        }
    }

    command("makerolegrantable") {
        description = "Adds a role to the list of grantable roles."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(RoleArg, AnyArg("Category")) { event ->
            val (role, category) = event.args

            persistentData.setGuildProperty(event.guild!!) {
                if (grantableRoles.any { it.value.contains(role.id) }) {
                    event.respond("Role is already grantable")
                } else {
                    val key = grantableRoles.keys.find {
                        it.compareTo(category, true) == 0
                    }

                    if (key == null) {
                        grantableRoles[category] = mutableListOf(role.id);
                    } else {
                        grantableRoles[key]!!.add(role.id)
                    }

                    event.respond("Added \"${role.name}\" to the category \"${key ?: category}\".")
                }
            }
        }
    }

    command("removegrantablerole") {
        description = "Removes a role to the list of grantable roles."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(RoleArg) { event ->
            val (role) = event.args

            persistentData.setGuildProperty(event.guild!!) {
                val entry = grantableRoles.entries.find {
                    it.value.contains(role.id)
                } ?: return@setGuildProperty event.respond("Role ${role.name} is not a grantable role.")

                entry.value.remove(role.id)

                if (entry.value.isEmpty()) {
                    grantableRoles.remove(entry.key)
                }

                event.respond("Removed \"${role.name}\" from the list of grantable roles.")
            }
        }
    }

    command("setbotchannel") {
        description = "Sets the bot channel. If set, the bot channel will be the only channel where the bot will accept commands from."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(TextChannelArg.makeNullableOptional(null)) {
            val channel = it.args.first

            persistentData.setGuildProperty(it.guild!!) {
                botChannel = channel?.id ?: ""
            }

            if(channel != null)
                it.respond("Bot channel set to #${channel.name}. The bot will now ignore commands from anywhere else.")
            else
                it.respond("Bot channel cleared. Now accepting commands in any channel.")
        }
    }
}
