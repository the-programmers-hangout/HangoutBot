package me.markhc.hangoutbot.conversations

import com.gitlab.kordlib.core.entity.Guild
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.conversation
import me.markhc.hangoutbot.services.PersistentData

class ConfigurationConversation(private val persistentData: PersistentData) {
    fun createConfigurationConversation(guild: Guild) = conversation {
        val prefix = promptMessage(EveryArg, "Bot prefix:")
        val welcomeChannel = promptMessage(ChannelArg, "Welcome channel:")
        val loggingChannel = promptMessage(ChannelArg, "Logging channel:")
        val muteRole = promptMessage(RoleArg, "Mute role:")
        val softMuteRole = promptMessage(RoleArg, "Soft mute role:")

        persistentData.setup(guild, prefix, welcomeChannel, loggingChannel, muteRole, softMuteRole)
    }
}