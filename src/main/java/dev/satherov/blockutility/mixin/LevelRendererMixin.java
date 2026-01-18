package dev.satherov.blockutility.mixin;

import dev.satherov.blockutility.config.BlockUtilityConfig;

import net.minecraft.client.renderer.LevelRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = LevelRenderer.class, priority = 1100)
public class LevelRendererMixin {
    
    @ModifyArgs(
            method = "renderHitOutline",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V")
    )
    private void modifyOutlineColor(Args args) {
        args.set(6, (float) BlockUtilityConfig.Client.Outline.getRed() / 255);   // red
        args.set(7, (float) BlockUtilityConfig.Client.Outline.getGreen() / 255); // green
        args.set(8, (float) BlockUtilityConfig.Client.Outline.getBlue() / 255);  // blue
        args.set(9, (float) BlockUtilityConfig.Client.Outline.getAlpha() / 255); // alpha
    }
}
