package me.markhc.hangoutbot.locale

import kotlin.random.Random

object Messages {
    val COMMAND_NOT_SUPPORTED_IN_DMS = "This command is not supported in DMs"
    private val FLIP_RESPONSE_CHOICES = listOf(
            "Hmm, I'd say %choice%.",
            "%choice%, no doubt.",
            "Perhaps... %choice%.",
            "%choice% sounds good to me.",
            "If it were up to me, I'd go with %choice%"
    )

    private val JOIN_MESSAGE_CHOICES = listOf(
            "Aww yeeee it\u0027s %name%",
            "Is that a person! Hey, %name% !",
            "Doug Dimmadome, owner of the Dimmsdale Dimmadome? Oh... no, it\u0027s just %name% :|",
            "It\u0027s dangerous to go alone %name%! Take this.",
            "ლ(｀ー´ლ) you wanna go %name% ?!",
            "(╬ ಠ益ಠ) %name%, take your shoes off dammit.",
            "Ayy lmao it\u0027s %name%... Say Hi!",
            "%name% I\u0027m going to have to ask you a very serious question... do you pronounce C# " +
                    "like C-hashtag... DO YOU!?! Because we do NOT need another one of them guys.",
            "%name% has come to save the day!",
            "%name% joined the party.",
            "%name% joined the guild.",
            "Quick everyone! %name% has bepis!",
            "Initializing welcome system for %name%...",
            "Hide! %name% uses tabs!",
            "Hide! %name% uses spaces!",
            "Is that? No, it can\u0027t be... Are you %name%?!?!?",
            "01010111 01100101 01101100 01100011 01101111 01101101 01100101 %name%",
            "Hold on %name%, your welcome message is compiling...",
            "Here\u0027s your sword, shield, helmet, now go %name%, and vanquish those bugs!"
    )

    const val welcomeDescription: String = "Hey there, be sure to check out the info section at the top!"

    fun getRandomFlipMessage(choice: String) =
            FLIP_RESPONSE_CHOICES[Random.nextInt(FLIP_RESPONSE_CHOICES.size)].replace("%choice%", choice)
    fun getRandomJoinMessage(name: String) =
            JOIN_MESSAGE_CHOICES[Random.nextInt(JOIN_MESSAGE_CHOICES.size)].replace("%name%", name)
}