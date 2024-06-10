package fr.cucubany.cucubanymod.world.biome;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import zombie_extreme.ZombieExtremeMod;
import zombie_extreme.init.ZombieExtremeModEntities;

public class IrradiatedBiome {

    public static Biome createIrradiatedBiome() {
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(-8692404)
                .waterColor(-12100265)
                .waterFogColor(-12100265)
                .skyColor(-8692404)
                .foliageColorOverride(-13158105)
                .grassColorOverride(-13158105)
                .ambientMoodSound(new AmbientMoodSettings(new SoundEvent(new ResourceLocation(ZombieExtremeMod.MODID,"biome_mood_sound_cave")), 3000, 8, 2.0))
                .backgroundMusic(new Music(new SoundEvent(new ResourceLocation(ZombieExtremeMod.MODID,"biome_wind_sound_radiation")), 12000, 24000, true))
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.005F))
                .build();

        BiomeGenerationSettings.Builder biomeGenerationSettings = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addDefaultCarversAndLakes(biomeGenerationSettings);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeGenerationSettings);
        BiomeDefaultFeatures.addDefaultOres(biomeGenerationSettings);


        MobSpawnSettings.Builder mobSpawnInfo = new MobSpawnSettings.Builder();
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.CLICKER.get(), 30, 1, 1));
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.DEVASTATED.get(), 40, 1, 1));
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.GOON.get(), 15, 1, 1));
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.RAT_KING.get(), 5, 1, 1));
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.REVIVED.get(), 10, 1, 1));
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.PATIENT_ZERO.get(), 5, 1, 1));
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.FACELESS.get(), 20, 1, 1));
        mobSpawnInfo.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ZombieExtremeModEntities.FETUS.get(), 12, 1, 1));

        return (new Biome.BiomeBuilder())
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.NONE)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(effects)
                .mobSpawnSettings(mobSpawnInfo.build())
                .generationSettings(biomeGenerationSettings.build())
                .build();
    }


}
