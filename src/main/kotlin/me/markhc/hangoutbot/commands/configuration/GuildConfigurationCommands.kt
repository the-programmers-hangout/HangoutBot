package me.markhc.hangoutbot.commands.configuration

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.services.ConversationService
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

@Suppress("unused")
@CommandSet("Guild")
fun produceGuildConfigurationCommands(config: Configuration, persistence: PersistenceService, conversationService: ConversationService) = commands {
    fun Configuration.save() {
        persistence.save(this)
    }

    command("setadminrole") {
        description = "Sets the role that distinguishes an Administrator"
        requiredPermissionLevel = Permission.GuildOwner
        requiresGuild = true
        execute(RoleArg) {
            val (role) = it.args

            config.getGuildConfig(it.guild!!).apply { adminRole = role.id }
            config.save()

            return@execute it.respond("Administrator role set to \"${role.name}\"")
        }
    }

    command("setstaffrole") {
        description = "Sets the role that distinguishes an Administrator"
        requiredPermissionLevel = Permission.GuildOwner
        requiresGuild = true
        execute(RoleArg) {
            val (role) = it.args

            config.getGuildConfig(it.guild!!).apply { staffRole = role.id }
            config.save()

            return@execute it.respond("Staff role set to \"${role.name}\"")
        }
    }

    command("setmuterole") {
        description = "Sets the role used to mute an user"
        requiredPermissionLevel = Permission.GuildOwner
        requiresGuild = true
        execute(RoleArg) {
            val (role) = it.args

            config.getGuildConfig(it.guild!!).apply { muteRole = role.id }
            config.save()

            return@execute it.respond("Mute role set to \"${role.name}\"")
        }
    }

    command("togglewelcome") {
        description = "Toggles the display of welcome messages upon guild user join."
        requiredPermissionLevel = Permission.Administrator
        requiresGuild = true
        execute {
            val guild = config.getGuildConfig(it.guild!!.id)

            config.getGuildConfig(it.guild!!).apply { welcomeEmbeds = !welcomeEmbeds }
            config.save()

            it.respond("Welcome embeds are now \"${if(guild.welcomeEmbeds) "enabled" else "disabled"}\"")
        }
    }

    command("setwelcomechannel") {
        description = "Sets the channel used for welcome embeds."
        requiredPermissionLevel = Permission.Administrator
        requiresGuild = true
        execute(TextChannelArg("Channel")) {
            config.getGuildConfig(it.guild!!).apply { welcomeChannel = it.args.first.id }
            config.save()

            it.respond("Welcome channel set to #${it.args.first.name}")
        }
    }

    command("getwelcomechannel") {
        description = "Gets the channel used for welcome embeds."
        requiredPermissionLevel = Permission.Administrator
        requiresGuild = true
        execute {
            config.getGuildConfig(it.guild!!).apply {
                if(welcomeChannel.isEmpty())
                    it.respond("Welcome channel not set")
                else
                    it.respond("Welcome channel is #${it.guild!!.getGuildChannelById(welcomeChannel)!!.name}")
            }
        }
    }

    command("makerolegrantable") {
        description = "Adds a role to the list of grantable roles."
        requiredPermissionLevel = Permission.Administrator
        requiresGuild = true
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
        description = "Removes a role to the list of grantable roles."
        requiredPermissionLevel = Permission.Administrator
        requiresGuild = true
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
        description = "Lists the available grantable roles."
        requiredPermissionLevel = Permission.Staff
        requiresGuild = true
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