package me.markhc.hangoutbot.utilities

import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.dsl.command.GenericContainer
import me.jakejmattson.kutils.api.extensions.jda.fullName
import me.markhc.hangoutbot.services.BotStatsService
import kotlin.system.measureTimeMillis

inline fun <T : GenericContainer> runLoggedCommand(event: CommandEvent<T>, action: () -> Unit) {
    val stats = event.discord.getInjectionObjects(BotStatsService::class)

    runCatching {
        measureTimeMillis {
            action()
        }
    }.onSuccess {
        stats.commandExecutionTime(event.command!!, it)
    }.onFailure {
        println(it.message);
        println("------------- [Event Data] ------------");
        println("Author: ${event.author.fullName()}")
        println("Guild: ${event.guild}")
        println("Channel: ${event.channel.id}")
        print("Message begin--[[")
        print(event.rawInputs.rawMessageContent)
        println("]]--Message end")
        println("---------------------------------------");
    }
}