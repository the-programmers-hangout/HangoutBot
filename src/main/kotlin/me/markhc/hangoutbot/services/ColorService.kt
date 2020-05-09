package me.markhc.hangoutbot.services

import com.github.kittinunf.result.Result
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.getRoleByName
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import java.awt.Color

@Service
class ColorService(private val persistentData: PersistentData, private val permissionsService: PermissionsService) {
    fun setMemberColor(member: Member, roleName: String, roleColor: Color?): Result<String, Exception> {
        if (!isValidName(member, roleName)) {
            return Result.Failure(Exception("The role name for regular users is only allowed ASCII characters ([\\x20-\\x7F])"))
        }

        return Result.of {
            if (roleColor == null) {
                assignExistingRole(member, roleName)
            } else {
                createAndAssignRole(member, roleName, roleColor)
            }
            "Successfully assigned role to user."
        }
    }

    fun clearMemberColor(member: Member) = Result.of<String, Exception> {
        removeColorRole(member)
        "Successfully cleared color roles from user."
    }

    private fun Member.addRole(role: Role) = this.guild.addRoleToMember(this, role).queue()
    private fun Member.removeRole(role: Role) = this.guild.removeRoleFromMember(this, role).queue()

    private fun assignExistingRole(member: Member, roleName: String) {
        val role = findExistingRole(member, roleName)
                ?: throw Exception("Could not find a color role named $roleName. If you meant to create a new role, specify the role color as well.")

        // Make sure we're not trying to add a role we already have
        if(role !in member.roles) {
            removeColorRole(member)
            addColorRole(member, role)
        }
    }

    private fun createAndAssignRole(member: Member, roleName: String, roleColor: Color) {
        val existingRole = persistentData.getGuildProperty(member.guild) {
            assignedColorRoles.keys.map { member.guild.getRoleById(it) }
                    .firstOrNull { it?.name.equals(roleName, true) && it?.color == roleColor }
        }

        val role = existingRole ?: createNewColorRole(member.guild, roleName, roleColor)

        // Make sure we're not trying to add a role we already have
        if(role !in member.roles) {
            removeColorRole(member)
            addColorRole(member, role)
        }
    }

    private fun addColorRole(member: Member, role: Role) {
        persistentData.setGuildProperty(member.guild) {
            if(assignedColorRoles[role.id] != null) {
                assignedColorRoles[role.id]!!.add(member.id)
            } else {
                assignedColorRoles[role.id] = mutableListOf(member.id)
            }
        }

        member.addRole(role)
    }

    private fun removeColorRole(member: Member) {
        val (roleId, isEmpty) = persistentData.setGuildProperty(member.guild) {
            val result = assignedColorRoles.entries.find { it.value.remove(member.id) }?.let {
                it.key to it.value.isEmpty()
            }
            assignedColorRoles.entries.removeIf { it.value.isEmpty() }
            result
        } ?: return

        member.roles.find { it.id == roleId }?.let {
            member.removeRole(it)
            if(isEmpty)
                it.delete().queue()
        }
    }

    private fun findExistingRole(member: Member, roleName: String): Role? {
        return persistentData.getGuildProperty(member.guild) {
            assignedColorRoles.keys
                    .map { member.guild.getRoleById(it) }
                    .firstOrNull { it?.name == roleName }
        }
    }

    private fun createNewColorRole(guild: Guild, roleName: String, roleColor: Color): Role {
        val separator = getSeparatorRole(guild)
                ?: throw Exception("Could not find separator role. The guild needs a role named \"Colors\" that marks where the new roles should be placed.")

        val role = guild.createCopyOfRole(separator)
                .setName(roleName)
                .setColor(roleColor)
                .setHoisted(false)
                .setMentionable(false)
                .complete()

        try {
            guild.modifyRolePositions()
                    .selectPosition(role)
                    .moveTo(separator.position - 1)
                    .complete()
            return role
        } catch (ex: Exception) {
            role.delete().queue()
            throw Exception("Failed to reorder roles. This is likely due to hierarchy issues, try moving the bot role higher.")
        }
    }

    private fun getSeparatorRole(guild: Guild) =
            persistentData.getGuildProperty(guild) { guild.getRoleByName("Colors") }

    private fun isValidName(member: Member, roleName: String): Boolean {
        // If user permissions are lower than Administrator, only allow
        // role names with ASCII characters
        if (permissionsService.getPermissionLevel(member) > PermissionLevel.Administrator) {
            if (!Regex("^[\\x20-\\x7F]+$").matches(roleName)) {
                return false
            }
        }
        return true
    }
}