package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.arguments.RoleArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import java.awt.Color

@CommandSet("Roles")
fun roleCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    command("addgrantablerole") {
        requiredPermissionLevel = Permission.Administrator
        description = "Adds a role to the list of grantable roles."
        execute(WordArg("Category"), RoleArg) { event ->
            val (category, role) = event.args

            val guildId = event.guild?.id
                    ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guildId)

            val key = guildConfig.grantableRoles.keys.find {
                it.compareTo(category, true) == 0
            }

            if(key == null) {
                guildConfig.grantableRoles[category] = mutableListOf(role.name);
            } else {
                guildConfig.grantableRoles[key]!!.add(role.name)
            }

            persistence.save(config)

            event.respond("Added \"${role.name}\" to the category \"${key ?: category}\".")
        }
    }

    command("removegrantablerole") {
        requiredPermissionLevel = Permission.Administrator
        description = "Removes a role to the list of grantable roles."
        execute(WordArg("Category"), RoleArg) { event ->
            val (category, role) = event.args

            val guildId = event.guild?.id ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guildId)

            val key = guildConfig.grantableRoles.keys.find {
                it.compareTo(category, true) == 0
            } ?: return@execute event.respond("Category \"$category\" not found")

            val removed = guildConfig.grantableRoles[key]!!.remove(role.name)

            if(guildConfig.grantableRoles[key].isNullOrEmpty()) {
                guildConfig.grantableRoles.remove(key)
            }

            persistence.save(config)

            if(removed)
                event.respond("Removed \"${role.name}\" from the category \"$category\".")
            else
                event.respond("Role \"${role.name}\" not found in category \"$category\".")
        }
    }

    command("listgrantableroles") {
        requiredPermissionLevel = Permission.Staff
        description = "Lists the available grantable roles."
        execute { event ->
            val guildId = event.guild?.id ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guildId)

            if(guildConfig.grantableRoles.isEmpty()) return@execute event.respond("No roles set")

            event.respond(buildRolesEmbed(guildConfig.grantableRoles))
        }
    }

    command("grant") {
        requiredPermissionLevel = Permission.Staff
        description = "Grants a role to a lower ranked member or yourself"
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args

            val guild = event.guild ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guild.id)

            guildConfig.grantableRoles.forEach {category ->
                if(containsIgnoreCase(category.value, role.name)) {
                    return@execute removeRoles(guild, member, category.value).also {
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
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args

            val guild = event.guild
                    ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guild.id)

            guildConfig.grantableRoles.forEach {category ->
                if(containsIgnoreCase(category.value, role.name)) {
                    return@execute removeRoles(guild, member, category.value)
                }
            }

            event.respond("\"${role.name}\" is not a grantable role")
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


fun containsIgnoreCase(list: List<String>, value: String): Boolean {
    list.forEach { item ->
        if(item.compareTo(value, true) == 0) {
            return true
        }
    }
    return false
}

fun buildRolesEmbed(roles: Map<String, List<String>>): MessageEmbed {
    return embed {
        title = "Grantable roles"
        color = Color.CYAN

        roles.iterator().forEach {
            addInlineField(
                    name = it.key,
                    value = (it.value as List<*>).filterIsInstance<String>().joinToString("\n"))
        }

    }
}