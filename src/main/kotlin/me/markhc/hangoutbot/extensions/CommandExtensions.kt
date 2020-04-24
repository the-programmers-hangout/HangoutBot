package me.markhc.hangoutbot.extensions

import me.aberrantfox.kjdautils.api.dsl.command.*
import me.markhc.hangoutbot.services.*
import java.util.*

object CommandsContainerPropertyStore {
    val setPermissions = WeakHashMap<CommandsContainer, Permission>()
}

val commandPermissions: MutableMap<Command, Permission> = mutableMapOf()

var CommandsContainer.requiredPermissionLevel
    get() = CommandsContainerPropertyStore.setPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        CommandsContainerPropertyStore.setPermissions[this] = value
    }

var Command.requiredPermissionLevel: Permission
    get() {
        val setLevel = CommandsContainerPropertyStore.setPermissions.toList()
                .firstOrNull { this in it.first.commands }?.second

        val cmdLevel = commandPermissions[this]

        if(cmdLevel != null) return cmdLevel
        if(setLevel != null) return setLevel
        return DEFAULT_REQUIRED_PERMISSION
    }
    set(value) { commandPermissions[this] = value }
