package me.markhc.hangoutbot.services

import dev.kord.common.entity.Permission
import dev.kord.common.kColor
import dev.kord.core.behavior.createRole
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.dataclasses.Configuration
import java.awt.Color

@Service
class ColorService(private val configuration: Configuration) {
    suspend fun setMemberColor(member: Member, roleName: String, roleColor: Color?) {
        if (!isValidName(member, roleName)) {
            throw Exception("The role name for regular users is only allowed ASCII characters ([\\x20-\\x7F])")
        }

        if (roleColor == null) {
            assignExistingRole(member, roleName)
        } else {
            createAndAssignRole(member, roleName, roleColor)
        }
    }

    suspend fun clearMemberColor(member: Member) = removeColorRole(member)

    private suspend fun assignExistingRole(member: Member, roleName: String) {
        val role = findExistingRole(member, roleName)
            ?: throw Exception("Could not find a color role named $roleName. If you meant to create a new role, specify the role color as well.")

        // Make sure we're not trying to add a role we already have
        if (role !in member.roles.toList()) {
            removeColorRole(member)
            addColorRole(member, role)
        }
    }

    private suspend fun createAndAssignRole(member: Member, roleName: String, roleColor: Color) {
        val existingRole = configuration[member.guild].assignedColorRoles.keys
            .map { it.let { it1 -> member.guild.getRole(it1) } }
            .firstOrNull { it.name.equals(roleName, true) && it.color == roleColor.kColor }

        val role = existingRole ?: createNewColorRole(member.guild.asGuild(), roleName, roleColor)

        // Make sure we're not trying to add a role we already have
        if (role !in member.roles.toList()) {
            removeColorRole(member)
            addColorRole(member, role)
        }
    }

    private suspend fun addColorRole(member: Member, role: Role) {
        member.addRole(role.id)

        val assignedColorRoles = configuration[member.guild].assignedColorRoles

        if (assignedColorRoles[role.id] != null) {
            assignedColorRoles[role.id]!!.add(member.id)
        } else {
            assignedColorRoles[role.id] = mutableListOf(member.id)
        }
    }

    private suspend fun removeColorRole(member: Member) {
        val assignedRoles = configuration[member.guild].assignedColorRoles.entries
            .filter { it.value.contains(member.id) }
            .map { it.key }

        assignedRoles.forEach { role ->
            member.roles.toList().find { it.id == role }?.let {
                member.removeRole(it.id)
            }
        }
    }

    private suspend fun findExistingRole(member: Member, roleName: String): Role? {
        return configuration[member.guild].assignedColorRoles.keys
            .map { it.let { member.guild.getRole(it) } }
            .firstOrNull { it.name == roleName }
    }
}

private suspend fun createNewColorRole(guild: Guild, roleName: String, roleColor: Color): Role {
    val separator = getSeparatorRole(guild)
        ?: throw Exception("Could not find separator role. The guild needs a role named \"Colors\" that marks where the new roles should be placed.")

    if (guild.roles.toList().any { it.name.equals(roleName, true) })
        throw Exception("This guild already has a role by that name.")

    val role = guild.createRole {
        name = roleName
        color = roleColor.kColor
        hoist = false
        mentionable = false
        permissions = separator.permissions
    }

    try {
        role.changePosition(separator.rawPosition - 1)
        return role
    } catch (ex: Exception) {
        role.delete()
        throw Exception("Failed to reorder roles. This is likely due to hierarchy issues, try moving the bot role higher.")
    }
}

private suspend fun getSeparatorRole(guild: Guild) =
    guild.roles.firstOrNull { it.name.equals("Colors", true) }

private suspend fun isValidName(member: Member, roleName: String): Boolean {
    // If user permissions are lower than Administrator, only allow
    // role names with ASCII characters
    if (!member.getPermissions().contains(Permission.Administrator)) {
        if (!Regex("^[\\x20-\\x7F]+$").matches(roleName)) {
            return false
        }
    }
    return true
}