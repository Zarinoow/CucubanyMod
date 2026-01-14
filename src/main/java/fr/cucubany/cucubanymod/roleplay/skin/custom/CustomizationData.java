package fr.cucubany.cucubanymod.roleplay.skin.custom;

import fr.cucubany.cucubanymod.CucubanyMod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomizationData {
    private static List<CharacterOption> options = new ArrayList<>();

    // Appelé quand on reçoit le paquet du serveur
    public static void setOptions(List<CharacterOption> newOptions) {
        // On pourrait faire un addAll si on reçoit plusieurs paquets
        // Ici on remplace tout pour faire simple
        options = newOptions;
        CucubanyMod.getLogger().info("Received " + options.size() + " customization options from server.");
    }

    // Si tu veux ajouter au lieu de remplacer (pour le multi-paquet)
    public static void addOptions(List<CharacterOption> newOptions) {
        options.addAll(newOptions);
    }

    public static void clear() {
        options.clear();
    }

    public static List<CharacterOption> getOptionsByCategory(SkinPart category) {
        return options.stream()
                .filter(opt -> opt.category().equals(category))
                .collect(Collectors.toList());
    }

}