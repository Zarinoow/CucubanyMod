package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.BodyHealthProvider;
import fr.cucubany.cucubanymod.capabilities.IBodyHealth;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;
import fr.cucubany.cucubanymod.hitbox.IMultiPartPlayer;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.hitbox.SyncBodyHealthPacket;
import fr.cucubany.cucubanymod.network.hitbox.SyncPartIdsPacket;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.wallet.IWalletCapability;
import fr.cucubany.cucubanymod.wallet.WalletCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class CapabilitiesSubscriber {

    /**
     * Se déclenche quand un joueur (tracker) commence à voir un autre joueur (target).
     * On doit envoyer au tracker les IDs des parties du corps de la target.
     */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player target && event.getPlayer() instanceof ServerPlayer tracker) {
            if (target instanceof IMultiPartPlayer multiPartTarget) {
                BodyPartEntity[] parts = multiPartTarget.getBodyParts();
                if (parts != null && parts.length > 0) {
                    // On envoie le paquet UNIQUEMENT au joueur qui regarde (tracker)
                    CucubanyPacketHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> tracker),
                            new SyncPartIdsPacket(target.getId(), parts)
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(CucubanyMod.MOD_ID, "identity"), new IdentityCapabilityProvider());
            if (!event.getObject().getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(CucubanyMod.MOD_ID, "body_health"), new BodyHealthProvider());
            }
            event.addCapability(new ResourceLocation(CucubanyMod.MOD_ID, "wallet"), new WalletCapabilityProvider());
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        event.register(IIdentityCapability.class);
        event.register(IBodyHealth.class);
        event.register(IWalletCapability.class);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            if(serverPlayer instanceof IMultiPartPlayer multi) {
                CucubanyPacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SyncPartIdsPacket(serverPlayer.getId(), multi.getBodyParts())
                );
            }
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

        // 3. GESTION DU PORTEFEUILLE
        if (!event.isWasDeath()) {
            oldPlayer.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).ifPresent(oldWallet -> {
                newPlayer.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).ifPresent(newWallet -> {
                    newWallet.readNBT(oldWallet.writeNBT());
                });
            });
        }

        // On invalide les caps de l'ancien joueur pour éviter des fuites de mémoire
        oldPlayer.invalidateCaps();

        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(cap -> {
                SyncBodyHealthPacket.sendHealthSync(serverPlayer, cap.serializeNBT());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).ifPresent(wallet -> {
                var container = wallet.getContainer();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (!stack.isEmpty()) {
                        player.drop(stack, true, false);
                        container.setItem(i, ItemStack.EMPTY);
                    }
                }
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
