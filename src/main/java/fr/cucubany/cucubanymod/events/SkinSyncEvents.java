package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.skin.SyncSkinPacket;
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
            byte[] skinData = ServerSkinUtils.getPlayerSkinBytes(target);
            if (skinData != null) {
                // On envoie le skin de la cible (target) au joueur qui regarde (tracker)
                CucubanyPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> tracker), new SyncSkinPacket(target.getUUID(), skinData, ServerSkinUtils.isPlayerSlim(target)));
            }
        }
    }

    // Quand le joueur se connecte, on lui envoie son propre skin (pour la main en vue F5/inventaire)
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            byte[] skinData = ServerSkinUtils.getPlayerSkinBytes(player);
            if (skinData != null) {
                CucubanyPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncSkinPacket(player.getUUID(), skinData, ServerSkinUtils.isPlayerSlim(player)));
            }
        }
    }
}