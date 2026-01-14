package fr.cucubany.cucubanymod.server;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.roleplay.skin.custom.CharacterOption;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ServerSkinManager {

    // Stocke toutes les options chargées en mémoire : Map<Categorie, Liste d'options>
    private static final Map<SkinPart, List<CharacterOption>> CACHE = new HashMap<>();

    public static void loadSkins() {
        CACHE.clear();

        // Dossier racine : ./run/config/cucubanymod/skins/
        File rootDir = FMLPaths.CONFIGDIR.get().resolve("cucubanymod/skins").toFile();
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        extractDefaultUnderwear(rootDir);

        int totalLoaded = 0;

        for (SkinPart part : SkinPart.values()) {
            // Sous-dossier par catégorie (ex: .../skins/hair/)
            File partDir = new File(rootDir, part.name().toLowerCase());
            if (!partDir.exists()) {
                partDir.mkdirs(); // Crée le dossier s'il n'existe pas pour aider l'admin
                continue;
            }

            List<CharacterOption> partOptions = new ArrayList<>();

            // On scanne les fichiers .png
            Collection<File> files = FileUtils.listFiles(partDir, new String[]{"png"}, false);

            for (File file : files) {
                try {
                    String id = file.getName().replace(".png", "");
                    byte[] data = FileUtils.readFileToByteArray(file);

                    // On crée l'option
                    partOptions.add(new CharacterOption(id, part, data));
                } catch (IOException e) {
                    CucubanyMod.getLogger().warn("Error while loading skin {}", file.getName(), e);
                }
            }

            CACHE.put(part, partOptions);
            totalLoaded += partOptions.size();
            CucubanyMod.getLogger().info("Loaded {} options for {}", partOptions.size(), part.name());
        }

        CucubanyMod.getLogger().info("Total loaded customization elements: {}", totalLoaded);
    }

    private static void extractDefaultUnderwear(File rootDir) {
        File pantsDir = new File(rootDir, "pants");
        if (!pantsDir.exists()) pantsDir.mkdirs();

        File targetFile = new File(pantsDir, "default.png");

        // Si le fichier existe déjà, on ne fait rien (pour permettre aux admins de le personnaliser)
        if (targetFile.exists()) return;

        CucubanyMod.getLogger().info("Extracting default underwear pants to {}", targetFile.getAbsolutePath());

        // Chemin interne dans le JAR (src/main/resources/assets/...)
        String internalPath = "/assets/cucubanymod/textures/character/underwear.png";

        try (InputStream is = CucubanyMod.class.getResourceAsStream(internalPath)) {
            if (is == null) {
                CucubanyMod.getLogger().error("FATAL ERROR. Failed to load default underwear pants from {}", internalPath);
                return;
            }

            // Copie du flux vers le fichier
            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            CucubanyMod.getLogger().info("Success: Underwear extracted to {}", targetFile.getAbsolutePath());

        } catch (IOException e) {
            CucubanyMod.getLogger().error("Error while extracting default underwear pants", e);
        }
    }

    /**
     * Récupère les options demandées par le client.
     * @param categories Les catégories demandées (ex: HAIR, EYES). Si null/vide, on ignore ce filtre.
     * @param specificIds Une liste d'IDs précis (ex: pour charger le skin d'un joueur existant). Si null/vide, on renvoie tout.
     */
    public static List<CharacterOption> getRequestedOptions(Set<SkinPart> categories, List<String> specificIds) {
        List<CharacterOption> result = new ArrayList<>();
        boolean hasSpecificIds = specificIds != null && !specificIds.isEmpty();

        for (Map.Entry<SkinPart, List<CharacterOption>> entry : CACHE.entrySet()) {
            // 1. Filtrage par Catégorie
            if (categories != null && !categories.isEmpty() && !categories.contains(entry.getKey())) {
                continue;
            }

            for (CharacterOption option : entry.getValue()) {
                // 2. Filtrage par ID spécifique (si demandé)
                if (hasSpecificIds) {
                    if (specificIds.contains(option.id())) {
                        result.add(option);
                    }
                } else {
                    // Sinon on ajoute tout le contenu de la catégorie
                    result.add(option);
                }
            }
        }
        return result;
    }

    public static CharacterOption getOptionById(SkinPart part, String id) {
        List<CharacterOption> list = CACHE.get(part);
        if (list == null) return null;

        return list.stream()
                .filter(opt -> opt.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}