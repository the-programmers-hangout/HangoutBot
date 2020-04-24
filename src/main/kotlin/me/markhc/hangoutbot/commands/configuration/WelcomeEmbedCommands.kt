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
    requiredPermissionLevel = Permission.Administrator

    command("togglewelcome") {
        description = "Toggles the display of welcome messages upon guild user join."
        execute {
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)
            val guild = config.getGuildConfig(guildId)

            guild.welcomeEmbeds = !guild.welcomeEmbeds
            persistence.save(config)

            it.respond("Welcome embeds are now \"${if(guild.welcomeEmbeds) "enabled" else "disabled"}\"")
        }
    }

    command("setwelcomechannel") {
        description = "Sets the channel used for welcome embeds."
        execute(TextChannelArg("Channel")) {
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)
            val guild = config.getGuildConfig(guildId)

            guild.welcomeChannel = it.args.first.name
            persistence.save(config)

            it.respond("Welcome channel set to #${it.args.first.name}")
        }
    }

    command("getwelcomechannel") {
        description = "Gets the channel used for welcome embeds."
        execute {
            val guildId = it.guild?.id ?: return@execute it.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)
            val guild = config.getGuildConfig(guildId)

            it.respond("Welcome channel is ${if(guild.welcomeChannel.isEmpty()) "<None>" else "#${guild.welcomeChannel}"}")
        }
    }
}
