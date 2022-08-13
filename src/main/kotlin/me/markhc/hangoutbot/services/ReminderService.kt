package me.markhc.hangoutbot.services

import com.github.kittinunf.result.Result
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.TimeStamp
import me.jakejmattson.discordkt.extensions.TimeStyle
import me.jakejmattson.discordkt.extensions.sendPrivateMessage
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.Reminder
import java.time.Instant

@Service
class ReminderService(private val configuration: Configuration, private val discord: Discord) {
    fun addReminder(user: User, ms: Long, what: String): Result<String, Exception> {
        val reminders = configuration.reminders

        if (reminders.count { it.user == user.id } > 10) {
            return Result.Failure(Exception("Sorry, you cannot create any more reminders!"))
        }

        val until = Instant.now().plusMillis(ms)

        configuration.reminders.add(Reminder(user.id, until.toString(), what))
        launchReminder(user.id, ms, what)

        return Result.Success("Got it, I'll remind you ${TimeStamp.at(until, TimeStyle.RELATIVE)}")
    }

    fun listReminders(user: User, fn: (Reminder) -> Unit) =
        configuration.reminders
            .filter { it.user == user.id }
            .also { it.forEach(fn) }
            .size

    fun launchTimers() {
        //TODO Handle time parsing

        configuration.reminders.forEach {
            val millis = 0L //it.timeUntil - System.currentTimeMillis()
            launchReminder(it.user, millis, it.what)
        }
    }

    private fun launchReminder(userId: Snowflake, ms: Long, reminder: String) {
        GlobalScope.launch {
            delay(ms)

            discord.kord.getUser(userId)?.sendPrivateMessage {
                title = "Reminder"
                description = reminder
                color = discord.configuration.theme
            }

            configuration.reminders.removeIf {
                false
                //it.timeUntil.millis < System.currentTimeMillis()
            }
        }
    }
}