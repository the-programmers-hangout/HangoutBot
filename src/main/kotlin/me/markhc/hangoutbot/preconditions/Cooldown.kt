package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.precondition
import me.markhc.hangoutbot.services.BotConfiguration
import me.markhc.hangoutbot.services.PersistentData
import org.joda.time.DateTime
import java.sql.Timestamp

val cooldownMap = mutableMapOf<Long, Long>()

@Precondition
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