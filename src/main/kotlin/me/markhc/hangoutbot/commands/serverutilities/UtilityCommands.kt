package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.arguments.RoleArg
import me.markhc.hangoutbot.arguments.TextChannelArg
import me.markhc.hangoutbot.dataclasses.GuildConfigurations
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.Permission
import me.markhc.hangoutbot.utilities.*
import net.dv8tion.jda.api.entities.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import kotlin.math.roundToLong

@Suppress("unused")
@CommandSet("Utility")
fun produceUtilityCommands(config: GuildConfigurations, persistence: PersistenceService) = commands {
    fun GuildConfigurations.save() {
        persistence.save(this)
    }

    val dateFormatter = DateTimeFormat.fullDateTime()

    command("echo") {
        requiredPermissionLevel = Permission.Staff
        description = "Echo a message to a channel."
        execute(TextChannelArg.makeOptional { it.channel as TextChannel }, SentenceArg) {
            val (target, message) = it.args

            target.sendMessage(message).queue()
        }
    }

    command("viewjoindate") {
        description = "Displays when a user joined the guild"
        execute(MemberArg) {
            val member = it.args.first

            val joinTime = DateTime(member.timeJoined.toInstant().toEpochMilli(), DateTimeZone.UTC)

            it.respond("${member.fullName()}'s join date: ${joinTime.toString(dateFormatter)}")
        }
    }

    command("viewcreationdate") {
        description = "Displays when a user was created"
        execute(UserArg) {
            val user = it.args.first

            val createdTime = DateTime(user.timeCreated.toInstant().toEpochMilli(), DateTimeZone.UTC)

            it.respond("${user.fullName()}'s creation date: ${createdTime.toString(dateFormatter)}")
        }
    }

    command("avatar") {
        description = "Gets the avatar from the given user"
        execute(UserArg) {
            val user = it.args.first

            it.respond("${user.effectiveAvatarUrl}?size=512")
        }
    }

    command("selfmute") {
        description = "Mute yourself for an amout of time. Default is 1 hour. Max is 24 hours."
        execute(TimeStringArg.makeOptional { 3600.0 }) {
            val (timeInSeconds) = it.args

            if(timeInSeconds > 24 * 3600.0) {
                return@execute it.respond("You cannot mute yourself for that long.")
            }

            val guild = it.guild!!
            val millis = timeInSeconds.roundToLong() * 1000

            config.getGuildConfig(guild).apply {
                if (muteRole.isEmpty()) {
                    return@execute it.respond("Sorry, this guild does not have a mute role.")
                }

                val role = guild.getRoleById(muteRole)
                        ?: return@execute it.respond("Sorry, this guild does not have a mute role.")

                val member = guild.getMember(it.author)!!
                
                if (muteRole in member.roles.map { r -> r.id }.toList()) {
                    return@execute it.respond("Nice try, but you're already muted!")
                }

                if (mutedUsers.any { muted -> muted.user == member.id }) {
                    return@execute it.respond("Sorry, you already have an active mute!")
                }

                addMutedMember(member, millis)
                config.save()

                muteMemberWithTimer(member, role, millis) {
                    removeMutedMember(this)
                    config.save()
                    unmuteMember(this, role)
                }

                it.author.sendPrivateMessage(buildSelfMuteEmbed(member, millis))
            }
        }
    }

    command("nuke") {
        requiredPermissionLevel = Permission.Staff
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

    command("getpermission") {
        requiredPermissionLevel = Permission.Staff
        description = "Returns the required permission level for the given command"
        execute(CommandArg) {
            val (cmd) = it.args

            it.respond("${cmd.requiredPermissionLevel}")
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

