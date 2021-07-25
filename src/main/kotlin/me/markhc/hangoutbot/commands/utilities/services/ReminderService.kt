package me.markhc.hangoutbot.commands.utilities.services

import com.github.kittinunf.result.Result
import com.gitlab.kordlib.core.entity.User
import kotlinx.coroutines.*
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.*
import me.markhc.hangoutbot.dataclasses.Reminder
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.utilities.TimeFormatter
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

@Service
class ReminderService(private val persistentData: PersistentData, private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")

    fun addReminder(user: User, ms: Long, what: String, repeatingTime: Long? = null): Result<String, Exception> {
        val reminders = persistentData.getGlobalProperty { reminders }

        if (reminders.count { it.user == user.id.value } > 10) {
            return Result.Failure(Exception("Sorry, you cannot create any more reminders!"))
        }
        val until = DateTime.now(DateTimeZone.UTC).plus(ms)
        val reminder = Reminder(user.id.value, until.toString(dateFormatter), what, repeatingTime)

        persistentData.setGlobalProperty {
            reminders.add(reminder)
        }
        launchReminder(user, reminder)

        return Result.Success("Got it, I'll remind you in ${TimeFormatter.toLongDurationString(ms)} about that.")
    }

    fun listReminders(user: User, fn: (Reminder) -> Unit) =
        persistentData
            .getGlobalProperty { reminders }
            .filter { it.user == user.id.value }
            .also { it.forEach(fn) }
            .size

    suspend fun launchTimers() {
        persistentData.getGlobalProperty { reminders }.forEach {
            discord.api.getUser(it.user.toSnowflake())?.let { user ->
                launchReminder(user, it)
            }
        }
    }

    private fun launchReminder(user: User, reminder: Reminder) {
        GlobalScope.launch {
            val ms = dateFormatter.parseDateTime(reminder.timeUntil).millis - DateTime.now().millis
            delay(ms)

            user.let {
                it.sendPrivateMessage {
                    title = "Reminder"
                    description = reminder.what
                    color = discord.configuration.theme
                }
            }

            persistentData.setGlobalProperty {
                reminders.removeIf {
                    dateFormatter.parseDateTime(it.timeUntil).millis < DateTime.now().millis
                }
                reminder.repeatingTime?.let {
                    addReminder(user, it, reminder.what, it)
                }
            }
        }
    }
}