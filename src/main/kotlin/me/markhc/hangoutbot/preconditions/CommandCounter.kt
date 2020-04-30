package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.markhc.hangoutbot.services.BotStatsService

@Precondition
fun produceCommandCounterPrecondition(botStats: BotStatsService) = precondition {
    botStats.commandExecuted(it)

    return@precondition Pass
}