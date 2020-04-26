package me.markhc.hangoutbot.dataclasses

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("data/guilds.json")
data class GuildConfigurations(val guildConfigurations: MutableList<GuildConfiguration> = mutableListOf()) {
    fun getGuildConfig(guildId: String): GuildConfiguration {
        val guild = guildConfigurations.find { it.guildId == guildId }

        if(guild != null) {
           return guild
        }

        guildConfigurations.add(GuildConfiguration(guildId, "+"))

        return guildConfigurations.first { it.guildId == guildId }
    }
}

data class GuildConfiguration(val guildId: String = "",
                              var prefix: String = "",
                              var reactToCommands: Boolean = false,
                              var welcomeEmbeds: Boolean = false,
                              var welcomeChannel: String = "",
                              var staffRole: String = "",
                              var adminRole: String = "",
                              var muteRole: String = "",
                              var grantableRoles: MutableMap<String, MutableList<String>> = mutableMapOf()) {
    fun reset() {
        reactToCommands = false
        welcomeEmbeds = false
        welcomeChannel = ""
        staffRole = ""
        adminRole = ""
        muteRole = ""
        grantableRoles = mutableMapOf()
    }
}