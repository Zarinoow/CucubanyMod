package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.BodyHealthProvider;
import fr.cucubany.cucubanymod.capabilities.IBodyHealth;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.SyncBodyHealthPacket;
import fr.cucubany.cucubanymod.roleplay.Identity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
            if (!event.getObject().getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(CucubanyMod.MOD_ID, "body_health"), new BodyHealthProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        event.register(IIdentityCapability.class);
        event.register(IBodyHealth.class);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(cap -> {
                SyncBodyHealthPacket.sendHealthSync(serverPlayer, cap.serializeNBT());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getPlayer();

        oldPlayer.reviveCaps();

        // 1. GESTION DE L'IDENTITÉ
        LazyOptional<IIdentityCapability> oldIdentityOpt = oldPlayer.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);
        LazyOptional<IIdentityCapability> newIdentityOpt = newPlayer.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);

        if (oldIdentityOpt.isPresent() && newIdentityOpt.isPresent()) {
            Identity oldIdentityValue = oldIdentityOpt.orElseThrow(RuntimeException::new).getIdentity();
            newIdentityOpt.ifPresent(cap -> cap.setIdentity(oldIdentityValue));
        }

        // 2. GESTION DE LA SANTÉ
        oldPlayer.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(oldHealth -> {
            newPlayer.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(newHealth -> {

                if (!event.isWasDeath()) newHealth.deserializeNBT(oldHealth.serializeNBT());
            });
        });

        // On invalide les caps de l'ancien joueur pour éviter des fuites de mémoire
        oldPlayer.invalidateCaps();

        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(cap -> {
                SyncBodyHealthPacket.sendHealthSync(serverPlayer, cap.serializeNBT());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(cap -> {
                SyncBodyHealthPacket.sendHealthSync(serverPlayer, cap.serializeNBT());
            });
        }
    }
}
