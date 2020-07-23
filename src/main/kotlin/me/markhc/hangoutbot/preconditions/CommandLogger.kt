package me.markhc.hangoutbot.preconditions

import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.dsl.preconditions.*
import me.jakejmattson.kutils.api.extensions.jda.fullName
import me.jakejmattson.kutils.api.extensions.stdlib.sanitiseMentions
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.PersistentData

class CommandLogger(private val botStats: BotStatsService,
                    private val persistentData: PersistentData) : Precondition() {
    override fun evaluate(event: CommandEvent<*>): PreconditionResult {
        event.command ?: return Fail()

        botStats.commandExecuted(event)

        val args = event.rawInputs.commandArgs.joinToString()

        if (args.length > 1500) {
            return Fail("Command is too long (${args.length} chars, max: 1500)")
        }

        if(event.guild != null) {
            val guild = event.guild!!
            val loggingChannel = persistentData.getGuildProperty(guild) { loggingChannel }

            if(loggingChannel.isNotEmpty()) {
                val message =
                        "${event.author.fullName()} :: ${event.author.id} :: " +
                                "Invoked `${event.command!!.names.first()}` in #${event.channel.name}." +
                                if(args.isEmpty()) "" else " Args: ${args.sanitiseMentions()}"

                guild.getTextChannelById(loggingChannel)
                        ?.sendMessage(message)?.queue()
            }
        }

        return Pass
    }
}
