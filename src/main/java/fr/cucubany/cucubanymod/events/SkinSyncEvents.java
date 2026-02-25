package fr.cucubany.cucubanymod.events;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.networking.PacketSendGenderInfo;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.skin.SyncSkinPacket;
import fr.cucubany.cucubanymod.network.wildfire.SyncWildfireToClientPacket;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import fr.cucubany.cucubanymod.server.ServerSkinUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class SkinSyncEvents {

    // Quand 'player' commence à voir 'target' (entre dans la render distance)
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getPlayer() instanceof ServerPlayer tracker) {
            // Sync du skin custom
            byte[] skinData = ServerSkinUtils.getPlayerSkinBytes(target);
            if (skinData != null) {
                // On envoie le skin de la cible (target) au joueur qui regarde (tracker)
                CucubanyPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> tracker), new SyncSkinPacket(target.getUUID(), skinData, ServerSkinUtils.isPlayerSlim(target)));
            }

            // Sync des données Wildfire (poitrine) de la cible vers le tracker
            try {
                GenderPlayer wPlayer = WildfireGender.getPlayerById(target.getUUID());
                if (wPlayer != null) {
                    PacketSendGenderInfo.send(wPlayer);
                }
            } catch (Exception e) {
                CucubanyMod.getLogger().error("Erreur lors du sync Wildfire au StartTracking pour {}", target.getName().getString(), e);
            }
        }
    }

    // Quand le joueur se connecte, on lui envoie son propre skin et on resynchronise ses données Wildfire
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            // Sync du skin
            byte[] skinData = ServerSkinUtils.getPlayerSkinBytes(player);
            if (skinData != null) {
                CucubanyPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncSkinPacket(player.getUUID(), skinData, ServerSkinUtils.isPlayerSlim(player)));
            }

            // Sync des données Wildfire depuis la capability (écrase la config locale du client)
            Identity identity = IdentityProvider.getIdentity(player);
            if (identity != null) {
                CucubanyPacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncWildfireToClientPacket(identity.getGenderOption())
                );
                CucubanyMod.getLogger().debug("Wildfire capability data sent to client for player: {}", player.getName().getString());
            }
        }
    }
}