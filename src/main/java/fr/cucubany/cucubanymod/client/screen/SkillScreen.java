package fr.cucubany.cucubanymod.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;
import fr.cucubany.cucubanymod.roleplay.education.Skill;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;

import java.util.ArrayList;
import java.util.List;

public class SkillScreen extends Screen {
    private static final int SKILLS_PER_COLUMN = 4;
    private static final int SKILL_WIDGET_WIDTH = 200;
    private static final int SKILL_WIDGET_HEIGHT = 20;
    private static final int SKILL_WIDGET_Y_SPACING = 30;
    private static final int SKILL_WIDGET_X_SPACING = 220; // Adjust as needed

    private final PlayerSkill playerSkill;

    public SkillScreen(PlayerSkill playerSkill) {
        super(new TranslatableComponent("screen.cucubanymod.skill_screen.title"));
        this.playerSkill = playerSkill;
    }

    @Override
    protected void init() {

    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {

    }
}