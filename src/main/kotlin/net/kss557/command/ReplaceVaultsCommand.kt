package net.kss557.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.VaultBlock
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object ReplaceVaultsCommand {

    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registryAccess: CommandBuildContext,
        environment: Commands.CommandSelection
    ) {

        dispatcher.register(
            Commands.literal("infinityvaults").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)).then(
                    Commands.literal("replace")

                        // /infinityvaults replace
                        .executes(ReplaceVaultsCommand::replaceVault)

                        // /infinityvaults replace ominous true/false
                        .then(
                            Commands.literal("ominous").then(
                                    Commands.argument(
                                        "value", BoolArgumentType.bool()
                                    ).executes(ReplaceVaultsCommand::replaceVault)
                                )
                        )

                        // /infinityvaults replace <pos>
                        .then(
                            Commands.argument(
                                "pos", BlockPosArgument.blockPos()
                            )

                                .executes(ReplaceVaultsCommand::replaceVault)

                                // /infinityvaults replace <pos> ominous true/false
                                .then(
                                    Commands.literal("ominous").then(
                                            Commands.argument(
                                                "value", BoolArgumentType.bool()
                                            ).executes(ReplaceVaultsCommand::replaceVault)
                                        )
                                )
                        )
                )
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun replaceVault(
        context: CommandContext<CommandSourceStack>
    ): Int {

        val source = context.source
        val sender = source.playerOrException
        val level = sender.level()

        val pos = try {
            BlockPosArgument.getBlockPos(context, "pos")
        } catch (_: Exception) {

            val hit = sender.pick(10.0, 0f, false)

            if (hit.type != HitResult.Type.BLOCK) {

                source.sendFailure(Component.translatable("infinityvaults.comand.vault.notfound"))

                return 0
            }
            (hit as BlockHitResult).blockPos
        }

        val oldState = level.getBlockState(pos)

        if (oldState.block !is VaultBlock) {
            source.sendFailure(Component.translatable("infinityvaults.comand.vault.notfound"))
            return 0
        }

        val oldEntity = level.getBlockEntity(pos)

        if (oldEntity !is VaultBlockEntity) {
            source.sendFailure(Component.translatable("infinityvaults.comand.vault.notfound"))
            return 0
        }

        val newOminous = try {
            BoolArgumentType.getBool(context, "value")
        } catch (_: Exception) {
            oldState.getValue(VaultBlock.OMINOUS)
        }

        val tag: CompoundTag = oldEntity.saveWithoutMetadata(level.registryAccess())

        tag.remove("server_data")
        tag.remove("shared_data")

        val facing = oldState.getValue(VaultBlock.FACING)
        val state = oldState.getValue(VaultBlock.STATE)

        level.removeBlockEntity(pos)

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)

        val newState: BlockState =
            Blocks.VAULT.defaultBlockState().setValue(VaultBlock.FACING, facing).setValue(VaultBlock.STATE, state)
                .setValue(VaultBlock.OMINOUS, newOminous)

        level.setBlock(pos, newState, Block.UPDATE_ALL)

        val newEntity = level.getBlockEntity(pos) as? VaultBlockEntity

        if (newEntity != null) {

            val configTag = CompoundTag()

            if (newOminous) {

                configTag.put(
                    "key_item", CompoundTag().apply {
                        putString(
                            "id", "minecraft:ominous_trial_key"
                        )

                        putInt("count", 1)
                    })

                configTag.putString(
                    "loot_table", "minecraft:chests/trial_chambers/reward_ominous"
                )

            } else {

                configTag.put(
                    "key_item", CompoundTag().apply {
                        putString(
                            "id", "minecraft:trial_key"
                        )

                        putInt("count", 1)
                    })

                configTag.putString(
                    "loot_table", "minecraft:chests/trial_chambers/reward"
                )
            }

            tag.put("config", configTag)

            newEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag))

            newEntity.setChanged()
        }

        level.sendBlockUpdated(
            pos, oldState, newState, Block.UPDATE_ALL
        )

        source.sendSuccess({ Component.translatable("") }, true)

        return 1
    }
}