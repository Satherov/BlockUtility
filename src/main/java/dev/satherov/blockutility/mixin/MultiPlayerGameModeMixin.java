package dev.satherov.blockutility.mixin;

import dev.satherov.blockutility.BlockUtilityClient;
import dev.satherov.blockutility.config.BlockUtilityConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    
    @Shadow
    @Final
    private Minecraft minecraft;
    
    @Shadow
    private int destroyDelay;
    
    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        if (!BlockUtilityClient.isDirectionLocked()) return;
        
        BlockPos targetPos = result.getBlockPos().relative(result.getDirection());
        
        if (!BlockUtilityClient.isValidPosition(targetPos)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
    
    @Inject(
            method = "useItemOn",
            at = @At("RETURN")
    )
    public void afterUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        if (!BlockUtilityClient.isDirectionLocked()) return;
        
        if (cir.getReturnValue().consumesAction()) {
            BlockPos pos = result.getBlockPos().relative(result.getDirection());
            BlockUtilityClient.onBlockPlaced(pos);
        }
    }
    
    @Inject(
            method = "continueDestroyBlock",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyDelay:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    public void afterDestroyDelayDecrement(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir) {
        BlockUtilityClient.setDelay(this.destroyDelay);
        BlockUtilityClient.setLast(Instant.now().getEpochSecond());
    }
    
    @Inject(
            method = "continueDestroyBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"
            )
    )
    public void modifyBreakDelay(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (BlockUtilityClient.isCustomBreak()) this.destroyDelay = BlockUtilityConfig.Client.getBreakDelay();
    }
    
    @Inject(
            method = "startDestroyBlock",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying:Z",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/GameType;isCreative()Z"
                    )
            )
    )
    public void setOptimalTool(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (BlockUtilityConfig.Client.isOptimalToolFinder()) {
            Player player = minecraft.player;
            Level level = minecraft.level;
            HitResult hit = minecraft.hitResult;
            if (player == null || level == null) return;
            
            if (hit instanceof BlockHitResult result) {
                BlockState state = level.getBlockState(result.getBlockPos());
                Inventory inv = player.getInventory();
                
                int bestSlot = -1;
                float bestSpeed = 1.0f;
                
                for (int slot = 0; slot < 9; slot++) {
                    ItemStack stack = inv.items.get(slot);
                    if (stack.isEmpty() || !stack.isCorrectToolForDrops(state)) continue;
                    
                    float speed = stack.getDestroySpeed(state);
                    
                    if (speed > bestSpeed) {
                        bestSpeed = speed;
                        bestSlot = slot;
                    }
                }
                
                if (bestSlot == -1) return;
                player.getInventory().selected = bestSlot;
            }
        }
    }
}
