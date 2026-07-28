package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.hitbox.data.PosesRegistry;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Permet à un datapack de surcharger data/cucubanymod/hitbox/poses.json et de recharger
 * les poses via /reload. Le socle embarqué dans le jar est chargé indépendamment par
 * {@link PosesRegistry} : ce listener ne fait que le remplacer si le datapack est valide.
 */
@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID)
public class HitboxDataSubscriber {

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager mgr, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager mgr, ProfilerFiller profiler) {
                PosesRegistry.load(mgr);
            }
        });
    }
}
