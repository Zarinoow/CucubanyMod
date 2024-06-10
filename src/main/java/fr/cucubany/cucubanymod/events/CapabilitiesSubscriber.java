package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.roleplay.Identity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilitiesSubscriber {

    @SubscribeEvent
    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(CucubanyMod.MOD_ID, "identity"), new IdentityCapabilityProvider());
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        event.register(IIdentityCapability.class);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if(!event.isWasDeath()) return;

        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getPlayer();

        if (oldPlayer != null && newPlayer != null) {

            oldPlayer.reviveCaps();

            LazyOptional<IIdentityCapability> oldIdentity = oldPlayer.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);
            LazyOptional<IIdentityCapability> newIdentity = newPlayer.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);

            if (oldIdentity.isPresent() && newIdentity.isPresent()) {
                Identity oldIdentityValue = oldIdentity.orElseThrow(() -> new RuntimeException("Identity capability is not present in old player")).getIdentity();
                newIdentity.ifPresent(cap -> cap.setIdentity(oldIdentityValue));
            }

            oldPlayer.invalidateCaps();
        }
    }
}
