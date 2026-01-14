package fr.cucubany.cucubanymod.client.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.skin.WildfireBridge;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, value = Dist.CLIENT)
public class WildfireGuiLockEvent {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.InitScreenEvent.Pre event) {
        if (event.getScreen().getClass().getName().contains("com.wildfire.gui.screen")) {

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            Identity identity = IdentityProvider.getIdentity(mc.player);

            if (identity != null) {
                WildfireBridge.setLock(mc.player, true);
            }
        }
    }

}
