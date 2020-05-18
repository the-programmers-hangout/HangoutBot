package me.markhc.hangoutbot.services

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import com.github.kittinunf.result.Result
import me.aberrantfox.kjdautils.api.dsl.embed
import me.markhc.hangoutbot.dataclasses.Reminder
import me.markhc.hangoutbot.utilities.toLongDurationString
import net.dv8tion.jda.api.entities.*

@Service
class ReminderService(private val persistentData: PersistentData,
                      private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.fullDateTime()

    fun addReminder(user: User, ms: Long, what: String): Result<String, Exception> {
        val reminders = persistentData.getGlobalProperty { reminders }

        if (reminders.count { it.user == user.id } > 10) {
            return Result.Failure(Exception("Sorry, you cannot create any more reminders!"))
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)

        persistentData.setGlobalProperty {
            reminders.add(Reminder(user.id, until.toString(dateFormatter), what))
        }

        launchReminder(user.id, ms, what)

        return Result.Success("Got it, I'll remind you in ${ms.toLongDurationString()} about \"${what}\"")
    }

    fun listReminders(user: User, fn: (Reminder) -> Unit): Int {
        val reminders = persistentData.getGlobalProperty { reminders }

        return reminders.filter { it.user == user.id }
                .also { it.forEach(fn) }
                .size
    }

    fun launchTimers() {
        persistentData.getGlobalProperty { reminders }.forEach {
            val millis = dateFormatter.parseMillis(it.timeUntil) - DateTime.now().millis
            launchReminder(it.user, millis, it.what)
        }
    }

    private fun launchReminder(userId: String, ms: Long, what: String) {
        GlobalScope.launch {
            delay(ms)

            discord.getUserById(userId)?.sendPrivateMessage(embed {
                title = "Reminder"
                description = what
                color = infoColor
            })

            persistentData.setGlobalProperty {
                reminders.removeIf { it.user == userId }
            }
        }
    }
}