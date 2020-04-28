package me.markhc.hangoutbot.commands.information

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.command.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.utilities.buildServerInfoEmbed
import net.dv8tion.jda.api.entities.*
import java.awt.Color

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
