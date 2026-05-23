package net.kss557.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.kss557.InfinityVaults
import net.kss557.VaultsAccess
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object ClearVaultsCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, registryAccess: CommandBuildContext, environment: Commands.CommandSelection) {
        dispatcher.register(
            Commands.literal("infinityvaults").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(
                    Commands.literal("clear")
                        .executes(ClearVaultsCommand::clearVault)
                        .then(Commands.argument("player", EntityArgument.player()).executes(ClearVaultsCommand::clearVault))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes(ClearVaultsCommand::clearVault)
                            .then(Commands.argument("player", EntityArgument.player()).executes(ClearVaultsCommand::clearVault)))
                )
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun clearVault(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val sender = source.playerOrException

        val targetPlayer = if (context.nodes.any { it.node.name == "player" }) {
            EntityArgument.getPlayer(context, "player")
        } else {
            null
        }

        InfinityVaults.LOGGER.info(targetPlayer?.name?.string)

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

        val blockEntity = sender.level().getBlockEntity(pos)

        if (blockEntity !is VaultBlockEntity) {
            source.sendFailure(Component.translatable("infinityvaults.comand.vault.notfound"))
            return 0
        }

        val serverData = blockEntity.serverData

        if (serverData == null) {
            source.sendFailure(Component.translatable("infinityvaults.comand.server.notfound"))
            return 0
        }

        val access = serverData as VaultsAccess

        if (targetPlayer != null) {

            access.clearVaultForPlayer(targetPlayer)

            source.sendSuccess(
                { Component.translatable("infinityvaults.comand.vault.clear.player").append(Component.literal(" ${targetPlayer.name.string}")) },
                true
            )

        } else {

            access.clearVaultForAllPlayers()

            source.sendSuccess(
                { Component.translatable("infinityvaults.comand.vault.clear.allplayers") },
                true
            )
        }

        return 1
    }
}