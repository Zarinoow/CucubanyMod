package fr.cucubany.cucubanymod.hitbox.data;

import com.google.gson.*;
import fr.cucubany.cucubanymod.hitbox.BodyPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PosesRegistry {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Emplacement datapack : surchargeable par un datapack, rechargeable via /reload. */
    private static final ResourceLocation POSES_LOCATION = new ResourceLocation("cucubanymod", "hitbox/poses.json");
    /** Même fichier, lu directement dans le jar. Sert de socle : garantit un registre non vide
     *  sur les DEUX sides, y compris sur un client en multijoueur (qui ne charge aucun datapack). */
    private static final String CLASSPATH_FALLBACK = "/data/cucubanymod/hitbox/poses.json";

    private static final PoseData EMPTY = new PoseData(new EnumMap<>(BodyPart.class));

    private static final Map<String, PoseData> registry = new HashMap<>();

    static {
        loadDefaults();
    }

    /**
     * Charge les poses embarquées dans le jar. Appelé au chargement de la classe pour que
     * {@link #get(String)} soit utilisable dès la construction du premier Player, avant tout
     * cycle de rechargement de ressources.
     */
    private static void loadDefaults() {
        try (InputStream in = PosesRegistry.class.getResourceAsStream(CLASSPATH_FALLBACK)) {
            if (in == null) {
                LOGGER.error("[CucubanyMod] Embedded hitbox poses not found at {}", CLASSPATH_FALLBACK);
                return;
            }
            parseInto(new InputStreamReader(in, StandardCharsets.UTF_8), registry);
            LOGGER.info("[CucubanyMod] Loaded {} embedded hitbox pose(s)", registry.size());
        } catch (Exception e) {
            LOGGER.error("[CucubanyMod] Failed to load embedded hitbox poses: {}", e.getMessage(), e);
        }
    }

    /**
     * Recharge depuis le ResourceManager du serveur (datapacks). Le registre embarqué n'est
     * remplacé que si le parsing réussit entièrement, pour ne jamais laisser un registre vide.
     */
    public static void load(ResourceManager rm) {
        Map<String, PoseData> parsed = new HashMap<>();
        try (Reader reader = new InputStreamReader(
                rm.getResource(POSES_LOCATION).getInputStream(), StandardCharsets.UTF_8)) {
            parseInto(reader, parsed);
        } catch (Exception e) {
            LOGGER.error("[CucubanyMod] Failed to reload hitbox poses from {} ({}), keeping {} embedded pose(s)",
                POSES_LOCATION, e.getMessage(), registry.size());
            return;
        }

        if (parsed.isEmpty()) {
            LOGGER.warn("[CucubanyMod] Reloaded hitbox poses file is empty, keeping {} embedded pose(s)", registry.size());
            return;
        }

        registry.clear();
        registry.putAll(parsed);
        LOGGER.info("[CucubanyMod] Reloaded {} hitbox pose(s) from {}", registry.size(), POSES_LOCATION);
    }

    private static void parseInto(Reader reader, Map<String, PoseData> target) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        for (Map.Entry<String, JsonElement> poseEntry : root.entrySet()) {
            String poseName = poseEntry.getKey();
            JsonObject poseObj = poseEntry.getValue().getAsJsonObject();
            Map<BodyPart, PartTransformData[]> map = new EnumMap<>(BodyPart.class);
            for (BodyPart part : BodyPart.values()) {
                JsonElement elem = poseObj.get(part.getName());
                if (elem == null) continue;
                JsonArray arr = elem.getAsJsonArray();
                PartTransformData[] data = new PartTransformData[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject o = arr.get(i).getAsJsonObject();
                    data[i] = new PartTransformData(
                        o.get("lateral").getAsDouble(),
                        o.get("backward").getAsDouble(),
                        o.get("y").getAsDouble(),
                        o.get("width").getAsFloat(),
                        o.get("height").getAsFloat()
                    );
                }
                map.put(part, data);
            }
            target.put(poseName, new PoseData(map));
        }
    }

    /**
     * Ne retourne jamais null : une pose inconnue donne une PoseData vide, ce qui laisse les
     * parties du corps à leur position précédente au lieu de faire crasher le tick du joueur.
     */
    public static PoseData get(String poseName) {
        PoseData data = registry.get(poseName);
        if (data == null) {
            LOGGER.warn("[CucubanyMod] Unknown hitbox pose '{}'", poseName);
            return EMPTY;
        }
        return data;
    }

    /** Variante stricte, pour l'éditeur dev qui doit savoir si la pose existe réellement. */
    public static PoseData getOrNull(String poseName) {
        return registry.get(poseName);
    }

    public static boolean isLoaded() {
        return !registry.isEmpty();
    }

    public static Map<String, PoseData> getAllPoses() {
        return Collections.unmodifiableMap(registry);
    }

    public static JsonObject serialize(Map<String, PoseData> poses) {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, PoseData> poseEntry : poses.entrySet()) {
            JsonObject poseObj = new JsonObject();
            for (BodyPart part : BodyPart.values()) {
                PartTransformData[] arr = poseEntry.getValue().get(part);
                if (arr == null) continue;
                JsonArray partArr = new JsonArray();
                for (PartTransformData d : arr) {
                    JsonObject o = new JsonObject();
                    o.addProperty("lateral", d.lateralOffset);
                    o.addProperty("backward", d.backwardOffset);
                    o.addProperty("y", d.yOffset);
                    o.addProperty("width", d.width);
                    o.addProperty("height", d.height);
                    partArr.add(o);
                }
                poseObj.add(part.getName(), partArr);
            }
            root.add(poseEntry.getKey(), poseObj);
        }
        return root;
    }
}
