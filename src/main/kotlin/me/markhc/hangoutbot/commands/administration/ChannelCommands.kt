package me.markhc.hangoutbot.commands.administration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData

@CommandSet("Channel")
fun channelCommands(persistentData: PersistentData) = commands {
    command("chnltopic") {
        description = "Gets or sets the topic of a channel."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(TextChannelArg, EveryArg.makeOptional("")) { event ->
            val channel = event.args.first
            val topic = event.args.second.trim()

            if(topic.isEmpty()) {
                if(channel.topic != null) {
                    event.respond("The topic for ${channel.asMention} is: \n```\n${channel.topic}\n```")
                } else {
                    event.respond("${channel.asMention} does not have a set topic!")
                }
            } else {
                channel.manager.setTopic(topic).queue(
                        { event.respond("Success!") },
                        { event.respond("Error while trying to set topic. ${it.message}") }
                )
            }
        }
    }

    command("slowmode") {
        description = "Get or set the slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(TextChannelArg, TimeArg.makeNullableOptional(null)) { event ->
            val (channel, interval) = event.args

            if(interval != null) {
                if (interval > 21600 || interval < 0) {
                    return@execute event.respond("Invalid time element passed.")
                }

                channel.manager.setSlowmode(interval.toInt()).queue {
                    event.respond("Successfully set slow-mode in channel ${channel.asMention} to ${interval.toInt()} seconds.")
                }
            } else {
                event.respond("The slowmode internal for ${channel.asMention} is ${channel.slowmode} seconds")
            }
        }
    }
}