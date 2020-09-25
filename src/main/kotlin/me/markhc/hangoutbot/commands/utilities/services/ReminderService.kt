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

@Service
class ReminderService(private val persistentData: PersistentData, private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")

    fun addReminder(user: User, ms: Long, what: String): Result<String, Exception> {
        val reminders = persistentData.getGlobalProperty { reminders }

        if (reminders.count { it.user == user.id.value } > 10) {
            return Result.Failure(Exception("Sorry, you cannot create any more reminders!"))
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)

        persistentData.setGlobalProperty {
            reminders.add(Reminder(user.id.value, until.toString(dateFormatter), what))
        }

        launchReminder(user.id.value, ms, what)

        return Result.Success("Got it, I'll remind you in ${TimeFormatter.toLongDurationString(ms)} about that.")
    }

    fun listReminders(user: User, fn: (Reminder) -> Unit) =
        persistentData
            .getGlobalProperty { reminders }
            .filter { it.user == user.id.value }
            .also { it.forEach(fn) }
            .size

    fun launchTimers() {
        persistentData.getGlobalProperty { reminders }.forEach {
            val millis = dateFormatter.parseDateTime(it.timeUntil).millis - DateTime.now().millis
            launchReminder(it.user, millis, it.what)
        }
    }

    private fun launchReminder(userId: String, ms: Long, what: String) {
        GlobalScope.launch {
            delay(ms)

            userId.toSnowflake()?.let {
                discord.api.getUser(it)?.sendPrivateMessage {
                    title = "Reminder"
                    description = what
                    color = discord.configuration.theme
                }
            }

            persistentData.setGlobalProperty {
                reminders.removeIf {
                    dateFormatter.parseDateTime(it.timeUntil).millis < DateTime.now().millis
                }
            }
        }
    }
}