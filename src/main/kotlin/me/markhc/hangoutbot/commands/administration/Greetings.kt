package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.commands.administration.services.GreetingService
import me.markhc.hangoutbot.services.*
import me.markhc.hangoutbot.utilities.executeLogged

fun greetingCommands(greetingService: GreetingService) = commands("Greetings") {
    command("greetings") {
        description = "Enables or disables the greetings on member join."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        executeLogged(BooleanArg("enable/disable", "enable", "disable").makeNullableOptional(null)) {
            val (enable) = args

            if (enable != null) {
                greetingService.setEnabled(guild!!, enable)
                respond("Welcome embeds are now ${if (enable) "enabled" else "disabled"}")
            } else {
                val state = greetingService.isEnabled(guild!!)
                respond("Welcome embeds are ${if (state) "enabled" else "disabled"}")
            }
        }
    }

    /*command("greetdms") {
        description = "Whether to send  greetings through DMs"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        executeLogged(BooleanArg("enable/disable", "enable", "disable")) {
            it.respond("Not implemented yet!")
        }
    }

    command("greetchnl") {
        description = "Whether to send  greetings in the configured greeting channel"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        executeLogged(BooleanArg("enable/disable", "enable", "disable")) {
            it.respond("Not implemented yet!")
        }
    }

    command("greetcontents") {
        description = "Configure the contents of the greeting message"
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        executeLogged {
            it.respond("Not implemented yet!")
        }
    }*/
}