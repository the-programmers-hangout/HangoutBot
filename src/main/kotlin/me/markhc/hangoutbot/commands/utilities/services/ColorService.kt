package me.markhc.hangoutbot.commands.utilities.services

import com.gitlab.kordlib.core.behavior.addRole
import com.gitlab.kordlib.core.entity.*
import kotlinx.coroutines.flow.*
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.*
import me.markhc.hangoutbot.services.*
import java.awt.Color

@Service
class ColorService(private val persistentData: PersistentData, private val permissionsService: PermissionsService) {
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
        val existingRole = persistentData.getGuildProperty(member.guild.asGuild()) {
            assignedColorRoles.keys.mapNotNull { it.toSnowflakeOrNull()?.let { it1 -> member.guild.getRole(it1) } }
                .firstOrNull { it.name.equals(roleName, true) && it.color == roleColor }
        }

        val role = existingRole ?: createNewColorRole(member.guild.asGuild(), roleName, roleColor)

        // Make sure we're not trying to add a role we already have
        if (role !in member.roles.toList()) {
            removeColorRole(member)
            addColorRole(member, role)
        }
    }

    private suspend fun addColorRole(member: Member, role: Role) {
        member.addRole(role.id)

        persistentData.setGuildProperty(member.getGuild()) {
            if (assignedColorRoles[role.id.value] != null) {
                assignedColorRoles[role.id.value]!!.add(member.id.value)
            } else {
                assignedColorRoles[role.id.value] = mutableListOf(member.id.value)
            }
        }
    }

    private suspend fun removeColorRole(member: Member) {
        val assignedRoles = persistentData.getGuildProperty(member.guild.asGuild()) {
            assignedColorRoles.entries
                .filter { it.value.contains(member.id.value) }
                .map { it.key }
        }

        assignedRoles.forEach { role ->
            member.roles.toList().find { it.id.value == role }?.let {
                member.removeRole(it.id)
            }
        }

    }

    private suspend fun findExistingRole(member: Member, roleName: String): Role? {
        return persistentData.getGuildProperty(member.guild.asGuild()) {
            assignedColorRoles.keys
                .mapNotNull { it.toSnowflakeOrNull()?.let { member.guild.getRole(it) } }
                .firstOrNull { it.name == roleName }
        }
    }

    private suspend fun createNewColorRole(guild: Guild, roleName: String, roleColor: Color): Role {
        val separator = getSeparatorRole(guild)
            ?: throw Exception("Could not find separator role. The guild needs a role named \"Colors\" that marks where the new roles should be placed.")

        if (guild.roles.toList().any { it.name.equals(roleName, true) })
            throw Exception("This guild already has a role by that name.")

        val role = guild.addRole {
            name = roleName
            color = roleColor
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
        persistentData.getGuildProperty(guild) { guild.roles.firstOrNull { it.name.equals("Colors", true) } }

    private suspend fun isValidName(member: Member, roleName: String): Boolean {
        // If user permissions are lower than Administrator, only allow
        // role names with ASCII characters
        if (!permissionsService.hasPermission(member, PermissionLevel.Administrator)) {
            if (!Regex("^[\\x20-\\x7F]+$").matches(roleName)) {
                return false
            }
        }
        return true
    }
}