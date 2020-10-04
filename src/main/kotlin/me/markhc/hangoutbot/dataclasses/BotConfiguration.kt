package me.markhc.hangoutbot.dataclasses

import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.api.dsl.Data

@Serializable
data class BotConfiguration(var prefix: String = "++",
                            val ownerId: String = "")

@Serializable
data class Properties(val version: String = "",
                      val discordkt: String = "",
                      val repository: String = "")