package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.arguments.RoleArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.availableThroughDMs

@CommandSet("GuildConfiguration")
fun guildConfigurationCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    command("setadminrole") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the role that distinguishes an Administrator"
        execute(RoleArg) {
            val (role) = it.args
            val guild = config.getGuildConfig(it.guild!!.id)

            guild.adminRole = role.id
            persistence.save(config)

            return@execute it.respond("Administrator role set to \"${role.name}\"")
        }
    }

    command("setstaffrole") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the role that distinguishes an Administrator"
        execute(RoleArg) {
            val (role) = it.args
            val guild = config.getGuildConfig(it.guild!!.id)

            guild.staffRole = role.id
            persistence.save(config)

            return@execute it.respond("Staff role set to \"${role.name}\"")
        }
    }

    command("setmuterole") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the role used to mute an user"
        execute(RoleArg) {
            val (role) = it.args
            val guild = config.getGuildConfig(it.guild!!.id)

            guild.muteRole = role.id
            persistence.save(config)

            return@execute it.respond("Mute role set to \"${role.name}\"")
        }
    }

    command("setprefix") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the prefix used by the bot in this guild"
        execute(WordArg("prefix")) {
            val (prefix) = it.args
            val guild = config.getGuildConfig(it.guild!!.id)

            guild.prefix = prefix
            it.discord.configuration.prefix = prefix

            persistence.save(config)

            return@execute it.respond("Guild prefix set to \"${prefix}\"")
        }
    }

    command("togglebotreactions") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the prefix used by the bot in this guild"
        execute() {
            val guild = config.getGuildConfig(it.guild!!.id)

            guild.reactToCommands = !guild.reactToCommands
            it.discord.configuration.reactToCommands = guild.reactToCommands

            persistence.save(config)

            return@execute it.respond("Bot reactions to commands are now ${if(it.discord.configuration.reactToCommands) "enabled" else "disabled"}")
        }
    }
}