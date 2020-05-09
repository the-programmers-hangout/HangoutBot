package me.markhc.hangoutbot.listeners;

import com.google.common.eventbus.Subscribe;
import me.markhc.hangoutbot.services.PersistentData
import kotlin.Suppress;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;

@Suppress("unused")
class RoleDeleteListener(private val persistentData: PersistentData) {
    @Subscribe
    fun onRoleDelete(roleDeleteEvent: RoleDeleteEvent) {
        val roleId = roleDeleteEvent.role.id
        persistentData.setGuildProperty(roleDeleteEvent.guild) {
            this.rolePermissions.remove(roleId)
            this.grantableRoles.entries.removeIf {
                it.value.remove(roleId)
                it.value.isEmpty()
            }
            this.assignedColorRoles.entries.removeIf {
                it.key == roleId
            }
            if(this.muteRole == roleId) {
                this.muteRole = ""
            }
        }
    }
}
