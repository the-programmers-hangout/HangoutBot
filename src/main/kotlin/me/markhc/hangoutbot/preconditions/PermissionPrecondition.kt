package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel

fun permissionPrecondition(persistentData: PersistentData, permissionsService: PermissionsService) = precondition {
    val command = command ?: return@precondition fail()

    if (guild == null) {
        if (permissionsService.hasClearance(null, author, command.requiredPermissionLevel)) {
            return@precondition
        } else {
            return@precondition fail(Messages.INSUFFICIENT_PERMS)
        }
    } else {
        val guild = guild!!
        val member = author.asMember(guild.id)

        if (!persistentData.hasGuildConfig(guild.id.toString()))
            return@precondition

        val botChannel = persistentData.getGuildProperty(guild) { botChannel }

        if (botChannel.isNotEmpty()
                && channel.id.toString() != botChannel
                && permissionsService.getPermissionLevel(member) > PermissionLevel.Administrator)

            return@precondition fail()

        val level = permissionsService.getCommandPermissionLevel(guild, command)

        if (!permissionsService.hasClearance(guild, author, level))
            return@precondition fail(Messages.INSUFFICIENT_PERMS)

        return@precondition
    }
}
