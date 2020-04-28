package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.arguments.RoleArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import java.awt.Color

@CommandSet("Roles")
@Suppress("unused")
class RoleCommands(private val config: GuildConfigurations, private val persistence: PersistenceService) {
    fun produce() = commands {
        command("makerolegrantable") {
            requiredPermissionLevel = Permission.Administrator
            description = "Adds a role to the list of grantable roles."
            execute(RoleArg, WordArg("Category")) { event ->
                val (role, category) = event.args

                config.findGuild(event.guild!!) {
                    if (grantableRoles.any { it.value.contains(role.id) }) {
                        return@findGuild event.respond("Role is already grantable")
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

                config.findGuild(event.guild!!) {
                    val entry = grantableRoles.entries.find {
                        it.value.contains(role.id)
                    } ?: return@findGuild event.respond("Role ${role.name} is not a grantable role.")

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

        command("grant") {
            requiredPermissionLevel = Permission.Staff
            description = "Grants a role to a lower ranked member or yourself"
            execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
                val (member, role) = event.args
                val guild = event.guild!!
                val guildConfig = config.getGuildConfig(guild.id)

                guildConfig.grantableRoles.forEach { category ->
                    if (containsIgnoreCase(category.value, role.id)) {
                        return@execute removeRoles(guild, member, category.value).also {
                            grantRole(guild, member, role)
                            event.respond("Granted \"${role.name}\" to ${member.fullName()}")
                        }
                    }
                }

                event.respond("\"${role.name}\" is not a grantable role")
            }
        }

        command("revoke") {
            requiredPermissionLevel = Permission.Staff
            description = "Revokes a role from a lower ranked member or yourself"
            execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
                val (member, role) = event.args
                val guild = event.guild!!
                val guildConfig = config.getGuildConfig(guild.id)

                guildConfig.grantableRoles.forEach { category ->
                    if (containsIgnoreCase(category.value, role.id)) {
                        removeRoles(guild, member, category.value)
                        return@execute event.respond("Revoked \"${role.name}\" from ${member.fullName()}")
                    }
                }

                event.respond("\"${role.name}\" is not a grantable role")
            }
        }
    }

    private fun GuildConfigurations.save() {
        persistence.save(this)
    }

    private fun removeRoles(guild: Guild, member: Member, roles: List<String>) {
        // TODO: Perhaps we should check if the user has more than 1 color role
        //       and remove all of them instead of just 1
        member.roles.find { it.id in roles }?.let {
            guild.removeRoleFromMember(member, it).queue()
        }
    }

    private fun grantRole(guild: Guild, member: Member, role: Role) {
        guild.addRoleToMember(member, role).queue()
    }


    private fun containsIgnoreCase(list: List<String>, value: String): Boolean {
        list.forEach { item ->
            if(item.compareTo(value, true) == 0) {
                return true
            }
        }
        return false
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
}