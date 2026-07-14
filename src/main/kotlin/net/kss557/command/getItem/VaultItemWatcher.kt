package net.kss557.command.getItem

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.VaultBlock
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.UUID

object VaultItemWatcher {

    private const val MAX_DISTANCE = 5.0
    private const val MAX_TICKS = 6000L

    data class Watch(
        val playerId: UUID,
        val pos: BlockPos,
        val level: ServerLevel,
        val wantedItem: Item,
        val keyItem: Item,
        val startTime: Long
    )

    private val watches = mutableListOf<Watch>()

    fun add(watch: Watch) {
        watches.removeAll { it.playerId == watch.playerId }
        watches.add(watch)
    }

    fun remove(playerId: UUID) {
        watches.removeAll { it.playerId == playerId }
    }

    fun tick(server: MinecraftServer) {

        val iterator = watches.iterator()

        while (iterator.hasNext()) {

            val watch = iterator.next()

            val player =
                server.playerList.getPlayer(watch.playerId)
                    ?: run {
                        iterator.remove()
                        continue
                    }

            val level = watch.level

            if (level != player.level()) {
                iterator.remove()
                continue
            }

            val state = level.getBlockState(watch.pos)

            if (state.block !is VaultBlock) {
                iterator.remove()
                continue
            }

            val elapsed = serverTickCount - watch.startTime

            if (elapsed >= MAX_TICKS) {
                player.sendSystemMessage(
                    Component.translatable(
                        "infinityvaults.command.getitem.timeout"
                    )
                )
                iterator.remove()
                continue
            }

            if (!player.mainHandItem.`is`(watch.keyItem)) {
                player.sendSystemMessage(
                    Component.translatable(
                        "infinityvaults.command.getitem.notfoundkey"
                    )
                )
                iterator.remove()
                continue
            }

            val hit = player.pick(MAX_DISTANCE, 0f, false)

            if (hit.type != HitResult.Type.BLOCK ||
                (hit as BlockHitResult).blockPos != watch.pos
            ) {
                player.sendSystemMessage(
                    Component.translatable(
                        "infinityvaults.command.getitem.disabled"
                    )
                )
                iterator.remove()
                continue
            }

            val playerPos = player.eyePosition
            val vaultCenter = Vec3.atCenterOf(watch.pos)

            if (playerPos.distanceTo(vaultCenter) > MAX_DISTANCE) {
                player.sendSystemMessage(
                    Component.translatable(
                        "infinityvaults.command.getitem.toofar"
                    )
                )
                iterator.remove()
                continue
            }

            val be =
                level.getBlockEntity(watch.pos) as? VaultBlockEntity
                    ?: run {
                        iterator.remove()
                        continue
                    }

            val displayed = be.sharedData.displayItem

            if (displayed.isEmpty ||
                !ItemStack.isSameItemSameComponents(
                    displayed,
                    ItemStack(watch.wantedItem)
                )
            ) {
                continue
            }

            val keyStack = ItemStack(watch.keyItem)

            val result =
                player.gameMode.useItemOn(
                    player,
                    level,
                    keyStack,
                    InteractionHand.MAIN_HAND,
                    hit
                )

            if (!result.consumesAction()) {
                player.sendSystemMessage(
                    Component.translatable(
                        "infinityvaults.command.getitem.failed"
                    )
                )
                iterator.remove()
                continue
            }

            player.sendSystemMessage(
                Component.translatable(
                    "infinityvaults.command.getitem.success"
                )
            )

            iterator.remove()
        }
    }

    @Volatile
    var serverTickCount: Long = 0L
        private set

    fun onServerTick() {
        serverTickCount++
    }
}
