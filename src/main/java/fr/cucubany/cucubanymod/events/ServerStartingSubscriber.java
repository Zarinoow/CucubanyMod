package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.items.advanced.IdentityCardNumberManager;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID)
public class ServerStartingSubscriber {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        IdentityCardNumberManager.registerWorldData(event.getServer().getWorldPath(LevelResource.ROOT));
    }
}
