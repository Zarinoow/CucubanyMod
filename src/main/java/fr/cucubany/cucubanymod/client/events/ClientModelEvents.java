package fr.cucubany.cucubanymod.client.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.model.NoShadeBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModelEvents {

    @SubscribeEvent
    public static void onModelsBaked(ModelBakeEvent event) {
        event.getModelRegistry().replaceAll((location, model) -> {
            if (location.getNamespace().equals(CucubanyMod.MOD_ID) && location.getPath().equals("closet")) {
                return new NoShadeBakedModel(model);
            }
            return model;
        });
    }
}