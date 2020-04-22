package me.markhc.tphbot.preconditions


import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.*
import me.markhc.tphbot.extensions.requiredPermissionLevel
import me.markhc.tphbot.services.*

@Precondition
fun produceHasPermissionPrecondition(permissionsService: PermissionsService) = precondition {
    val command = it.container[it.commandStruct.commandName]
    val requiredPermissionLevel = command?.requiredPermissionLevel ?: DEFAULT_REQUIRED_PERMISSION
    val guild = it.guild!!
    val member = it.author.toMember(guild)!!

    if (!permissionsService.hasClearance(member, requiredPermissionLevel))
        return@precondition Fail("You do not have the required permissions to perform this action.")

    return@precondition Pass
}
