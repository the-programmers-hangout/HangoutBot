package me.markhc.hangoutbot.extensions

import me.aberrantfox.kjdautils.api.dsl.command.*
import me.markhc.hangoutbot.services.*

val commandPermissions: MutableMap<Command, PermissionLevel> = mutableMapOf()

var Command.requiredPermissionLevel: PermissionLevel
    get() = commandPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        commandPermissions[this] = value
    }