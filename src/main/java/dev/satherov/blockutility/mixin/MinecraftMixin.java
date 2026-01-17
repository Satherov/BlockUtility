package dev.satherov.blockutility.mixin;

import dev.satherov.blockutility.BlockUtilityClient;
import dev.satherov.blockutility.config.BlockUtilityConfig;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    
    @ModifyConstant(
            method = "startUseItem",
            constant = @Constant(intValue = 4)
    )
    private int modifyPlacementDelay(int original) {
        return BlockUtilityClient.isCustomPlace() ? BlockUtilityConfig.Client.getPlaceDelay() : original;
    }
}
