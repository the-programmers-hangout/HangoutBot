package me.markhc.hangoutbot.preconditions

import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.dsl.precondition
import me.jakejmattson.discordkt.extensions.sanitiseMentions
import me.markhc.hangoutbot.dataclasses.Configuration

fun commandLogger(configuration: Configuration) = precondition {
    command ?: return@precondition fail()

    val args = rawInputs.commandArgs.joinToString()

    if (args.length > 1500)
        return@precondition fail("Command is too long (${args.length} chars, max: 1500")

    if (guild != null) {
        val guild = guild!!
        val loggingChannel = configuration[guild].loggingChannel
        val channel = channel as TextChannel

        val message =
            "${author.tag} :: ${author.id} :: " +
                "Invoked `${command!!.names.first()}` in #${channel.name}." +
                if (args.isEmpty()) "" else " Args: ${args.sanitiseMentions(discord)}"

        guild.getChannelOf<TextChannel>(loggingChannel).createMessage(message)
    }

    return@precondition
}
