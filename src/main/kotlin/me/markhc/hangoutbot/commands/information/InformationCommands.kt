package me.markhc.hangoutbot.commands.information

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.markhc.hangoutbot.utilities.buildServerInfoEmbed

@CommandSet("Information")
@Suppress("unused")
class InformationCommands {
    fun produce()  = commands {
        command("ping") {
            description = "pong"
            execute {
                it.respond("Gateway ping: ${it.discord.jda.gatewayPing}")
            }
        }

        command("serverinfo") {
            description = "Display a message giving basic server information"
            execute {
                val guild = it.guild!!

                it.respond(buildServerInfoEmbed(guild))
            }
        }
    }
}
