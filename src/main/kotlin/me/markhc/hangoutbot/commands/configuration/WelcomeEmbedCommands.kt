package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.arguments.TextChannelArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.Permission

@CommandSet("WelcomeEmbeds")
fun welcomeEmbedCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    command("togglewelcome") {
        requiredPermissionLevel = Permission.Administrator
        description = "Toggles the display of welcome messages upon guild user join."
        execute {
            val guild = config.getGuildConfig(it.guild!!.id)

            guild.welcomeEmbeds = !guild.welcomeEmbeds
            persistence.save(config)

            it.respond("Welcome embeds are now \"${if(guild.welcomeEmbeds) "enabled" else "disabled"}\"")
        }
    }

    command("setwelcomechannel") {
        requiredPermissionLevel = Permission.Administrator
        description = "Sets the channel used for welcome embeds."
        execute(TextChannelArg("Channel")) {
            val guild = config.getGuildConfig(it.guild!!.id)

            guild.welcomeChannel = it.args.first.id
            persistence.save(config)

            it.respond("Welcome channel set to #${it.args.first.name}")
        }
    }

    command("getwelcomechannel") {
        requiredPermissionLevel = Permission.Administrator
        description = "Gets the channel used for welcome embeds."
        execute {
            val guild = config.getGuildConfig(it.guild!!.id)

            it.respond("Welcome channel is ${if(guild.welcomeChannel.isEmpty()) "<None>" else "#${guild.welcomeChannel}"}")
        }
    }
}
