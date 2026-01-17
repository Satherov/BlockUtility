package dev.satherov.blockutility.mixin;

import dev.satherov.blockutility.config.BlockUtilityConfig;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LevelRenderer.class, priority = 1100)
public class LevelRendererMixin {
    
    @Shadow
    private ClientLevel level;
    
    /**
     * @author Satherov
     * @reason Custom Block Outline Rendering
     */
    @Overwrite
    private void renderHitOutline(
            PoseStack poseStack,
            VertexConsumer consumer,
            Entity entity,
            double camX,
            double camY,
            double camZ,
            BlockPos pos,
            BlockState state
    ) {
        final float a = ((float) BlockUtilityConfig.Client.Outline.getAlpha() / 255);
        final float r = ((float) BlockUtilityConfig.Client.Outline.getRed() / 255);
        final float g = ((float) BlockUtilityConfig.Client.Outline.getGreen() / 255);
        final float b = ((float) BlockUtilityConfig.Client.Outline.getBlue() / 255);
        
        renderShape(
                poseStack,
                consumer,
                state.getShape(this.level, pos, CollisionContext.of(entity)),
                (double) pos.getX() - camX,
                (double) pos.getY() - camY,
                (double) pos.getZ() - camZ,
                r, g, b, a
        );
    }
    
    @Shadow
    private static void renderShape(
            PoseStack poseStack,
            VertexConsumer consumer,
            VoxelShape shape,
            double x,
            double y,
            double z,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        throw new AssertionError();
    }
}
