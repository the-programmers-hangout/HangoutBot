package me.markhc.hangoutbot.dataclasses

import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.api.dsl.Data

data class BotConfiguration(var prefix: String = "++",
                            val ownerId: String = "") : Data("config/config.json")

@Serializable
data class Properties(val version: String = "",
                      val discordkt: String = "",
                      val repository: String = "")