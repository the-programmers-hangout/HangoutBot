package me.markhc.hangoutbot.services

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel
import me.jakejmattson.discordkt.annotations.Service
import me.markhc.hangoutbot.dataclasses.*

@Service
class PersistentData(private val configuration: Configuration) {
    fun getGuilds() = configuration.guildConfigurations

    fun <R> setGlobalProperty(fn: Configuration.() -> R) =
        fn(configuration).also { configuration.save() }

    fun <R> getGlobalProperty(fn: Configuration.() -> R) =
        configuration.let(fn)

    suspend fun <R> setGuildProperty(guild: Guild, fn: suspend GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { configuration.save() }
    }

    suspend fun <R> getGuildProperty(guild: Guild, fn: suspend GuildConfiguration.() -> R) =
        fn(getGuildConfig(guild))

    private fun getGuildConfig(guild: Guild) = configuration.guildConfigurations.first { it.guildId == guild.id.toString() }

    fun hasGuildConfig(guildId: String): Boolean = configuration.guildConfigurations.any { it.guildId == guildId }

    fun setup(guild: Guild, prefix: String, welcomeChannel: Channel, loggingChannel: Channel,
              muteRole: Role, softMuteRole: Role) {

        configuration.guildConfigurations.add(GuildConfiguration (
                guildId = guild.id.toString(),
                prefix = prefix,
                loggingChannel = loggingChannel.id.toString(),
                muteRole = muteRole.id.toString(),
                softMuteRole = softMuteRole.id.toString()
        ))

        configuration.save()
    }
}