package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.precondition
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import org.joda.time.DateTime
import kotlin.collections.set

val cooldownMap = mutableMapOf<Long, Long>()

fun cooldown(persistentData: PersistentData,
             permissionsService: PermissionsService) = precondition {
    command ?: return@precondition

    val member = guild?.getMember(author.id)

    if (member != null && permissionsService.getPermissionLevel(member) >= PermissionLevel.Staff)
        return@precondition

    val cd = guild?.let {
        persistentData.getGuildProperty(it) { cooldown }
    } ?: 5

    if (cooldownMap[author.id.longValue] != null) {
        val diff = DateTime.now().millis - cooldownMap[author.id.longValue]!!

        if (diff < cd * 1000) {
            return@precondition fail("You're doing that too quickly. (${String.format("%.2f", (cd * 1000 - diff) / 1000.0f)} s)")
        }
    }

    cooldownMap[author.id.longValue] = DateTime.now().millis

    return@precondition

}