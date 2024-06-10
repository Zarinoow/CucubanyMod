package fr.cucubany.cucubanymod.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {

    public static void register() {
        ClientRegistry.registerKeyBinding(SKILL_SCREEN);
    }

    public static final String KEYBIND_CATEGORY = "key.categories.cucubanymod";

    /*
     * Keys
     */

    public static final KeyMapping SKILL_SCREEN = new KeyMapping("key.cucubanymod.skill_screen", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, KEYBIND_CATEGORY);
}
