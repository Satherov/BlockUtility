package dev.satherov.blockutility;

import lombok.Getter;
import lombok.Setter;

import dev.satherov.blockutility.config.BlockUtilityConfig;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.player.Player;

import com.mojang.blaze3d.platform.InputConstants;

import org.joml.Math;

import java.time.Instant;

@Mod(value = BlockUtility.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BlockUtility.MOD_ID, value = Dist.CLIENT)
public class BlockUtilityClient {
    
    private static @Getter @Setter int delay = 0;
    private static @Getter @Setter long last = -1;
    
    private static @Getter @Setter boolean customBreak = false;
    private static @Getter @Setter boolean customPlace = false;
    private static @Getter @Setter boolean directionLocked = false;
    
    private static @Getter @Setter BlockPos lastPlacedPos = null;
    private static @Getter @Setter Direction.Axis lockedAxis = null;
    private static @Getter @Setter boolean placementActive = false;
    
    private static final KeyMapping TOGGLE_CUSTOM_BREAK = new KeyMapping("key.blockutility.toggle_custom_break", InputConstants.KEY_COMMA, "key.category.blockutility");
    private static final KeyMapping TOGGLE_CUSTOM_PLACE = new KeyMapping("key.blockutility.toggle_custom_place", InputConstants.KEY_PERIOD, "key.category.blockutility");
    private static final KeyMapping TOGGLE_DIRECTION_LOCK = new KeyMapping("key.blockutility.toggle_direction_lock", InputConstants.KEY_SLASH, "key.category.blockutility");
    
    public BlockUtilityClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
    
    @SubscribeEvent
    private static void registerKeyBindings(final RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CUSTOM_BREAK);
        event.register(TOGGLE_CUSTOM_PLACE);
        event.register(TOGGLE_DIRECTION_LOCK);
    }
    
    @SubscribeEvent
    private static void onKeyInput(final InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Options options = mc.options;
        if (player == null) return;
        
        if (!options.keyUse.isDown()) {
            BlockUtilityClient.reset();
        }
        
        if (TOGGLE_DIRECTION_LOCK.consumeClick()) {
            directionLocked = !directionLocked;
            final String content = directionLocked ? "message.blockutility.enabled" : "message.blockutility.disabled";
            final ChatFormatting color = directionLocked ? ChatFormatting.GREEN : ChatFormatting.RED;
            Component message = Component.translatable(content).withStyle(color);
            player.displayClientMessage(Component.translatable("message.blockutility.direction_locked", message), true);
        }
        
        if (TOGGLE_CUSTOM_PLACE.consumeClick()) {
            customPlace = !customPlace;
            final String content = customPlace ? "message.blockutility.enabled" : "message.blockutility.disabled";
            final ChatFormatting color = customPlace ? ChatFormatting.GREEN : ChatFormatting.RED;
            Component message = Component.translatable(content).withStyle(color);
            player.displayClientMessage(Component.translatable("message.blockutility.custom_place", message), true);
        }
        
        if (TOGGLE_CUSTOM_BREAK.consumeClick()) {
            customBreak = !customBreak;
            final String content = customBreak ? "message.blockutility.enabled" : "message.blockutility.disabled";
            final ChatFormatting color = customBreak ? ChatFormatting.GREEN : ChatFormatting.RED;
            Component message = Component.translatable(content).withStyle(color);
            player.displayClientMessage(Component.translatable("message.blockutility.custom_break", message), true);
        }
    }
    
    @SubscribeEvent
    private static void onRenderHud(final RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;
        
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        Font font = mc.font;
        
        if (BlockUtilityConfig.Client.isInfoDisplay()) {
            int displays = 0;
            if (BlockUtilityClient.isCustomBreak()) {
                final Component key = ComponentUtils.wrapInSquareBrackets(Component.empty().append(TOGGLE_CUSTOM_BREAK.getTranslatedKeyMessage()).withStyle(ChatFormatting.GOLD));
                final Component message = Component.translatable("message.blockutility.custom_break", Component.translatable("message.blockutility.enabled").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" "))
                        .append(Component.translatable("message.blockutility.toggle", key).withStyle(ChatFormatting.GRAY));
                displays++;
                graphics.drawString(font, message, 10, displays * (font.lineHeight + 1), 0xFFFFFF, true);
            }
            if (BlockUtilityClient.isCustomPlace()) {
                final Component key = ComponentUtils.wrapInSquareBrackets(Component.empty().append(TOGGLE_CUSTOM_PLACE.getTranslatedKeyMessage()).withStyle(ChatFormatting.GOLD));
                final Component message = Component.translatable("message.blockutility.custom_place", Component.translatable("message.blockutility.enabled").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" "))
                        .append(Component.translatable("message.blockutility.toggle", key).withStyle(ChatFormatting.GRAY));
                displays++;
                graphics.drawString(font, message, 10, displays * (font.lineHeight + 1), 0xFFFFFF, true);
            }
            if (BlockUtilityClient.isDirectionLocked()) {
                final Component key = ComponentUtils.wrapInSquareBrackets(Component.empty().append(TOGGLE_DIRECTION_LOCK.getTranslatedKeyMessage()).withStyle(ChatFormatting.GOLD));
                final Component message = Component.translatable("message.blockutility.direction_locked", Component.translatable("message.blockutility.enabled").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" "))
                        .append(Component.translatable("message.blockutility.toggle", key).withStyle(ChatFormatting.GRAY));
                displays++;
                graphics.drawString(font, message, 10, displays * (font.lineHeight + 1), 0xFFFFFF, true);
            }
        }
        
        if (!BlockUtilityClient.isCustomBreak()) return;
        if (!BlockUtilityConfig.Client.isBreakDisplay()) return;
        if (BlockUtilityConfig.Client.getBreakDelay() <= 20) return;
        if (Instant.now().isAfter(Instant.ofEpochSecond(BlockUtilityClient.getLast()).plusSeconds(2))) return;
        
        int delay = BlockUtilityClient.getDelay();
        if (delay <= 0) return;
        
        MultiPlayerGameMode mode = mc.gameMode;
        if (mode == null) return;
        
        String text = String.valueOf(delay);
        
        int x = (width / 2) + 10;
        int y = (height / 2) - 4;
        
        graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
    }
    
    public static void onBlockPlaced(BlockPos pos) {
        if (!placementActive) {
            lastPlacedPos = pos;
            placementActive = true;
            lockedAxis = null;
        } else if (lastPlacedPos != null && lockedAxis == null) {
            lockedAxis = BlockUtilityClient.getAxis(lastPlacedPos, pos);
            lastPlacedPos = pos;
        } else {
            lastPlacedPos = pos;
        }
    }
    
    public static void reset() {
        lastPlacedPos = null;
        lockedAxis = null;
        placementActive = false;
    }
    
    public static boolean isValidPosition(BlockPos pos) {
        if (!placementActive || lockedAxis == null || lastPlacedPos == null) {
            return true;
        }
        
        return switch (lockedAxis) {
            case X -> pos.getY() == lastPlacedPos.getY() && pos.getZ() == lastPlacedPos.getZ();
            case Y -> pos.getX() == lastPlacedPos.getX() && pos.getZ() == lastPlacedPos.getZ();
            case Z -> pos.getX() == lastPlacedPos.getX() && pos.getY() == lastPlacedPos.getY();
        };
    }
    
    private static Direction.Axis getAxis(BlockPos from, BlockPos to) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());
        
        if (dx >= dy && dx >= dz) return Direction.Axis.X;
        if (dy >= dx && dy >= dz) return Direction.Axis.Y;
        return Direction.Axis.Z;
    }
}
