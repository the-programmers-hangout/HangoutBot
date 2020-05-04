package me.markhc.hangoutbot.services

import com.github.kittinunf.result.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.markhc.hangoutbot.dataclasses.MuteEntry
import me.markhc.hangoutbot.utilities.buildSelfMuteEmbed
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

@Service
class MuteService(private val persistentData: PersistentData,
                  private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.shortDateTime()

    fun addMutedMember(member: Member, ms: Long): Result<MessageEmbed, Exception> {
        val guild      = member.guild
        val muteRoleId = persistentData.getGuildProperty(guild) { muteRole }

        if (muteRoleId.isEmpty()) {
            return Result.Failure(Exception("Sorry, this guild does not have a mute role."))
        }

        val muteRole = guild.getRoleById(muteRoleId)
                ?: return Result.Failure(Exception("Sorry, this guild does not have a mute role."))

        if (muteRole.id in member.roles.map { it.id }) {
            return Result.Failure(Exception("Nice try, but you're already muted!"))
        }

        val mutedUsers = persistentData.getGuildProperty(guild) { mutedUsers }

        if (mutedUsers.any { muted -> muted.user == member.id }) {
            return Result.Failure(Exception("Sorry, you already have an active mute!"))
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)

        persistentData.setGuildProperty(guild) {
            mutedUsers.add(MuteEntry(member.id, until.toString(dateFormatter)))
        }

        applyMute(member, muteRole, ms)

        return Result.Success(buildSelfMuteEmbed(member, ms))
    }

    fun launchTimers() {
        persistentData.getGuilds().forEach {
            if(it.mutedUsers.isEmpty()) return@forEach
            if(it.muteRole.isEmpty()) return@forEach

            val guild = discord.jda.getGuildById(it.guildId)
                    ?: return@forEach
            val role = guild.getRoleById(it.muteRole)
                    ?: return@forEach

            it.mutedUsers.forEach { entry ->
                val member = guild.getMemberById(entry.user)
                if(member != null) {
                    val millis = dateFormatter.parseMillis(entry.timeUntil) - DateTime.now().millis
                    applyMute(member, role, millis)
                }
            }
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
}