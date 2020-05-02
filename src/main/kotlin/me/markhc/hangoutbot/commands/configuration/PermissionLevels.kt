package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.CommandArg
import me.aberrantfox.kjdautils.internal.arguments.MultipleArg
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.arguments.PermissionLevelArg
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService


@CommandSet("Permissions")
fun producePermissionCommands(config: Configuration, persistence: PersistenceService, permissionsService: PermissionsService) = commands {
    fun Configuration.save() {
        persistence.save(this)
    }

    command("getpermission") {
        requiredPermissionLevel = PermissionLevel.Staff
        description = "Returns the required permission level for the given command"
        execute(CommandArg) {
            val (cmd) = it.args

            it.respond("${permissionsService.getCommandPermissionLevel(it.guild, cmd)}")
        }
    }

    command("setpermission") {
        requiredPermissionLevel = PermissionLevel.Administrator
        description = "Sets the required permission level for the given commands"
        execute(PermissionLevelArg, MultipleArg(CommandArg, "Commands...")) { event ->
            val(level, commands) = event.args

            commands.forEach {
                permissionsService.setCommandPermissionLevel(event.guild, it, level)
            }

            event.respond("${
                commands.joinToString(", ") {
                    cmd -> cmd.names.joinToString()
                }} are now available to ${level}.")
        }
    }

    command("setrolepermissions") {
        description = "Sets the permission level of the given role"
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(RoleArg, PermissionLevelArg) {
            val (role, level) = it.args

            if(level == PermissionLevel.BotOwner || level == PermissionLevel.GuildOwner) {
                return@execute it.respond("Sorry, cannot set permission level to $level.")
            }

            config.getGuildConfig(it.guild!!).apply {
                rolePermissions[role.id] = level
            }
            config.save()

            it.respond("${role.name} permission level set to $level")
        }
    }

    command("getrolepermissions") {
        description = "Gets the permission level of the given role"
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(RoleArg) {
            val (role) = it.args

            config.getGuildConfig(it.guild!!).apply {
                it.respond("The permission level for ${role.name} is ${rolePermissions[role.id]}")
            }
        }
    }
}