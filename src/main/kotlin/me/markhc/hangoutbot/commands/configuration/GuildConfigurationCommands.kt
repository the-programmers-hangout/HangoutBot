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

@CommandSet("GuildConfiguration")
fun guildConfigurationCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    command("setadminrole") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the role that distinguishes an Administrator"
        execute(RoleArg) {
            val (role) = it.args
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)
            val guild = config.getGuildConfig(guildId)

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
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)
            val guild = config.getGuildConfig(guildId)

            guild.staffRole = role.id
            persistence.save(config)

            return@execute it.respond("Staff role set to \"${role.name}\"")
        }
    }

    command("setprefix") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the prefix used by the bot in this guild"
        execute(WordArg("prefix")) {
            val (prefix) = it.args
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)
            val guild = config.getGuildConfig(guildId)

            guild.prefix = prefix
            it.discord.configuration.prefix = prefix

            persistence.save(config)

            return@execute it.respond("Guild prefix set to \"${prefix}\"")
        }
    }

    command("resetconfig") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Resets the guild configuration to its default state"
        execute {
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guild = config.getGuildConfig(guildId)

            guild.reset()
            persistence.save(config)

            it.discord.configuration.prefix = guild.prefix
            it.discord.configuration.reactToCommands = guild.reactToCommands

            it.respond("Guild configuration, except bot prefix, has been reset.")
        }
    }

    command("togglebotreactions") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the prefix used by the bot in this guild"
        execute() {
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guild = config.getGuildConfig(guildId)

            guild.reactToCommands = !guild.reactToCommands
            it.discord.configuration.reactToCommands = guild.reactToCommands

            persistence.save(config)

            return@execute it.respond("Bot reactions to commands are now ${if(it.discord.configuration.reactToCommands) "enabled" else "disabled"}")
        }
    }
}