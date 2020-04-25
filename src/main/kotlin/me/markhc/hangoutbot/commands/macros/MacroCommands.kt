package me.markhc.hangoutbot.commands.macros

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.Permission

@CommandSet("MacroManagement")
fun macroManagementCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    command("addmacro") {
        requiredPermissionLevel = Permission.Staff
        description = "Adds a macro to a category. If the category does not exist, it will be created."
        execute(WordArg("Category"), WordArg("MacroName"), SentenceArg("Message")) {
            val (category, macroName, macroMessage) = it.args
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guildId)

            if(guildConfig.macros.any { entry -> entry.value.containsKey(macroName) }) {
                return@execute it.respond("A macro by this name already exists")
            }

            val key = guildConfig.macros.keys.find {key ->
                key.compareTo(category, true) == 0
            }

            if(key == null) {
                guildConfig.macros[category] = mutableMapOf(macroName to macroMessage)
            } else {
                guildConfig.macros[key]!![macroName] = macroMessage
            }
            persistence.save(config)

            return@execute it.respond("OK. **$macroName** (category **${key ?: category}**) will respond with: ```\n$macroMessage\n```")
        }
    }

    command("renamecategory") {
        requiredPermissionLevel = Permission.Staff
        description = "Renames a macro category."
        execute(WordArg("OldName"), WordArg("NewName")) {
            val (category, newName) = it.args
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guildId)

            val key = guildConfig.macros.keys.find {key ->
                key.compareTo(category, true) == 0
            } ?: return@execute it.respond("No category named **$category**.")

            guildConfig.macros[newName] = guildConfig.macros[key]!!
            guildConfig.macros.remove(key)
            persistence.save(config)

            it.respond("Renamed category **$key** to **$newName**.")
        }
    }

    command("removemacro") {
        requiredPermissionLevel = Permission.Staff
        description = "Removes a macro. If it is the last macro in its category, the category will be removed."
        execute(WordArg("MacroName")) {
            it.respond("Not implemented yet")
        }
    }

    command("listmacros") {
        requiredPermissionLevel = Permission.Everyone
        description = "List all available macros."
        execute {
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val macros = config.getGuildConfig(guildId).macros

            it.respond(buildMacroEmbed(macros))
        }
    }
}

fun buildMacroEmbed(macros: MutableMap<String, MutableMap<String, String>>) = embed {
    title = "Available Macros"

    macros.forEach {
        field {
            name = it.key
            value = it.value.toList().sortedBy { it.first }.joinToString { it.first }
        }
    }
}