package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.keybind.KeyBinding;
import fr.cucubany.cucubanymod.client.screen.SkillScreen;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import fr.cucubany.cucubanymod.roleplay.dummy.DummyPlayer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if(KeyBinding.SKILL_SCREEN.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new SkillScreen(IdentityProvider.getIdentity(mc.player).getEducation()));
        }

    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameplateEvent event) {
        // Si l'entité qui va être dessinée est notre DummyPlayer
        if (event.getEntity() instanceof DummyPlayer) {
            // On annule le rendu du NameTag.
            // Le renderer n'essaiera même pas de dessiner le fond ou le texte.
            event.setResult(Event.Result.DENY);
        }
    }

}
