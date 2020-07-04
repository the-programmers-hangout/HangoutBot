package me.markhc.hangoutbot.preconditions

import me.jakejmattson.kutils.api.annotations.Precondition
import me.jakejmattson.kutils.api.dsl.preconditions.Fail
import me.jakejmattson.kutils.api.dsl.preconditions.Pass
import me.jakejmattson.kutils.api.dsl.preconditions.precondition
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import org.joda.time.DateTime

val cooldownMap = mutableMapOf<Long, Long>()

@Precondition(1)
fun produceCooldownPrecondition(botConfiguration: BotConfiguration, persistentData: PersistentData, permissionsService: PermissionsService) = precondition { event ->
    if(event.command == null)
        return@precondition Pass

    val member = event.guild?.getMember(event.author)

    if(member != null && permissionsService.getPermissionLevel(member) >= PermissionLevel.Staff)
        return@precondition Pass

    val cd = event.guild?.let {
        persistentData.getGuildProperty(it) { cooldown }
    } ?: 5

    if(cooldownMap[event.author.idLong] != null) {
        val diff = DateTime.now().millis - cooldownMap[event.author.idLong]!!

        if(diff < cd * 1000) {
            return@precondition Fail("You're doing that too quickly. (${String.format("%.2f", (cd * 1000 - diff) / 1000.0f)} s)")
        }
    }

    cooldownMap[event.author.idLong] = DateTime.now().millis

    return@precondition Pass
}