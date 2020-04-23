package me.markhc.tphbot.commands

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.getRoleByName
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.markhc.tphbot.extensions.requiredPermissionLevel
import me.markhc.tphbot.services.Configuration
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.Permission
import me.markhc.tphbot.services.findOrCreate
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.StringReader

@CommandSet("Configuration")
fun configurationCommands(configuration: Configuration) = commands {

    requiredPermissionLevel = Permission.Administrator

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
        execute(TextChannelArg("Channel")) { event ->
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

    command("AddGrantableRole") {
        description = "Adds a role to the list of grantable roles."
        execute(WordArg("Category"), SentenceArg("RoleName")) { event ->
            val (category, role) = event.args

            val guild = event.guild ?: return@execute

            guild.getRoleByName(role, true) ?: return@execute event.respond("Invalid role.")

            transaction {
                val guildConfig = GuildConfiguration.findOrCreate(guild.id)
                val roles = Klaxon().parseJsonObject(StringReader(guildConfig.grantableRoles))

                if(roles[category] == null) {
                    roles[category] = listOf(role);
                } else {
                    val list = (roles[category] as List<*>)
                            .filterIsInstance<String>()
                            .toMutableList()
                    list.add(role)
                    roles[category] = list;
                }

                guildConfig.grantableRoles = Klaxon().toJsonString(roles)

                event.respond("Added \"$role\" to the category \"$category\".")
            }
        }
    }

    command("RemoveGrantableRole") {
        description = "Removes a role to the list of grantable roles."
        execute(WordArg("Category"), SentenceArg("RoleName")) { event ->
            val (category, role) = event.args

            val guild = event.guild ?: return@execute

            guild.getRoleByName(role, true) ?: return@execute event.respond("Invalid role.")

            transaction {
                val guildConfig = GuildConfiguration.findOrCreate(guild.id)
                val roles = Klaxon().parseJsonObject(StringReader(guildConfig.grantableRoles))

                if(roles[category] == null) {
                    return@transaction event.respond("\"$category\" has no role named \"$role\"")
                } else {
                    val list = (roles[category] as List<*>)
                            .filterIsInstance<String>()
                            .toMutableList()
                    list.remove(role)
                    roles[category] = list;
                }

                guildConfig.grantableRoles = Klaxon().toJsonString(roles)

                event.respond("Removed \"$role\" from the category \"$category\".")
            }
        }
    }

    command("ListGrantableRoles") {
        description = "Lists the available grantable roles."
        execute { event ->
            event.guild ?: return@execute

            val roles = event.guild!!.id.let {
                transaction {
                    GuildConfiguration.findOrCreate(it).grantableRoles
                }.let {
                    Klaxon().parseJsonObject(StringReader(it))
                }
            }

            if(roles.isEmpty()) return@execute event.respond("No roles set")

            event.respond(buildRolesEmbed(roles))
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

fun buildRolesEmbed(roles: JsonObject) =
        embed {
            title = "Grantable roles"
            color = Color.CYAN

            roles.iterator().forEach {
                addField(
                        name = it.key,
                        value = (it.value as List<*>).filterIsInstance<String>().joinToString("\n"))
            }

        }