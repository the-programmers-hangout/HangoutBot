package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.precondition
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.*

fun permissionPrecondition(persistentData: PersistentData, permissionsService: PermissionsService) = precondition {
    val command = command ?: return@precondition fail()

    if (guild == null) {
        return@precondition if (permissionsService.hasClearance(null, author, command.requiredPermissionLevel))
            Unit
        else
            fail(Messages.INSUFFICIENT_PERMS)
    } else {
        val guild = guild!!
        val member = author.asMember(guild.id)

        val botChannel = persistentData.getGuildProperty(guild) { botChannel }
        if (botChannel.isNotEmpty()
            && channel.id.value != botChannel
            && permissionsService.getPermissionLevel(member) > PermissionLevel.Administrator)
            fail()

        val level = permissionsService.getCommandPermissionLevel(guild, command)

        if (!permissionsService.hasClearance(guild, author, level))
            fail(Messages.INSUFFICIENT_PERMS)
    }
}
