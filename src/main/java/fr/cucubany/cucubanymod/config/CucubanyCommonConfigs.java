package fr.cucubany.cucubanymod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CucubanyCommonConfigs {

    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        COMMON_BUILDER.push("Common configs for Cucubany Mod");

        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();
    }



}
