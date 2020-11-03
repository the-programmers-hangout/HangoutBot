package me.markhc.hangoutbot.preconditions

import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.dsl.precondition
import me.jakejmattson.discordkt.api.extensions.sanitiseMentions
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.PersistentData

fun commandLogger(botStats: BotStatsService, persistentData: PersistentData) = precondition {
    command ?: return@precondition fail()

    val args = rawInputs.commandArgs.joinToString()

    if (args.length > 1500)
        return@precondition fail("Command is too long (${args.length} chars, max: 1500")

    if (guild != null) {
        val guild = guild!!

        if (!persistentData.hasGuildConfig(guild.id.value))
            return@precondition

        val loggingChannel = persistentData.getGuildProperty(guild) { loggingChannel }.toSnowflakeOrNull()

        if (loggingChannel != null) {
            val channel = channel as TextChannel

            val message =
                    "${author.tag} :: ${author.id.value} :: " +
                            "Invoked `${command!!.names.first()}` in #${channel.name}." +
                            if (args.isEmpty()) "" else " Args: ${args.sanitiseMentions(discord)}"

            guild.getChannelOf<TextChannel>(loggingChannel).createMessage(message)
        }
    }

    botStats.commandExecuted(guild)

    return@precondition
}
