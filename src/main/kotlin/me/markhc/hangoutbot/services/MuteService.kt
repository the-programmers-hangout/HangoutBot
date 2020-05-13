package me.markhc.hangoutbot.services

import com.github.kittinunf.result.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.discord.Discord
import me.markhc.hangoutbot.dataclasses.GuildConfiguration
import me.markhc.hangoutbot.dataclasses.MuteEntry
import me.markhc.hangoutbot.utilities.toShortDurationString
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

@Service
class MuteService(private val persistentData: PersistentData,
                  private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.longDateTime()

    fun addSoftMutedMember(member: Member, ms: Long) = Result.of<MessageEmbed, Exception> {
        val guild      = member.guild
        val muteRoleId = persistentData.getGuildProperty(guild) { softMuteRole }

        if (muteRoleId.isEmpty()) {
            throw Exception("Sorry, this guild does not have a soft mute role.")
        }

        val muteRole = guild.getRoleById(muteRoleId)
                ?: throw Exception("Sorry, the configured mute role could not be found.")

        if (muteRole.id in member.roles.map { it.id }) {
            throw Exception("Nice try, but you're already muted!")
        }

        val mutedUsers = persistentData.getGuildProperty(guild) { softMutedUsers }

        if (mutedUsers.any { muted -> muted.user == member.id }) {
            throw Exception("Sorry, you already have an active mute!")
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)

        persistentData.setGuildProperty(guild) {
            this.softMutedUsers.add(MuteEntry(member.id, until.toString(dateFormatter)))
        }

        applyMute(member, muteRole, ms)

        return@of buildSoftMuteEmbed(member, ms)
    }

    fun addMutedMember(member: Member, ms: Long) = Result.of<MessageEmbed, Exception> {
        val guild      = member.guild
        val muteRoleId = persistentData.getGuildProperty(guild) { muteRole }

        if (muteRoleId.isEmpty()) {
            throw Exception("Sorry, this guild does not have a mute role.")
        }

        val muteRole = guild.getRoleById(muteRoleId)
                ?: throw Exception("Sorry, the configured mute role could not be found.")

        if (muteRole.id in member.roles.map { it.id }) {
            throw Exception("Nice try, but you're already muted!")
        }

        val mutedUsers = persistentData.getGuildProperty(guild) { mutedUsers }

        if (mutedUsers.any { muted -> muted.user == member.id }) {
            throw Exception("Sorry, you already have an active mute!")
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)

        persistentData.setGuildProperty(guild) {
            this.mutedUsers.add(MuteEntry(member.id, until.toString(dateFormatter)))
        }

        applyMute(member, muteRole, ms)

        return@of buildMuteEmbed(member, ms)
    }

    fun launchTimers() {
        persistentData.getGuilds().forEach {
            startMuteTimers(it)
            startSoftMuteTimers(it)
        }
    }

    private fun startSoftMuteTimers(config: GuildConfiguration) {
        if (config.softMutedUsers.isEmpty()) return
        if (config.softMuteRole.isEmpty()) return

        val guild = discord.jda.getGuildById(config.guildId) ?: return
        val role = guild.getRoleById(config.softMuteRole) ?: return

        config.softMutedUsers.forEach { entry ->
            val millis = dateFormatter.parseMillis(entry.timeUntil) - DateTime.now().millis
            val member = guild.getMemberById(entry.user)
            if (member != null) {
                applySoftMute(member, role, millis)
            }
        }
    }

    private fun startMuteTimers(config: GuildConfiguration) {
        if (config.mutedUsers.isEmpty()) return
        if (config.muteRole.isEmpty()) return

        val guild = discord.jda.getGuildById(config.guildId) ?: return
        val role = guild.getRoleById(config.muteRole) ?: return

        config.mutedUsers.forEach { entry ->
            val millis = dateFormatter.parseMillis(entry.timeUntil) - DateTime.now().millis
            val member = guild.getMemberById(entry.user)
            if (member != null) {
                applyMute(member, role, millis)
            }
        }
    }

    private fun applySoftMute(member: Member, role: Role, ms: Long) {
        muteMemberWithTimer(member, role, ms) {
            persistentData.setGuildProperty(guild) {
                softMutedUsers.removeIf { member.id == it.user }
            }
            member.guild.removeRoleFromMember(member, role).queue()
        }
    }

    private fun applyMute(member: Member, role: Role, ms: Long) {
        muteMemberWithTimer(member, role, ms) {
            persistentData.setGuildProperty(guild) {
                mutedUsers.removeIf { member.id == it.user }
            }
            member.guild.removeRoleFromMember(member, role).queue()
        }
    }

    private fun muteMemberWithTimer(member: Member, role: Role, millis: Long, fn: Member.() -> Unit) {
        member.guild.addRoleToMember(member, role).queue()
        GlobalScope.launch {
            delay(millis)
            fn(member)
        }
    }

    private fun buildSoftMuteEmbed(member: Member, duration: Long) = embed {
        title = "You have been soft muted"
        description = "You have been soft muted as a result of invoking the `productivemute` command. " +
                "While muted, you won't be able to use the social channels.\n\n" +
                "This mute will be automatically removed when the time expires."
        color = infoColor

        field {
            inline = true
            name = "Duration"
            value = duration.toShortDurationString()
        }

        field {
            inline = true
            name = "You will be unmuted on"
            value = DateTime.now(DateTimeZone.UTC).plus(duration).toString(DateTimeFormat.fullDateTime())
        }

        thumbnail = member.user.avatarUrl
    }

    private fun buildMuteEmbed(member: Member, duration: Long) = embed {
        title = "You have been muted"
        description = "You have been muted as a result of invoking the `selfmute` command.\n\n" +
                "This mute will be automatically removed when the time expires."
        color = infoColor

        field {
            inline = true
            name = "Duration"
            value = duration.toShortDurationString()
        }

        field {
            inline = true
            name = "You will be unmuted on"
            value = DateTime.now(DateTimeZone.UTC).plus(duration).toString(DateTimeFormat.fullDateTime())
        }

        thumbnail = member.user.avatarUrl
    }

}