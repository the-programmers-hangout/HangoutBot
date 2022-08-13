package me.markhc.hangoutbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.arguments.MemberArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.commands.subcommand
import me.markhc.hangoutbot.dataclasses.Configuration

fun grantableRoles(configuration: Configuration) = subcommand("GrantableRoles", Permissions(Permission.ManageGuild)) {
    sub("Add") {
        description = "Add a role to the list of grantable roles"
        execute(RoleArg) {
            val role = args.first
            val grantableRoles = configuration[guild].grantableRoles

            if (role.id in grantableRoles) {
                respond("Role is already grantable")
            } else {
                grantableRoles.add(role.id)
                respond("Added \"${role.name}\" to grantable roles.")
            }
        }
    }

    sub("Remove") {
        description = "Remove a role from the list of grantable roles"
        execute(RoleArg) {
            val role = args.first
            val grantableRoles = configuration[guild].grantableRoles

            if (role.id !in grantableRoles) {
                respond("Role ${role.name} is not a grantable role.")
                return@execute
            }

            grantableRoles.remove(role.id)
            respond("Removed \"${role.name}\" from the list of grantable roles.")
        }
    }

    sub("List") {
        description = "List all grantable roles"
        execute {
            val grantableRoles = configuration[guild].grantableRoles

            if (grantableRoles.isEmpty()) {
                respond("No roles set")
                return@execute
            }

            respond {
                color = discord.configuration.theme

                field {
                    name = "Grantable roles"
                    value = grantableRoles.map { id ->
                        guild.getRoleOrNull(id)?.name ?: id
                    }.joinToString("\n")
                }
            }
        }
    }
}

fun roleCommands(configuration: Configuration) = commands("Roles", Permissions(Permission.ManageMessages)) {
    slash("grant") {
        description = "Grants a role to a lower ranked member or yourself"
        execute(MemberArg("Member"), RoleArg("GrantableRole")) {
            val (member, role) = args
            val roles = configuration[guild].grantableRoles

            if (roles.any { r -> r == role.id }) {
                member.addRole(role.id)
                respond("Granted ${role.name} to ${member.tag}")
            } else {
                respond("${role.name} is not a grantable role")
            }
        }
    }

    slash("revoke") {
        description = "Revokes a role from a lower ranked member or yourself"
        execute(MemberArg("Member"), RoleArg("GrantableRole")) {
            val (member, role) = args
            val isGrantable = role.id in configuration[guild].grantableRoles

            if (isGrantable) {
                member.removeRole(role.id)
                respond("Revoked ${role.name} from ${member.tag}")
            } else {
                respond("${role.name} is not a grantable role")
            }
        }
    }
}