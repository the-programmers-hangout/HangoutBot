package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.CommandArg
import me.aberrantfox.kjdautils.internal.arguments.MultipleArg
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.markhc.hangoutbot.arguments.PermissionLevelArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import java.util.concurrent.TimeUnit

@CommandSet("Permissions")
fun producePermissionCommands(persistentData: PersistentData,
                              permissionsService: PermissionsService) = commands {
    command("getpermission") {
        requiredPermissionLevel = PermissionLevel.Staff
        description = "Returns the required permission level for the given command"
        requiresGuild = true
        execute(CommandArg) {
            val (cmd) = it.args

            it.respond("${permissionsService.getCommandPermissionLevel(it.guild!!, cmd)}")
        }
    }

    command("setpermission") {
        requiredPermissionLevel = PermissionLevel.Administrator
        description = "Sets the required permission level for the given commands"
        requiresGuild = true
        execute(MultipleArg(CommandArg, "Commands..."), PermissionLevelArg) { event ->
            val (commands, level) = event.args

            val guild = event.guild!!
            val member = guild.getMember(event.author)!!

            val higherPerms = commands.find {
                permissionsService.getCommandPermissionLevel(event.guild!!, it) < permissionsService.getPermissionLevel(member)
            }

            if(higherPerms != null) {
                return@execute event.respondTimed(
                        "Sorry, you cannot change permissions for ${higherPerms.names.first()}",
                        TimeUnit.SECONDS.toMillis(10))
            }

            commands.forEach {
                permissionsService.setCommandPermissionLevel(guild, it, level)
            }

            event.respond("${
                commands.joinToString(", ") {
                    cmd -> cmd.names.joinToString()
                }} ${if(commands.size > 1) "are" else "is"} now available to ${level}.")
        }
    }

    command("setrolepermissions") {
        description = "Sets the permission level of the given role"
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(RoleArg, PermissionLevelArg) {
            val (role, level) = it.args

            val authorLevel = permissionsService.getPermissionLevel(it.guild!!.getMember(it.author)!!)
            if(level == PermissionLevel.BotOwner || (level == PermissionLevel.GuildOwner && authorLevel > PermissionLevel.GuildOwner)) {
                return@execute it.respondTimed("Sorry, cannot set permission level to $level.", TimeUnit.SECONDS.toMillis(10))
            }

            persistentData.setGuildProperty(it.guild!!) {
                rolePermissions[role.id] = level
            }

            it.respond("${role.name} permission level set to $level")
        }
    }

    command("getrolepermissions") {
        description = "Gets the permission level of the given role"
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(RoleArg) {
            val (role) = it.args

            persistentData.getGuildProperty(it.guild!!) {
                it.respond("The permission level for ${role.name} is ${rolePermissions[role.id] ?: "not set"}")
            }
        }
    }
}