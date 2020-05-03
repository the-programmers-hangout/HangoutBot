package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild

@Service
class PersistentData(private val configuration: Configuration, private val persistenceService: PersistenceService) {
    fun getGuilds() = configuration.guildConfigurations

    fun <R> setProperty(fn: Configuration.() -> R): R {
        return fn(configuration).also { persistenceService.save(configuration) }
    }

    fun <R> getProperty(fn: Configuration.() -> R) =
            configuration.let(fn)

    fun <R> setGuildProperty(guild: Guild, fn: GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { persistenceService.save(configuration) }
    }

    fun <R> getGuildProperty(guild: Guild, fn: GuildConfiguration.() -> R) =
            getGuildConfig(guild).let(fn)

    private fun getGuildConfig(guildId: String): GuildConfiguration {
        val guild = configuration.guildConfigurations.find { it.guildId == guildId }

        if(guild != null) {
            return guild
        }

        configuration.guildConfigurations.add(GuildConfiguration(guildId))

        return configuration.guildConfigurations.first { it.guildId == guildId }
    }
    private fun getGuildConfig(guild: Guild) = getGuildConfig(guild.id)
}