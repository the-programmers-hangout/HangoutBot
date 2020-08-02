package me.markhc.hangoutbot.commands.utilities.services

import com.github.kittinunf.result.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.embed.embed
import me.markhc.hangoutbot.dataclasses.GuildConfiguration
import me.markhc.hangoutbot.dataclasses.MuteEntry
import me.markhc.hangoutbot.services.PersistentData
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


    fun addMutedMember(member: Member, ms: Long, soft: Boolean) = Result.of<MessageEmbed, Exception> {
        val guild      = member.guild
        val muteRoleId = persistentData.getGuildProperty(guild) { if(soft) softMuteRole else muteRole }

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
            this.mutedUsers.add(MuteEntry(member.id, until.toString(dateFormatter), soft))
        }

        applyMute(member, muteRole, ms)

        return@of buildMuteEmbed(member, ms)
    }

    fun launchTimers() {
        persistentData.getGuilds().forEach {
            startMuteTimers(it)
        }
    }

    private fun startMuteTimers(config: GuildConfiguration) {
        if (config.mutedUsers.isEmpty()) return

        val guild        = discord.jda.getGuildById(config.guildId) ?: return
        val muteRole     = config.muteRole.ifBlank { null }?.let {
            guild.getRoleById(it)
        }

        val softMuteRole     = config.softMuteRole.ifBlank { null }?.let {
            guild.getRoleById(it)
        }

        config.mutedUsers.forEach { entry ->
            val millis = dateFormatter.parseMillis(entry.timeUntil) - DateTime.now().millis
            val member = guild.getMemberById(entry.user)
            if (member != null) {
                if(entry.isSoft && softMuteRole != null) {
                    applyMute(member, softMuteRole, millis)
                } else if(muteRole != null) {
                    applyMute(member, muteRole, millis)
                }
            }
        }
    }

    private fun applyMute(member: Member, role: Role, ms: Long) {
        member.guild.addRoleToMember(member, role).queue()

        startUnmuteTimer(member.guild.id, member.id, role.id, ms)
    }

    private fun startUnmuteTimer(guildId: String, memberId: String, roleId: String, millis: Long) {
        GlobalScope.launch {
            delay(millis)

            val guild = discord.jda.getGuildById(guildId) ?: return@launch
            val member = guild.getMemberById(memberId) ?: return@launch
            val role = guild.getRoleById(roleId) ?: return@launch

            persistentData.setGuildProperty(guild) {
                mutedUsers.removeIf { member.id == it.user }
            }
            member.guild.removeRoleFromMember(member, role).queue()
        }
    }

    private fun buildMuteEmbed(member: Member, duration: Long) = embed {
        title { text = "You have been muted" }
        description = "The mute will be automatically removed when the timer expires. " +
                "If you think this was an error, contact a staff member."
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