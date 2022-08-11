package me.markhc.hangoutbot.dataclasses

import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.dsl.Data

@Serializable
data class Configuration(val guildConfigurations: MutableList<GuildConfiguration> = mutableListOf(),
                         var totalCommandsExecuted: Int = 0,
                         val reminders: MutableList<Reminder> = mutableListOf()) : Data()

@Serializable
data class GuildConfiguration(val guildId: String = "",
                              var prefix: String = "++",
                              var cooldown: Int = 5,
                              var loggingChannel: String = "",
                              var muteRole: String = "",
                              var softMuteRole: String = "",
                              var totalCommandsExecuted: Int = 0,
                              val grantableRoles: MutableMap<String, MutableList<String>> = mutableMapOf(),
                              val assignedColorRoles: MutableMap<String, MutableList<String>> = mutableMapOf(),
                              val mutedUsers: MutableList<MuteEntry> = mutableListOf())

@Serializable
data class MuteEntry(val user: String = "", val timeUntil: String = "", val isSoft: Boolean = false)

@Serializable
data class Reminder(val user: String = "", val timeUntil: String = "", val what: String = "")