package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.arguments.*
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.jakejmattson.kutils.api.extensions.jda.fullName
import me.markhc.hangoutbot.extensions.addRole
import me.markhc.hangoutbot.extensions.removeRole
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.services.requiresPermission
import me.markhc.hangoutbot.utilities.executeLogged
import net.dv8tion.jda.api.entities.Guild

@CommandSet("Roles")
fun roleCommands(persistentData: PersistentData) = commands {
    command("grantablerole") {
        description = "Adds, removes or lists grantble roles."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        executeLogged(ChoiceArg("add/rem/list", "add", "rem", "list"),
                RoleArg.makeNullableOptional(null),
                AnyArg("Category").makeNullableOptional(null)) { event ->
            val (choice, role, category) = event.args

            when (choice) {
                "add" -> {
                    event.requiresPermission(PermissionLevel.Administrator) {
                        if (role == null || category == null) {
                            return@requiresPermission event.respond(
                                    "Received less arguments than expected. Expected: `(Role) (Category)`"
                            )
                        }

                        persistentData.setGuildProperty(event.guild!!) {
                            if (grantableRoles.any { it.value.contains(role.id) }) {
                                event.respond("Role is already grantable")
                            } else {
                                val key = grantableRoles.keys.find {
                                    it.compareTo(category, true) == 0
                                }

                                if (key == null) {
                                    grantableRoles[category] = mutableListOf(role.id)
                                } else {
                                    grantableRoles[key]!!.add(role.id)
                                }

                                event.respond("Added \"${role.name}\" to the category \"${key ?: category}\".")
                            }
                        }
                    }
                }
                "rem" -> {
                    event.requiresPermission(PermissionLevel.Administrator) {
                        if (role == null) {
                            return@requiresPermission event.respond(
                                    "Received less arguments than expected. Expected: `(Command)`"
                            )
                        }

                        persistentData.setGuildProperty(event.guild!!) {
                            val entry = grantableRoles.entries.find {
                                it.value.contains(role.id)
                            } ?: return@setGuildProperty event.respond("Role ${role.name} is not a grantable role.")

                            entry.value.remove(role.id)

                            if (entry.value.isEmpty()) {
                                grantableRoles.remove(entry.key)
                            }

                            event.respond("Removed \"${role.name}\" from the list of grantable roles.")
                        }
                    }
                }
                "list" -> {
                    persistentData.getGuildProperty(event.guild!!) {
                        if (grantableRoles.isEmpty()) {
                            event.respond("No roles set")
                        } else {
                            val responseEmbed = embed {
                                title { text = "Grantable roles" }
                                color = infoColor

                                grantableRoles.entries.forEach {
                                    field {
                                        name = it.key
                                        value = it.value.joinToString("\n") { id ->
                                            event.guild!!.getRoleById(id)?.name ?: id
                                        }
                                    }
                                }
                            }

                            event.respond(responseEmbed)
                        }
                    }
                }
                else -> {
                    event.respond("Invalid choice")
                }
            }
        }
    }

    command("grant") {
        description = "Grants a role to a lower ranked member or yourself"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        executeLogged(MemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! },
                RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args
            val guild = event.guild!!

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }

            if (roles.values.any { r -> r.contains(role.id) }) {
                member.addRole(role).queue {
                    event.respond("Granted ${role.name} to ${member.user.fullName()}")
                }
            } else {
                event.respond("${role.name} is not a grantable role")
            }
        }
    }

    command("revoke") {
        description = "Revokes a role from a lower ranked member or yourself"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        executeLogged(MemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! },
                RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args
            val guild = event.guild!!

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }

            val isGrantable = roles.any { it.value.any { r -> r.equals(role.id, true) } }

            if (isGrantable) {
                member.removeRole(role).queue {
                    event.respond("Revoked ${role.name} from ${member.user.fullName()}")
                }
            } else {
                event.respond("${role.name} is not a grantable role")
            }
        }
    }

    command("listroles") {
        description = "List all the roles available in the guild."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        executeLogged(EveryArg("GrepRegex").makeNullableOptional(null)) { event ->
            val guild = event.guild!!
            val message = event.channel.sendMessage("Working...").complete()

            guild.retrieveMembers().thenRun {
                val messages = buildRolelistMessages(guild,
                        (event.args.first ?: "").toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)))

                if (messages.isNotEmpty()) {
                    message.editMessage(messages.first()).queue()

                    for (i in 1 until messages.size) {
                        event.channel.sendMessage(messages[i]).queue()
                    }
                } else {
                    message.editMessage("No results").queue()
                }
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
private fun buildRolelistMessages(guild: Guild, regex: Regex): List<String> {
    val list = guild.roles.map {
        "${it.id} (${String.format("#%02x%02x%02x", it.color?.red?:0, it.color?.green?:0, it.color?.blue?:0)}) - ${it.name}: ${guild.getMembersWithRoles(it).size} users"
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
            for(i in list.indices) {
                if(data.length + list[i].length < 1990) {
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
