package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.markhc.hangoutbot.arguments.TextChannelArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Configuration
import me.markhc.hangoutbot.services.GuildConfiguration
import me.markhc.hangoutbot.services.Permission
import me.markhc.hangoutbot.services.findOrCreate
import org.jetbrains.exposed.sql.transactions.transaction

@CommandSet("WelcomeEmbeds")
fun welcomeEmbedCommands() = commands {
    requiredPermissionLevel = Permission.Administrator

    command("togglewelcomeembeds") {
        description = "Toggles the display of welcome messages upon guild user join."
        execute { event ->
            event.guild?.id?.let {
                transaction {
                    val guild = GuildConfiguration.findOrCreate(it)

                    guild.welcomeEmbeds = !guild.welcomeEmbeds

                    event.respond("Welcome embeds are now \"${if(guild.welcomeEmbeds) "enabled" else "disabled"}\"")
                }
            }
        }
    }

    command("setwelcomechannel") {
        description = "Sets the channel used for welcome embeds."
        execute(TextChannelArg("Channel")) { event ->
            event.guild?.id?.let {
                setWelcomeChannel(it, event.args.first.id)

                event.respond("Welcome channel set to #${event.args.first.name}")
            }
        }
    }

    command("getwelcomechannel") {
        description = "Gets the channel used for welcome embeds."
        execute { event ->
            event.guild?.id?.let {
                event.respond(getWelcomeChannel(it) ?: "None")
            }
        }
    }
}

fun setWelcomeChannel(guildId: String, channel: String) = transaction {
    val guild = GuildConfiguration.findOrCreate(guildId)

    guild.welcomeChannel = channel
}

fun getWelcomeChannel(guildId: String) = transaction {
    val guild = GuildConfiguration.findOrCreate(guildId)

    guild.welcomeChannel
}
