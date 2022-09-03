package me.markhc.hangoutbot.services

import dev.kord.common.entity.Permission
import dev.kord.common.kColor
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.createRole
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.rest.request.RestRequestException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.dsl.edit
import me.jakejmattson.discordkt.extensions.stringify
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.GuildConfiguration
import java.awt.Color

@Service
class ColorService(private val configuration: Configuration) {
    suspend fun setMemberColor(member: Member, role: Role): Boolean {
        if (role.id !in configuration[member.guild].assignedColorRoles.keys)
            return false

        removeColorRole(member)
        assignColorRole(member, role)
        deleteUnusedRoles(configuration[member.guild], member.guild)
        return true
    }

    suspend fun findRole(name: String, color: Color, guild: Guild) =
        configuration[guild].assignedColorRoles.keys
            .mapNotNull { guild.getRoleOrNull(it) }
            .find { it.name.equals(name, true) || it.color == color.kColor }

    suspend fun removeColorRole(member: Member) {
        val guildConfiguration = configuration[member.guild]
        guildConfiguration.assignedColorRoles.entries
            .filter { it.value.contains(member.id) }
            .map { it.key }
            .forEach { role ->
                member.roles.toList().find { it.id == role }?.let { roleToRemove ->
                    member.removeRole(roleToRemove.id)
                    configuration.edit {
                        guildConfiguration.assignedColorRoles[role]?.remove(member.id)
                    }
                }
            }
    }

    suspend fun deleteUnusedRoles(config: GuildConfiguration, guild: GuildBehavior) {
        config.assignedColorRoles.entries
            .filter { it.value.isEmpty() }
            .forEach {
                it.key.let { guild.getRoleOrNull(it) }?.delete()
            }
        configuration.edit {
            config.assignedColorRoles.entries.removeIf {
                it.value.isEmpty()
            }
        }
    }

    suspend fun createRole(name: String, color: Color, guild: Guild): Role {
        val separator = getSeparatorRole(guild)
            ?: throw Exception("Could not find separator role. The guild needs a role named \"Colors\" that marks where the new roles should be placed.")

        val role = guild.createRole {
            this.name = name
            this.color = color.kColor
            hoist = false
            mentionable = false
            permissions = separator.permissions
        }

        println("${role.id} - $name - ${stringify(color)}")

        configuration[guild].assignedColorRoles[role.id] = mutableListOf()

        try {
            role.changePosition(separator.rawPosition - 1)
            return role
        } catch (ex: RestRequestException) {
            role.delete()
            throw Exception("Failed to reorder roles. This is likely due to hierarchy issues, try moving the bot role higher.")
        }
    }

    private suspend fun assignColorRole(member: Member, role: Role) {
        member.addRole(role.id)
        configuration.edit { this[member.guild].assignedColorRoles.getOrPut(role.id) { mutableListOf() }.add(member.id) }
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