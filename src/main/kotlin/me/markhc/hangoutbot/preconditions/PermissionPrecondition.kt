package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.*

class PermissionPrecondition(private val persistentData: PersistentData,
                             private val permissionsService: PermissionsService) : Precondition() {
    override suspend fun evaluate(event: CommandEvent<*>): PreconditionResult {
        val command = event.command ?: return Fail()

        if (event.guild == null) {
            return if (permissionsService.hasClearance(null, event.author, command.requiredPermissionLevel))
                Pass
            else
                Fail(Messages.INSUFFICIENT_PERMS)
        } else {
            val guild = event.guild!!
            val member = event.author.asMember(guild.id)

            val botChannel = persistentData.getGuildProperty(guild) { botChannel }
            if (botChannel.isNotEmpty()
                && event.channel.id.value != botChannel
                && permissionsService.getPermissionLevel(member) > PermissionLevel.Administrator)
                return Fail()

            val level = permissionsService.getCommandPermissionLevel(guild, command)

            if (!permissionsService.hasClearance(guild, event.author, level))
                return Fail(Messages.INSUFFICIENT_PERMS)

            return Pass
        }
    }
}
