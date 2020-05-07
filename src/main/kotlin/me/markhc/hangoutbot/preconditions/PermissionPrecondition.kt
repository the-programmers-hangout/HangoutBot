package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData

@Precondition
fun produceHasPermissionPrecondition(persistentData: PersistentData, permissionsService: PermissionsService) = precondition {
    val guild = it.guild ?: return@precondition Fail()
    val member = it.guild!!.getMember(it.author)!!

    val botChannel = persistentData.getGuildProperty(guild) { botChannel }
    if (botChannel.isNotEmpty()
            && it.channel.id != botChannel
            && permissionsService.getPermissionLevel(member) > PermissionLevel.Administrator)
        return@precondition Fail()

    val command = it.container[it.commandStruct.commandName] ?: return@precondition Fail()
    val level = permissionsService.getCommandPermissionLevel(it.guild!!, command)

    if (!permissionsService.hasClearance(member, level))
        return@precondition Fail("You do not have the required permissions to perform this action.")

    return@precondition Pass
}
