package me.markhc.hangoutbot.services

import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.command.Command
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.dsl.embed.embed
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

@Service
class HelpService(private val permissionsService: PermissionsService) {
    private fun Command.isVisible(guild: Guild, user: User) =
            permissionsService.isCommandVisible(guild, user, this)

    fun buildHelpEmbed(event: CommandEvent<*>) = embed {
        val container = event.container

        title {
            text = "Help information"
        }
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

    private fun generateExample(event: CommandEvent<*>, command: Command) =
            command.arguments.joinToString(" ") {
                it.generateExamples(event).random()
            }

    fun buildHelpEmbedForCommand(event: CommandEvent<*>, command: Command) = embed {
        title { text = command.names.joinToString() }
        description = command.description
        color = infoColor

        val commandInvocation = "${event.relevantPrefix}${command.names.first()}"

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