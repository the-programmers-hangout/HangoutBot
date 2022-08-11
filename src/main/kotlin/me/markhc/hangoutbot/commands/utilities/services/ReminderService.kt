package me.markhc.hangoutbot.commands.utilities.services

import com.github.kittinunf.result.Result
import dev.kord.core.entity.User
import kotlinx.coroutines.*
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.*
import me.markhc.hangoutbot.dataclasses.Reminder
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.utilities.TimeFormatter
import java.time.Instant

@Service
class ReminderService(private val persistentData: PersistentData, private val discord: Discord) {
    fun addReminder(user: User, ms: Long, what: String): Result<String, Exception> {
        val reminders = persistentData.getGlobalProperty { reminders }

        if (reminders.count { it.user == user.id.toString() } > 10) {
            return Result.Failure(Exception("Sorry, you cannot create any more reminders!"))
        }

        val until = Instant.now().plusMillis(ms)

        persistentData.setGlobalProperty {
            reminders.add(Reminder(user.id.toString(), until.toString(), what))
        }

        launchReminder(user.id.toString(), ms, what)

        return Result.Success("Got it, I'll remind you in ${TimeFormatter.toLongDurationString(ms)} about that.")
    }

    fun listReminders(user: User, fn: (Reminder) -> Unit) =
        persistentData
            .getGlobalProperty { reminders }
            .filter { it.user == user.id.toString() }
            .also { it.forEach(fn) }
            .size

    fun launchTimers() {
        TODO("Handle time parsing")

        persistentData.getGlobalProperty { reminders }.forEach {
            val millis = 0L //it.timeUntil - System.currentTimeMillis()
            launchReminder(it.user, millis, it.what)
        }
    }

    private fun launchReminder(userId: String, ms: Long, what: String) {
        GlobalScope.launch {
            delay(ms)

            userId.toSnowflakeOrNull()?.let {
                discord.kord.getUser(it)?.sendPrivateMessage {
                    title = "Reminder"
                    description = what
                    color = discord.configuration.theme
                }
            }

            persistentData.setGlobalProperty {
                reminders.removeIf {
                    false
                    //it.timeUntil.millis < System.currentTimeMillis()
                }
            }
        }
    }
}