package me.markhc.hangoutbot.services

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.channel.Channel
import me.jakejmattson.discordkt.api.annotations.Service
import me.markhc.hangoutbot.dataclasses.*
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.GuildConfiguration

@Service
class PersistentData(private val botConfiguration: BotConfiguration,
                     private val configuration: Configuration) {
    fun getGuilds() = configuration.guildConfigurations

    fun <R> setGlobalProperty(fn: Configuration.() -> R) =
        fn(configuration).also { configuration.save() }

    fun <R> getGlobalProperty(fn: Configuration.() -> R) =
        configuration.let(fn)

    suspend fun <R> setGuildProperty(guild: Guild, fn: suspend GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { configuration.save() }
    }

    fun <R> setGuildProperty(guild: String, fn: GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { configuration.save() }
    }

    suspend fun <R> getGuildProperty(guild: Guild, fn: suspend GuildConfiguration.() -> R) =
        fn(getGuildConfig(guild))

    private fun getGuildConfig(guildId: String): GuildConfiguration {
        val guild = configuration.guildConfigurations.find { it.guildId == guildId }

        if (guild != null) {
            return guild
        }

        configuration.guildConfigurations.add(GuildConfiguration(guildId, botConfiguration.prefix))

        return configuration.guildConfigurations.first { it.guildId == guildId }
    }

    private fun getGuildConfig(guild: Guild) = configuration.guildConfigurations.first { it.guildId == guild.id.value }

    fun hasGuildConfig(guildId: String): Boolean = configuration.guildConfigurations.any { it.guildId == guildId }

    fun setup(guild: Guild, prefix: String, welcomeChannel: Channel, loggingChannel: Channel,
              muteRole: Role, softMuteRole: Role) {

        configuration.guildConfigurations.add(GuildConfiguration (
                guildId = guild.id.value,
                prefix = prefix,
                welcomeChannel = welcomeChannel.id.value,
                loggingChannel = loggingChannel.id.value,
                muteRole = muteRole.id.value,
                softMuteRole = softMuteRole.id.value
        ))

        configuration.save()
    }
}