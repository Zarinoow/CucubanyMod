package fr.cucubany.cucubanymod.client.skin;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import fr.cucubany.cucubanymod.CucubanyMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public class WildfireBridge {

    public static void setPlayerGender(Player player, GenderPlayer.Gender gender) {
        try {
            // On récupère ou on crée les données de genre pour ce joueur (fonctionne aussi pour le DummyPlayer)
            GenderPlayer genderPlayer = WildfireGender.getOrAddPlayerById(player.getUUID());

            if (genderPlayer != null) {
                // On met à jour le genre
                genderPlayer.updateGender(gender);
            }
        } catch (Exception e) {
            // Si le mod n'est pas chargé ou erreur
            e.printStackTrace();
        }
    }

    public static void tickPhysics(Player player) {
        try {
            GenderPlayer genderPlayer = WildfireGender.getPlayerById(player.getUUID());
            if (genderPlayer == null) return;

            IGenderArmor armor = WildfireHelper.getArmorConfig(player.getItemBySlot(EquipmentSlot.CHEST));

            if (genderPlayer.getLeftBreastPhysics() != null) {
                genderPlayer.getLeftBreastPhysics().update(player, armor);
            }
            if (genderPlayer.getRightBreastPhysics() != null) {
                genderPlayer.getRightBreastPhysics().update(player, armor);
            }

        } catch (Exception e) {
            CucubanyMod.getLogger().error(e.getLocalizedMessage(), e);
        }
    }

    public static void applyBreastParams(Player player, float bustSize, float xOffset, float yOffset, float zOffset, float cleavage) {
        try {
            GenderPlayer genderPlayer = WildfireGender.getOrAddPlayerById(player.getUUID());
            if (genderPlayer != null) {
                genderPlayer.updateBustSize(bustSize);
                genderPlayer.getBreasts().updateXOffset(xOffset);
                genderPlayer.getBreasts().updateYOffset(yOffset);
                genderPlayer.getBreasts().updateZOffset(zOffset);
                genderPlayer.getBreasts().updateCleavage(cleavage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLock(Player player, boolean locked) {
        try {
            GenderPlayer genderPlayer = WildfireGender.getPlayerById(player.getUUID());
            if (genderPlayer != null) {
                genderPlayer.lockSettings = locked;
            }
        } catch (Exception e) {
            CucubanyMod.getLogger().error(e.getLocalizedMessage(), e);
        }
    }
}