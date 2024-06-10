package fr.cucubany.cucubanymod.world.biome;

import fr.cucubany.cucubanymod.CucubanyMod;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CucubanyBiomes {

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, CucubanyMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        BIOMES.register(eventBus);
    }

    /*
     * Register biomes
     */

    public static final RegistryObject<Biome> IRRADIATED = BIOMES.register("irradiated", () -> IrradiatedBiome.createIrradiatedBiome());



}
