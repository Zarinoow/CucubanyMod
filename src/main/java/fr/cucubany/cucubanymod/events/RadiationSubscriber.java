package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.armor.HazmatSuit;
import fr.cucubany.cucubanymod.items.CucubanyItems;
import fr.cucubany.cucubanymod.world.biome.CucubanyBiomes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zombie_extreme.init.ZombieExtremeModMobEffects;

import java.util.Set;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID)
public class RadiationSubscriber {

    @SubscribeEvent
    public static void onRadiationEvent(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        if(HazmatSuit.isWearingFullHazmatSuit(player)) return;

        Level world = player.getLevel();

        boolean addEffect = false;

        if(world.getBiome(player.getOnPos()).value().getRegistryName()
                .equals(CucubanyBiomes.IRRADIATED.get().getRegistryName())) addEffect = true;

        if(!addEffect) {
            // Check if the player has an radioactive item in his inventory
            Set<Item> radioactiveItems = Set.of(CucubanyItems.URANIUM_ROD.get(),
                    CucubanyItems.URANIUM_USED_ROD.get());
            if(player.getInventory().hasAnyOf(radioactiveItems)) addEffect = true;
        }

        if(addEffect) player.addEffect(new MobEffectInstance(ZombieExtremeModMobEffects.RADIATION_EFFECT.get(), 300));


    }
}
