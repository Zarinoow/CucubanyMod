package fr.cucubany.cucubanymod.effects;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.effects.advanced.TrippyEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CucubanyEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CucubanyMod.MOD_ID);

    public static final RegistryObject<MobEffect> TRIP_VISION = EFFECTS.register("trip_vision", TrippyEffect::new);

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }

}
