package me.markhc.hangoutbot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.*
import dev.kord.x.emoji.*
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.commands.GuildCommandEvent
import me.jakejmattson.discordkt.extensions.*
import me.markhc.hangoutbot.dataclasses.*
import java.time.Instant

@Service
class MuteService(private val persistentData: PersistentData, private val discord: Discord) {
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

        if (mutedUsers.any { muted -> muted.user == member.id.toString() }) {
            event.respond("Sorry, you already have an active mute!")
            return
        }

        val until = Instant.now().plusMillis(ms)

        persistentData.setGuildProperty(guild.asGuild()) {
            this.mutedUsers.add(MuteEntry(member.id.toString(), until.toString(), soft))
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

        val guild = config.guildId.toSnowflakeOrNull()?.let { discord.kord.getGuild(it) } ?: return

        val muteRole = config.muteRole.ifBlank { null }?.let {
            it.toSnowflakeOrNull()?.let { guild.getRole(it) }
        }

        val softMuteRole = config.softMuteRole.ifBlank { null }?.let {
            it.toSnowflakeOrNull()?.let { guild.getRole(it) }
        }

        config.mutedUsers.forEach { entry ->
            TODO("Handle time parsing")

            val millis = 0L //parseMillis(entry.timeUntil) - System.currentTimeMillis()
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

            val guild = discord.kord.getGuild(guildId) ?: return@launch
            val member = guild.getMemberOrNull(memberId) ?: return@launch
            val role = guild.getRoleOrNull(roleId) ?: return@launch

            persistentData.setGuildProperty(guild) {
                mutedUsers.removeIf { member.id.toString() == it.user }
            }
            member.removeRole(role.id)
        }
    }

    private suspend fun CommandEvent<*>.buildMuteEmbed(member: Member, duration: Long) = member.sendPrivateMessage {
        title = "You have been muted"
        description = "The mute will be automatically removed when the timer expires."
        color = discord.configuration.theme

        field {
            inline = true
            name = "Duration"
            value = "For a bit"//TODO TimeFormatter.toShortDurationString(duration)
        }

        field {
            inline = true
            name = "You will be unmuted on"
            value = "Someday"// TODO timestamp.offsetBy(duration.toInt())
        }

        thumbnail {
            url = member.pfpUrl
        }
    }
}