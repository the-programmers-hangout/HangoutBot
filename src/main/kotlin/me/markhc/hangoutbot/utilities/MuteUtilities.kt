package me.markhc.hangoutbot.utilities

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.GuildConfiguration
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.MuteEntry
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit

private val dateFormatter = DateTimeFormat.shortDateTime()

fun GuildConfiguration.addMutedMember(member: Member, millis: Long) {
    val until = DateTime.now(DateTimeZone.UTC).plus(millis)
    mutedUsers.add(MuteEntry(user = member.id, timeUntil = until.toString(dateFormatter)))
}
fun GuildConfiguration.removeMutedMember(member: Member) {
    mutedUsers.removeIf { member.id == it.user }
}

fun muteMemberWithTimer(member: Member, role: Role, millis: Long, fn: Member.() -> Unit) {
    member.guild.addRoleToMember(member, role).queue()
    GlobalScope.launch {
        delay(millis)
        fn(member)
    }
}

fun unmuteMember(member: Member, role: Role) {
    member.guild.removeRoleFromMember(member, role).queue()
}

fun launchMuteTimers(config: Configuration, persistence: PersistenceService, discord: Discord) {
    config.guildConfigurations.forEach {
        if(it.mutedUsers.isEmpty()) return@forEach
        if(it.muteRole.isEmpty()) return@forEach

        val guild = discord.jda.getGuildById(it.guildId) ?: return@forEach
        val role = guild.getRoleById(it.muteRole)
                ?: return@forEach

        it.mutedUsers.forEach { entry ->
            val member = guild.getMemberById(entry.user)
            if(member != null) {
                val millis = dateFormatter.parseMillis(entry.timeUntil) - DateTime.now().millis
                muteMemberWithTimer(member, role, millis) {
                    it.removeMutedMember(this)
                    persistence.save(config)
                    unmuteMember(this, role)
                }
            }
        }
    }
}