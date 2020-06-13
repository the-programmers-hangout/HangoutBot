package me.markhc.hangoutbot.modules.utilities.services

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jakejmattson.kutils.api.annotations.Service
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import com.github.kittinunf.result.Result
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.jakejmattson.kutils.api.extensions.jda.sendPrivateMessage
import me.markhc.hangoutbot.dataclasses.Reminder
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.utilities.toLongDurationString
import net.dv8tion.jda.api.entities.*

@Service
class ReminderService(private val persistentData: PersistentData,
                      private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")

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

        return Result.Success("Got it, I'll remind you in ${ms.toLongDurationString()} about that.")
    }

    fun listReminders(user: User, fn: (Reminder) -> Unit): Int {
        val reminders = persistentData.getGlobalProperty { reminders }

        return reminders.filter { it.user == user.id }
                .also { it.forEach(fn) }
                .size
    }

    fun launchTimers() {
        persistentData.getGlobalProperty { reminders }.forEach {
            val millis = dateFormatter.parseDateTime(it.timeUntil).millis - DateTime.now().millis
            launchReminder(it.user, millis, it.what)
        }
    }

    private fun launchReminder(userId: String, ms: Long, what: String) {
        GlobalScope.launch {
            delay(ms)

            discord.jda.getUserById(userId)?.sendPrivateMessage(embed {
                title = "Reminder"
                description = what
                color = infoColor
            })

            persistentData.setGlobalProperty {
                reminders.removeIf {
                    dateFormatter.parseDateTime(it.timeUntil).millis < DateTime.now().millis
                }
            }
        }
    }
}