package me.markhc.hangoutbot.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class BotConfiguration(var prefix: String = "++",
                            val ownerId: String = "")