package me.markhc.hangoutbot.preconditions

import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.dsl.precondition
import me.jakejmattson.discordkt.extensions.sanitiseMentions
import me.markhc.hangoutbot.dataclasses.Configuration

fun commandLogger(configuration: Configuration) = precondition {
    command ?: return@precondition

    val args = rawInputs.commandArgs.joinToString()

    if (guild != null) {
        val guild = guild!!

        if (!configuration.hasGuildConfig(guild))
            return@precondition

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
