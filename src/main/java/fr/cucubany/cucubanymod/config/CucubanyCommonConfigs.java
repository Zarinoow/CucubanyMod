package fr.cucubany.cucubanymod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CucubanyCommonConfigs {

    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_SPEC;

    // --- Limites des options de morphologie exposées au joueur ---
    public static final ForgeConfigSpec.DoubleValue BREAST_SIZE_MIN;
    public static final ForgeConfigSpec.DoubleValue BREAST_SIZE_MAX;
    public static final ForgeConfigSpec.DoubleValue BREAST_X_OFFSET_MIN;
    public static final ForgeConfigSpec.DoubleValue BREAST_X_OFFSET_MAX;
    public static final ForgeConfigSpec.DoubleValue BREAST_Y_OFFSET_MIN;
    public static final ForgeConfigSpec.DoubleValue BREAST_Y_OFFSET_MAX;
    public static final ForgeConfigSpec.DoubleValue BREAST_Z_OFFSET_MIN;
    public static final ForgeConfigSpec.DoubleValue BREAST_Z_OFFSET_MAX;
    public static final ForgeConfigSpec.DoubleValue BREAST_CLEAVAGE_MIN;
    public static final ForgeConfigSpec.DoubleValue BREAST_CLEAVAGE_MAX;

    // --- Banque ---
    public static final ForgeConfigSpec.ConfigValue<String> BANK_NAME;
    public static final ForgeConfigSpec.ConfigValue<String> BANK_CURRENCY;

    // --- Valeurs fixes (cachées, communes à tous) ---
    public static final ForgeConfigSpec.DoubleValue BREAST_BOUNCE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BREAST_FLOPPY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue BREAST_PHYSICS;
    public static final ForgeConfigSpec.BooleanValue BREAST_PHYSICS_ARMOR;
    public static final ForgeConfigSpec.BooleanValue BREAST_SHOW_IN_ARMOR;
    public static final ForgeConfigSpec.BooleanValue BREAST_HURT_SOUNDS;
    public static final ForgeConfigSpec.BooleanValue BREAST_UNIBOOB;

    static {
        COMMON_BUILDER.push("Common configs for Cucubany Mod");

        COMMON_BUILDER.comment("Banque").push("bank");

        BANK_NAME = COMMON_BUILDER
                .comment("Nom de la banque affiché dans l'ATM et sur les relevés")
                .define("bankName", "Banque de Cucubany");
        BANK_CURRENCY = COMMON_BUILDER
                .comment("Symbole de la monnaie (ex : €, $, £)")
                .define("bankCurrency", "€");

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Morphologie - Limites des curseurs exposés au joueur").push("breast_limits");

        BREAST_SIZE_MIN = COMMON_BUILDER
                .comment("Taille de poitrine minimale (réaliste : 0.3)")
                .defineInRange("bustSizeMin", 0.3, 0.0, 1.5);
        BREAST_SIZE_MAX = COMMON_BUILDER
                .comment("Taille de poitrine maximale (réaliste : 0.9)")
                .defineInRange("bustSizeMax", 0.9, 0.0, 1.5);

        BREAST_X_OFFSET_MIN = COMMON_BUILDER
                .comment("Écart horizontal minimal (rapproché)")
                .defineInRange("bustXOffsetMin", -0.05, -0.15, 0.15);
        BREAST_X_OFFSET_MAX = COMMON_BUILDER
                .comment("Écart horizontal maximal (écarté)")
                .defineInRange("bustXOffsetMax", 0.08, -0.15, 0.15);

        BREAST_Y_OFFSET_MIN = COMMON_BUILDER
                .comment("Décalage vertical minimal (vers le bas)")
                .defineInRange("bustYOffsetMin", -0.05, -0.2, 0.2);
        BREAST_Y_OFFSET_MAX = COMMON_BUILDER
                .comment("Décalage vertical maximal (vers le haut)")
                .defineInRange("bustYOffsetMax", 0.05, -0.2, 0.2);

        BREAST_Z_OFFSET_MIN = COMMON_BUILDER
                .comment("Profondeur minimale (dans le torse)")
                .defineInRange("bustZOffsetMin", 0.0, -0.1, 0.3);
        BREAST_Z_OFFSET_MAX = COMMON_BUILDER
                .comment("Profondeur maximale (vers l'avant)")
                .defineInRange("bustZOffsetMax", 0.07, -0.1, 0.3);

        BREAST_CLEAVAGE_MIN = COMMON_BUILDER
                .comment("Décolleté minimal (fermé)")
                .defineInRange("bustCleavageMin", 0.0, 0.0, 0.3);
        BREAST_CLEAVAGE_MAX = COMMON_BUILDER
                .comment("Décolleté maximal (ouvert, réaliste : 0.12)")
                .defineInRange("bustCleavageMax", 0.12, 0.0, 0.3);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Morphologie - Valeurs fixes appliquées automatiquement (non exposées au joueur)").push("breast_fixed");

        BREAST_BOUNCE_MULTIPLIER = COMMON_BUILDER
                .comment("Multiplicateur de rebond (physique). Ignoré si physique désactivée.")
                .defineInRange("bounceMultiplier", 0.34, 0.0, 2.0);
        BREAST_FLOPPY_MULTIPLIER = COMMON_BUILDER
                .comment("Souplesse (physique). Ignoré si physique désactivée.")
                .defineInRange("floppyMultiplier", 0.95, 0.0, 1.0);
        BREAST_PHYSICS = COMMON_BUILDER
                .comment("Activer la physique de la poitrine")
                .define("breastPhysics", false);
        BREAST_PHYSICS_ARMOR = COMMON_BUILDER
                .comment("Activer la physique avec l'armure")
                .define("breastPhysicsArmor", false);
        BREAST_SHOW_IN_ARMOR = COMMON_BUILDER
                .comment("Afficher la poitrine avec l'armure")
                .define("showInArmor", true);
        BREAST_HURT_SOUNDS = COMMON_BUILDER
                .comment("Sons lors des dommages")
                .define("hurtSounds", false);
        BREAST_UNIBOOB = COMMON_BUILDER
                .comment("Fusionner en une seule poitrine")
                .define("uniboob", false);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();
    }
}