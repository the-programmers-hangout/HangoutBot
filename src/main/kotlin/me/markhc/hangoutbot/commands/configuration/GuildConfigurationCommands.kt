package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Configuration
import me.markhc.hangoutbot.services.GuildConfiguration
import me.markhc.hangoutbot.services.Permission
import me.markhc.hangoutbot.services.findOrCreate
import org.jetbrains.exposed.sql.transactions.transaction
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.markhc.hangoutbot.locale.Messages

@CommandSet("GuildConfiguration")
fun guildConfigurationCommands() = commands {
    requiredPermissionLevel = Permission.GuildOwner

    command("setadminrole") {
        description = "Sets the role that distinguishes an Administrator"
        execute(RoleArg) {
            val (role) = it.args
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            transaction {
                val guild = GuildConfiguration.findOrCreate(guildId)

                guild.adminRoleName = role.name
            }
            return@execute it.respond("Administrator role set to \"${role.name}\"")
        }
    }

    command("setstaffrole") {
        description = "Sets the role that distinguishes an Administrator"
        execute(RoleArg) {
            val (role) = it.args
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            transaction {
                val guild = GuildConfiguration.findOrCreate(guildId)

                guild.staffRoleName = role.name
            }
            return@execute it.respond("Staff role set to \"${role.name}\"")
        }
    }

    command("setprefix") {
        description = "Sets the prefix used by the bot in this guild"
        execute(WordArg("prefix")) {
            val (prefix) = it.args
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            transaction {
                val guild = GuildConfiguration.findOrCreate(guildId)

                guild.prefix = prefix
            }
            it.discord.configuration.prefix = prefix

            return@execute it.respond("Guild prefix set to \"${prefix}\"")
        }
    }

    command("togglebotreactions") {
        description = "Sets the prefix used by the bot in this guild"
        execute() {
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            transaction {
                val guild = GuildConfiguration.findOrCreate(guildId)

                guild.reactToCommands = !guild.reactToCommands
                it.discord.configuration.reactToCommands = guild.reactToCommands
            }

            return@execute it.respond("Bot reactions to commands are now ${if(it.discord.configuration.reactToCommands) "enabled" else "disabled"}")
        }
    }
}