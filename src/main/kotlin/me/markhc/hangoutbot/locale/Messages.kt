package me.markhc.hangoutbot.locale

import kotlin.random.Random

object Messages {
    const val INSUFFICIENT_PERMS = "Sorry, you do not have the required permissions."
    private val FLIP_RESPONSE_CHOICES = listOf(
        "Hmm, I'd say %choice%.",
        "%choice%, no doubt.",
        "Perhaps... %choice%.",
        "%choice% sounds good to me.",
        "If it were up to me, I'd go with %choice%",
        "East or west, %choice% is the best."
    )

    fun getRandomFlipMessage(choice: String) =
        FLIP_RESPONSE_CHOICES[Random.nextInt(FLIP_RESPONSE_CHOICES.size)].replace("%choice%", choice)
}
