package fr.cucubany.cucubanymod.roleplay.exposure;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Cette classe gère UNIQUEMENT les données (Data), pas le visuel.
public class ExposureEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // On ne veut exécuter la logique de sauvegarde de données que sur le SERVEUR
        // (Ou des deux côtés si nécessaire, mais ici on évite les crashs)
        if (event.phase != TickEvent.Phase.END) return;

        // Côté logique (Serveur + Client logique)
        Player player = event.player;

        if (player.level.dimension().location().toString().equals("cucubany:exterieur")) {
            ExposureHandler.incrementExposure(player);
        } else {
            ExposureHandler.resetExposure(player);
        }
    }
}