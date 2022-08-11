package me.markhc.hangoutbot.commands.administration

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.commands.subcommand
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.services.PersistentData

fun grantableRoles(persistentData: PersistentData) = subcommand("GrantableRoles", Permissions(Permission.ManageGuild)) {
    sub("Add") {
        execute(RoleArg) {
            val role = args.first

            persistentData.setGuildProperty(guild) {
                if (grantableRoles.any { it.value.contains(role.id.toString()) }) {
                    respond("Role is already grantable")
                } else {
                    val key = grantableRoles.keys.find {
                        it.compareTo(category, true) == 0
                    }

                    if (key == null) {
                        grantableRoles[category] = mutableListOf(role.id.toString())
                    } else {
                        grantableRoles[key]!!.add(role.id.toString())
                    }

                    respond("Added \"${role.name}\" to the category \"${key ?: category}\".")
                }
            }
        }
    }

    sub("Remove") {
        execute(RoleArg) {
            val role = args.first

            persistentData.setGuildProperty(guild) {
                val entry = grantableRoles.entries.find {
                    it.value.contains(role.id.toString())
                }
                if (entry == null) {
                    respond("Role ${role.name} is not a grantable role.")
                    return@setGuildProperty
                }

                entry.value.remove(role.id.toString())

                if (entry.value.isEmpty()) {
                    grantableRoles.remove(entry.key)
                }

                respond("Removed \"${role.name}\" from the list of grantable roles.")
            }
        }
    }

    sub("List") {
        execute {
            persistentData.getGuildProperty(guild) {
                if (grantableRoles.isEmpty()) {
                    respond("No roles set")
                } else {
                    respond {
                        title = "Grantable roles"
                        color = discord.configuration.theme

                        grantableRoles.entries.forEach {
                            field {
                                name = it.key
                                value = it.value.map { id ->
                                    id.toSnowflakeOrNull()?.let { guild.getRole(it).name } ?: id
                                }.joinToString("\n")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun roleCommands(persistentData: PersistentData) = commands("Roles", Permissions(Permission.ManageMessages)) {
    text("grant") {
        description = "Grants a role to a lower ranked member or yourself"
        execute(MemberArg("Member").optional { it.guild!!.getMember(it.author.id) },
            RoleArg("GrantableRole")) {
            val (member, role) = args
            val guild = guild

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }

            if (roles.values.any { r -> r.contains(role.id.toString()) }) {
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

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }
            val isGrantable = roles.any {
                it.value.any { it.equals(role.id.toString(), true) }
            }

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

        "${role.id.toString()} $colorString - ${role.name}: ${guild.members.count { role in it.roles.toList() }} users"
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
