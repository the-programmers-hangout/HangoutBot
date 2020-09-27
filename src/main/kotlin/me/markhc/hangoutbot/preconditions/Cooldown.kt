package me.markhc.hangoutbot.preconditions

import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.services.*
import org.joda.time.DateTime

class Cooldown(private val botConfiguration: BotConfiguration,
               private val persistentData: PersistentData,
               private val permissionsService: PermissionsService) : Precondition() {
    val cooldownMap = mutableMapOf<Long, Long>()

    override suspend fun evaluate(event: GlobalCommandEvent<*>): PreconditionResult {
        if (event.command == null)
            return Pass

        val member = event.guild?.getMember(event.author.id)

        if (member != null && permissionsService.getPermissionLevel(member) >= PermissionLevel.Staff)
            return Pass

        val cd = event.guild?.let {
            persistentData.getGuildProperty(it) { cooldown }
        } ?: 5

        if (cooldownMap[event.author.id.longValue] != null) {
            val diff = DateTime.now().millis - cooldownMap[event.author.id.longValue]!!

            if (diff < cd * 1000) {
                return Fail("You're doing that too quickly. (${String.format("%.2f", (cd * 1000 - diff) / 1000.0f)} s)")
            }
        }

        cooldownMap[event.author.id.longValue] = DateTime.now().millis

        return Pass
    }
}