package me.markhc.hangoutbot

import dev.kord.common.annotation.KordPreview
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.extensions.addInlineField
import me.jakejmattson.discordkt.extensions.pfpUrl
import me.jakejmattson.discordkt.extensions.plus
import me.markhc.hangoutbot.commands.utilities.services.MuteService
import me.markhc.hangoutbot.commands.utilities.services.ReminderService
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.services.*
import java.awt.Color

@OptIn(KordPreview::class)
@PrivilegedIntent
suspend fun main(args: Array<String>) {
    val token = args.firstOrNull()
        ?: System.getenv("BOT_TOKEN")
        ?: throw IllegalArgumentException("Missing bot token.")

    val defaultPrefix = System.getenv("BOT_PREFIX") ?: "++"
    val botOwnerId = System.getenv("BOT_OWNER") ?: "210017247048105985"

    bot(token) {
        data("data/guilds.json") { BotConfiguration(prefix = defaultPrefix, ownerId = botOwnerId) }

        prefix {
            if (guild === null) {
                return@prefix defaultPrefix
            }

            val persistentData = discord.getInjectionObjects(PersistentData::class)

            if (persistentData.hasGuildConfig(guild!!.id.toString())) {
                return@prefix guild!!.let { persistentData.getGuildProperty(it) { prefix } }
            }

            defaultPrefix
        }

        configure {
            mentionAsPrefix = true
            documentCommands = false
            commandReaction = null
            theme = Color.CYAN
            intents = Intent.GuildMembers + Intent.Guilds + Intent.GuildMessages
        }

        onStart {
            val (muteService, reminderService) = getInjectionObjects(MuteService::class, ReminderService::class)

            muteService.launchTimers()
            reminderService.launchTimers()
        }
    }
}