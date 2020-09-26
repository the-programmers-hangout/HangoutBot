package me.markhc.hangoutbot.commands.utilities

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.dsl.command.commands
import me.jakejmattson.discordkt.api.arguments.*
import me.markhc.hangoutbot.commands.utilities.services.MacroService
import me.markhc.hangoutbot.services.*
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Macros")
fun macroCommands(macroService: MacroService) = commands {
    command("addmacro") {
        description = "Adds a macro to a specific channel or globally, if no channel is given"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        usageExamples = listOf(
            "coolname Miscellaneous This is a global macro",
            "promises Programming #javascript Channel specific macro"
        )
        execute(AnyArg("Name"),
                AnyArg("Category"),
                TextChannelArg("Channel").makeNullableOptional(),
                EveryArg("Contents")) {
            val (name, category, channel, contents) = it.args

            it.respond(macroService.addMacro(it.guild!!, name, category, channel, contents))
        }
    }

    command("removemacro") {
        description = "Removes a macro"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(AnyArg("Name"), TextChannelArg("Channel").makeNullableOptional()) {
            it.respond(macroService.removeMacro(it.guild!!, it.args.first, it.args.second))
        }
    }

    command("editmacro") {
        description = "Edits the contents of a macro"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(AnyArg("Name"), TextChannelArg("Channel").makeNullableOptional(), EveryArg("Contents")) {
            it.respond(macroService.editMacro(it.guild!!, it.args.first, it.args.second, it.args.third))
        }
    }

    command("editmacrocategory") {
        description = "Edits the category of a macro"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(AnyArg("Name"), TextChannelArg("Channel").makeNullableOptional(), AnyArg("New Category")) {
            it.respond(macroService.editMacroCategory(it.guild!!, it.args.first, it.args.second, it.args.third))
        }
    }

    command("listmacros") {
        description = "Lists all macros available in the given channel. If no channel is specified, defaults to the current channel."
        requiredPermissionLevel = PermissionLevel.Everyone
        requiresGuild = true
        execute(TextChannelArg("Channel").makeOptional { it.channel as TextChannel }) {
            it.respond(macroService.listMacros(it.guild!!, it.args.first))
        }
    }
}