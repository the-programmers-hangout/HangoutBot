package me.markhc.hangoutbot.dataclasses

import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.dsl.Data

@Serializable
data class BotConfiguration(var prefix: String = "++",
                            val ownerId: String = "") : Data()