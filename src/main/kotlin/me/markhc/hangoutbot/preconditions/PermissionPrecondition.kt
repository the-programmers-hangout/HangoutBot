package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.DEFAULT_REQUIRED_PERMISSION
import me.markhc.hangoutbot.services.PermissionsService

@Precondition
fun produceHasPermissionPrecondition(botStats: BotStatsService, permissionsService: PermissionsService) = precondition {
    val command = it.container[it.commandStruct.commandName] ?: return@precondition Fail()
    val level = permissionsService.getCommandPermissionLevel(it.guild, command)

    if (!permissionsService.hasClearance(it.guild, it.author, level))
        return@precondition Fail("You do not have the required permissions to perform this action.")

    return@precondition Pass
}
