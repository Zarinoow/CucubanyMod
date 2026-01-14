package fr.cucubany.cucubanymod.client.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Ease;
import fr.cucubany.cucubanymod.CucubanyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ClientAnimationManager {

    private static final Map<String, KeyframeAnimation> ANIMATIONS = new HashMap<>();
    private static final Map<AbstractClientPlayer, ModifierLayer<IAnimation>> PLAYER_LAYERS = new WeakHashMap<>();
    private static boolean isLoaded = false;

    public static void registerLayer(AbstractClientPlayer player, ModifierLayer<IAnimation> layer) {
        PLAYER_LAYERS.put(player, layer);
    }

    public static void loadAnimations() {
        if (isLoaded) return;

        // Assure-toi que ces chemins correspondent bien à tes fichiers dans 'assets/cucubanymod/'
        loadAnimation("anim_legs", "panimations/legs.json");
        loadAnimation("anim_torso", "panimations/torso.json");
        loadAnimation("anim_head", "panimations/head.json");
        loadAnimation("anim_feet", "panimations/feet.json");

        isLoaded = true;
    }

    private static void loadAnimation(String name, String path) {
        ResourceLocation location = new ResourceLocation(CucubanyMod.MOD_ID, path);
        try {
            if (!Minecraft.getInstance().getResourceManager().hasResource(location)) return;
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(location);
            try (InputStream stream = resource.getInputStream()) {
                KeyframeAnimation animation = AnimationSerializing.deserializeAnimation(stream).get(0);
                ANIMATIONS.put(name, animation);
            }
        } catch (Exception e) {
            CucubanyMod.getLogger().error("Failed to load animation: " + path, e);
        }
    }

    public static void playAnimation(AbstractClientPlayer player, String animationName) {
        loadAnimations();

        if (!ANIMATIONS.containsKey(animationName)) return;

        ModifierLayer<IAnimation> layer = PLAYER_LAYERS.get(player);

        if (layer != null) {
            KeyframeAnimation anim = ANIMATIONS.get(animationName);
            AbstractFadeModifier fade = AbstractFadeModifier.standardFadeIn(10, Ease.LINEAR);
            layer.replaceAnimationWithFade(fade, new KeyframeAnimationPlayer(anim));

        }
    }

    public static void tickLayer(AbstractClientPlayer player) {
        ModifierLayer<IAnimation> layer = PLAYER_LAYERS.get(player);
        if (layer != null) {
            layer.tick();
        }
    }
}