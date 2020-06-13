package me.markhc.hangoutbot.services

import me.jakejmattson.kutils.api.annotations.Service
import me.jakejmattson.kutils.api.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.dataclasses.CustomAlert
import me.markhc.hangoutbot.dataclasses.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

@Service
class PersistentData(private val botConfiguration: BotConfiguration,
                     private val configuration: Configuration,
                     private val persistenceService: PersistenceService) {
    fun getGuilds() = configuration.guildConfigurations

    fun <R> setGlobalProperty(fn: Configuration.() -> R) =
            fn(configuration).also { persistenceService.save(configuration) }

    fun <R> getGlobalProperty(fn: Configuration.() -> R) =
            configuration.let(fn)

    fun <R> setGuildProperty(guild: Guild, fn: GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { persistenceService.save(configuration) }
    }

    fun <R> setGuildProperty(guild: String, fn: GuildConfiguration.() -> R): R {
        val config = getGuildConfig(guild)
        return fn(config).also { persistenceService.save(configuration) }
    }

    fun <R> getGuildProperty(guild: Guild, fn: GuildConfiguration.() -> R) =
            getGuildConfig(guild).let(fn)

    fun addCustomAlertUnchecked(guild: Guild, user: User, channel: TextChannel?, text: String): Int {
        return setGuildProperty(guild) {
            val lastId = customAlerts.filter { it.user == user.idLong }.maxBy { it.id }?.id ?: 0

            customAlerts.add(CustomAlert(
                    id = lastId + 1,
                    user = user.idLong,
                    channel = channel?.idLong ?: 0,
                    text = text,
                    disabled = false
            ))

            lastId + 1
        }
    }

    private fun getGuildConfig(guildId: String): GuildConfiguration {
        val guild = configuration.guildConfigurations.find { it.guildId == guildId }

        if(guild != null) {
            return guild
        }

        configuration.guildConfigurations.add(GuildConfiguration(guildId, botConfiguration.prefix))

        return configuration.guildConfigurations.first { it.guildId == guildId }
    }
    private fun getGuildConfig(guild: Guild) = getGuildConfig(guild.id)
}