package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.arguments.PermissionLevelArg
import me.markhc.hangoutbot.services.*


fun producePermissionCommands(persistentData: PersistentData,
                              permissionsService: PermissionsService) = commands("Permissions") {
    suspend fun listPermissions(event: GuildCommandEvent<*>) {
        val commands = event.discord.commands
            .sortedBy { it.names.joinToString() }
            .groupBy { it.category }
            .toList()
            .sortedByDescending { it.second.size }

        event.respond {
            title = "Required permissions"
            description = "```css\n" +
                "[B] → Bot Owner\n" +
                "[G] → Guild Owner\n" +
                "[A] → Administrator\n" +
                "[S] → Staff\n" +
                "[E] → Everyone```"
            commands.forEach {
                field {
                    name = it.first
                    value = "```css\n${
                        it.second.map {
                            "[${permissionsService.getCommandPermissionLevel(event.guild, it).toString().first()}]\u202F${it.names.first()}"
                        }.joinToString("\n")
                    }\n```"
                    inline = true
                }
            }
        }
    }

    guildCommand("permission", "permissions") {
        description = "Gets or sets the permissions for a command. Use `list` to view all permissions"
        requiredPermissionLevel = PermissionLevel.Staff
        execute(ChoiceArg("set/get/list", "set", "get", "list").makeOptional("get"),
                CommandArg.makeNullableOptional(null),
                PermissionLevelArg.makeNullableOptional(null)) {
            val (choice, command, level) = args

            when (choice) {
                "get" -> {
                    if (command == null) {
                        respond("Received less arguments than expected. Expected: `(Command)`")
                    } else {
                        respond("${
                            permissionsService.getCommandPermissionLevel(guild, command)
                        }")
                    }
                }
                "set" -> {
                    requiresPermission(PermissionLevel.Administrator) {
                        if (command == null || level == null) {
                            respond("Received less arguments than expected. Expected: `(Command) (Level)`")
                        } else {
                            if (permissionsService.trySetCommandPermission(guild, author, command, level)) {
                                respond("${command.names.first()} is now available to ${level}.")
                            } else {
                                respond("Sorry, you cannot change permissions for ${command.names.first()}")
                            }
                        }
                    }
                }
                "list" -> {
                    listPermissions(this)
                }
            }
        }
    }

    guildCommand("roleperms") {
        description = "Gets or sets the permission level of the given role"
        requiredPermissionLevel = PermissionLevel.GuildOwner
        execute(RoleArg, PermissionLevelArg.makeNullableOptional(null)) {
            val (role, level) = args

            if (level != null) {
                if (level == PermissionLevel.BotOwner || level == PermissionLevel.GuildOwner) {
                    respond("Sorry, cannot set permission level to $level.")
                    return@execute
                }

                persistentData.setGuildProperty(guild) {
                    rolePermissions[role.id.value] = level
                }

                respond("${role.name} permission level set to $level")
            } else {
                persistentData.getGuildProperty(guild) {
                    respond("The permission level for ${role.name} is ${rolePermissions[role.id.value] ?: "not set"}")
                }
            }
        }
    }
}