package me.markhc.hangoutbot.modules.administration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

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
                try {
                    channel.manager.setTopic(topic).queue { event.respond("Success!") }
                } catch(e: InsufficientPermissionException) {
                    event.respond(e.message!!)
                }
            }
        }
    }

    command("slowmode") {
        description = "Set the slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(TextChannelArg, TimeArg) { event ->
            val (channel, interval) = event.args

            if (interval > 21600 || interval < 0) {
                return@execute event.respond("Invalid time element passed.")
            }
            try {
                channel.manager.setSlowmode(interval.toInt()).queue {
                    event.respond("Successfully set slow-mode in channel ${channel.asMention} to ${interval.toInt()} seconds.")
                }
            } catch(e: InsufficientPermissionException) {
                event.respond(e.message!!)
            }
        }
    }
}