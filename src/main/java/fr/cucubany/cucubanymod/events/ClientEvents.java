package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.keybind.KeyBinding;
import fr.cucubany.cucubanymod.client.screen.SkillScreen;
import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if(KeyBinding.SKILL_SCREEN.consumeClick()) {
            Minecraft.getInstance().setScreen(new SkillScreen(new PlayerSkill()));
        }

    }


}
