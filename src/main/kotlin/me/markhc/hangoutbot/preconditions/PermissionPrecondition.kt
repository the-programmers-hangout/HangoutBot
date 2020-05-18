package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData

@Precondition
fun produceHasPermissionPrecondition(persistentData: PersistentData, permissionsService: PermissionsService) = precondition {
    val command = it.command ?: return@precondition Fail()

    if(it.guild == null) {
        return@precondition if (permissionsService.hasClearance(null, it.author, command.requiredPermissionLevel))
            Pass
        else
            Fail("You do not have the required permissions to perform this action.")
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
            return@precondition Fail("You do not have the required permissions to perform this action.")

        return@precondition Pass
    }
}
