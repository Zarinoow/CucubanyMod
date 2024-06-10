package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID)
public class AdvancementSubscriber {

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
        Advancement advancement = event.getAdvancement();

        String key = advancement.getId().toString();
        if (key.startsWith("cucubany:") || key.startsWith(CucubanyMod.MOD_ID + ":")) return;

        AdvancementRewards rewards = advancement.getRewards();

        if (rewards.getRecipes().length > 0) {
            Collection<Recipe<?>> recipes = new HashSet<>();
            for(ResourceLocation recipe : rewards.getRecipes()) {
                Recipe<?> recipeInstance = event.getPlayer().level.getRecipeManager().byKey(recipe).orElse(null);
                if (recipeInstance != null) {
                    recipes.add(recipeInstance);
                }
            }
            event.getPlayer().resetRecipes(recipes);
        }
    }

}