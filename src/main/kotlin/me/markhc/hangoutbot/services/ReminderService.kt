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
import net.dv8tion.jda.api.entities.*

@Service
class ReminderService(private val configuration: Configuration,
                      private val persistenceService: PersistenceService,
                      private val discord: Discord) {
    private val dateFormatter = DateTimeFormat.forPattern("MMMMM dd, yyyy HH:mm:ss z")

    fun addReminder(member: Member, ms: Long, what: String): Result<String, Exception> {
        val guild       = member.guild
        val guildConfig = configuration.getGuildConfig(guild)

        if (guildConfig.reminders.any { it.user == member.id }) {
            return Result.Failure(Exception("Sorry, you already have an active reminder!"))
        }

        val until = DateTime.now(DateTimeZone.UTC).plus(ms)
        guildConfig.addReminder(member.id, until.toString(dateFormatter), what)
        persistenceService.save(configuration)

        launchReminder(guild, member.user, ms, what)

        return Result.Success("Got it, I'll remind you in ${until.toString(dateFormatter)} about \"${what}\"")
    }

    fun launchTimers() {
        configuration.guildConfigurations.forEach {
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
            configuration.getGuildConfig(guild).apply {
                reminders.removeIf { user.id == it.user }
            }
            persistenceService.save(configuration)
        }
    }
}