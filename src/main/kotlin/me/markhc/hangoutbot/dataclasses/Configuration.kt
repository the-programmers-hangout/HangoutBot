package me.markhc.hangoutbot.dataclasses

import me.aberrantfox.kjdautils.api.annotation.Data
import me.markhc.hangoutbot.services.PermissionLevel

@Data("data/guilds.json", killIfGenerated = false)
data class Configuration(val guildConfigurations: MutableList<GuildConfiguration> = mutableListOf(),
                         var totalCommandsExecuted: Int = 0,
                         val commandPermission: MutableMap<String, PermissionLevel> = mutableMapOf(),
                         val reminders: MutableList<Reminder> = mutableListOf())

data class GuildConfiguration(val guildId: String = "",
                              var prefix: String = "++",
                              var welcomeEmbeds: Boolean = false,
                              var welcomeChannel: String = "",
                              var botChannel: String = "",
                              var loggingChannel: String = "",
                              var muteRole: String = "",
                              var softMuteRole: String = "",
                              var totalCommandsExecuted: Int = 0,
                              val grantableRoles: MutableMap<String, MutableList<String>> = mutableMapOf(),
                              val rolePermissions: MutableMap<String, PermissionLevel> = mutableMapOf(),
                              val commandPermission: MutableMap<String, PermissionLevel> = mutableMapOf(),
                              val assignedColorRoles: MutableMap<String, MutableList<String>> = mutableMapOf(),
                              val mutedUsers: MutableList<MuteEntry> = mutableListOf())

data class MuteEntry(val user: String = "", val timeUntil: String = "", val isSoft: Boolean = false)
data class Reminder(val user: String = "", val timeUntil: String = "", val what: String = "")