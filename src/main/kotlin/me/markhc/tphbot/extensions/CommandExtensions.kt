package me.markhc.tphbot.extensions

import me.aberrantfox.kjdautils.api.dsl.command.*
import me.markhc.tphbot.services.*
import java.util.*

private object CommandsContainerPropertyStore {
    val setPermissions = WeakHashMap<CommandsContainer, Permission>()
}

private val commandPermissions: MutableMap<Command, Permission> = mutableMapOf()

var CommandsContainer.requiredPermissionLevel
    get() = CommandsContainerPropertyStore.setPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        CommandsContainerPropertyStore.setPermissions[this] = value
    }

var Command.requiredPermissionLevel: Permission
    get() {
        val setLevel = CommandsContainerPropertyStore.setPermissions.toList()
                .firstOrNull { this in it.first.commands }?.second ?: DEFAULT_REQUIRED_PERMISSION

        val cmdLevel = commandPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION

        return if(cmdLevel < setLevel) cmdLevel else setLevel
    }
    set(value) { commandPermissions[this] = value }
