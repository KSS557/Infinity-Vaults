package net.kss557.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.kss557.config.ModConfigs
import java.util.concurrent.CompletableFuture

object ConfigCommand {

    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registryAccess: CommandBuildContext,
        environment: Commands.CommandSelection
    ) {
        dispatcher.register(
            Commands.literal("infinityvaults").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(
                    Commands.literal("config")
                        .executes(ConfigCommand::getConfig)

                        .then(
                            Commands.argument("setting", StringArgumentType.word())
                                .suggests { _: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder ->
                                    SharedSuggestionProvider.suggest(
                                        ModConfigs.configEntries.keys,
                                        builder
                                    )
                                }
                                .then(
                                    Commands.argument("value", StringArgumentType.greedyString())
                                        .suggests(ConfigCommand::suggestValue)
                                        .executes(ConfigCommand::setValue)
                                )
                        )
                )
        )
    }

    private fun getConfig(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source

        val lines = ModConfigs.configEntries.values.joinToString("\n") { entry ->
            val value = entry.get()
            val default = entry.defaultValue
            val isDefault = value == default
            val suffix = if (isDefault) " (default)" else ""
            "§7  ${entry.key}: §f$value$suffix"
        }

        source.sendSuccess(
            { Component.literal("§6Config:\n$lines") },
            false
        )

        return 1
    }

    private fun suggestValue(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val setting = try {
            context.getArgument("setting", String::class.java)
        } catch (_: Exception) {
            return builder.buildFuture()
        }

        val entry = ModConfigs.configEntries[setting] ?: return builder.buildFuture()

        val current = entry.get().toString()
        val default = entry.defaultValue.toString()

        if (builder.remaining.isEmpty()) {
            when (entry.type) {
                ModConfigs.ConfigType.BOOLEAN -> {
                    builder.suggest("true")
                    builder.suggest("false")
                }
                ModConfigs.ConfigType.LONG -> {
                    builder.suggest(default) { "default: $default" }
                    if (default != current) {
                        builder.suggest(current) { "current: $current" }
                    }
                    builder.suggest("-1") { "disable" }
                }
            }
        }

        return builder.buildFuture()
    }

    private fun setValue(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source

        val setting = try {
            context.getArgument("setting", String::class.java)
        } catch (_: Exception) {
            source.sendFailure(Component.translatable("infinityvaults.command.config.notfound"))
            return 0
        }

        val entry = ModConfigs.configEntries[setting]
        if (entry == null) {
            source.sendFailure(Component.translatable("infinityvaults.command.config.notfound"))
            return 0
        }

        val rawValue = StringArgumentType.getString(context, "value").trim()

        val parsed: Any = when (entry.type) {
            ModConfigs.ConfigType.BOOLEAN -> {
                when (rawValue.lowercase()) {
                    "true" -> true
                    "false" -> false
                    else -> {
                        source.sendFailure(
                            Component.translatable(
                                "infinityvaults.command.config.invalid.bool",
                                entry.key
                            )
                        )
                        return 0
                    }
                }
            }
            ModConfigs.ConfigType.LONG -> {
                val longVal = rawValue.toLongOrNull()
                if (longVal == null) {
                    source.sendFailure(
                        Component.translatable(
                            "infinityvaults.command.config.invalid.long",
                            entry.key
                        )
                    )
                    return 0
                }
                longVal
            }
        }

        entry.set(parsed)

        val displayValue = when (parsed) {
            is Long -> if (parsed <= -1L) "disabled" else "$parsed ticks"
            else -> parsed.toString()
        }

        source.sendSuccess(
            {
                Component.translatable(
                    "infinityvaults.command.config.set",
                    entry.key,
                    displayValue
                )
            },
            true
        )

        return 1
    }
}
