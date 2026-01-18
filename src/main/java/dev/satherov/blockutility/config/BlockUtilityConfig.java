package dev.satherov.blockutility.config;

import lombok.Getter;
import lombok.Setter;

import dev.satherov.blockutility.config.annotation.Config;
import dev.satherov.blockutility.config.annotation.ConfigVal;

import net.neoforged.fml.config.ModConfig;

public class BlockUtilityConfig {
    
    @Config(ModConfig.Type.CLIENT)
    public static class Client {
        
        @ConfigVal(name = "place_delay", comment = """
                Delay in ticks between placing blocks.
                Vanilla: 4
                """)
        @ConfigVal.Integer(min = 0, max = 200)
        private static @Getter @Setter int placeDelay = 0;
        
        @ConfigVal(name = "break_delay", comment = """
                Delay in ticks between breaking blocks.
                Vanilla: 0
                """)
        @ConfigVal.Integer(min = 0, max = 200)
        private static @Getter @Setter int breakDelay = 4;
        
        @ConfigVal(name = "break_display", comment = """
                Show the time in ticks remaining until another block can be broken in the hud.
                This only shows up if break_delay is greater than 20.
                """)
        @ConfigVal.Boolean
        private static @Getter @Setter boolean breakDisplay = true;
        
        @ConfigVal(name = "info_display", comment = """
                Shows the active settings in the top left corner
                """)
        @ConfigVal.Boolean
        private static @Getter @Setter boolean infoDisplay = true;
        
        @ConfigVal(name = "optimal_tool_finder", comment = "Automatically scroll to best tool in the hotbar for the current block")
        @ConfigVal.Boolean
        private static @Getter @Setter boolean optimalToolFinder = true;
        
        @Config.Group("block_outline")
        public static class Outline {
            
            @ConfigVal(name = "enabled", comment = "Enable block outline rendering.")
            @ConfigVal.Boolean
            private static @Getter @Setter boolean enabled = true;
            
            @ConfigVal(name = "alpha", comment = "Alpha of block outline.")
            @ConfigVal.Integer(min = 0, max = 255)
            private static @Getter @Setter int alpha = 100;
            
            @ConfigVal(name = "red", comment = "Red channel of the outline")
            @ConfigVal.Integer(min = 0, max = 255)
            private static @Getter @Setter int red = 0;
            
            @ConfigVal(name = "green", comment = "Green channel of the outline")
            @ConfigVal.Integer(min = 0, max = 255)
            private static @Getter @Setter int green = 0;
            
            @ConfigVal(name = "blue", comment = "Blue channel of the outline")
            @ConfigVal.Integer(min = 0, max = 255)
            private static @Getter @Setter int blue = 0;
        }
    }
}
