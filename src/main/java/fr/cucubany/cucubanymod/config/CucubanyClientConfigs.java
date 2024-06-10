package fr.cucubany.cucubanymod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CucubanyClientConfigs {

    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        CLIENT_BUILDER.push("Client configs for Cucubany Mod");

        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

}
