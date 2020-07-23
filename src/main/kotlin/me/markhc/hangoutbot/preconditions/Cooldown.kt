package me.markhc.hangoutbot.preconditions

import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.dsl.preconditions.Fail
import me.jakejmattson.kutils.api.dsl.preconditions.Pass
import me.jakejmattson.kutils.api.dsl.preconditions.Precondition
import me.jakejmattson.kutils.api.dsl.preconditions.PreconditionResult
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PermissionsService
import me.markhc.hangoutbot.services.PersistentData
import org.joda.time.DateTime

class Cooldown(private val botConfiguration: BotConfiguration,
               private val persistentData: PersistentData,
               private val permissionsService: PermissionsService) : Precondition() {
    val cooldownMap = mutableMapOf<Long, Long>()

    override fun evaluate(event: CommandEvent<*>): PreconditionResult {
        if (event.command == null)
            return Pass

        val member = event.guild?.getMember(event.author)

        if (member != null && permissionsService.getPermissionLevel(member) >= PermissionLevel.Staff)
            return Pass

        val cd = event.guild?.let {
            persistentData.getGuildProperty(it) { cooldown }
        } ?: 5

        if (cooldownMap[event.author.idLong] != null) {
            val diff = DateTime.now().millis - cooldownMap[event.author.idLong]!!

            if (diff < cd * 1000) {
                return Fail("You're doing that too quickly. (${String.format("%.2f", (cd * 1000 - diff) / 1000.0f)} s)")
            }
        }

        cooldownMap[event.author.idLong] = DateTime.now().millis

        return Pass
    }
}