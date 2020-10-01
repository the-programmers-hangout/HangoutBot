package me.markhc.hangoutbot.listeners

import com.gitlab.kordlib.core.event.guild.MemberUpdateEvent
import com.gitlab.kordlib.core.event.role.RoleDeleteEvent
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.services.PersistentData

fun roleMigration(persistentData: PersistentData) = listeners {
    on<RoleDeleteEvent> {
        val roleId = role!!.id.value
        persistentData.setGuildProperty(guild.asGuild()) {
            this.rolePermissions.remove(roleId)
            this.grantableRoles.entries.removeIf {
                it.value.remove(roleId)
                it.value.isEmpty()
            }
            this.assignedColorRoles.entries.removeIf {
                it.key == roleId
            }
            if (this.muteRole == roleId) {
                this.muteRole = ""
            }
        }
    }

    on<MemberUpdateEvent> {
        if (old?.roleIds == currentRoleIds)
            return@on

        val guild = guild.asGuild()

        val colors = persistentData.getGuildProperty(guild) {
            assignedColorRoles.map { it.key }
        }

        val roles = currentRoles.toList().map { it.id }.intersect(colors)

        if (roles.isNotEmpty()) {
            persistentData.setGuildProperty(guild) {
                roles.forEach { roleId ->
                    // Remove member from the users of this color
                    assignedColorRoles[roleId]?.remove(memberId.value)
                }

                // Find any roles without users and delete them
                assignedColorRoles.entries
                    .filter { it.value.isEmpty() }
                    .forEach {
                        it.key.toSnowflakeOrNull()?.let { guild.getRole(it) }?.delete()
                    }

                // and then remove them from the list
                assignedColorRoles.entries.removeIf {
                    it.value.isEmpty()
                }
            }
        }
    }
}