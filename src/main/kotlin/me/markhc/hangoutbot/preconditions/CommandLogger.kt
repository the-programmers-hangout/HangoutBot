package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.DEFAULT_REQUIRED_PERMISSION
import me.markhc.hangoutbot.services.PermissionsService

@Precondition
fun produceCommandLoggerPrecondition(botStats: BotStatsService, config: Configuration, persistenceService: PersistenceService) = precondition {
    val command = it.container[it.commandStruct.commandName] ?: return@precondition Fail()

    botStats.commandExecuted(it)

    if(it.guild != null) {
        val guild = it.guild!!
        config.getGuildConfig(guild).apply {
            if(loggingChannel.isNotEmpty()) {
                val args = it.commandStruct.commandArgs
                val message =
                        "${it.author.fullName()} :: ${it.author.id} :: " +
                        "Invoked `${it.commandStruct.commandName}` in #${it.channel.name}." +
                        if(args.isEmpty()) "" else " Args: ${args.joinToString(", ")}"

                guild.getTextChannelById(loggingChannel)
                        ?.sendMessage(message)?.queue()
            }
        }
    }



    return@precondition Pass
}
