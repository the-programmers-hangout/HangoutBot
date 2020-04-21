package me.markhc.tphbot.commands

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.markhc.tphbot.services.Configuration
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.findGuild

@CommandSet("Configuration")
fun configurationCommands(configuration: Configuration) = commands {
    command("EnableWelcomeEmbed") {
        description = "Enables the display of welcome messages upon guild user join."
        execute {
           setWelcomeEmbed(configuration, it.guild?.id, true)
        }
    }
    command("DisableWelcomeEmbed") {
        description = "Enables the display of welcome messages upon guild user join."
        execute {
            setWelcomeEmbed(configuration, it.guild?.id, false)
        }
    }
}

fun setWelcomeEmbed(configuration: Configuration, guildId: String?, enable: Boolean) {
    if(guildId != null) {
        GuildConfiguration.findGuild(guildId) {
            if(it != null)
                it[GuildConfiguration.welcomeEmbeds] = enable
        }
    }
}