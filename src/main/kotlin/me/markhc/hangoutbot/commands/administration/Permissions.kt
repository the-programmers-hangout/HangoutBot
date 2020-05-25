package me.markhc.hangoutbot.commands.administration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.internal.arguments.ChoiceArg
import me.aberrantfox.kjdautils.internal.arguments.CommandArg
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.markhc.hangoutbot.arguments.PermissionLevelArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.extensions.requiresPermission
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData

@CommandSet("Permissions")
fun producePermissionCommands(persistentData: PersistentData,
                              permissionsService: PermissionsService) = commands {
    fun getPermissions(event: CommandEvent<*>, cmd: Command) {
        event.respond("${
            permissionsService.getCommandPermissionLevel(event.guild!!, cmd)
        }")
    }

    fun setPermission(event: CommandEvent<*>,
                      command: Command,
                      level: PermissionLevel) {
        val guild = event.guild!!
        val member = guild.getMember(event.author)!!

        val cmdPerms = permissionsService.getCommandPermissionLevel(event.guild!!, command)
        val authorPerms = permissionsService.getPermissionLevel(member)

        if (cmdPerms > authorPerms) {
            event.respond(
                    "Sorry, you cannot change permissions for ${command.names.first()}")
        } else {
                permissionsService.setCommandPermissionLevel(guild, command, level)

            event.respond("${command.names.first()} is now available to ${level}.")
        }
    }

    fun listPermissions(event: CommandEvent<*>) {
        val commands = event.container.commands
                .sortedBy { it.names.joinToString() }
                .groupBy { it.category }
                .toList()
                .sortedByDescending { it.second.size }

        event.respond(embed{
            title = "Required permissions"
            description= "```css\n" +
                    "[B] → Bot Owner\n" +
                    "[G] → Guild Owner\n" +
                    "[A] → Administrator\n" +
                    "[S] → Staff\n" +
                    "[E] → Everyone```"
            commands.forEach {
                field {
                    name = it.first
                    value = "```css\n${it.second.joinToString("\n") {
                        "[${permissionsService.getCommandPermissionLevel(event.guild!!, it).toString().first()}]\u202F${it.names.first()}"
                    }}\n```"
                    inline = true
                }
            }
        })
    }

    command("permission", "permissions") {
        description = "Returns the required permission level for the given command"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(ChoiceArg("set/get/list", "set", "get", "list").makeOptional("get"),
                CommandArg.makeNullableOptional(null),
                PermissionLevelArg.makeNullableOptional(null)) {
            val (choice, command, level) = it.args

            when(choice) {
                "get" -> {
                    if(command == null) {
                        it.respond("Received less arguments than expected. Expected: `(Command)`")
                    } else {
                        getPermissions(it, command)
                    }
                }
                "set" -> {
                    it.requiresPermission(PermissionLevel.Administrator) {
                        if (command == null || level == null) {
                            it.respond("Received less arguments than expected. Expected: `(Command) (Level)`")
                        } else {
                            setPermission(it, command, level)
                        }
                    }
                }
                "list" -> {
                    listPermissions(it)
                }
            }
        }
    }

    command("roleperms") {
        description = "Gets or sets the permission level of the given role"
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(RoleArg, PermissionLevelArg.makeNullableOptional(null)) {
            val (role, level) = it.args

            if(level != null) {
                if (level == PermissionLevel.BotOwner || level == PermissionLevel.GuildOwner) {
                    return@execute it.respond("Sorry, cannot set permission level to $level.")
                }

                persistentData.setGuildProperty(it.guild!!) {
                    rolePermissions[role.id] = level
                }

                it.respond("${role.name} permission level set to $level")
            } else {
                persistentData.getGuildProperty(it.guild!!) {
                    it.respond("The permission level for ${role.name} is ${rolePermissions[role.id] ?: "not set"}")
                }
            }
        }
    }
}