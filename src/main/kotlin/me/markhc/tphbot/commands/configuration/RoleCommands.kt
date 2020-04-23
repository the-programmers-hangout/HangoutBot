package me.markhc.tphbot.commands.configuration

import me.markhc.tphbot.arguments.LowerRankedUserArg
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.command.SingleArg
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
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
import net.dv8tion.jda.api.entities.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.StringReader

@CommandSet("RoleCommands")
fun configurationCommands(configuration: Configuration) = commands {
    command("AddGrantableRole") {
        requiredPermissionLevel = Permission.Administrator
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
        requiredPermissionLevel = Permission.Administrator
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
        requiredPermissionLevel = Permission.Staff
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

    command("grant") {
        requiredPermissionLevel = Permission.Staff
        description = "Grants a role to a lower ranked member or yourself"
        execute(LowerRankedUserArg("Member").makeOptional { it.author }, SentenceArg("Role Name")) { event ->
            val (user, roleName) = event.args

            val guild = event.guild
                    ?: return@execute
            val member = guild.getMember(user)
                    ?: return@execute event.respond("\"${user.name}\" is not a member of this guild")
            val role  = guild.getRoleByName(roleName, true)
                    ?: return@execute event.respond("\"${roleName}\" is not a valid role")

            val grantableRoles = guild.id.let { transaction { GuildConfiguration.findOrCreate(it) }.grantableRoles }
            val rolesConfig = Klaxon().parseJsonObject(StringReader(grantableRoles))

            rolesConfig.forEach {category ->
                val roles = (category.value as List<*>).filterIsInstance<String>()
                if(roleName in roles) {
                    return@execute removeRoles(guild, member, roles).also {
                        grantRole(guild, member, role)
                    }
                }
            }

            event.respond("\"$roleName\" is not a grantable role")
        }
    }

    command("revoke") {
        requiredPermissionLevel = Permission.Staff
        description = "Revokes a role from a lower ranked member or yourself"
        execute(LowerRankedUserArg("Member").makeOptional { it.author }, SentenceArg("Role Name")) { event ->
            val (user, roleName) = event.args

            val guild = event.guild
                    ?: return@execute
            val member = guild.getMember(user)
                    ?: return@execute event.respond("\"${user.name}\" is not a member of this guild")
            val role  = guild.getRoleByName(roleName, true)
                    ?: return@execute event.respond("\"${roleName}\" is not a valid role")

            val grantableRoles = guild.id.let { transaction { GuildConfiguration.findOrCreate(it) }.grantableRoles }
            val rolesConfig = Klaxon().parseJsonObject(StringReader(grantableRoles))

            rolesConfig.forEach {category ->
                val roles = (category.value as List<*>).filterIsInstance<String>()
                if(roleName in roles) {
                    return@execute removeRoles(guild, member, roles)
                }
            }

            event.respond("\"$roleName\" is not a revokable role")
        }
    }
}

fun buildRolesEmbed(roles: JsonObject): MessageEmbed  {
    return embed {
        title = "Grantable roles"
        color = Color.CYAN

        roles.iterator().forEach {
            addField(
                    name = it.key,
                    value = (it.value as List<*>).filterIsInstance<String>().joinToString("\n"))
        }

    }
}

private fun removeRoles(guild: Guild, member: Member, roles: List<String>) {
    // TODO: Perhaps we should check if the user has more than 1 color role
    //       and remove all of them instead of just 1
    member.roles.find { it.name in roles }?.let {
        guild.removeRoleFromMember(member, it).queue()
    }
}

private fun grantRole(guild: Guild, member: Member, role: Role) {
    guild.addRoleToMember(member, role).queue()
}
