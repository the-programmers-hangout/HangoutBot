package me.markhc.hangoutbot.services

import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.command.Command
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.dsl.embed.embed
import me.markhc.hangoutbot.locale.Messages
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role

val commandUsage: MutableMap<Command, List<String>> = mutableMapOf()

var Command.usageExamples: List<String>
    get() = commandUsage[this] ?: emptyList()
    set(value) {
        commandUsage[this] = value
    }

@Service
class HelpService(private val permissionsService: PermissionsService) {
    private fun Command.isVisible(guild: Guild, user: User) =
            permissionsService.isCommandVisible(guild, user, this)

    fun buildHelpEmbed(event: CommandEvent<*>) = embed {
        val container = event.container

        simpleTitle = "Help information"
        description = "Use `${event.relevantPrefix}help <command>` for more information"
        color = infoColor

        fun joinNames(value: List<Command>) =
                value.joinToString("\n") { it.names.first() }

        val commands = container.commands
                .filter { it.isVisible(event.guild!!, event.author) }
                .groupBy { it.category }
                .toList()
                .sortedByDescending { it.second.size }

        if(commands.isNotEmpty()) {
            commands.map { (category, commands) ->
                val sorted = commands
                        .sortedBy { it.names.first() }

                field {
                    name = "**$category**"
                    value = "```css\n${joinNames(sorted)}\n```"
                    inline = true
                }
            }
        }
    }

    private fun generateStructure(command: Command) =
            command.arguments.joinToString(" ") {
                val type = it.name
                if (it.isOptional) "($type)" else "[$type]"
            }

    private fun generateExample(event: CommandEvent<*>, command: Command): String {
        return if(command.usageExamples.isEmpty()) {
            command.arguments.joinToString(" ") {
                it.generateExamples(event).random()
            }
        } else {
            command.usageExamples.random()
        }
    }

    fun buildHelpEmbedForCommand(event: CommandEvent<*>, command: Command) = embed {
        title { text = command.names.joinToString() }
        description = command.description
        color = infoColor

        val commandInvocation = "${event.relevantPrefix}${command.names.first()}"

        field {
            name = "What is the structure of the command?"
            value = "```\n$commandInvocation ${generateStructure(command)}\n```"
        }

        field {
            name = "Show me an example of someone using the command."
            value = "```\n$commandInvocation ${generateExample(event, command)}\n```"
        }
    }
}