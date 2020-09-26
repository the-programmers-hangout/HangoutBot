package me.markhc.hangoutbot.services

import com.gitlab.kordlib.core.entity.*
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.*

@Service
class HelpService(private val permissionsService: PermissionsService) {
    private suspend fun Command.isVisible(guild: Guild, user: User) =
        permissionsService.isCommandVisible(guild, user, this)

    suspend fun buildHelpEmbed(event: CommandEvent<*>) = event.respond {
        val container = event.discord.commands

        title = "Help information"
        description = "Use `${event.prefix()}help <command>` for more information"
        color = event.discord.configuration.theme

        fun joinNames(value: List<Command>) =
            value.joinToString("\n") { it.names.first() }

        val commands = container
            .filter { it.isVisible(event.guild!!, event.author) }
            .groupBy { it.category }
            .toList()
            .sortedByDescending { it.second.size }

        if (commands.isNotEmpty()) {
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

    private fun generateExample(event: CommandEvent<*>, command: Command) =
        command.arguments.joinToString(" ") {
            it.generateExamples(event).random()
        }

    suspend fun buildHelpEmbedForCommand(event: CommandEvent<*>, command: Command) = event.respond {
        title = command.names.joinToString()
        description = command.description
        color = event.discord.configuration.theme

        val commandInvocation = "${event.prefix()}${command.names.first()}"

        field {
            name = "What is the structure of the command?"
            value = "$commandInvocation ${generateStructure(command)}"
        }

        field {
            name = "Show me an example of someone using the command."
            value = "$commandInvocation ${generateExample(event, command)}"
        }
    }
}