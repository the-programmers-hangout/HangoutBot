package me.markhc.hangoutbot.commands.utilities

import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.markhc.hangoutbot.commands.utilities.services.MacroService
import me.markhc.hangoutbot.services.*

fun macroCommands(macroService: MacroService) = commands("Macros") {
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
                ChannelArg<TextChannel>("Channel").makeNullableOptional(),
                EveryArg("Contents")) {
            val (name, category, channel, contents) = args

            respond(macroService.addMacro(guild!!, name, category, channel, contents))
        }
    }

    command("removemacro") {
        description = "Removes a macro"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(AnyArg("Name"), ChannelArg<TextChannel>("Channel").makeNullableOptional()) {
            respond(macroService.removeMacro(guild!!, args.first, args.second))
        }
    }

    command("editmacro") {
        description = "Edits the contents of a macro"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(AnyArg("Name"), ChannelArg<TextChannel>("Channel").makeNullableOptional(), EveryArg("Contents")) {
            respond(macroService.editMacro(guild!!, args.first, args.second, args.third))
        }
    }

    command("editmacrocategory") {
        description = "Edits the category of a macro"
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(AnyArg("Name"), ChannelArg<TextChannel>("Channel").makeNullableOptional(), AnyArg("New Category")) {
            respond(macroService.editMacroCategory(guild!!, args.first, args.second, args.third))
        }
    }

    command("listmacros") {
        description = "Lists all macros available in the given channel. If no channel is specified, defaults to the current channel."
        requiredPermissionLevel = PermissionLevel.Everyone
        requiresGuild = true
        execute(ChannelArg<TextChannel>("Channel").makeOptional { it.channel as TextChannel }) {
            macroService.listMacros(this, guild!!, args.first)
        }
    }

    command("listallmacros") {
        description = "Lists all macros available in the guild, grouped by channel."
        requiredPermissionLevel = PermissionLevel.Everyone
        requiresGuild = true
        execute {
            respond(macroService.listAllMacros(this, guild!!))
        }
    }
}