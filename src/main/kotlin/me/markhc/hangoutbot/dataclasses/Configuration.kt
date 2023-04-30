package me.markhc.hangoutbot.dataclasses

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.dsl.Data
import me.jakejmattson.discordkt.dsl.edit
import me.jakejmattson.discordkt.util.sendPrivateMessage

@Serializable
data class Configuration(val guildConfigurations: MutableMap<Snowflake, GuildConfiguration> = mutableMapOf(),
                         val reminders: MutableList<Reminder> = mutableListOf()) : Data() {
    operator fun get(guild: GuildBehavior) = guildConfigurations[guild.id]!!

    operator fun set(id: Snowflake, value: GuildConfiguration) {
        guildConfigurations[id] = value
    }

    fun hasGuildConfig(guild: Guild) = guildConfigurations[guild.id] != null
}

@Serializable
data class GuildConfiguration(var muteRole: Snowflake,
                              var softMuteRole: Snowflake,
                              var loggingChannel: Snowflake,
                              val grantableRoles: MutableSet<Snowflake> = mutableSetOf(),
                              val assignedColorRoles: MutableMap<Snowflake, MutableList<Snowflake>> = mutableMapOf(),
                              val mutedUsers: MutableList<MuteEntry> = mutableListOf())

@Serializable
data class MuteEntry(val user: Snowflake, val endTime: Long, val isSoft: Boolean = false)

@Serializable
data class Reminder(val user: Snowflake, val endTime: Long, val message: String) {
    fun launch(discord: Discord, configuration: Configuration) {
        GlobalScope.launch {
            delay(endTime - System.currentTimeMillis())

            discord.kord.getUser(user)?.sendPrivateMessage {
                title = "Reminder"
                description = message
                color = discord.configuration.theme
            }

            configuration.edit { reminders.remove(this@Reminder) }
        }
    }
}