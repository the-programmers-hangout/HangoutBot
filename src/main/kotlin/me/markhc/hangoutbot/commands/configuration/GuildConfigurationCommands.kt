package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

@Suppress("unused")
@CommandSet("Guild")
fun produceGuildConfigurationCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    fun GuildConfigurations.save() {
        persistence.save(this)
    }

    command("setadminrole") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the role that distinguishes an Administrator"
        execute(RoleArg) {
            val (role) = it.args

            config.getGuildConfig(it.guild!!).apply { adminRole = role.id }
            config.save()

            return@execute it.respond("Administrator role set to \"${role.name}\"")
        }
    }

    command("setstaffrole") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the role that distinguishes an Administrator"
        execute(RoleArg) {
            val (role) = it.args

            config.getGuildConfig(it.guild!!).apply { staffRole = role.id }
            config.save()

            return@execute it.respond("Staff role set to \"${role.name}\"")
        }
    }

    command("setmuterole") {
        requiredPermissionLevel = Permission.GuildOwner
        description = "Sets the role used to mute an user"
        execute(RoleArg) {
            val (role) = it.args

            config.getGuildConfig(it.guild!!).apply { muteRole = role.id }
            config.save()

            return@execute it.respond("Mute role set to \"${role.name}\"")
        }
    }

    command("togglewelcome") {
        requiredPermissionLevel = Permission.Administrator
        description = "Toggles the display of welcome messages upon guild user join."
        execute {
            val guild = config.getGuildConfig(it.guild!!.id)

            config.getGuildConfig(it.guild!!).apply { welcomeEmbeds = !welcomeEmbeds }
            config.save()

            it.respond("Welcome embeds are now \"${if(guild.welcomeEmbeds) "enabled" else "disabled"}\"")
        }
    }

    command("setwelcomechannel") {
        requiredPermissionLevel = Permission.Administrator
        description = "Sets the channel used for welcome embeds."
        execute(TextChannelArg("Channel")) {
            val guild = config.getGuildConfig(it.guild!!.id)

            config.getGuildConfig(it.guild!!).apply { welcomeChannel = it.args.first.id }
            config.save()

            it.respond("Welcome channel set to #${it.args.first.name}")
        }
    }

    command("getwelcomechannel") {
        requiredPermissionLevel = Permission.Administrator
        description = "Gets the channel used for welcome embeds."
        execute {
            config.getGuildConfig(it.guild!!).apply {
                it.respond("Welcome channel is ${if(welcomeChannel.isEmpty()) "<None>" else "#${welcomeChannel}"}")
            }
        }
    }

    command("makerolegrantable") {
        requiredPermissionLevel = Permission.Administrator
        description = "Adds a role to the list of grantable roles."
        execute(RoleArg, WordArg("Category")) { event ->
            val (role, category) = event.args

            config.getGuildConfig(event.guild!!).apply {
                if (grantableRoles.any { it.value.contains(role.id) }) {
                    return@execute event.respond("Role is already grantable")
                } else {
                    val key = grantableRoles.keys.find {
                        it.compareTo(category, true) == 0
                    }

                    if (key == null) {
                        grantableRoles[category] = mutableListOf(role.id);
                    } else {
                        grantableRoles[key]!!.add(role.id)
                    }

                    event.respond("Added \"${role.name}\" to the category \"${key ?: category}\".")
                    config.save()
                }
            }
        }
    }

    command("removegrantablerole") {
        requiredPermissionLevel = Permission.Administrator
        description = "Removes a role to the list of grantable roles."
        execute(RoleArg) { event ->
            val (role) = event.args

            config.getGuildConfig(event.guild!!).apply {
                val entry = grantableRoles.entries.find {
                    it.value.contains(role.id)
                } ?: return@execute event.respond("Role ${role.name} is not a grantable role.")

                entry.value.remove(role.id)

                if (entry.value.isEmpty()) {
                    grantableRoles.remove(entry.key)
                }

                config.save()

                event.respond("Removed \"${role.name}\" from the list of grantable roles.")
            }
        }
    }

    command("listgrantableroles") {
        requiredPermissionLevel = Permission.Staff
        description = "Lists the available grantable roles."
        execute { event ->
            val guildConfig = config.getGuildConfig(event.guild!!.id)

            if (guildConfig.grantableRoles.isEmpty()) return@execute event.respond("No roles set")

            event.respond(buildRolesEmbed(event.guild!!, guildConfig.grantableRoles))
        }
    }
}

private fun buildRolesEmbed(guild: Guild, roles: Map<String, List<String>>): MessageEmbed {
    return embed {
        title = "Grantable roles"
        color = Color.CYAN

        roles.iterator().forEach {
            addInlineField(
                    name = it.key,
                    value = (it.value as List<*>).filterIsInstance<String>().map {id -> guild.getRoleById(id)?.name }.joinToString("\n"))
        }

    }
}