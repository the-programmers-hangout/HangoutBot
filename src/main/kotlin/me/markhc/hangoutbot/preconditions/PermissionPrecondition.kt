package me.markhc.hangoutbot.preconditions


import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.*
import me.markhc.hangoutbot.extensions.commandPermissions
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.extensions.setPermissions
import me.markhc.hangoutbot.services.*
import mu.KLogger

@Precondition
fun produceHasPermissionPrecondition(logger: KLogger, permissionsService: PermissionsService) = precondition {
    val command = it.container[it.commandStruct.commandName]
    val requiredPermissionLevel = command?.requiredPermissionLevel ?: DEFAULT_REQUIRED_PERMISSION
    val guild = it.guild!!
    val member = it.author.toMember(guild)!!

    //
    // For debugging purposes, REMOVE AFTER BUG FIX
    //
    val setLevel = setPermissions.toList()
            .firstOrNull { command in it.first.commands }?.second

    val cmdLevel = commandPermissions[command]

    logger.debug { "setLevel: $setLevel" }
    logger.debug { "cmdLevel: $cmdLevel" }
    logger.debug { "command?.requiredPermissionLevel: ${command?.requiredPermissionLevel}" }
    // end of debugging

    if (!permissionsService.hasClearance(member, requiredPermissionLevel))
        return@precondition Fail("You do not have the required permissions to perform this action.")

    return@precondition Pass
}
