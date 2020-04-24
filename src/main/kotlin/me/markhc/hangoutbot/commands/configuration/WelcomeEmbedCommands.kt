package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Configuration
import me.markhc.hangoutbot.services.GuildConfiguration
import me.markhc.hangoutbot.services.Permission
import me.markhc.hangoutbot.services.findOrCreate
import org.jetbrains.exposed.sql.transactions.transaction


@CommandSet("WelcomeEmbeds")
fun welcomeEmbedCommands(configuration: Configuration) = commands {
    requiredPermissionLevel = Permission.Administrator

    command("enableWelcomeEmbed") {
        description = "Enables the display of welcome messages upon guild user join."
        execute { event ->
            event.guild?.id?.let {
                setWelcomeEmbed(it, true)
            }
        }
    }

    command("disableWelcomeEmbed") {
        description = "Disables the display of welcome messages upon guild user join."
        execute { event ->
            event.guild?.id?.let {
                setWelcomeEmbed(it, false)
            }
        }
    }

    command("setWelcomeChannel") {
        description = "Sets the channel used for welcome embeds."
        execute(TextChannelArg("Channel")) { event ->
            event.guild?.id?.let {
                setWelcomeChannel(it, event.args.first.id)
            }
        }
    }

    command("getWelcomeChannel") {
        description = "Gets the channel used for welcome embeds."
        execute { event ->
            event.guild?.id?.let {
                event.respond(getWelcomeChannel(it) ?: "None")
            }
        }
    }
}

fun setWelcomeEmbed(guildId: String, enable: Boolean) {
    transaction {
        val guild = GuildConfiguration.findOrCreate(guildId)

        if(guild.welcomeChannel != null) {
            guild.welcomeEmbeds = enable
        }
        return@transaction guild.welcomeEmbeds
    }
}

fun setWelcomeChannel(guildId: String, channel: String) {
    return transaction {
        val guild = GuildConfiguration.findOrCreate(guildId)

        guild.welcomeChannel = channel
    }
}

fun getWelcomeChannel(guildId: String): String? {
    return transaction {
        val guild = GuildConfiguration.findOrCreate(guildId)

        guild.welcomeChannel
    }
}
