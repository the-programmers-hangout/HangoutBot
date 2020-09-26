package me.markhc.hangoutbot.commands.administration

import com.gitlab.kordlib.core.behavior.edit
import com.gitlab.kordlib.core.entity.Guild
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.services.*
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.services.requiresPermission


fun roleCommands(persistentData: PersistentData) = commands("Roles") {
    command("grantablerole", "grantableroles") {
        description = "Adds, removes or lists grantable roles."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(ChoiceArg("add/rem/list", "add", "rem", "list").makeOptional("list"),
                RoleArg.makeNullableOptional(null),
                AnyArg("Category").makeNullableOptional(null)) {
            val (choice, role, category) = args

            when (choice) {
                "add" -> {
                    requiresPermission(PermissionLevel.Administrator) {
                        if (role == null || category == null) {
                            return@requiresPermission respond(
                                "Received less arguments than expected. Expected: `(Role) (Category)`"
                            )
                        }

                        persistentData.setGuildProperty(guild!!) {
                            if (grantableRoles.any { it.value.contains(role.id) }) {
                                respond("Role is already grantable")
                            } else {
                                val key = grantableRoles.keys.find {
                                    it.compareTo(category, true) == 0
                                }

                                if (key == null) {
                                    grantableRoles[category] = mutableListOf(role.id.value)
                                } else {
                                    grantableRoles[key]!!.add(role.id.value)
                                }

                                respond("Added \"${role.name}\" to the category \"${key ?: category}\".")
                            }
                        }
                    }
                }
                "rem" -> {
                    requiresPermission(PermissionLevel.Administrator) {
                        if (role == null) {
                            return@requiresPermission respond(
                                "Received less arguments than expected. Expected: `(Command)`"
                            )
                        }

                        persistentData.setGuildProperty(guild!!) {
                            val entry = grantableRoles.entries.find {
                                it.value.contains(role.id)
                            } ?: return@setGuildProperty respond("Role ${role.name} is not a grantable role.")

                            entry.value.remove(role.id)

                            if (entry.value.isEmpty()) {
                                grantableRoles.remove(entry.key)
                            }

                            respond("Removed \"${role.name}\" from the list of grantable roles.")
                        }
                    }
                }
                "list" -> {
                    persistentData.getGuildProperty(guild!!) {
                        if (grantableRoles.isEmpty()) {
                            respond("No roles set")
                        } else {
                            respond {
                                title = "Grantable roles"
                                color = discord.configuration.theme

                                grantableRoles.entries.forEach {
                                    runBlocking {  }
                                    field {
                                        name = it.key
                                        value = it.value.map { id ->
                                            id.toSnowflake()?.let { guild!!.getRole(it).name } ?: id
                                        }.joinToString("\n")
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    respond("Invalid choice")
                }
            }
        }
    }

    command("grant") {
        description = "Grants a role to a lower ranked member or yourself"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(MemberArg("Member").makeOptional { it.guild!!.getMember(it.author.id) },
                RoleArg("GrantableRole")) {
            val (member, role) = args
            val guild = guild!!

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }

            if (roles.values.any { r -> r.contains(role.id) }) {
                member.addRole(role.id)
                respond("Granted ${role.name} to ${member.tag}")
            } else {
                respond("${role.name} is not a grantable role")
            }
        }
    }

    command("revoke") {
        description = "Revokes a role from a lower ranked member or yourself"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(MemberArg("Member").makeOptional { it.guild!!.getMember(it.author.id) },
            RoleArg("GrantableRole")) {
            val (member, role) = args
            val guild = guild!!

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }
            val isGrantable = roles.any {
                it.value.any { it.equals(role.id.value, true) }
            }

            if (isGrantable) {
                member.removeRole(role.id)
                respond("Revoked ${role.name} from ${member.tag}")
            } else {
                respond("${role.name} is not a grantable role")
            }
        }
    }

    command("listroles") {
        description = "List all the roles available in the guild."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(EveryArg("GrepRegex").makeNullableOptional(null)) {
            val guild = guild!!
            val message = channel.createMessage("Working...")

            val messages = buildRolelistMessages(guild,
                (args.first ?: "").toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)))

            if (messages.isNotEmpty()) {
                message.edit {
                    content = messages.first()
                }

                for (i in 1 until messages.size) {
                    channel.createMessage(messages[i])
                }
            } else {
                message.edit {
                    content = "No results"
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
private suspend fun buildRolelistMessages(guild: Guild, regex: Regex): List<String> {
    val list = guild.roles.toList().map { role ->
        "${role.id} (${String.format("#%02x%02x%02x", role.color.red, role.color.green, role.color.blue)}) - ${role.name}: ${guild.members.count { role in it.roles.toList() }} users"
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
