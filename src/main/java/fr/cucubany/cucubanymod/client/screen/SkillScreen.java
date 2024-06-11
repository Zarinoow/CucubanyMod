package fr.cucubany.cucubanymod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;
import fr.cucubany.cucubanymod.roleplay.education.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class SkillScreen extends Screen {

    private static final int ROWS = 4;

    private static final ResourceLocation backgroundTexture = new ResourceLocation("minecraft", "textures/gui/demo_background.png");


    /*
     * Instance variables
     */
    private int mouseX;
    private int mouseY;

    private final PlayerSkill playerSkill;
    private Skill tooltipSkill;

    public SkillScreen(PlayerSkill playerSkill) {
        super(new TranslatableComponent("screen.cucubanymod.skill_screen.title"));
        this.playerSkill = playerSkill;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Clear the screen
        this.renderBackground(poseStack);

        // Charger la texture de fond
        RenderSystem.setShaderTexture(0, backgroundTexture);

        // Calcul des dimensions et positionnements dynamiques
        int backgroundWidth = 248;
        int backgroundHeight = 166;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Rendu de l'image de fond centré
        blit(poseStack, centerX - backgroundWidth / 2, centerY - backgroundHeight / 2, 0, 0, backgroundWidth, backgroundHeight);

        super.render(poseStack, mouseX, mouseY, partialTicks);

        // Dessiner le titre
        drawCenteredString(poseStack, this.font, this.title.getString(), centerX, centerY - backgroundHeight / 2 - 10, 0xFFFFFF);

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Calcul des positions pour les éléments
        int leftColumnX = centerX - backgroundWidth / 2 + 22;
        int rightColumnX = leftColumnX + 122;
        int startY = centerY - backgroundHeight / 2 + 10;

        int i = 0;
        for (Skill skill : playerSkill.getSkills()) {
            if (i < ROWS) {
                renderElement(poseStack, leftColumnX, startY + i * 40, skill);
            } else {
                renderElement(poseStack, rightColumnX, startY + (i - ROWS) * 40, skill);
            }
            i++;
        }

        if(tooltipSkill != null) {
            renderSkillTooltip(poseStack, tooltipSkill);
            tooltipSkill = null;
        }
    }

    private void renderElement(PoseStack poseStack, int x, int y, Skill skill) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(getItemStackFromName(skill.getDisplayIcon()), x - 20, y + 5);

        drawString(poseStack, this.font, skill.getDisplayName(), x, y, 0xFFFFFF);

        int maxValue = 100;
        int barWidth = 100;
        int barHeight = 10;
        int barX = x;
        int barY = y + 15;
        int filledBarWidth = (int) ((float) skill.getLevel() / maxValue * barWidth);

        // Fond de la barre de progression
        fill(poseStack, barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);

        // Barre de progression
        fill(poseStack, barX, barY, barX + filledBarWidth, barY + barHeight, 0xFF00FF00);

        // Niveau au centre de la barre de progression
        String levelText = skill.getLevel() + " / " + maxValue;
        drawCenteredString(poseStack, this.font, levelText, barX + barWidth / 2, barY + 1, 0xFFFFFF);

        // Si la souris est sur la barre de progression, l'objet ou le titre, afficher le tooltip
        if (mouseX >= x - 20 && mouseX <= x + barWidth && mouseY >= y && mouseY <= y + 25) {
            tooltipSkill = skill;
        }
    }

    private ItemStack getItemStackFromName(String itemName) {
        ItemStack item = new ItemStack(Registry.ITEM.get(new ResourceLocation(itemName)));
        if (item.isEmpty()) {
            item = new ItemStack(Items.BARRIER);
        }
        return item;
    }

    private void renderSkillTooltip(PoseStack poseStack, Skill skill) {
        String[] desc = skill.getDisplayDescription().getString().split("\n");
        List<Component> tooltipText = new ArrayList<>();
        tooltipText.add(skill.getDisplayName().withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
        for (String line : desc) {
            tooltipText.add(new TextComponent(ChatFormatting.GRAY + line));
        }
        tooltipText.add(TextComponent.EMPTY);
        tooltipText.add(new TranslatableComponent("skill.cucubanymod.general.current_level", skill.getLevel(), "100").withStyle(ChatFormatting.GREEN));
        renderComponentTooltip(poseStack, tooltipText, mouseX, mouseY);
    }

}