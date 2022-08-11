package me.markhc.hangoutbot.commands.information

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.pfpUrl
import me.markhc.hangoutbot.utilities.*

fun produceInformationCommands() = commands("Information") {
    text("serverinfo") {
        description = "Display a message giving basic server information."
        execute {
            buildGuildInfoEmbed(guild)
        }
    }

    text("userinfo") {
        description = "Displays information about the given user."
        execute(UserArg("user").optional { it.author }) {
            val (user) = args
            val member = guild?.getMember(user.id)

            if (member != null)
                buildMemberInfoEmbed(member)
            else
                buildUserInfoEmbed(user)
        }
    }

    text("roleinfo") {
        description = "Displays information about the given role."
        execute(RoleArg) {
            buildRoleInfoEmbed(args.first)
        }
    }

    text("avatar") {
        description = "Gets the avatar from the given user"
        execute(UserArg("user").optional { it.author }) {
            val user = args.first

            respond("${user.pfpUrl}?size=512")
        }
    }

    text("spotlight") {
        description = "Gets relevant link to spotlights."
        execute(AnyArg("name").optional("")) {
            val (name) = args

            if (name.isBlank()) {
                respond("Spotlights are occasional, temporary channels that cover a piece of technology that might be unknown to part of our users. Past tech spotlight write-ups are available here https://theprogrammershangout.com/archives. If you have a suggestion, please post in server-meta or contact ModMail.")
                return@execute
            }

            val link = "https://theprogrammershangout.com/archives/what-is-${name}.md/"
            val (_, _, result) = Fuel
                .get(link)
                .set("User-Agent", "HangoutBot (https://github.com/the-programmers-hangout/HangoutBot/)")
                .responseString()

            when (result) {
                is Result.Failure -> {
                    respond("Sorry, a spotlight for `${name}` could not be found. If the spotlight happened recently, it's possible the spotlight hasn't been uploaded to the website yet. If so, please try again later.")
                }

                is Result.Success -> {
                    respond("Checkout our spotlight on `${name}`! Available here: ${link}")
                }
            }
        }
    }
}
