package dev.satherov.blockutility;

import dev.satherov.blockutility.config.ConfigLoader;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

@Mod(BlockUtility.MOD_ID)
@EventBusSubscriber(modid = BlockUtility.MOD_ID)
public class BlockUtility {
    
    public static final String MOD_ID = "blockutility";
    public static final ConfigLoader CONFIG = ConfigLoader.create();
    
    public BlockUtility(IEventBus bus, FMLModContainer container) {
        BlockUtility.CONFIG.discover(container);
    }
    
    @SubscribeEvent
    private static void onConfigLoad(final ModConfigEvent.Loading event) {
        BlockUtility.CONFIG.update(event.getConfig().getSpec());
    }
    
    @SubscribeEvent
    private static void onConfigReload(final ModConfigEvent.Reloading event) {
        BlockUtility.CONFIG.update(event.getConfig().getSpec());
    }
    
    @SubscribeEvent
    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!SharedConstants.IS_RUNNING_IN_IDE) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        
        server.getPlayerList().op(player.getGameProfile());
    }
}
