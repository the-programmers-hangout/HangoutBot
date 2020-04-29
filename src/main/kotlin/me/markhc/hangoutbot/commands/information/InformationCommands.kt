package me.markhc.hangoutbot.commands.information

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.api.dsl.command.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.UserArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.utilities.buildRoleInfoEmbed
import me.markhc.hangoutbot.utilities.buildServerInfoEmbed
import me.markhc.hangoutbot.utilities.buildUserInfoEmbed
import me.markhc.hangoutbot.utilities.buildMemberInfoEmbed
import java.awt.Color
import kotlin.math.ceil

@Suppress("unused")
@CommandSet("Information")
fun produceInformationCommands(configurations: GuildConfigurations) = commands {
    command("help") {
        description = "Display help information."
        execute {
            it.respond(buildHelpEmbed("+", it.container))
        }
    }

    command("ping") {
        description = "pong."
        execute {
            it.respond("Gateway ping: ${it.discord.jda.gatewayPing}")
        }
    }

    command("serverinfo") {
        description = "Display a message giving basic server information."
        execute {
            val guild = it.guild!!

            it.respond(buildServerInfoEmbed(guild))
        }
    }

    command("userinfo") {
        description = "Displays information about the given user."
        execute(UserArg) {
            val (user) = it.args
            val member = it.guild?.getMember(user)
            if(member != null)
                it.respond(buildMemberInfoEmbed(member))
            else
                it.respond(buildUserInfoEmbed(user))
        }
    }

    command("roleinfo") {
        description = "Displays information about the given role."
        execute(RoleArg) {
            it.respond(buildRoleInfoEmbed(it.args.first))
        }
    }

    command("github") {
        description = "Displays information about the bot's repository."
        execute {
            it.respond("NotImplementedYet")
        }
    }

    command("uptime") {
        description = "Displays how long the bot has been running for."
        execute {
            it.respond("NotImplementedYet")
        }
    }

}

private fun buildHelpEmbed(prefix: String, container: CommandsContainer) = embed {

    title = "Help information"
    description = "Use `${prefix}help <command>` for more information"
    color = Color.green

    fun joinNames(value: List<Command>) =
            value.sortedBy { it.names.joinToString() }.joinToString("\n") { it.names.joinToString() }

    container.commands
            .groupBy { it.category }
            .map {(category, commands) ->
                when {
                    commands.size >= 6 -> { // Split into 3 columns
                        val n = ceil(commands.size / 3.0).toInt()
                        field {
                            name = "**$category**"
                            value = joinNames(commands.subList(0, n))
                            inline = true
                        }
                        field {
                            value = joinNames(commands.subList(n, n * 2))
                            inline = true
                        }
                        field {
                            value = joinNames(commands.subList(n * 2, commands.size))
                            inline = true
                        }
                    }
                    else -> {
                        field {
                            name = "**$category**"
                            value = joinNames(commands)
                            inline = true
                        }
                        field { inline = true }
                        field { inline = true }
                    }
                }
            }
}
