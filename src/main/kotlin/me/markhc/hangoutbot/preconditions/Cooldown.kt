package me.markhc.hangoutbot.preconditions

import me.jakejmattson.kutils.api.annotations.Precondition
import me.jakejmattson.kutils.api.dsl.preconditions.Fail
import me.jakejmattson.kutils.api.dsl.preconditions.Pass
import me.jakejmattson.kutils.api.dsl.preconditions.precondition
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.services.PersistentData
import org.joda.time.DateTime

val cooldownMap = mutableMapOf<Long, Long>()

@Precondition(1)
fun produceCooldownPrecondition(botConfiguration: BotConfiguration, persistentData: PersistentData) = precondition { event ->
    if(event.command == null)
        return@precondition Pass
    if(event.author.id == botConfiguration.ownerId)
        return@precondition Pass

    val cd = event.guild?.let {
        persistentData.getGuildProperty(it) { cooldown }
    } ?: 5

    if(cooldownMap[event.author.idLong] != null) {
        val diff = DateTime.now().millis - cooldownMap[event.author.idLong]!!

        if(diff < cd * 1000) {
            return@precondition Fail("Cooldown! You must wait ${(cd * 1000 - diff) / 1000} seconds before doing that!")
        }
    }

    cooldownMap[event.author.idLong] = DateTime.now().millis

    return@precondition Pass
}