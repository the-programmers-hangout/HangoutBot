package me.markhc.hangoutbot.utilities

import me.jakejmattson.discordkt.api.dsl.arguments.ArgumentType
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.api.extensions.jda.fullName
import me.markhc.hangoutbot.services.BotStatsService
import kotlin.system.measureTimeMillis

inline fun <T : GenericContainer> runLoggedCommand(event: CommandEvent<T>, action: (CommandEvent<T>) -> Unit) {
    val stats = event.discord.getInjectionObjects(BotStatsService::class)

    runCatching {
        measureTimeMillis {
            action(event)
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

fun Command.executeLogged(e: (CommandEvent<NoArgs>) -> Unit)
        = execute { runLoggedCommand(it, e) }
fun <A> Command.executeLogged(a1: ArgumentType<A>, e: (CommandEvent<Args1<A>>) -> Unit)
        = execute(a1) { runLoggedCommand(it, e) }
fun <A, B> Command.executeLogged(a1: ArgumentType<A>, a2: ArgumentType<B>, e: (CommandEvent<Args2<A, B>>) -> Unit)
        = execute(a1, a2) { runLoggedCommand(it, e) }
fun <A, B, C> Command.executeLogged(a1: ArgumentType<A>,
                                    a2: ArgumentType<B>,
                                    a3: ArgumentType<C>,
                                    e: (CommandEvent<Args3<A, B, C>>) -> Unit)
        = execute(a1, a2, a3) { runLoggedCommand(it, e) }