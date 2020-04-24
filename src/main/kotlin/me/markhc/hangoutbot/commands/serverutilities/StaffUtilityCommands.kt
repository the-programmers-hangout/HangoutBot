package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.arguments.RoleArg
import me.markhc.hangoutbot.arguments.TextChannelArg
import me.markhc.hangoutbot.commands.configuration.buildRolesEmbed
import me.markhc.hangoutbot.commands.configuration.containsIgnoreCase
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.locale.Messages
import me.markhc.hangoutbot.services.Permission
import net.dv8tion.jda.api.entities.*
import java.text.SimpleDateFormat

@CommandSet("StaffUtility")
fun staffUtilityCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    requiredPermissionLevel = Permission.Staff

    command("echo") {
        description = "Echo a message to a channel."
        execute(TextChannelArg.makeOptional { it.channel as TextChannel }, SentenceArg) {
            val (target, message) = it.args

            target.sendMessage(message).queue()
        }
    }

    command("nuke") {
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        execute(TextChannelArg.makeOptional { it.channel as TextChannel },
                IntegerArg) {
            val (channel, amount) = it.args

            if (amount !in 2..99) {
                return@execute it.respond("You can only nuke between 2 and 99 messages")
            }

            val sameChannel = it.channel.id == channel.id
            val singlePrefixInvocationDeleted = it.stealthInvocation

            channel.history.retrievePast(amount + if (sameChannel) 1 else 0).queue { past ->
                val noSinglePrefixMsg = past.drop(if (sameChannel && singlePrefixInvocationDeleted) 1 else 0)

                safeDeleteMessages(channel, noSinglePrefixMsg)

                channel.sendMessage("Be nice. No spam.").queue()

                if (!sameChannel) it.respond("$amount messages deleted.")
            }
        }
    }

    command("listgrantableroles") {
        requiredPermissionLevel = Permission.Staff
        description = "Lists the available grantable roles."
        execute { event ->
            val guildId = event.guild?.id ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guildId)

            if(guildConfig.grantableRoles.isEmpty()) return@execute event.respond("No roles set")

            event.respond(buildRolesEmbed(guildConfig.grantableRoles))
        }
    }

    command("grant") {
        requiredPermissionLevel = Permission.Staff
        description = "Grants a role to a lower ranked member or yourself"
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args

            val guild = event.guild ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guild.id)

            guildConfig.grantableRoles.forEach {category ->
                if(containsIgnoreCase(category.value, role.name)) {
                    return@execute removeRoles(guild, member, category.value).also {
                        grantRole(guild, member, role)
                    }
                }
            }

            event.respond("\"$role.name\" is not a grantable role")
        }
    }

    command("revoke") {
        requiredPermissionLevel = Permission.Staff
        description = "Revokes a role from a lower ranked member or yourself"
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args

            val guild = event.guild
                    ?: return@execute event.respond(Messages.COMMAND_NOT_SUPPORTED_IN_DMS)

            val guildConfig = config.getGuildConfig(guild.id)

            guildConfig.grantableRoles.forEach {category ->
                if(containsIgnoreCase(category.value, role.name)) {
                    return@execute removeRoles(guild, member, category.value)
                }
            }

            event.respond("\"${role.name}\" is not a grantable role")
        }
    }
}

private fun safeDeleteMessages(channel: TextChannel,
                       messages: List<Message>) {
    try {
        channel.deleteMessages(messages).queue()
    } catch (e: IllegalArgumentException) { // some messages older than 2 weeks => can't mass delete
        messages.forEach { it.delete().queue() }
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
