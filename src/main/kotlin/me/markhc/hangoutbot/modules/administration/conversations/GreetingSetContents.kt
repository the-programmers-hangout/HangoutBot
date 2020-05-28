package me.markhc.hangoutbot.modules.administration.conversations

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.*
import java.awt.Color

class GreetingSetContents : Conversation() {
    private var title = ""
    private var description = ""
    private var color = Color.WHITE
    private var image = ""
    private var thumbnail = ""

    @Start
    fun conversation() = conversation(exitString = "exit") {
        val info = channel.sendMessage(embed {
            title = "Greeting contents setup"
            description = """
                Available templates:
                ```
                {username} -> The user's name
                {fullName} -> The member's full username (name#discrim)
                {mention}  -> User as a mention
                {avatar}   -> Link to the user's avatar
                ```""".trimIndent()
            color = infoColor
        })

        while(true) {
            val msg = channel.sendMessage(embedPrompt())
        }
    }

    private fun verifyInput(input: Either<Int, String>) =
            input.getData({ it in 1..6 }, { it in listOf("\uD83D\uDD0D", "\uD83C\uDF89", "❌") })

    private fun runSubs(c: ConversationStateContainer, text: String?): String? {
        return text?.replace("{username}", c.user.name)
                ?.replace("{fullName}", c.user.fullName())
                ?.replace("{mention}", c.user.asMention)
                ?.replace("{avatar}", c.user.effectiveAvatarUrl)
    }

    private fun previewEmbed(c: ConversationStateContainer): Boolean {
        val self = this
        with(c) {
            respond(embed {
                title = runSubs(c, self.title.ifBlank { null })
                description = runSubs(c, self.description.ifBlank { null })
                color = self.color
                thumbnail = runSubs(c, self.thumbnail.ifBlank { null })
                image = runSubs(c, self.image.ifBlank { null })
            })
        }
        return true
    }

    private fun saveContents(c: ConversationStateContainer): Boolean {
        return true
    }

    private fun processCommand(c: ConversationStateContainer, choice: Int) =
            when(choice) {
                1 -> promptTitle(c)
                2 -> promptDescription(c)
                3 -> promptThumbnail(c)
                4 -> promptImage(c)
                5 -> promptColor(c)
                /*6 -> promptField()*/
                else -> false
            }

    private fun promptTitle(c: ConversationStateContainer): Boolean {
        with(c) {
            title = blockingPrompt(EveryArg) {
                "Current title is: ```\n${title.ifBlank { "Not set" }}\n```" +
                        "Please input the new value or \"exit\" to cancel"
            }
        }

        return true
    }

    private fun promptDescription(c: ConversationStateContainer): Boolean {
        with(c) {
            description = blockingPrompt(EveryArg) {
                "Current description is: ```\n${description.ifBlank { "Not set" }}\n```" +
                        "Please input the new value or \"exit\" to cancel"
            }
        }

        return true
    }

    private fun promptThumbnail(c: ConversationStateContainer): Boolean {
        with(c) {
            thumbnail = blockingPrompt(EveryArg) {
                "Current thumbnail is: ${thumbnail.ifBlank { "Not set" }}" +
                        "Please input the new value or \"exit\" to cancel"
            }
        }

        return true
    }

    private fun promptImage(c: ConversationStateContainer): Boolean {
        with(c) {
            image = blockingPrompt(EveryArg) {
                "Current image is: ${image.ifBlank { "Not set" }}\n" +
                        "Please input the new value or \"exit\" to cancel"
            }
        }

        return true
    }

    private fun promptColor(c: ConversationStateContainer): Boolean {
        with(c) {
            color = blockingPrompt(HexColorArg) {
                "Current color is: ${color.toString()}\n" +
                        "Please input the new value or \"exit\" to cancel"
            }
        }

        return true
    }

}

private fun embedPrompt() = embed {
    title = "What would you like to do?"
    color = infoColor

    field {
        name = "1) Set Title"
        inline = true
    }
    field {
        name = "2) Set Description"
        inline = true
    }
    field {
        name = "3) Set Thumbnail"
        inline = true
    }
    field {
        name = "4) Set Image"
        inline = true
    }
    field {
        name = "5) Set Color"
        inline = true
    }
    field {
        name = "6) Add Field"
        inline = true
    }

    field {
        name = "\uD83D\uDD0D Preview"
        inline = true
    }
    field {
        name = "\uD83C\uDF89 Save"
        inline = true
    }
    field {
        name = "\u274C Cancel"
        inline = true
    }
}
