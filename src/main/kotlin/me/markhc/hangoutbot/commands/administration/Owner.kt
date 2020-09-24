package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.dsl.command.commands
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.dsl.command.GenericContainer
import me.markhc.hangoutbot.commands.administration.services.ScriptEngineService
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import me.markhc.hangoutbot.services.requiredPermissionLevel
import javax.script.ScriptContext
import javax.script.ScriptEngine

@CommandSet("Owner Commands")
fun ownerCommands(persistentData: PersistentData, scriptEngineService: ScriptEngineService) = commands {
    command("cooldown") {
        description = "Gets or sets the command cooldown period (in seconds)."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(IntegerArg.makeNullableOptional(null)) {
            val (cd) = it.args

            if (cd != null) {
                if (cd < 1) {
                    return@execute it.respond("Cooldown cannot be less than 1 second!")
                }
                if (cd > 3600) {
                    return@execute it.respond("Cooldown cannot be more than 1 hour!")
                }

                persistentData.setGuildProperty(it.guild!!) {
                    cooldown = cd.toInt()
                }

                it.respond("Command cooldown set to $cd seconds")
            } else {
                val value = persistentData.getGuildProperty(it.guild!!) {
                    cooldown
                }
                it.respond("Command cooldown is $value seconds")
            }
        }
    }

    command("setprefix") {
        description = "Sets the bot prefix."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        execute(AnyArg("Prefix")) {
            persistentData.setGuildProperty(it.guild!!) {
                prefix = it.args.first
            }

            it.respond("Bot prefix set to **${it.args.first}**")
        }
    }

    command("eval") {
        description = "Evaluates a script"
        requiredPermissionLevel = PermissionLevel.BotOwner
        requiresGuild = true
        execute(EveryArg) {
            evalCommand(scriptEngineService.engine, it)
        }
    }
}

fun <T: GenericContainer> evalCommand(
        engine: ScriptEngine,
        commandEvent: CommandEvent<T>) {

    val input = commandEvent.message.contentRaw
            .removePrefix(commandEvent.relevantPrefix)
            .removePrefix(commandEvent.rawInputs.commandName)

    try {
        val bindings = engine.createBindings()
        bindings["discord"] = commandEvent.discord
        bindings["container"] = commandEvent.container
        bindings["event"] = commandEvent

        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        engine.eval(
                """
                val discord = bindings["discord"] as me.jakejmattson.discordkt.api.Discord
                val container = bindings["container"] as me.jakejmattson.discordkt.api.dsl.command.CommandsContainer
                val event = bindings["event"] as me.jakejmattson.discordkt.api.dsl.command.CommandEvent<*>
                val jda = discord.jda
                
                fun evalScript() {
                    $input
                } 
                
                evalScript();
                """.trimIndent()
        )
    } catch(e: Exception) {
        System.err.print(e.message ?: "An exception occurred in the scripting engine.")
    }
}