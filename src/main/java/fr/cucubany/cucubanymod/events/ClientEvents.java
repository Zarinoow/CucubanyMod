package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.client.keybind.KeyBinding;
import fr.cucubany.cucubanymod.client.screen.SkillScreen;
import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if(KeyBinding.SKILL_SCREEN.consumeClick()) {
            // Get the player's skill capability
            Minecraft mc = Minecraft.getInstance();
            LazyOptional<IIdentityCapability> identityCap = mc.player.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);
            identityCap.ifPresent(cap -> {
                PlayerSkill skills = cap.getIdentity().getEducation();
                mc.setScreen(new SkillScreen(skills));
            });
        }

    }


}
