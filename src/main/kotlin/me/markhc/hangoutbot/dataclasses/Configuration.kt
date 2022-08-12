package me.markhc.hangoutbot.dataclasses

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.dsl.Data

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
                              var cooldown: Int = 5,
                              val grantableRoles: MutableSet<Snowflake> = mutableSetOf(),
                              val assignedColorRoles: MutableMap<Snowflake, MutableList<Snowflake>> = mutableMapOf(),
                              val mutedUsers: MutableList<MuteEntry> = mutableListOf())

@Serializable
data class MuteEntry(val user: Snowflake, val timeUntil: String = "", val isSoft: Boolean = false)

@Serializable
data class Reminder(val user: Snowflake, val timeUntil: String = "", val what: String = "")