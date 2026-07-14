package net.kss557.command.getItem

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.kss557.config.ModConfigs
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.VaultBlock
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object GetItemCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, registryAccess: CommandBuildContext, environment: Commands.CommandSelection) {
        dispatcher.register(
            Commands.literal("infinityvaults").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(
                    Commands.literal("getitem").then(
                        Commands.argument("item", ItemArgument.item(registryAccess)).executes(GetItemCommand::getItem)
                        .suggests { context, builder ->

                            val source = context.source

                            val player =
                                try {
                                    source.playerOrException
                                } catch (_: Exception) {
                                    return@suggests builder.buildFuture()
                                }

                            val hit = player.pick(10.0, 0f, false)

                            if (hit.type != HitResult.Type.BLOCK) {
                                return@suggests builder.buildFuture()
                            }

                            val pos = (hit as BlockHitResult).blockPos

                            val level = player.level()

                            val state = level.getBlockState(pos)

                            if (state.block !is VaultBlock) {
                                return@suggests builder.buildFuture()
                            }

                            val ominous = state.getValue(VaultBlock.OMINOUS)

                            val lootTableKey: ResourceKey<LootTable> =
                                ResourceKey.create(
                                    Registries.LOOT_TABLE,
                                    Identifier.withDefaultNamespace(
                                        if (ominous)
                                            "chests/trial_chambers/reward_ominous"
                                        else
                                            "chests/trial_chambers/reward"
                                    )
                                )

                            val lootTable = level.server
                                    .reloadableRegistries()
                                    .getLootTable(lootTableKey)

                            val generated = HashSet<Identifier>()

                            repeat(300) {

                                val items = lootTable.getRandomItems(
                                    LootParams.Builder(level)
                                        .withParameter(
                                            LootContextParams.ORIGIN,
                                            player.position()
                                        )
                                        .create(
                                            LootContextParamSets.CHEST
                                        )
                                    )

                                for (stack in items) {

                                    if (stack.isEmpty) {
                                        continue
                                    }

                                    generated.add(BuiltInRegistries.ITEM.getKey(stack.item))
                                }
                            }

                            SharedSuggestionProvider.suggestResource(
                                generated,
                                builder
                            )
                        })
                )
        )
    }

    @Throws(CommandSyntaxException::class)
    fun getItem(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val sender = source.playerOrException
        val level = source.level

        if (!ModConfigs.DROP_DISPLAYED_ITEM)
        {
            source.sendFailure(
                Component.translatable(
                    "infinityvaults.command.getitem.dropdisplayeditem.off",
                )
            )
            return 0
        }

        val hit = sender.pick(5.0, 0f, false)

        if (hit.type != HitResult.Type.BLOCK) {
            source.sendFailure(Component.translatable("infinityvaults.comand.vault.notfound"))
            return 0
        }

        val pos = (hit as BlockHitResult).blockPos

        val state = level.getBlockState(pos)

        if (state.block !is VaultBlock) {
            source.sendFailure(
                Component.translatable(
                    "infinityvaults.command.vault.notfound"
                )
            )

            return 0
        }

        val blockEntity = level.getBlockEntity(pos)

        if (blockEntity !is VaultBlockEntity) {
            source.sendFailure(
                Component.translatable(
                    "infinityvaults.command.vault.notfound"
                )
            )

            return 0
        }

        val ominous = state.getValue(VaultBlock.OMINOUS)

        val keyItem =
            if (ominous) {
                net.minecraft.world.item.Items.OMINOUS_TRIAL_KEY

            } else {
                net.minecraft.world.item.Items.TRIAL_KEY
            }

        val item = ItemArgument.getItem(context, "item").item.value()

        VaultItemWatcher.add(
            VaultItemWatcher.Watch(
                playerId = sender.uuid,
                pos = pos,
                level = level,
                wantedItem = item,
                keyItem = keyItem,
                startTime = VaultItemWatcher.serverTickCount
            )
        )

        source.sendSuccess(
            {
                Component.translatable(
                    "infinityvaults.command.getitem.searching"
                )
            },
            false
        )

        return 1
    }
}
