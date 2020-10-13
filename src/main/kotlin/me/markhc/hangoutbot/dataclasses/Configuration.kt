package me.markhc.hangoutbot.dataclasses

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.channel.Channel
import me.jakejmattson.discordkt.api.dsl.Data
import me.markhc.hangoutbot.services.PermissionLevel

data class Configuration(val guildConfigurations: MutableList<GuildConfiguration> = mutableListOf(),
                         var totalCommandsExecuted: Int = 0,
                         val commandPermission: MutableMap<String, PermissionLevel> = mutableMapOf(),
                         val reminders: MutableList<Reminder> = mutableListOf()) : Data("data/guilds.json", killIfGenerated = false)

data class GuildConfiguration(val guildId: String = "",
                              var prefix: String = "++",
                              var cooldown: Int = 5,
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
                              val availableMacros: MutableMap<String, TextMacro> = mutableMapOf(),
                              val mutedUsers: MutableList<MuteEntry> = mutableListOf())

data class MuteEntry(val user: String = "", val timeUntil: String = "", val isSoft: Boolean = false)
data class Reminder(val user: String = "", val timeUntil: String = "", val what: String = "")
data class TextMacro(val name: String = "", var contents: String = "", val channel: String = "", var category: String = "")