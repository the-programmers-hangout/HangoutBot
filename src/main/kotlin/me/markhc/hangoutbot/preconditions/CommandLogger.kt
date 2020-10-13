package me.markhc.hangoutbot.preconditions

import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.extensions.*
import me.markhc.hangoutbot.services.*

class CommandLogger(private val botStats: BotStatsService, private val persistentData: PersistentData) : Precondition() {
    override suspend fun evaluate(event: CommandEvent<*>): PreconditionResult {

        event.command ?: return Fail()

        val args = event.rawInputs.commandArgs.joinToString()

        if (args.length > 1500) {
            return Fail("Command is too long (${args.length} chars, max: 1500)")
        }

        if (event.guild != null) {
            val guild = event.guild!!

            if (!persistentData.hasGuildConfig(guild.id.value))
                return Pass

            val loggingChannel = persistentData.getGuildProperty(guild) { loggingChannel }.toSnowflakeOrNull()

            if (loggingChannel != null) {
                val channel = event.channel as TextChannel

                val message =
                    "${event.author.tag} :: ${event.author.id.value} :: " +
                        "Invoked `${event.command!!.names.first()}` in #${channel.name}." +
                        if (args.isEmpty()) "" else " Args: ${args.sanitiseMentions(event.discord)}"

                guild.getChannelOf<TextChannel>(loggingChannel).createMessage(message)
            }
        }

        botStats.commandExecuted(event)

        return Pass
    }
}
