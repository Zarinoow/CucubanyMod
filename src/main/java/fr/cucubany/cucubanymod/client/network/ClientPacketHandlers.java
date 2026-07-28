package fr.cucubany.cucubanymod.client.network;

import fr.cucubany.cucubanymod.capabilities.BodyHealthProvider;
import fr.cucubany.cucubanymod.client.screen.ATMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Logique client des paquets, isolée hors des classes de paquets elles-mêmes.
 * <p>
 * Les classes de paquets sont chargées et vérifiées par la JVM au moment du
 * {@code registerMessage()}, y compris sur un serveur dédié. Le vérificateur charge les
 * types impliqués dans un contrôle d'assignabilité : {@code Player p = mc.player}
 * (LocalPlayer → Player) ou {@code mc.setScreen(new ATMScreen(..))} (ATMScreen → Screen)
 * suffisent donc à faire échouer le démarrage du serveur avec
 * « Attempted to load class ... for invalid dist DEDICATED_SERVER ».
 * <p>
 * Cette classe n'est atteinte que via {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)},
 * elle n'est donc jamais chargée côté serveur.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {}

    public static void applyBodyHealth(CompoundTag data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY)
            .ifPresent(cap -> cap.deserializeNBT(data));
    }

    public static void openAtmScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new ATMScreen(pos));
    }
}
