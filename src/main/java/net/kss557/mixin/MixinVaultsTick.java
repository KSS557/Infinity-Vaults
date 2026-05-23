package net.kss557.mixin;

import net.kss557.VaultsAccess;
import net.kss557.config.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VaultBlockEntity.Server.class)
public class MixinVaultsTick {

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private static void onTick(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            VaultConfig config,
            VaultServerData serverData,
            VaultSharedData sharedData,
            CallbackInfo ci
    ) {

        if (serverData instanceof VaultsAccess access) {
            if (ModConfigs.INSTANCE.getVAULT_COOLDOWN() <= -1) return;
            access.tickCleanup(ModConfigs.INSTANCE.getVAULT_COOLDOWN());
        }
    }
}
