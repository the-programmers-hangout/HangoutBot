package me.markhc.hangoutbot.extensions

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.markhc.hangoutbot.services.*
import java.util.*

val setPermissions: MutableMap<CommandsContainer, Permission> = mutableMapOf()
val commandPermissions: MutableMap<Command, Permission> = mutableMapOf()

var CommandsContainer.requiredPermissionLevel
    get() = setPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        setPermissions[this] = value
    }

var Command.requiredPermissionLevel: Permission
    get() {
        val setLevel = setPermissions.toList()
                .firstOrNull { this in it.first.commands }?.second

        val cmdLevel = commandPermissions[this]

        if(cmdLevel != null) return cmdLevel
        if(setLevel != null) return setLevel
        return DEFAULT_REQUIRED_PERMISSION
    }
    set(value) {
        commandPermissions[this] = value
    }
