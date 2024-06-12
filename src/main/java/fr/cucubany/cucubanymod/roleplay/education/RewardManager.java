package fr.cucubany.cucubanymod.roleplay.education;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.foxelia.tools.java.files.json.reader.JsonFileReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class RewardManager {
    /*
     * JSON Structure
     *
     *{
     *  "no_aspiration": [
     *    {
     *      "level": 1,
     *      "reward_recipes": [
     *        "minecraft:crafting_table",
     *        "minecraft:furnace"
     *      ]
     *    },
     *    {
     *      "level": 101,
     *      "reward_recipes": [
     *          "minecraft:*"
     *      ]
     *    }
     *  ],
     *  "aspiration_1": [
     *    {
     *      "level": 1,
     *      "reward_recipes": [
     *        "mod:example_item"
     *      ]
     *    }
     *  ]
     *}
     */

    private static Path CONFIG_PATH = null;

    private final Skill skill;
    private final File configFile;
    private final JsonObject rewards;


    public RewardManager(Skill skill) {
        if(CONFIG_PATH == null) {
            throw new IllegalStateException("CONFIG_PATH is not set");
        }
        this.skill = skill;
        configFile = new File(CONFIG_PATH.toFile(), skill.getKeyName() + "-rewards.json");
        if(!configFile.exists()) {
            // Create example file
            ResourceLocation resourceLocation = new ResourceLocation("cucubanymod", "config/default_skill_config.json");
            Path exampleFile = Paths.get(resourceLocation.getPath(), "default_skill_config.json");
            // Save example file to config directory
            try {
                Files.createDirectories(configFile.getParentFile().toPath());
                Files.copy(exampleFile, configFile.toPath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            rewards = JsonFileReader.getJsonAsObject(configFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void rewardRecipes(ServerPlayer player) {
        String aspirationKey = "no_aspiration";
        if(skill instanceof IAspiration) {
            // aspirationKey = "aspiration_" + ((IAspiration) skill).getAspiration();
        }
        if(rewards.has(aspirationKey)) {;
            JsonArray rewardsArray = rewards.getAsJsonArray(aspirationKey);
            JsonArray rewardRecipes = null;
            for(int i = 0; i < rewardsArray.size(); i++) {
                JsonObject currentReward = rewardsArray.get(i).getAsJsonObject();
                if(currentReward.get("level").getAsInt() == skill.getLevel()) {
                    if(currentReward.has("reward_recipes")) {
                        rewardRecipes = currentReward.getAsJsonArray("reward_recipes");
                    }
                    break;
                }
            }
            if(rewardRecipes == null) return;
            RecipeManager recipeManager = player.getLevel().getRecipeManager();
            for(int i = 0; i < rewardRecipes.size(); i++) {
                String reward = rewardRecipes.get(i).getAsString();
                if (reward.contains("*")) {
                    // wildcard case
                    String[] parts = reward.split(":");
                    String modId = parts[0];
                    String itemId = parts[1].replace("*", "");
                    for (Recipe<?> recipe : recipeManager.getRecipes()) {
                        ResourceLocation recipeId = recipe.getId();
                        if (recipeId.getNamespace().equals(modId) && recipeId.getPath().startsWith(itemId)) {
                            unlockRecipe(player, recipe);
                        }
                    }
                } else {
                    // specific recipe case
                    Recipe<?> recipe = recipeManager.byKey(new ResourceLocation(reward)).orElse(null);
                    if (recipe != null) {
                        unlockRecipe(player, recipe);
                    }
                }
            }
        }
    }

    private void unlockRecipe(ServerPlayer player, Recipe<?> recipe) {
        player.awardRecipes(Collections.singleton(recipe));
    }


    /*
     * Static method to register set the configuration directory
     */

    public static void registerWorldPath(Path path) {
        CONFIG_PATH = Paths.get(path.toString(), "serverconfig", "cucubany", "education");
    }
}
