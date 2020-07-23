package me.markhc.hangoutbot.preconditions

import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.dsl.preconditions.*
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel

class PermissionPrecondition(private val persistentData: PersistentData,
                             private val permissionsService: PermissionsService) : Precondition() {
    override fun evaluate(event: CommandEvent<*>): PreconditionResult {
        val command = event.command ?: return Fail()

        if (event.guild == null) {
            return if (permissionsService.hasClearance(null, event.author, command.requiredPermissionLevel))
                Pass
            else
                Fail(Messages.INSUFFICIENT_PERMS)
        } else {
            val guild = event.guild!!
            val member = guild.getMember(event.author)!!

            val botChannel = persistentData.getGuildProperty(guild) { botChannel }
            if (botChannel.isNotEmpty()
                    && event.channel.id != botChannel
                    && permissionsService.getPermissionLevel(member) > PermissionLevel.Administrator)
                return Fail()

            val level = permissionsService.getCommandPermissionLevel(guild, command)

            if (!permissionsService.hasClearance(guild, event.author, level))
                return Fail(Messages.INSUFFICIENT_PERMS)

            return Pass
        }
    }
}
