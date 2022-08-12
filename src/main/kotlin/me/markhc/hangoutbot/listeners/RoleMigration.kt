package me.markhc.hangoutbot.listeners

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.guild.MemberUpdateEvent
import dev.kord.core.event.role.RoleDeleteEvent
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.dsl.listeners
import me.markhc.hangoutbot.dataclasses.Configuration

fun roleMigration(configuration: Configuration) = listeners {
    on<RoleDeleteEvent> {
        val config = configuration[getGuild()]

        val roleId = role!!.id
        config.grantableRoles.remove(roleId)

        config.assignedColorRoles.entries.removeIf { it.key == roleId }

        if (config.muteRole == roleId) {
            config.muteRole = Snowflake(0)
        }
    }

    on<MemberUpdateEvent> {
        if (old?.roleIds == member.roleIds)
            return@on

        val guild = guild.asGuild()
        val config = configuration[guild]
        val colors = config.assignedColorRoles.map { it.key }.toSet()
        val roles = member.roles.toList().map { it.id }.intersect(colors)

        if (roles.isNotEmpty()) {
            roles.forEach { roleId ->
                // Remove member from the users of this color
                config.assignedColorRoles[roleId]?.remove(member.id)
            }

            // Find any roles without users and delete them
            config.assignedColorRoles.entries
                .filter { it.value.isEmpty() }
                .forEach {
                    it.key.let { guild.getRoleOrNull(it) }?.delete()
                }

            // and then remove them from the list
            config.assignedColorRoles.entries.removeIf {
                it.value.isEmpty()
            }
        }
    }
}