package me.markhc.hangoutbot

import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.extensions.addInlineField
import me.markhc.hangoutbot.commands.utilities.services.*
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.services.*
import java.awt.Color

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
            description = "*\"The best bot in TheProgrammersHangout Discord server\"*"

            thumbnail {
                url = self.avatar.url
            }

            addInlineField("Prefix", it.prefix())
            addInlineField("Contributors", "markhc#8366, JakeyWakey#1569")

            field {
                name = "Uptime"
                value = botStats.uptime
            }
            author {
                name = "Hangoutbot"
                url = "https://github.com/the-programmers-hangout/HangoutBot/"
                icon = self.avatar.url
            }
            field {
                name = "Source"
                value = "[[GitHub]](https://github.com/the-programmers-hangout/HangoutBot/)"
            }
        }

        permissions {
            val permissionsService = discord.getInjectionObjects(PermissionsService::class)

            if (guild != null) {
                if (user.asMember(guild!!.id).isOwner() && command.names.contains("setup")) {
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