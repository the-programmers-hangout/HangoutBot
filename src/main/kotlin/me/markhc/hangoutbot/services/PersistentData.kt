package me.markhc.hangoutbot.services

import com.gitlab.kordlib.core.entity.Guild
import me.jakejmattson.discordkt.api.annotations.Service
import me.markhc.hangoutbot.dataclasses.*

@Service
class PersistentData(private val botConfiguration: BotConfiguration,
                     private val configuration: Configuration) {
    fun getGuilds() = configuration.guildConfigurations

    fun <R> setGlobalProperty(fn: Configuration.() -> R) =
        fn(configuration).also { configuration.save() }

    fun <R> getGlobalProperty(fn: Configuration.() -> R) =
        configuration.let(fn)

    fun <R> setGuildProperty(guild: Guild, fn: GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { configuration.save() }
    }

    fun <R> setGuildProperty(guild: String, fn: GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { configuration.save() }
    }

    fun <R> getGuildProperty(guild: Guild, fn: suspend GuildConfiguration.() -> R) =
        getGuildConfig(guild).let(fn)

    private fun getGuildConfig(guildId: String): GuildConfiguration {
        val guild = configuration.guildConfigurations.find { it.guildId == guildId }

        if (guild != null) {
            return guild
        }

        configuration.guildConfigurations.add(GuildConfiguration(guildId, botConfiguration.prefix))

        return configuration.guildConfigurations.first { it.guildId == guildId }
    }

    private fun getGuildConfig(guild: Guild) = getGuildConfig(guild.id)
}