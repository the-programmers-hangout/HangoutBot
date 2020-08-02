package me.markhc.hangoutbot.commands.administration.services

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory

@Service
class ScriptEngineService(discord: Discord) {
    val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    init {
        setIdeaIoUseFallback()

        with(engine) {
            put("discord", discord)
            eval("println(\"ScriptEngineService online!\")")
        }
    }
}