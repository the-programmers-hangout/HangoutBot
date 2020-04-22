package me.markhc.tphbot.commands

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.markhc.tphbot.extensions.requiredPermissionLevel
import me.markhc.tphbot.services.Configuration
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.Permission
import me.markhc.tphbot.services.findOrCreate
import org.jetbrains.exposed.sql.transactions.transaction

@CommandSet("Configuration")
fun configurationCommands(configuration: Configuration) = commands {

    requiredPermissionLevel = Permission.Staff

    command("EnableWelcomeEmbed") {
        description = "Enables the display of welcome messages upon guild user join."
        execute { event ->
            event.guild?.id?.let {
                setWelcomeEmbed(it, true)
            }
        }
    }

    command("DisableWelcomeEmbed") {
        description = "Disables the display of welcome messages upon guild user join."
        execute { event ->
            event.guild?.id?.let {
                setWelcomeEmbed(it, false)
            }
        }
    }

    command("SetWelcomeChannel") {
        description = "Sets the channel used for welcome embeds."
        execute(TextChannelArg) { event ->
            event.guild?.id?.let {
                setWelcomeChannel(it, event.args.first.id)
            }
        }
    }

    command("GetWelcomeChannel") {
        description = "Gets the channel used for welcome embeds."
        execute { event ->
            event.guild?.id?.let {
                event.respond(getWelcomeChannel(it) ?: "None")
            }
        }
    }
}

fun setWelcomeEmbed(guildId: String, enable: Boolean) {
    return transaction {
        val guild = GuildConfiguration.findOrCreate(guildId)

        if(guild.welcomeChannel != null) {
            guild.welcomeEmbeds = enable
        }
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