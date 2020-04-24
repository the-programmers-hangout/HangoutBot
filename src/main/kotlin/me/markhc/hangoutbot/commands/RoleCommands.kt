package me.markhc.hangoutbot.commands.configuration

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.getRoleByName
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.arguments.RoleArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.GuildConfiguration
import me.markhc.hangoutbot.services.Permission
import me.markhc.hangoutbot.services.findOrCreate
import net.dv8tion.jda.api.entities.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.StringReader

@CommandSet("RoleCommands")
fun configurationCommands() = commands {
    command("addgrantablerole") {
        requiredPermissionLevel = Permission.Administrator
        description = "Adds a role to the list of grantable roles."
        execute(WordArg("Category"), RoleArg) { event ->
            val (category, role) = event.args

            val guild = event.guild
                    ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            transaction {
                val guildConfig = GuildConfiguration.findOrCreate(guild.id)
                val roles = Klaxon().parseJsonObject(StringReader(guildConfig.grantableRoles))

                val key = roles.keys.find {
                    it.compareTo(category, true) == 0
                }

                if(key == null) {
                    roles[category] = listOf(role.name);
                } else {
                    val list = (roles[key] as List<*>)
                            .filterIsInstance<String>()
                            .toMutableList()
                    list.add(role.name)
                    roles[key] = list;
                }

                guildConfig.grantableRoles = Klaxon().toJsonString(roles)

                event.respond("Added \"${role.name}\" to the category \"${key ?: category}\".")
            }
        }
    }

    command("removegrantablerole") {
        requiredPermissionLevel = Permission.Administrator
        description = "Removes a role to the list of grantable roles."
        execute(WordArg("Category"), RoleArg) { event ->
            val (category, role) = event.args

            val guild = event.guild ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            transaction {
                val guildConfig = GuildConfiguration.findOrCreate(guild.id)
                val roles = Klaxon().parseJsonObject(StringReader(guildConfig.grantableRoles))

                val key = roles.keys.find {
                    it.compareTo(category, true) == 0
                } ?: return@transaction event.respond("Category \"$category\" not found")

                val list = (roles[category] as List<*>)
                    .filterIsInstance<String>()
                    .toMutableList()
                val removed = list.remove(role.name)
                roles[category] = list;

                guildConfig.grantableRoles = Klaxon().toJsonString(roles)

                if(removed)
                    event.respond("Removed \"${role.name}\" from the category \"$category\".")
                else
                    event.respond("Role \"${role.name}\" not found in category \"$category\".")
            }
        }
    }

    command("listgrantableroles") {
        requiredPermissionLevel = Permission.Staff
        description = "Lists the available grantable roles."
        execute { event ->
            event.guild ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val roles = event.guild!!.id.let {
                transaction {
                    GuildConfiguration.findOrCreate(it).grantableRoles
                }.let {
                    Klaxon().parseJsonObject(StringReader(it))
                }
            }

            if(roles.isEmpty()) return@execute event.respond("No roles set")

            event.respond(buildRolesEmbed(roles))
        }
    }

    command("grant") {
        requiredPermissionLevel = Permission.Staff
        description = "Grants a role to a lower ranked member or yourself"
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg) { event ->
            val (member, role) = event.args

            val guild = event.guild
                    ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val grantableRoles = guild.id.let { transaction { GuildConfiguration.findOrCreate(it) }.grantableRoles }
            val rolesConfig = Klaxon().parseJsonObject(StringReader(grantableRoles))

            rolesConfig.forEach {category ->
                val roles = (category.value as List<*>).filterIsInstance<String>()
                if(containsIgnoreCase(roles, role.name)) {
                    return@execute removeRoles(guild, member, roles).also {
                        grantRole(guild, member, role)
                    }
                }
            }

            event.respond("\"$role.name\" is not a grantable role")
        }
    }

    command("revoke") {
        requiredPermissionLevel = Permission.Staff
        description = "Revokes a role from a lower ranked member or yourself"
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg) { event ->
            val (member, role) = event.args

            val guild = event.guild
                    ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val grantableRoles = guild.id.let { transaction { GuildConfiguration.findOrCreate(it) }.grantableRoles }
            val rolesConfig = Klaxon().parseJsonObject(StringReader(grantableRoles))

            rolesConfig.forEach {category ->
                val roles = (category.value as List<*>).filterIsInstance<String>()
                if(containsIgnoreCase(roles, role.name)) {
                    return@execute removeRoles(guild, member, roles)
                }
            }

            event.respond("\"$role.name\" is not a revokable role")
        }
    }
}

fun containsIgnoreCase(list: List<String>, value: String): Boolean {
    list.forEach { item ->
        if(item.compareTo(value, true) == 0) {
            return true
        }
    }
    return false
}

fun buildRolesEmbed(roles: JsonObject): MessageEmbed  {
    return embed {
        title = "Grantable roles"
        color = Color.CYAN

        roles.iterator().forEach {
            addField(
                    name = it.key,
                    value = (it.value as List<*>).filterIsInstance<String>().joinToString("\n"))
        }

    }
}

private fun removeRoles(guild: Guild, member: Member, roles: List<String>) {
    // TODO: Perhaps we should check if the user has more than 1 color role
    //       and remove all of them instead of just 1
    member.roles.find { it.name in roles }?.let {
        guild.removeRoleFromMember(member, it).queue()
    }
}

private fun grantRole(guild: Guild, member: Member, role: Role) {
    guild.addRoleToMember(member, role).queue()
}
