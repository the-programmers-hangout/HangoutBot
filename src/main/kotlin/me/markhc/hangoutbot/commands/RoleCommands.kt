package me.markhc.hangoutbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.arguments.MemberArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.commands.subcommand
import me.markhc.hangoutbot.dataclasses.Configuration

fun grantableRoles(configuration: Configuration) = subcommand("GrantableRoles", Permissions(Permission.ManageGuild)) {
    sub("Add") {
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
    text("grant") {
        description = "Grants a role to a lower ranked member or yourself"
        execute(MemberArg("Member").optional { it.guild!!.getMember(it.author.id) },
            RoleArg("GrantableRole")) {
            val (member, role) = args
            val guild = guild

            val roles = configuration[guild].grantableRoles

            if (roles.any { r -> r == role.id }) {
                member.addRole(role.id)
                respond("Granted ${role.name} to ${member.tag}")
            } else {
                respond("${role.name} is not a grantable role")
            }
        }
    }

    text("revoke") {
        description = "Revokes a role from a lower ranked member or yourself"
        execute(MemberArg("Member").optional { it.guild!!.getMember(it.author.id) },
            RoleArg("GrantableRole")) {
            val (member, role) = args
            val guild = guild
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

/**
 *  @brief Builds the list of messages required to display the roles
 *
 *  The role list may be too big to send in a single message.
 *  This function returns a list of messages that should be sent.
 *
 *  @param guild The target guild
 *
 *  @return The list of messages to send
 */
private suspend fun buildRolelistMessages(guild: Guild, regex: Regex): List<String> {
    val list = guild.roles.toList().map { role ->
        val colorString = with(role.color) {
            "(${String.format("#%02x%02x%02x", red, green, blue)})"
        }

        "${role.id} $colorString - ${role.name}: ${guild.members.count { role in it.roles.toList() }} users"
    }.filter { regex.containsMatchIn(it) }

    // Try joining them in a single message
    val response = list.joinToString("\n")

    return when {
        response.isEmpty() -> {
            listOf()
        }

        response.length < 1990 -> {
            // If the length is less than the max, we good.
            listOf("```\n$response\n```")
        }

        else -> {
            // Otherwise, break it into multiple messages
            val result = mutableListOf<String>()
            var data = "```\n"
            for (i in list.indices) {
                if (data.length + list[i].length < 1990) {
                    data += list[i] + '\n'
                } else {
                    result.add("$data```")
                    data = "```\n${list[i]}\n"
                }
            }
            result.add("$data```")
            result
        }
    }
}
