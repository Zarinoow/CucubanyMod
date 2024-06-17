package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID)
public class PlayerNameFormatHandler {

    @SubscribeEvent
    public static void onPlayerNameFormat(PlayerEvent.NameFormat event) {
        event.setDisplayname(getDisplayName(event.getPlayer()));
    }

    private static Component getDisplayName(Player player) {
        Identity identity = IdentityProvider.getIdentity(player);

        if (identity != null && !identity.getFirstName().isEmpty() && !identity.getLastName().isEmpty()) {
            return new TextComponent(identity.getFullName());
        }

        return new TextComponent(player.getName().getString());
    }
}
