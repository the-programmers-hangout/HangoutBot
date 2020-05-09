package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.*
import net.dv8tion.jda.api.entities.*

@Suppress("unused")
@CommandSet("StaffUtility")
fun produceStaffUtilityCommands(persistentData: PersistentData,
                                colorService: ColorService) = commands {
    command("echo") {
        requiredPermissionLevel = PermissionLevel.Staff
        description = "Echo a message to a channel."
        execute(TextChannelArg.makeOptional { it.channel as TextChannel }, SentenceArg) {
            val (target, message) = it.args

            target.sendMessage(message).queue()
        }
    }

    command("nuke") {
        requiredPermissionLevel = PermissionLevel.Staff
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
        description = "Lists the available grantable roles."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute {
            persistentData.getGuildProperty(it.guild!!) {
                if (grantableRoles.isEmpty()) {
                    it.respond("No roles set")
                } else {
                    it.respond(buildRolesEmbed(it.guild!!, grantableRoles))
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
            val category = roles.asIterable().find { it.value.any { r -> r.equals(role.id, true) } }

            category?.also { removeRoles(guild, member, *it.value.toTypedArray()) }
                    ?.also { grantRole(guild, member, role) }
                    ?.also { event.respond("Granted \"${role.name}\" to ${member.fullName()}") }
                    ?: event.respond("\"${role.name}\" is not a grantable role")
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
                removeRoles(guild, member, role.id)
                event.respond("Revoked \"${role.name}\" from ${member.fullName()}")
            } else {
                event.respond("\"${role.name}\" is not a grantable role")
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

            runCatching {
                colorService.setMemberColor(member, roleName, color)
            }.onSuccess {
                event.respond("Successfully assigned color $roleName")
            }.onFailure {
                event.respond(it.message!!)
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
                event.guild!!.getRoleById(it.key)?.asMention ?: it.key
            }.sortedBy { it.length }.joinToString("\n")

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
        description = "List all the roles available in the guild. Number of users might not be accurate as it only includes cached users."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            val guild = event.guild!!

            val message = event.channel.sendMessage("Working...").complete()

            guild.retrieveMembers().thenApply {
                val list = guild.roles.map {
                    "${it.id} - ${it.name}: ${guild.getMembersWithRoles(it).size} users"
                }

                val response = list.joinToString("\n")
                if(response.length < 1990) {
                    message.editMessage("```\n$response\n```").queue()
                } else {
                    var data = ""
                    var edited = false
                    for(i in 0..list.size) {
                        if(data.length + list[i].length < 1990) {
                            data += list[i] + '\n'
                        } else {
                            if(!edited) {
                                message.editMessage("```\n$data\n```").queue()
                            } else {
                                event.channel.sendMessage("```\n$data\n```").queue()
                            }
                            data = list[i] + '\n'
                        }
                    }
                    event.channel.sendMessage("```\n$data\n```").queue()
                }
            }
        }
    }

    command("deleterole") {
        description = "Deletes the given role or roles. HERE BE DRAGONS."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(MultipleArg(RoleArg)) { event ->
            event.args.first.forEach { role ->
                role.delete().queue(
                        { event.respond("Deleted role ${role.name}") },
                        { event.respond("Failed to delete role ${role.name}") }
                )
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

private fun removeRoles(guild: Guild, member: Member, vararg roles: String) {
    // TODO: Perhaps we should check if the user has more than 1 color role
    //       and remove all of them instead of just 1
    member.roles.find { it.id in roles }?.let {
        guild.removeRoleFromMember(member, it).queue()
    }
}

private fun grantRole(guild: Guild, member: Member, role: Role) =
        guild.addRoleToMember(member, role).queue()

private fun buildRolesEmbed(guild: Guild, roles: Map<String, List<String>>): MessageEmbed {
    return embed {
        title = "Grantable roles"
        color = infoColor

        roles.iterator().forEach {
            addInlineField(
                    name = it.key,
                    value = it.value.joinToString("\n") { id -> guild.getRoleById(id)?.name ?: id })
        }

    }
}