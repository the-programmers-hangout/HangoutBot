package me.markhc.hangoutbot.commands.utilities.services

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.kordx.emoji.*
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.extensions.*
import me.markhc.hangoutbot.dataclasses.*
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.utilities.TimeFormatter
import org.joda.time.*
import org.joda.time.format.DateTimeFormat

@Service
class MuteService(private val persistentData: PersistentData, private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.longDateTime()

    suspend fun addMutedMember(event: GuildCommandEvent<*>, member: Member, ms: Long, soft: Boolean) {
        val guild = event.guild
        val muteRoleId = persistentData.getGuildProperty(guild.asGuild()) { if (soft) softMuteRole else muteRole }.toSnowflakeOrNull()

        if (muteRoleId == null) {
            event.respond("Sorry, this guild does not have a mute role.")
            return
        }

        val muteRole = guild.getRole(muteRoleId)

        if (muteRole.id in member.roleIds) {
            event.respond("Nice try, but you're already muted!")
            return
        }

        val mutedUsers = persistentData.getGuildProperty(guild) { mutedUsers }

        if (mutedUsers.any { muted -> muted.user == member.id.value }) {
            event.respond("Sorry, you already have an active mute!")
            return
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)

        persistentData.setGuildProperty(guild.asGuild()) {
            this.mutedUsers.add(MuteEntry(member.id.value, until.toString(dateFormatter), soft))
        }

        applyMute(member, muteRole, ms)

        event.message.addReaction(Emojis.mute.toReaction())
        event.buildMuteEmbed(member, ms)
    }

    suspend fun launchTimers() {
        persistentData.getGuilds().forEach {
            startMuteTimers(it)
        }
    }

    private suspend fun startMuteTimers(config: GuildConfiguration) {
        if (config.mutedUsers.isEmpty()) return

        val guild = config.guildId.toSnowflakeOrNull()?.let { discord.api.getGuild(it) } ?: return

        val muteRole = config.muteRole.ifBlank { null }?.let {
            it.toSnowflakeOrNull()?.let { guild.getRole(it) }
        }

        val softMuteRole = config.softMuteRole.ifBlank { null }?.let {
            it.toSnowflakeOrNull()?.let { guild.getRole(it) }
        }

        config.mutedUsers.forEach { entry ->
            val millis = dateFormatter.parseMillis(entry.timeUntil) - DateTime.now().millis
            val member = entry.user.toSnowflakeOrNull()?.let { guild.getMember(it) }

            if (member != null) {
                if (entry.isSoft && softMuteRole != null) {
                    applyMute(member, softMuteRole, millis)
                } else if (muteRole != null) {
                    applyMute(member, muteRole, millis)
                }
            }
        }
    }

    private suspend fun applyMute(member: Member, role: Role, ms: Long) {
        member.addRole(role.id)

        startUnmuteTimer(member.guild.id, member.id, role.id, ms)
    }

    private fun startUnmuteTimer(guildId: Snowflake, memberId: Snowflake, roleId: Snowflake, millis: Long) {
        GlobalScope.launch {
            delay(millis)

            val guild = discord.api.getGuild(guildId) ?: return@launch
            val member = guild.getMemberOrNull(memberId) ?: return@launch
            val role = guild.getRoleOrNull(roleId) ?: return@launch

            persistentData.setGuildProperty(guild) {
                mutedUsers.removeIf { member.id.value == it.user }
            }
            member.removeRole(role.id)
        }
    }

    private suspend fun CommandEvent<*>.buildMuteEmbed(member: Member, duration: Long) = member.sendPrivateMessage {
        title = "You have been muted"
        description = "The mute will be automatically removed when the timer expires. " +
            "If you think this was an error, contact a staff member."
        color = discord.configuration.theme

        field {
            inline = true
            name = "Duration"
            value = TimeFormatter.toShortDurationString(duration)
        }

        field {
            inline = true
            name = "You will be unmuted on"
            value = DateTime.now(DateTimeZone.UTC).plus(duration).toString(DateTimeFormat.fullDateTime())
        }

        thumbnail {
            url = member.avatar.url
        }
    }
}