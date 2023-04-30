package me.markhc.hangoutbot

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.util.plus
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.services.MuteService
import java.awt.Color

@PrivilegedIntent
suspend fun main(args: Array<String>) {
    val token = args.firstOrNull()
        ?: System.getenv("BOT_TOKEN")
        ?: throw IllegalArgumentException("Missing bot token.")

    bot(token) {
        val configuration = data("data/guilds.json") { Configuration() }

        configure {
            documentCommands = false
            theme = Color.CYAN
            intents = Intent.GuildMembers + Intent.Guilds + Intent.GuildMessages
            defaultPermissions = Permissions(Permission.UseApplicationCommands)
        }

        onStart {
            val muteService = getInjectionObjects<MuteService>()
            muteService.launchTimers()

            configuration.reminders.removeIf { it.endTime < System.currentTimeMillis() }
            configuration.reminders.forEach { it.launch(this, configuration) }
        }
    }
}