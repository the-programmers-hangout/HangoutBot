package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
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
            commands.joinToString { 
                it.names.first()
            }} ${if(commands.size > 1) "are" else "is"} now available to ${level}.")
        }
    }

    command("setrolepermissions") {
        description = "Sets the permission level of the given role"
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(RoleArg, PermissionLevelArg) {
            val (role, level) = it.args

            if(level == PermissionLevel.BotOwner || level == PermissionLevel.GuildOwner) {
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

    command("listpermissionlevels") {
        description = "Lists each permission level and the roles assigned to it"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute { event ->
            val perms = persistentData.getGuildProperty(event.guild!!) {
                rolePermissions
            }

            event.respond(embed {
                title = "Permission Levels"
                listOf(PermissionLevel.Administrator, PermissionLevel.Staff).forEach {
                    val roles = perms.filter { p -> p.value == it }.map { it.key }
                    field {
                        name = "**${it.name}**"
                        value = roles.joinToString() { id -> event.guild!!.getRoleById(id)!!.name }.ifBlank { "Not set" }
                        inline = true
                    }
                }
            })
        }
    }

    command("listpermissions") {
        description = "Lists each command and their required permission level"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            val commands = event.container.commands
                    .sortedBy { it.names.joinToString() }
                    .groupBy { it.category }

            event.respond(embed{
                title = "Required permissions"
                commands.forEach {
                    field {
                        name = it.key
                        value = it.value.joinToString("\n") {
                            "${it.names.first()}: ${permissionsService.getCommandPermissionLevel(event.guild!!, it)} "
                        }
                    }
                }
            })
        }
    }
}