package me.markhc.hangoutbot.preconditions

import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.dsl.precondition
import me.jakejmattson.discordkt.api.extensions.*
import me.markhc.hangoutbot.services.*

fun commandLogger(botStats: BotStatsService, persistentData: PersistentData) = precondition {
    command ?: fail()

    botStats.commandExecuted(this)

    val args = rawInputs.commandArgs.joinToString()

    if (args.length > 1500) {
        fail("Command is too long (${args.length} chars, max: 1500)")
    }

    if (guild != null) {
        val guild = guild!!
        val loggingChannel = persistentData.getGuildProperty(guild) { loggingChannel }.toSnowflakeOrNull()

        if (loggingChannel != null) {
            val message =
                "${author.tag} :: ${author.id} :: " +
                    "Invoked `${command!!.names.first()}` in #${channel}." +
                    if (args.isEmpty) "" else " Args: ${args.sanitiseMentions(discord)}"

            guild.getChannelOf<TextChannel>(loggingChannel).createMessage(message)
        }
    }
}
