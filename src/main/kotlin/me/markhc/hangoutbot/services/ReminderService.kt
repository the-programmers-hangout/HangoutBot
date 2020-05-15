package me.markhc.hangoutbot.services

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import com.github.kittinunf.result.Result
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.markhc.hangoutbot.dataclasses.MuteEntry
import me.markhc.hangoutbot.dataclasses.Reminder
import me.markhc.hangoutbot.utilities.toLongDurationString
import net.dv8tion.jda.api.entities.*

@Service
class ReminderService(private val persistentData: PersistentData,
                      private val persistenceService: PersistenceService,
                      private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.fullDateTime()

    fun addReminder(member: Member, ms: Long, what: String): Result<String, Exception> {
        val guild     = member.guild
        val reminders = persistentData.getGuildProperty(guild) { reminders }

        if (reminders.count { it.user == member.id } > 10) {
            return Result.Failure(Exception("Sorry, you cannot create any more reminders!"))
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)

        persistentData.setGuildProperty(guild) {
            reminders.add(Reminder(member.id, until.toString(dateFormatter), what))
        }

        launchReminder(guild, member.user, ms, what)

        return Result.Success("Got it, I'll remind you in ${ms.toLongDurationString()} about \"${what}\"")
    }

    fun listReminders(member: Member, fn: (Reminder) -> Unit): Int {
        val guild     = member.guild
        val reminders = persistentData.getGuildProperty(guild) { reminders }

        val list = reminders.filter { it.user == member.id }

        list.forEach(fn)

        return list.size
    }

    fun launchTimers() {
        persistentData.getGuilds().forEach {
            if(it.reminders.isEmpty()) return@forEach

            val guild = discord.jda.getGuildById(it.guildId)
                    ?: return@forEach

            it.reminders.forEach { entry ->
                val member = guild.getMemberById(entry.user)
                if(member != null) {
                    val millis = dateFormatter.parseMillis(entry.timeUntil) - DateTime.now().millis
                    launchReminder(guild, member.user, millis, entry.what)
                }
            }
        }
    }

    private fun launchReminder(guild: Guild, user: User, ms: Long, what: String) {
        GlobalScope.launch {
            delay(ms)
            user.sendPrivateMessage(embed {
                title = "Reminder"
                description = what
                color = infoColor
            })
            persistentData.setGuildProperty(guild) {
                reminders.removeIf { user.id == it.user }
            }
        }
    }
}