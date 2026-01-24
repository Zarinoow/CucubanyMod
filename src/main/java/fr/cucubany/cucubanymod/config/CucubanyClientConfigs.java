package fr.cucubany.cucubanymod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CucubanyClientConfigs {

    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.DoubleValue HEALTH_OVERLAY_SCALE;

    static {
        CLIENT_BUILDER.push("Client configs for Cucubany Mod");

        HEALTH_OVERLAY_SCALE = CLIENT_BUILDER
                .comment("Scale of the body health overlay on the HUD. Default is 1.3 (130%).")
                .defineInRange("healthOverlayScale", 1.3f, 0.5f, 2.0f);

        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

}
