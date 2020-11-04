package me.markhc.hangoutbot

import com.gitlab.kordlib.gateway.Intent
import com.gitlab.kordlib.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.extensions.addInlineField
import me.markhc.hangoutbot.commands.utilities.services.MuteService
import me.markhc.hangoutbot.commands.utilities.services.ReminderService
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.services.BotStatsService
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import java.awt.Color

@PrivilegedIntent
suspend fun main(args: Array<String>) {
    val token = args.firstOrNull()
            ?: System.getenv("BOT_TOKEN")
            ?: throw IllegalArgumentException("Missing bot token.")

    val defaultPrefix = System.getenv("BOT_PREFIX") ?: "++"
    val botOwnerId = System.getenv("BOT_OWNER") ?: "210017247048105985"

    val config = BotConfiguration(prefix = defaultPrefix, ownerId = botOwnerId)

    bot(token) {
        inject(config)

        prefix {
            if (guild === null) {
                return@prefix defaultPrefix
            }

            val persistentData = discord.getInjectionObjects(PersistentData::class)

            if (persistentData.hasGuildConfig(guild!!.id.value)) {
                return@prefix guild!!.let { persistentData.getGuildProperty(it) { prefix } }
            }

            defaultPrefix
        }

        intents {
            +Intent.GuildMessages
            +Intent.GuildMembers
            +Intent.Guilds
        }

        configure {
            allowMentionPrefix = true
            generateCommandDocs = false
            commandReaction = null
            theme = Color.CYAN
        }

        mentionEmbed {
            val botStats = it.discord.getInjectionObjects(BotStatsService::class)
            val channel = it.channel
            val self = channel.kord.getSelf()

            color = it.discord.configuration.theme
            title = "HangoutBot"
            description = "*\"The best bot in TheProgrammersHangout Discord server\"*"

            author {
                name = "Hangoutbot"
                url = "https://github.com/the-programmers-hangout/HangoutBot/"
                icon = self.avatar.url
            }

            addInlineField("Prefix", it.prefix())
            addInlineField("Contributors", "markhc#8366\nJakeyWakey#1569")
            addInlineField("Ping", botStats.ping)

            field {
                val versions = it.discord.versions

                name = "Bot Info"
                value = "```" +
                        "Version: 3.0.0\n" +
                        "DiscordKt: ${versions.library}\n" +
                        "Kord: ${versions.kord}\n" +
                        "Kotlin: ${versions.kotlin}" +
                        "```"
            }

            addInlineField("Uptime", botStats.uptime)
            addInlineField("Source", "[[GitHub]](https://github.com/the-programmers-hangout/HangoutBot/)")
        }

        permissions {
            val permissionsService = discord.getInjectionObjects(PermissionsService::class)

            if (guild != null) {
                if (permissionsService.hasPermission(user.asMember(guild!!.id), PermissionLevel.GuildOwner)
                        && command.names.contains("setup")) {
                    return@permissions true
                }

                permissionsService.isCommandVisible(guild!!, user, command)
            } else {
                false
            }
        }

        onStart {
            val (muteService, reminderService) = getInjectionObjects(MuteService::class, ReminderService::class)

            muteService.launchTimers()
            reminderService.launchTimers()
        }
    }
}