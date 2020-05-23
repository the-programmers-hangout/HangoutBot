package me.markhc.hangoutbot.extensions

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.getInjectionObject
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService

fun CommandEvent<*>.requiresPermission(level: PermissionLevel, action: CommandEvent<*>.() -> Unit) {
    val svc = this.discord.getInjectionObject<PermissionsService>()

    if(svc?.hasClearance(this.guild, this.author, level) == true) {
        action(this)
    } else {
        respond(Messages.INSUFFICIENT_PERMS)
    }
}