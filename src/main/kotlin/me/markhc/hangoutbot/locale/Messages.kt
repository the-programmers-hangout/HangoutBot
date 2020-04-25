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
    fun getRandomFlipMessage(choice: String) =
            FLIP_RESPONSE_CHOICES[Random.nextInt(FLIP_RESPONSE_CHOICES.size)].replace("%choice%", choice)
}