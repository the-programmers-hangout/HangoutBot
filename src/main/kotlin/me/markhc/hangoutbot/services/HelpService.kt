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
    fun isCommandVisible(visibilityContext: VisibilityContext) =
        visibilityContext.command.isVisible(visibilityContext.guild, visibilityContext.user)

    private fun Command.isVisible(guild: Guild?, user: User): Boolean {
        guild ?: return false

        val member = user.toMember(guild)!!
        val permission = requiredPermissionLevel

        return permissionsService.hasClearance(member, permission)
    }

    fun buildHelpEmbed(event: CommandEvent<*>) = embed {
        val discord = event.discord
        val container = event.container

        title = "Help information"
        description = "Use `${discord.configuration.prefix}help <command>` for more information"
        color = infoColor

        fun joinNames(value: List<Command>) =
                value.sortedBy { it.names.joinToString() }.joinToString("\n") { it.names.joinToString() }

        container.commands
                .filter { it.isVisible(event.guild, event.author) }
                .takeIf { it.isNotEmpty() }
                ?.groupBy { it.category }
                ?.map {(category, commands) ->
                    val sorted = commands
                            .sortedByDescending { it.names.joinToString() }
                    when {
                        sorted.size >= 6 -> { // Split into 3 columns
                            val n = sorted.size / 3
                            field {
                                name = "**$category**"
                                value = joinNames(sorted.subList(n * 2, sorted.size))
                                inline = true
                            }
                            field {
                                value = joinNames(sorted.subList(n, n * 2))
                                inline = true
                            }
                            field {
                                value = joinNames(sorted.subList(0, n))
                                inline = true
                            }
                        }
                        else -> {
                            field {
                                name = "**$category**"
                                value = joinNames(sorted)
                                inline = true
                            }
                            field { inline = true }
                            field { inline = true }
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