package net.kss557.mixin;

import net.kss557.VaultsAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(VaultServerData.class)
public abstract class MixinVaultsServerData
        implements VaultsAccess {

    @Shadow
    @Final
    private Set<UUID> rewardedPlayers;

    @Unique
    private final Map<UUID, Long> playerTicks = new HashMap<>();

    @Inject(method = "addToRewardedPlayers(Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private void onAddToRewardedPlayers(Player player, CallbackInfo ci) {

        UUID id = player.getUUID();

        rewardedPlayers.add(id);
        playerTicks.put(id, 0L);
    }

    @Override
    public void tickCleanup(long maxTicks) {

        Iterator<Map.Entry<UUID, Long>> it = playerTicks.entrySet().iterator();

        while (it.hasNext()) {

            Map.Entry<UUID, Long> entry = it.next();

            long newTicks = entry.getValue() + 1;
            entry.setValue(newTicks);

            if (newTicks >= maxTicks) {

                UUID id = entry.getKey();

                rewardedPlayers.remove(id);
                it.remove();
            }
        }
    }

    @Override
    public void clearVaultForAllPlayers() {
        rewardedPlayers.clear();
        playerTicks.clear();
    }

    @Override
    public void clearVaultForPlayer(Player player) {
        UUID uuid = player.getUUID();
        rewardedPlayers.remove(uuid);
        playerTicks.remove(uuid);
    }
}
