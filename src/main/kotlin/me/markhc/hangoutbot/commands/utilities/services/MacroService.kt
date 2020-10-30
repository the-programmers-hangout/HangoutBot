package me.markhc.hangoutbot.commands.utilities.services

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.precondition
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.markhc.hangoutbot.dataclasses.TextMacro
import me.markhc.hangoutbot.services.PersistentData

@Service
class MacroService(private val persistentData: PersistentData) {
    suspend fun addMacro(guild: Guild, name: String, category: String, channel: TextChannel?, contents: String): String {
        val channelId = channel?.id?.value ?: ""

        val result = persistentData.setGuildProperty(guild) {
            availableMacros.putIfAbsent("$name#$channelId", TextMacro(name, contents, channelId, category))
        }

        return if(result == null) {
            "Success. Macro `$name` is now available ${ if(channel == null) "globally" else "on channel ${channel.mention}"} and will respond with ```\n$contents\n```"
        } else {
            "A macro with that name already exists."
        }
    }

    suspend fun removeMacro(guild: Guild, name: String, channel: TextChannel?): String {
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

    suspend fun editMacro(guild: Guild, name: String, channel: TextChannel?, contents: String): String {
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
            "Success. Macro `$name` available ${ if(channel == null) "globally" else "on channel ${channel.mention}"} will now respond with ```\n$contents\n```"
        } else {
            "Cannot find a macro by that name. If it is a channel specific macro you need to provide the channel as well."
        }
    }

    suspend fun editMacroCategory(guild: Guild, name: String, channel: TextChannel?, category: String): String {
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
            "Success. Macro `$name` available ${ if(channel == null) "globally" else "on channel ${channel.mention}"} is now in category `${category}`"
        } else {
            "Cannot find a macro by that name. If it is a channel specific macro you need to provide the channel as well."
        }
    }

    suspend fun listMacros(event: CommandEvent<*>, guild: Guild, channel: TextChannel) = with(event) {
        val availableMacros = getMacrosAvailableIn(guild, channel)
                .groupBy { it.category }
                .toList()
                .sortedByDescending { it.second.size }

        val chunks = availableMacros.chunked(25)

        event.respondMenu {
            chunks.map {
                page {
                    title = "Macros available in ${channel.name}"
                    color = discord.configuration.theme

                    if (it.isNotEmpty()) {
                        it.map { (category, macros) ->
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
        }
    }

    suspend fun listAllMacros(event: CommandEvent<*>, guild: Guild) {
        val allMacros = persistentData.getGuildProperty(guild) { availableMacros }
                .map { it.value }
                .groupBy { it.channel.toSnowflakeOrNull()?.let { guild.getChannel(it).name } ?: "Global Macros" }
                .toList()
                .sortedByDescending { it.second.size }

        val chunks = allMacros.chunked(25)

        event.respondMenu {
            chunks.map {
                page {
                    title = "All available macros"
                    color = event.discord.configuration.theme

                    if (it.isNotEmpty()) {
                        it.map { (channel, macros) ->
                            field {
                                name = "**$channel**"
                                value = "```css\n${macros.joinToString("\n") { it.name }}\n```"
                                inline = true
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun getMacrosAvailableIn(guild: Guild, channel: TextChannel): List<TextMacro> {
        val macroList = persistentData.getGuildProperty(guild) {
            availableMacros.filter {
                it.key.endsWith('#') || it.key.takeLast(18) == channel.id.value
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

    suspend fun findMacro(guild: Guild?, name: String, channel: MessageChannel): TextMacro? {
        if (guild == null)
            return null

        if (!persistentData.hasGuildConfig(guild.id.value))
            return null


        return persistentData.getGuildProperty(guild) {
            // first try to find a channel specific macro
            // if it fails, default to a global macro
            availableMacros["$name#${channel.id}"] ?: availableMacros["$name#"]
        }
    }
}

fun macroPrecondition(macroService: MacroService) = precondition {
    command ?: return@precondition fail()


    val macro = macroService.findMacro(guild, rawInputs.commandName, channel.asChannel())
            ?: return@precondition

    message.delete()
    channel.createMessage(macro.contents)

    return@precondition
}