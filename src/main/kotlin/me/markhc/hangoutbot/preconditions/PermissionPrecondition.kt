package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel

@Precondition
fun produceHasPermissionPrecondition(persistentData: PersistentData, permissionsService: PermissionsService) = precondition {
    val command = it.command ?: return@precondition Fail()

    if(it.guild == null) {
        return@precondition if (permissionsService.hasClearance(null, it.author, command.requiredPermissionLevel))
            Pass
        else
            Fail(Messages.INSUFFICIENT_PERMS)
    } else {
        val guild = it.guild!!
        val member = guild.getMember(it.author)!!

        val botChannel = persistentData.getGuildProperty(guild) { botChannel }
        if (botChannel.isNotEmpty()
                && it.channel.id != botChannel
                && permissionsService.getPermissionLevel(member) > PermissionLevel.Administrator)
            return@precondition Fail()

        val level = permissionsService.getCommandPermissionLevel(guild, command)

        if (!permissionsService.hasClearance(guild, it.author, level))
            return@precondition Fail(Messages.INSUFFICIENT_PERMS)

        return@precondition Pass
    }
}
