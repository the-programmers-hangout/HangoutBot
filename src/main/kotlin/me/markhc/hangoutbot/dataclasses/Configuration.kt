package me.markhc.hangoutbot.dataclasses

import me.aberrantfox.kjdautils.api.annotation.Data
import net.dv8tion.jda.api.entities.Guild

@Data("data/guilds.json")
data class Configuration(val guildConfigurations: MutableList<GuildConfiguration> = mutableListOf(),
                         var totalCommandsExecuted: Int = 0) {
    fun getGuildConfig(guildId: String): GuildConfiguration {
        val guild = guildConfigurations.find { it.guildId == guildId }

        if(guild != null) {
            return guild
        }

        guildConfigurations.add(GuildConfiguration(guildId, "+"))

        return guildConfigurations.first { it.guildId == guildId }
    }
    fun getGuildConfig(guild: Guild) = getGuildConfig(guild.id)
}

data class GuildConfiguration(val guildId: String = "",
                              var prefix: String = "",
                              var reactToCommands: Boolean = false,
                              var welcomeEmbeds: Boolean = false,
                              var welcomeChannel: String = "",
                              var staffRole: String = "",
                              var adminRole: String = "",
                              var muteRole: String = "",
                              var totalCommandsExecuted: Int = 0,
                              var grantableRoles: MutableMap<String, MutableList<String>> = mutableMapOf(),
                              var mutedUsers: MutableList<MuteEntry> = mutableListOf())

data class MuteEntry(val user: String = "", val timeUntil: String = "")