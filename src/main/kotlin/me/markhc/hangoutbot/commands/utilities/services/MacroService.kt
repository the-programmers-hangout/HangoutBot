package me.markhc.hangoutbot.commands.utilities.services

import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.dsl.embed.embed
import me.jakejmattson.discordkt.api.dsl.preconditions.Pass
import me.jakejmattson.discordkt.api.dsl.preconditions.Precondition
import me.jakejmattson.discordkt.api.dsl.preconditions.PreconditionResult
import me.markhc.hangoutbot.dataclasses.TextMacro
import me.markhc.hangoutbot.services.PersistentData
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel


@Service
class MacroService(private val persistentData: PersistentData) {
    fun addMacro(guild: Guild, name: String, category: String, channel: TextChannel?, contents: String): String {
        val channelId = channel?.id ?: ""

        val result = persistentData.setGuildProperty(guild) {
            availableMacros.putIfAbsent("$name#$channelId", TextMacro(name, contents, channelId, category))
        }

        return if(result == null) {
            "Success. Macro `$name` is now available ${ if(channel == null) "globally" else "on channel ${channel.asMention}"} and will respond with ```\n$contents\n```"
        } else {
            "A macro with that name already exists."
        }
    }

    fun removeMacro(guild: Guild, name: String, channel: TextChannel?): String {
        val channelId = channel?.id ?: ""

        val result = persistentData.setGuildProperty(guild) {
            availableMacros.remove("$name#$channelId")
        }

        return if(result == null) {
            "Success. Macro `$name` has been removed"
        } else {
            "Cannot find a macro by that name. If it is a channel specific macro you need to provide the channel as well."
        }
    }

    fun editMacro(guild: Guild, name: String, channel: TextChannel?, contents: String): String {
        val channelId = channel?.id ?: ""

        val result = persistentData.setGuildProperty(guild) {
            if(availableMacros.containsKey("$name#$channelId")) {
                availableMacros["$name#$channelId"]!!.contents = contents
                true
            } else {
                false
            }
        }

        return if(result) {
            "Success. Macro `$name` available ${ if(channel == null) "globally" else "on channel ${channel.asMention}"} will now respond with ```\n$contents\n```"
        } else {
            "Cannot find a macro by that name. If it is a channel specific macro you need to provide the channel as well."
        }
    }

    fun editMacroCategory(guild: Guild, name: String, channel: TextChannel?, category: String): String {
        val channelId = channel?.id ?: ""

        val result = persistentData.setGuildProperty(guild) {
            if(availableMacros.containsKey("$name#$channelId")) {
                availableMacros["$name#$channelId"]!!.category = category
                true
            } else {
                false
            }
        }

        return if(result) {
            "Success. Macro `$name` available ${ if(channel == null) "globally" else "on channel ${channel.asMention}"} is now in category `${category}`"
        } else {
            "Cannot find a macro by that name. If it is a channel specific macro you need to provide the channel as well."
        }
    }

    fun listMacros(guild: Guild, channel: TextChannel): MessageEmbed {
        val macros = getMacrosAvailableIn(guild, channel)
                .groupBy { it.category }
                .toList()
                .sortedByDescending { it.second.size }

        return embed {
            simpleTitle = "Macros available in ${channel.name}"
            color = infoColor

            if(macros.isNotEmpty()) {
                macros.map { (category, macros) ->
                    val sorted = macros.sortedBy { it.name }

                    field {
                        name = "**$category**"
                        value = "```css\n${sorted.joinToString("\n") { it.name }}\n```"
                        inline = true
                    }
                }
            }
        }
    }

    private fun getMacrosAvailableIn(guild: Guild, channel: TextChannel): List<TextMacro> {
        val macroList = persistentData.getGuildProperty(guild) {
            availableMacros.filter {
                it.key.endsWith('#') || it.key.takeLast(18) == channel.id
            }
        }

        return macroList.filterKeys { key ->
            if(key.endsWith('#')) {
                macroList.keys.none { it.startsWith(key) && !it.endsWith('#') }
            } else {
                true
            }
        }.map { it.value }
    }

    fun findMacro(guild: Guild?, name: String, channel: MessageChannel): TextMacro? {
        if(guild == null)
            return null

        return persistentData.getGuildProperty(guild) {
            // first try to find a channel specific macro
            // if it fails, default to a global macro
            availableMacros["$name#${channel.id}"] ?: availableMacros["$name#"]
        }
    }
}

class MacroPrecondition(private val macroService: MacroService) : Precondition() {
    override fun evaluate(event: CommandEvent<*>): PreconditionResult {
        if(event.command != null)
            return Pass

        val macro = macroService.findMacro(event.guild, event.rawInputs.commandName, event.channel)

        if(macro != null) {
            event.message.delete().queue()
            event.channel.sendMessage(macro.contents).queue()
        }

        return Pass
    }
}