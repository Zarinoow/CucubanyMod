package fr.cucubany.cucubanymod.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.IdentityChoicePacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class IdentityGui extends Screen {
    private EditBox firstNameField;
    private EditBox lastNameField;
    private Button validateButton;

    public IdentityGui() {
        super(new TextComponent("Créez votre personnage RP"));
    }

    @Override
    protected void init() {
        this.firstNameField = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TextComponent(""));
        this.lastNameField = new EditBox(this.font, this.width / 2 - 100, 100, 200, 20, new TextComponent(""));
        this.firstNameField.setMaxLength(22);
        this.lastNameField.setMaxLength(22);
        this.validateButton = this.addRenderableWidget(new Button(this.width / 2 - 100, 140, 200, 20, new TextComponent("Valider"), button -> {
            this.onClose();
        }));
        this.addWidget(this.firstNameField);
        this.addWidget(this.lastNameField);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.drawCenteredString(poseStack, this.font, this.title.getString(), this.width / 2, 20, 16777215);
        this.drawString(poseStack, this.font, "Prénom", this.width / 2 - 100, 50, 16777215);
        this.drawString(poseStack, this.font, "Nom de famille", this.width / 2 - 100, 90, 16777215);
        this.firstNameField.render(poseStack, mouseX, mouseY, partialTicks);
        this.lastNameField.render(poseStack, mouseX, mouseY, partialTicks);
        String firstName = this.firstNameField.getValue();
        String lastName = this.lastNameField.getValue();
        boolean valid = true;
        if(!firstName.isBlank()) {
            if(!isNameValid(firstName)) {
                this.firstNameField.setTextColor(16733525);
                valid = false;
            } else this.firstNameField.setTextColor(14737632);
        } else {
            valid = false;
            this.firstNameField.setTextColor(14737632);
        }
        if(!lastName.isBlank()) {
            if(!isNameValid(lastName)) {
                this.lastNameField.setTextColor(16733525);
                valid = false;
            } else this.lastNameField.setTextColor(14737632);
        } else {
            valid = false;
            this.lastNameField.setTextColor(14737632);
        }
        this.validateButton.active = valid;
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
       if(this.validateButton.active) {
           IdentityChoicePacket packet = new IdentityChoicePacket(formatName(firstNameField.getValue()), formatName(lastNameField.getValue()));
           CucubanyPacketHandler.INSTANCE.sendToServer(packet);
           super.onClose();
       }
    }

    private String formatName(String name) {
        // Remove useless spaces
        String formattedName = name.replace("-", " ").trim().replaceAll("\\s+", " ");
        // Upper case the first letter of each word and separate them with a dash
        String[] words = formattedName.split(" ");
        formattedName = "";
        for (String word : words) {
            formattedName += word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase() + "-";
        }
        return formattedName.substring(0, formattedName.length() - 1);
    }

    private boolean isNameValid(String name) {
        return name.matches("^[\\p{L} \\p{M}-]+$");
    }
}