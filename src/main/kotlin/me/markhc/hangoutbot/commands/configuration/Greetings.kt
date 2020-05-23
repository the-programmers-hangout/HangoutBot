package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.BooleanArg
import me.aberrantfox.kjdautils.internal.arguments.ChoiceArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.services.ConversationResult
import me.aberrantfox.kjdautils.internal.services.ConversationService
import me.markhc.hangoutbot.commands.configuration.conversations.GreetingSetContents
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Greetings")
fun greetingCommands(persistentData: PersistentData, conversationService: ConversationService) = commands {
    command("greetings") {
        description = "Enables or disables the greetings on member join."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(BooleanArg("enable/disable", "enable", "disable").makeNullableOptional(null)) {
            val (enable) = it.args

            if(enable != null) {
                persistentData.setGuildProperty(it.guild!!) {
                    welcomeEmbeds = enable
                }

                it.respond("Welcome embeds are now ${if (enable) "enabled" else "disabled"}")
            } else {
                val state = persistentData.setGuildProperty(it.guild!!) { welcomeEmbeds }

                it.respond("Welcome embeds are ${if (state) "enabled" else "disabled"}")
            }
        }
    }

    command("greetdms") {
        description = "Whether to send  greetings through DMs"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(BooleanArg("enable/disable", "enable", "disable")) {
            it.respond("Not implemented yet!")
        }
    }

    command("greetchnl") {
        description = "Whether to send  greetings in the configured greeting channel"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(BooleanArg("enable/disable", "enable", "disable")) {
            it.respond("Not implemented yet!")
        }
    }

    command("greetcontents") {
        description = "Configure the contents of the greeting message"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute {
            it.respond("Not implemented yet!")
        }
    }
}

private fun evaluateConversationResult(conversationResult: ConversationResult) =
        when (conversationResult) {
            ConversationResult.COMPLETE -> "Conversation Completed!"
            ConversationResult.EXITED -> "The conversation was exited by the user."
            ConversationResult.INVALID_USER -> "User must share a guild and cannot be a bot."
            ConversationResult.CANNOT_DM -> "User has DM's off or has blocked the bot."
            ConversationResult.HAS_CONVO -> "This user already has a conversation."
        }
