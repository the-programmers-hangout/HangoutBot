package me.markhc.hangoutbot.modules.administration

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.BooleanArg
import me.jakejmattson.kutils.api.dsl.command.commands
import me.markhc.hangoutbot.modules.administration.services.GreetingService
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel
import me.markhc.hangoutbot.utilities.runLoggedCommand

@CommandSet("Greetings")
fun greetingCommands(greetingService: GreetingService) = commands {
    command("greetings") {
        description = "Enables or disables the greetings on member join."
        requiredPermissionLevel = PermissionLevel.Administrator
        requiresGuild = true
        execute(BooleanArg("enable/disable", "enable", "disable").makeNullableOptional(null)) {
            runLoggedCommand(it) {
                val (enable) = it.args

                if (enable != null) {
                    greetingService.setEnabled(it.guild!!, enable)

                    it.respond("Welcome embeds are now ${if (enable) "enabled" else "disabled"}")
                } else {
                    val state = greetingService.isEnabled(it.guild!!)

                    it.respond("Welcome embeds are ${if (state) "enabled" else "disabled"}")
                }
            }
        }
    }

    /*command("greetdms") {
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
    }*/
}