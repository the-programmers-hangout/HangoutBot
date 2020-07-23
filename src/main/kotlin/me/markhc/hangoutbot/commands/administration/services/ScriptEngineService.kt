package me.markhc.hangoutbot.commands.administration.services

import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.annotations.Service
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