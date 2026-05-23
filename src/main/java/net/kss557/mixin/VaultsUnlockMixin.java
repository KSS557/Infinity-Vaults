package net.kss557.mixin;

import net.kss557.config.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VaultBlockEntity.Server.class)
public class VaultsUnlockMixin {

    @Inject(
            method = "unlock",
            at = @At("HEAD")
    )
    private static void lockFirstDisplayedItem(
            ServerLevel serverLevel,
            BlockState blockState,
            BlockPos pos,
            VaultConfig config,
            VaultServerData serverData,
            VaultSharedData sharedData,
            List<ItemStack> itemsToEject,
            CallbackInfo ci
    ) {
        if(!ModConfigs.INSTANCE.getDROP_DISPLAYED_ITEM()) return;
        if (itemsToEject == null || itemsToEject.isEmpty()) return;
        ItemStack displayed = sharedData.getDisplayItem();
        if (displayed.isEmpty()) return;
        itemsToEject.addLast(displayed);
    }
}
