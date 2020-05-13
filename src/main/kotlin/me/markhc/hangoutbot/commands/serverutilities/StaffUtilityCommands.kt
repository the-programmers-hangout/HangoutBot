package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.arguments.GuildTextChannelArg
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.extensions.addRole
import me.markhc.hangoutbot.extensions.removeRole
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.*
import net.dv8tion.jda.api.entities.*
import java.awt.Color

@Suppress("unused")
@CommandSet("StaffUtility")
fun produceStaffUtilityCommands(persistentData: PersistentData,
                                colorService: ColorService) = commands {
    command("echo") {
        requiredPermissionLevel = PermissionLevel.Staff
        description = "Echo a message to a channel."
        execute(GuildTextChannelArg.makeOptional { it.channel as TextChannel }, SentenceArg) {
            val (target, message) = it.args

            target.sendMessage(message).queue()
        }
    }

    command("nuke") {
        requiredPermissionLevel = PermissionLevel.Staff
        description = "Delete 2 - 99 past messages in the given channel (default is the invoked channel)"
        execute(GuildTextChannelArg.makeOptional { it.channel as TextChannel },
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
        description = "Lists the available grantable roles."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            persistentData.getGuildProperty(event.guild!!) {
                if (grantableRoles.isEmpty()) {
                    event.respond("No roles set")
                } else {
                    val responseEmbed = embed {
                        title = "Grantable roles"
                        color = infoColor

                        grantableRoles.entries.forEach {
                            field {
                                name = it.key
                                value = it.value.joinToString("\n") { id -> event.guild!!.getRoleById(id)?.name ?: id }
                            }
                        }
                    }

                    event.respond(responseEmbed)
                }
            }
        }
    }

    command("grant") {
        requiredPermissionLevel = PermissionLevel.Staff
        description = "Grants a role to a lower ranked member or yourself"
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args
            val guild = event.guild!!

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }

            if(roles.values.any { r -> r.contains(role.id) }) {
                member.addRole(role).queue {
                    event.respond("Granted ${role.name} to ${member.fullName()}")
                }
            } else {
                event.respond("${role.name} is not a grantable role")
            }
        }
    }

    command("revoke") {
        requiredPermissionLevel = PermissionLevel.Staff
        description = "Revokes a role from a lower ranked member or yourself"
        execute(LowerRankedMemberArg("Member").makeOptional { it.guild!!.getMember(it.author)!! }, RoleArg("GrantableRole")) { event ->
            val (member, role) = event.args
            val guild = event.guild!!

            val roles = persistentData.getGuildProperty(guild) { grantableRoles }

            val isGrantable = roles.any { it.value.any { r -> r.equals(role.id, true) } }

            if(isGrantable) {
                member.removeRole(role).queue {
                    event.respond("Revoked ${role.name} from ${member.fullName()}")
                }
            } else {
                event.respond("${role.name} is not a grantable role")
            }
        }
    }

    val commandsInExecution = mutableMapOf<Long, Boolean>()

    command("setcolor") {
        description = "Creates a role with the given name and color and assigns it to the user."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(HexColorArg("HexColor").makeNullableOptional(), SentenceArg("RoleName")) { event ->
            val (color, roleName) = event.args

            val guild = event.guild!!
            val member = guild.getMember(event.author)!!

            if(commandsInExecution[guild.idLong] == true)
                return@execute event.respond("Cannot do that right now")

            commandsInExecution[guild.idLong] = true

            val message = event.channel.sendMessage("Working...").complete()

            runCatching {
                colorService.setMemberColor(member, roleName, color)
            }.onSuccess {
                message.editMessage("Successfully assigned color $roleName").queue()
            }.onFailure {
                message.editMessage(it.message!!).queue()
            }
            commandsInExecution[guild.idLong] = false
        }
    }

    command("clearcolor") {
        description = "Clears the current color role."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            val member = event.guild!!.getMember(event.author)!!

            colorService.clearMemberColor(member)

            event.respond("Cleared user color")
        }
    }

    command("listcolors") {
        description = "Creates a role with the given name and color and assigns it to the user."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            val colorRoles = persistentData.getGuildProperty(event.guild!!) { assignedColorRoles }

            if (colorRoles.isEmpty()) {
                return@execute event.respond("No colors set. For more information, see `${event.discord.configuration.prefix}help setcolor`")
            }

            val colorInfo = colorRoles.map {
                event.guild!!.getRoleById(it.key)
            }.filterNotNull().sortedBy {
                val rgb = it.color!!

                val hsv = Color.RGBtoHSB(rgb.red, rgb.green, rgb.blue, null)

                hsv[0]
            }.joinToString("\n") { it.asMention }


            event.respond(embed {
                title = "Currently used colors"
                description = "Run `setcolor <name>` to use one of the colors here.\n" +
                        "Run `setcolor <hexcolor> <name>` to create a new color."
                color = infoColor

                field {
                    name = "Colors"
                    value = colorInfo
                }
            })
        }
    }

    command("listroles") {
        description = "List all the roles available in the guild."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            val guild = event.guild!!

            val message = event.channel.sendMessage("Working...").complete()

            guild.retrieveMembers().thenRun {
                val messages = buildRolelistMessages(guild)

                message.editMessage(messages.first()).queue()
                for(i in 1 until messages.size) {
                    event.channel.sendMessage(messages[i]).queue()
                }
            }
        }
    }

    command("deleterole") {
        description = "Deletes the given role or roles."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(MultipleArg(RoleArg)) { event ->
            event.args.first.distinct().forEach { role ->
                role.delete().queue(
                        { event.respond("Deleted role ${role.name}") },
                        { event.respond("Failed to delete role ${role.name}") }
                )
            }
        }
    }
  
  command("setslowmode") {
        description = "Set slowmode in a channel."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(GuildTextChannelArg, TimeStringArg) {
            val (channel, interval) = it.args

            if (interval > 21600 || interval < 0) {
                return@execute it.respond("Invalid time element passed.")
            }

            val res = it

            channel.manager.setSlowmode(interval.toInt()).queue {
                res.respond("Successfully set slow-mode in channel ${channel
                    .asMention} to ${interval.toInt()} seconds.")
            }
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

/**
 *  @brief Builds the list of messages required to display the roles
 *
 *  The role list may be too big to send in a single message.
 *  This function returns a list of messages that should be sent.
 *
 *  @param guild The target guild
 *
 *  @return The list of messages to send
 */
private fun buildRolelistMessages(guild: Guild): List<String> {
    val list = guild.roles.map {
        "${it.id} - ${it.name}: ${guild.getMembersWithRoles(it).size} users"
    }

    // Try joining them in a single message
    val response = list.joinToString("\n")
    return if(response.length < 1990) {
        // If the length is less than the max, we good.
        listOf("```\n$response\n```")
    } else {
        // Otherwise, break it into multiple messages
        val result = mutableListOf<String>()
        var data = "```\n"
        for(i in list.indices) {
            if(data.length + list[i].length < 1990) {
                data += list[i] + '\n'
            } else {
                result.add("$data```")
                data = "```\n${list[i]}\n"
            }
        }
        result.add("$data```")
        result
    }
}
