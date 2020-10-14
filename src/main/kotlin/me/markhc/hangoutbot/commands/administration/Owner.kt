package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.commands.administration.services.ScriptEngineService
import me.markhc.hangoutbot.services.*

import javax.script.*

fun ownerCommands(persistentData: PersistentData, scriptEngineService: ScriptEngineService) = commands("Owner Commands") {
    guildCommand("cooldown") {
        description = "Gets or sets the command cooldown period (in seconds)."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        execute(IntegerArg.makeNullableOptional(null)) {
            val (cd) = args

            if (cd != null) {
                if (cd < 1) {
                    respond("Cooldown cannot be less than 1 second!")
                    return@execute
                }
                if (cd > 3600) {
                    respond("Cooldown cannot be more than 1 hour!")
                    return@execute
                }

                persistentData.setGuildProperty(guild) {
                    cooldown = cd.toInt()
                }

                respond("Command cooldown set to $cd seconds")
            } else {
                val value = persistentData.getGuildProperty(guild) {
                    cooldown
                }
                respond("Command cooldown is $value seconds")
            }
        }
    }

    guildCommand("setprefix") {
        description = "Sets the bot prefix."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        execute(AnyArg("Prefix")) {
            val newPrefix = args.first

            persistentData.setGuildProperty(guild) {
                prefix = newPrefix
            }

            respond("Bot prefix set to **$newPrefix**")
        }
    }

    guildCommand("eval") {
        description = "Evaluates a script"
        requiredPermissionLevel = PermissionLevel.BotOwner
        execute(EveryArg) {
            //evalCommand(scriptEngineService.engine, this)
        }
    }
}

suspend fun <T : TypeContainer> evalCommand(
    engine: ScriptEngine,
    commandEvent: CommandEvent<T>) {

    val input = commandEvent.message.content
        .removePrefix(commandEvent.prefix())
        .removePrefix(commandEvent.rawInputs.commandName)

    try {
        val bindings = engine.createBindings()
        bindings["discord"] = commandEvent.discord
        bindings["commands"] = commandEvent.discord.commands
        bindings["event"] = commandEvent

        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        engine.eval(
            """
                val discord = bindings["discord"] as me.jakejmattson.discordkt.api.Discord
                val commands = bindings["commands"] as MutableList<me.jakejmattson.discordkt.api.dsl.Command>
                val event = bindings["event"] as me.jakejmattson.discordkt.api.dsl.CommandEvent<*>
                val kord = discord.api
                
                fun evalScript() {
                    $input
                } 
                
                evalScript();
                """.trimIndent()
        )
    } catch (e: Exception) {
        System.err.print(e.message ?: "An exception occurred in the scripting engine.")
    }
}