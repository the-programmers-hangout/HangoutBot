package me.markhc.hangoutbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.arguments.LowerRankedMemberArg
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.MuteService
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.ReminderService
import net.dv8tion.jda.api.entities.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@Suppress("unused")
@CommandSet("Utility")
fun produceUtilityCommands(persistentData: PersistentData,
                           muteService: MuteService,
                           reminderService: ReminderService) = commands {
    val dateFormatter = DateTimeFormat.fullDateTime()

    command("echo") {
        requiredPermissionLevel = PermissionLevel.Staff
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

            if(timeInSeconds > TimeUnit.HOURS.toSeconds(24)) {
                return@execute it.respond("You cannot mute yourself for that long.")
            }

            val guild = it.guild!!
            val member = guild.getMember(it.author)!!
            val millis = timeInSeconds.roundToLong() * 1000

            muteService.addMutedMember(member, millis).fold(
                    success = { embed -> it.author.sendPrivateMessage(embed) },
                    failure = { ex -> it.respond(ex.message!!) }
            )
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

    command("remindme") {
        description = "A command that'll remind you about something after the specified time."
        execute(TimeStringArg, SentenceArg) {
            val (timeInSeconds, sentence) = it.args

            if(timeInSeconds > TimeUnit.DAYS.toSeconds(30)) {
                return@execute it.respond("You cannot set a reminder that far into the future.")
            }

            val guild = it.guild!!
            val member = guild.getMember(it.author)!!
            val millis = timeInSeconds.roundToLong() * 1000

            reminderService.addReminder(member, millis, sentence).fold(
                    success = { msg -> it.respond(msg) },
                    failure = { ex -> it.respond(ex.message!!) }
            )
        }
    }
<<<<<<< Updated upstream
=======

    fun createRole(guild: Guild, name: String, color: Color) =
            guild.createRole()
                    .setName(name)
                    .setColor(color)
                    .setHoisted(false)
                    .setMentionable(false)
                    .complete()

    fun Role.placeBelow(other: Role) {
        val position = guild
                .modifyRolePositions()
                .selectPosition(other)
                .selectedPosition

        guild.modifyRolePositions()
                .selectPosition(this)
                .moveTo(position - 1)
                .queue()
    }
>>>>>>> Stashed changes
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

private fun grantRole(guild: Guild, member: Member, role: Role) {
    guild.addRoleToMember(member, role).queue()
}