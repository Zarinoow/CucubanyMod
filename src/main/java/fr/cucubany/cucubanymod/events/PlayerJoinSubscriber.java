package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.IdentityUpdatePacket;
import fr.cucubany.cucubanymod.network.OpenIdentityScreenPacket;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID)
public class PlayerJoinSubscriber {

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinWorldEvent event) {
        if(event.getWorld().isClientSide()) return;

        if(event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();

            Identity identity = IdentityProvider.getIdentity(player);

            if (identity == null || identity.getFirstName().isEmpty() || identity.getLastName().isEmpty()){
                player.setInvulnerable(true);
                CucubanyPacketHandler.INSTANCE.sendTo(new OpenIdentityScreenPacket(), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            } else {
                IdentityUpdatePacket packet = new IdentityUpdatePacket(player.getUUID(), identity);
                CucubanyPacketHandler.INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }

    }
}
