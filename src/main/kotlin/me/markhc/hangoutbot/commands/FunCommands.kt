package me.markhc.hangoutbot.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.jakejmattson.discordkt.arguments.SplitterArg
import me.jakejmattson.discordkt.commands.commands
import java.net.HttpURLConnection
import java.net.URL

fun produceFunCommands() = commands("Fun") {
    slash("flip", "Choose one of the given choices.") {
        execute(SplitterArg(";", "Choices")) {
            val response = listOf(
                "Hmm, I'd say %choice%.",
                "%choice%, no doubt.",
                "Perhaps... %choice%.",
                "%choice% sounds good to me.",
                "If it were up to me, I'd go with %choice%",
                "East or west, %choice% is the best."
            ).random().replace("%choice%", args.first.random())

            respondPublic(response)
        }
    }

    slash("dadjoke", "Returns a random dad joke.") {
        execute {
            val connection = URL("https://icanhazdadjoke.com/").openConnection() as HttpURLConnection
            connection.setRequestProperty(
                "User-Agent",
                "HangoutBot (https://github.com/the-programmers-hangout/HangoutBot/)"
            )
            connection.setRequestProperty("Accept", "text/plain")
            connection.setRequestProperty("Accept-Language", "en-US")
            connection.setRequestProperty("Connection", "close")
            respondPublic(withContext(Dispatchers.IO) {
                String(connection.inputStream.readAllBytes())
            })
        }
    }

    slash("stupid") {
        execute {
            respond("${this.author.avatar?.url}")
        }
    }

    slash("getadmin") {
        execute {
            respond("You can't see me but I'm laughing at you right now for trying that.")
        }
    }
}