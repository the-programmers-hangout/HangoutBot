package me.markhc.hangoutbot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.extensions.pfpUrl
import me.jakejmattson.discordkt.extensions.sendPrivateMessage
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.GuildConfiguration
import me.markhc.hangoutbot.dataclasses.MuteEntry
import java.time.Instant

@Service
class MuteService(private val configuration: Configuration, private val discord: Discord) {
    suspend fun addMutedMember(event: GuildSlashCommandEvent<*>, member: Member, ms: Long, soft: Boolean) {
        val guild = event.guild
        val config = configuration[guild]

        val muteRoleId = with(config) {
            if (soft) softMuteRole else muteRole
        }

        val muteRole = guild.getRole(muteRoleId)

        if (muteRole.id in member.roleIds) {
            event.respond("Nice try, but you're already muted!")
            return
        }

        val mutedUsers = configuration[guild].mutedUsers

        if (config.mutedUsers.any { muted -> muted.user == member.id }) {
            event.respond("Sorry, you already have an active mute!")
            return
        }

        val until = Instant.now().plusMillis(ms)

        config.mutedUsers.add(MuteEntry(member.id, until.toString(), soft))
        applyMute(member, muteRole, ms)

        event.buildMuteEmbed(member, ms)
    }

    suspend fun launchTimers() {
        configuration.guildConfigurations.forEach { (guild, config) ->
            startMuteTimers(guild, config)
        }
    }

    private suspend fun startMuteTimers(guildId: Snowflake, config: GuildConfiguration) {
        if (config.mutedUsers.isEmpty()) return

        val guild = discord.kord.getGuild(guildId) ?: return
        val muteRole = guild.getRoleOrNull(config.muteRole)
        val softMuteRole = guild.getRoleOrNull(config.softMuteRole)

        config.mutedUsers.forEach { entry ->
            val millis = 0L //parseMillis(entry.timeUntil) - System.currentTimeMillis()
            val member = entry.user.let { guild.getMemberOrNull(it) }

            if (member != null) {
                if (entry.isSoft && softMuteRole != null) {
                    applyMute(member, softMuteRole, millis)
                } else if (muteRole != null) {
                    applyMute(member, muteRole, millis)
                }
            }
        }

        TODO("Handle time parsing")
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

            configuration[guild].mutedUsers.removeIf { member.id == it.user }
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