package net.kss557

import net.minecraft.world.entity.player.Player

interface VaultsAccess {
    fun tickCleanup(maxTicks: Long)
    fun clearVaultForAllPlayers()
    fun clearVaultForPlayer(player: Player)
}