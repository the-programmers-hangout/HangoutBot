package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.*

@Precondition
fun produceCommandLoggerPrecondition(botStats: BotStatsService, persistentData: PersistentData) = precondition {
    it.container[it.commandStruct.commandName] ?: return@precondition Fail()

    botStats.commandExecuted(it)

    val args = it.commandStruct.commandArgs.joinToString(", ")

    if (args.length > 1500) {
        return@precondition Fail("Command is too long (${args.length} chars, max: 1500)")
    }

    if(it.guild != null) {
        val guild = it.guild!!
        val loggingChannel = persistentData.getGuildProperty(guild) { loggingChannel }

        if(loggingChannel.isNotEmpty()) {
            val message =
                    "${it.author.fullName()} :: ${it.author.id} :: " +
                    "Invoked `${it.commandStruct.commandName}` in #${it.channel.name}." +
                    if(args.isEmpty()) "" else " Args: ${args.sanitiseMentions()}"

            guild.getTextChannelById(loggingChannel)
                    ?.sendMessage(message)?.queue()
        }
    }

    return@precondition Pass
}
