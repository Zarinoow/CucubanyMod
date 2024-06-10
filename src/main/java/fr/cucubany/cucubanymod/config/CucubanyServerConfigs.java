package fr.cucubany.cucubanymod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CucubanyServerConfigs {

    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> DEATH_WEBHOOK_URL;

    static {
        SERVER_BUILDER.push("Server configs for Cucubany Mod");

        DEATH_WEBHOOK_URL = SERVER_BUILDER.comment("The webhook URL to send death messages to")
                .define("deathWebhookUrl", "your-webhook-url-here");


        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();
    }



}
