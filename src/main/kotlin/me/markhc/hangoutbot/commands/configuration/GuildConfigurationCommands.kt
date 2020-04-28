package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.arguments.RoleArg
import me.markhc.hangoutbot.arguments.TextChannelArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations

@CommandSet("GuildConfiguration")
@Suppress("unused")
class GuildConfigurationCommands(private val config: GuildConfigurations, private val persistence: PersistenceService) {
    fun produce() = commands {
        command("setadminrole") {
            requiredPermissionLevel = Permission.GuildOwner
            description = "Sets the role that distinguishes an Administrator"
            execute(RoleArg) {
                val (role) = it.args

                config.findGuild(it.guild!!) { adminRole = role.id }
                config.save()

                return@execute it.respond("Administrator role set to \"${role.name}\"")
            }
        }

        command("setstaffrole") {
            requiredPermissionLevel = Permission.GuildOwner
            description = "Sets the role that distinguishes an Administrator"
            execute(RoleArg) {
                val (role) = it.args

                config.findGuild(it.guild!!) { staffRole = role.id }
                config.save()

                return@execute it.respond("Staff role set to \"${role.name}\"")
            }
        }

        command("setmuterole") {
            requiredPermissionLevel = Permission.GuildOwner
            description = "Sets the role used to mute an user"
            execute(RoleArg) {
                val (role) = it.args

                config.findGuild(it.guild!!) { muteRole = role.id }
                config.save()

                return@execute it.respond("Mute role set to \"${role.name}\"")
            }
        }

        command("togglewelcome") {
            requiredPermissionLevel = Permission.Administrator
            description = "Toggles the display of welcome messages upon guild user join."
            execute {
                val guild = config.getGuildConfig(it.guild!!.id)

                config.findGuild(it.guild!!) { welcomeEmbeds = !welcomeEmbeds }
                config.save()

                it.respond("Welcome embeds are now \"${if(guild.welcomeEmbeds) "enabled" else "disabled"}\"")
            }
        }

        command("setwelcomechannel") {
            requiredPermissionLevel = Permission.Administrator
            description = "Sets the channel used for welcome embeds."
            execute(TextChannelArg("Channel")) {
                val guild = config.getGuildConfig(it.guild!!.id)

                config.findGuild(it.guild!!) { welcomeChannel = it.args.first.id }
                config.save()

                it.respond("Welcome channel set to #${it.args.first.name}")
            }
        }

        command("getwelcomechannel") {
            requiredPermissionLevel = Permission.Administrator
            description = "Gets the channel used for welcome embeds."
            execute {
                config.findGuild(it.guild!!) {
                    it.respond("Welcome channel is ${if(welcomeChannel.isEmpty()) "<None>" else "#${welcomeChannel}"}")
                }
            }
        }
    }

    private fun GuildConfigurations.save() {
        persistence.save(this)
    }
}