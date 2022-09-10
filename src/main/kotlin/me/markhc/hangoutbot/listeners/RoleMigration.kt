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
}