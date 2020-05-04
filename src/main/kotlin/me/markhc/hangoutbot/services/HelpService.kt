package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.VisibilityContext
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

@Service
class HelpService(private val permissionsService: PermissionsService) {
    private fun Command.isVisible(guild: Guild, user: User) =
            permissionsService.isCommandVisible(guild, user, this)

    fun buildHelpEmbed(event: CommandEvent<*>, mobile: Boolean) = embed {
        val discord = event.discord
        val container = event.container

        title = "Help information"
        description = "Use `${discord.configuration.prefix}help <command>` for more information"
        color = infoColor

        fun joinNames(value: List<Command>) =
                value.joinToString("\n") { it.names.joinToString() }

        val commands = container.commands
                .filter { it.isVisible(event.guild!!, event.author) }
                .groupBy { it.category }

        if(commands.isNotEmpty()) {
            commands.map { (category, commands) ->
                        val sorted = commands
                                .sortedBy { it.names.joinToString() }
                        when {
                            !mobile && sorted.size >= 5 -> { // Split into 3 columns
                                val cols = 3
                                val n1 = (sorted.size + cols - 1) / cols
                                val n2 = (sorted.size + cols - 2) / cols
                                val n3 = (sorted.size + cols - 3) / cols
                                field {
                                    name = "**$category**"
                                    value = joinNames(sorted.subList(0, n1))
                                    inline = true
                                }
                                field {
                                    value = joinNames(sorted.subList(n1, n1+n2))
                                    inline = true
                                }
                                field {
                                    value = joinNames(sorted.subList(n1+n2, sorted.size))
                                    inline = true
                                }
                            }
                            !mobile -> {
                                field {
                                    name = "**$category**"
                                    value = joinNames(sorted)
                                    inline = true
                                }
                                field { inline = true }
                                field { inline = true }
                            }
                            else -> field {
                                name = "**$category**"
                                value = joinNames(sorted)
                                inline = true
                            }
                        }
                    }
        }
    }

    private fun generateStructure(command: Command) =
            command.expectedArgs.arguments.joinToString(" ") {
                val type = it.name
                if (it.isOptional) "($type)" else "[$type]"
            }

    private fun generateExample(event: CommandEvent<*>, command: Command) =
            command.expectedArgs.arguments.joinToString(" ") {
                it.generateExamples(event).random()
            }

    fun buildHelpEmbedForCommand(event: CommandEvent<*>, command: Command) = embed {
        val discord = event.discord
        val container = event.container

        title = command.names.joinToString()
        description = command.description
        color = infoColor

        val commandInvocation = "${discord.configuration.prefix}${command.names.first()}"

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