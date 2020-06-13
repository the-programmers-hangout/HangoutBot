package me.markhc.hangoutbot.modules.utilities.services

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jakejmattson.kutils.api.annotations.Service
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import com.github.kittinunf.result.Result
import com.google.common.eventbus.Subscribe
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.jakejmattson.kutils.api.extensions.jda.sendPrivateMessage
import me.markhc.hangoutbot.dataclasses.CustomAlert
import me.markhc.hangoutbot.dataclasses.Reminder
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.utilities.toLongDurationString
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent

@Service
class CustomAlertsService(private val persistentData: PersistentData) {
    fun addAlert(guild: Guild, user: User, channel: TextChannel?, text: String): String {
        if(text.length < 4) {
            throw Exception("The matching expression needs be at least 4 characters long")
        }

        val userAlerts = persistentData.getGuildProperty(guild) { customAlerts }.filter {
            it.user == user.idLong
        }

        if(userAlerts.size >= 10) {
            throw Exception("You have reached the max number of alerts for this guild!")
        }

        // Find global (no target channel) alerts with this same text
        val globalAlerts = userAlerts.filter { it.channel == 0.toLong() && it.text == text }

        if(globalAlerts.isNotEmpty()) {
            throw Exception("You already have an alert that matches the alert you're trying to set! (Alert id: ${globalAlerts.first().id})")
        }

        val id = persistentData.addCustomAlertUnchecked(guild, user, channel, text)

        return when (channel) {
            null -> "You will be notified whenever a new message contains \"$text\". Alert id: $id"
            else -> "You will be notified whenever a new message in ${channel.asMention} contains \"$text\". Alert id: $id"
        }
    }

    fun listAlerts(guild: Guild, user: User, fn: (CustomAlert) -> Unit): Int {
        val alerts = persistentData.getGuildProperty(guild) { customAlerts }

        return alerts.filter { it.user == user.idLong }
                .also { it.forEach(fn) }
                .size
    }

    fun enableAlert(guild: Guild, user: User, id: Int) {
        persistentData.setGuildProperty(guild) {
            val entry = customAlerts.find { it.user == user.idLong && it.id == id }
                    ?: throw Exception("No matching alert found for id $id!")

            if(!entry.disabled) {
                throw Exception("Alert is already enabled!")
            }

            entry.disabled = false
        }
    }

    fun disableAlert(guild: Guild, user: User, id: Int) {
        persistentData.setGuildProperty(guild) {
            val entry = customAlerts.find { it.user == user.idLong && it.id == id }
                    ?: throw Exception("No matching alert found for id $id!")

            if(entry.disabled) {
                throw Exception("Alert is already disabled!")
            }

            entry.disabled = true
        }
    }

    fun removeAlert(guild: Guild, user: User, id: Int) {
        persistentData.setGuildProperty(guild) {
            if(!customAlerts.removeIf { it.user == user.idLong && it.id == id }) {
                throw Exception("No matching alert found for id $id!")
            }
        }
    }

    @Subscribe
    fun onGuildMessageEvent(guildMessageEvent: GuildMessageReceivedEvent) {
        if(guildMessageEvent.author.isBot)
            return;

        val alerts    = persistentData.getGuildProperty(guildMessageEvent.guild) { customAlerts }.toList()
        val channelId = guildMessageEvent.channel.idLong
        val contents  = guildMessageEvent.message.contentRaw
        val messageLink  =
                "https://discordapp.com/channels/${guildMessageEvent.guild.id}/${channelId}/${guildMessageEvent.messageId}"
        val jda       = guildMessageEvent.jda

        GlobalScope.launch {
            val matches = alerts.filter { !it.disabled }.filter {
                it.channel == 0.toLong() || it.channel == channelId
            }.filter {
                contents.contains(it.text)
            }

            matches.forEach {
                jda.getUserById(it.user)?.sendPrivateMessage(buildAlertEmbed(it, contents, messageLink))
                delay(3000)
            }
        }
    }
}

fun buildAlertEmbed(alert: CustomAlert, contents: String, link: String) = embed {
    title = "Custom alert triggered"
    color = infoColor
    description = "A custom alert you have set has been triggered!"

    field {
        name = "Message link"
        value = link
    }

    field {
        name = "Message contents"
        value = contents.take(512)
    }

    field {
        name = "Alert details"
        value = "Id: ${alert.id}. Channel: ${alert.channel}. Text: `${alert.text}`"
    }
}