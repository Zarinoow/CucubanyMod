package fr.cucubany.cucubanymod.hitbox.dev;

import com.google.gson.GsonBuilder;
import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.PoseManager;
import fr.cucubany.cucubanymod.hitbox.data.PartTransformData;
import fr.cucubany.cucubanymod.hitbox.data.PoseData;
import fr.cucubany.cucubanymod.hitbox.data.PosesRegistry;
import fr.cucubany.cucubanymod.roleplay.dummy.DummyPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HitboxDevEditor {
    private static final Logger LOGGER = LogManager.getLogger();

    // Flat ordered list of all (part, subIndex) pairs for Tab cycling
    private static final List<int[]> ALL_POSITIONS = new ArrayList<>();

    static {
        for (BodyPart part : BodyPart.values()) {
            for (int i = 0; i < part.getPartCount(); i++) {
                ALL_POSITIONS.add(new int[]{ part.ordinal(), i });
            }
        }
    }

    private static boolean active = false;
    private static Vec3 ghostPos = Vec3.ZERO;
    private static float ghostYBodyRot = 0f;
    private static float ghostYHeadRot = 0f;
    /** Entité fantôme isolée : copie complète du joueur au moment de l'activation.
     *  Comme elle n'est pas ajoutée au level et n'est jamais tickée, son état
     *  (vanilla, PlayerAnimator, autres mods) reste figé naturellement. */
    private static DummyPlayer ghostEntity = null;
    private static String currentPoseName = "standing";
    private static PoseData editedPoseData = null;
    /** Copies éditées, une par pose. Permet de basculer entre standing et sneak (P)
     *  sans perdre les ajustements déjà faits sur l'autre pose. */
    private static final Map<String, PoseData> editedPoses = new HashMap<>();
    private static int currentPositionIdx = 0;
    private static BodyPart selectedPart = BodyPart.HEAD;
    private static int selectedPartIndex = 0;
    private static int selectedAxis = 0; // 0=lateral 1=backward 2=y 3=width 4=height

    public static boolean isAvailable() {
        Minecraft mc = Minecraft.getInstance();
        return mc.hasSingleplayerServer()
            && mc.getSingleplayerServer() != null
            && !mc.getSingleplayerServer().isPublished();
    }

    public static void activate(Player player) {
        if (!(player instanceof AbstractClientPlayer realClient)) {
            LOGGER.warn("[HitboxDevEditor] activate() called with non-client player");
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        currentPoseName = PoseManager.getPoseName(player);
        editedPoses.clear();
        editedPoseData = checkoutPose(currentPoseName);
        if (editedPoseData == null) {
            LOGGER.warn("[HitboxDevEditor] No pose data for '{}', cannot activate editor", currentPoseName);
            return;
        }

        ghostPos = new Vec3(player.getX(), player.getY(), player.getZ());
        ghostYBodyRot = player.yBodyRot;
        ghostYHeadRot = player.yHeadRot;

        ghostEntity = buildGhost(realClient, level);

        currentPositionIdx = 0;
        selectedPart = BodyPart.HEAD;
        selectedPartIndex = 0;
        selectedAxis = 0;
        active = true;
    }

    public static void deactivate() {
        active = false;
        editedPoseData = null;
        editedPoses.clear();
        ghostEntity = null;
    }

    /** Retourne la copie éditable de la pose, en la créant depuis le registre au premier accès. */
    private static PoseData checkoutPose(String poseName) {
        PoseData edited = editedPoses.get(poseName);
        if (edited != null) return edited;

        PoseData source = PosesRegistry.getOrNull(poseName);
        if (source == null) return null;
        edited = source.deepCopy();
        editedPoses.put(poseName, edited);
        return edited;
    }

    /** Construit une copie figée du joueur. Aucun tick ne sera appelé dessus :
     *  tout ce que les renderers liront restera figé. */
    private static DummyPlayer buildGhost(AbstractClientPlayer src, ClientLevel level) {
        DummyPlayer g = new DummyPlayer(level, src.getGameProfile());
        g.setSkin(src.getSkinTextureLocation());
        g.setModelType("slim".equals(src.getModelName()));

        g.setPos(src.getX(), src.getY(), src.getZ());
        g.xo = g.xOld = src.getX();
        g.yo = g.yOld = src.getY();
        g.zo = g.zOld = src.getZ();

        g.yBodyRot = g.yBodyRotO = src.yBodyRot;
        g.yHeadRot = g.yHeadRotO = src.yHeadRot;

        g.setPose(src.getPose());
        g.tickCount = src.tickCount;
        return g;
    }

    public static void scrollAdjust(double rawDelta, boolean coarse) {
        if (!active || editedPoseData == null) return;
        PartTransformData[] arr = editedPoseData.get(selectedPart);
        if (arr == null || selectedPartIndex >= arr.length) return;
        PartTransformData d = arr[selectedPartIndex];
        double step = coarse ? 0.1 : 0.01;
        double delta = rawDelta * step;
        switch (selectedAxis) {
            case 0 -> d.lateralOffset += delta;
            case 1 -> d.backwardOffset += delta;
            case 2 -> d.yOffset += delta;
            // Les dimensions doivent rester strictement positives : une AABB de taille nulle
            // ou négative casse le rendu et la détection de collision.
            case 3 -> d.width = (float) Math.max(0.01, d.width + delta);
            case 4 -> d.height = (float) Math.max(0.01, d.height + delta);
        }
    }

    public static void cyclePartForward() {
        currentPositionIdx = (currentPositionIdx + 1) % ALL_POSITIONS.size();
        applyPosition(ALL_POSITIONS.get(currentPositionIdx));
    }

    public static void cyclePartBackward() {
        currentPositionIdx = (currentPositionIdx - 1 + ALL_POSITIONS.size()) % ALL_POSITIONS.size();
        applyPosition(ALL_POSITIONS.get(currentPositionIdx));
    }

    private static void applyPosition(int[] pos) {
        selectedPart = BodyPart.values()[pos[0]];
        selectedPartIndex = pos[1];
    }

    public static void selectAxis(int axis) {
        if (axis >= 0 && axis <= 4) selectedAxis = axis;
    }

    public static void togglePose() {
        String next = currentPoseName.equals("standing") ? "sneak" : "standing";
        PoseData nextData = checkoutPose(next);
        if (nextData == null) {
            LOGGER.warn("[HitboxDevEditor] No pose data for '{}', staying on '{}'", next, currentPoseName);
            return;
        }
        currentPoseName = next;
        editedPoseData = nextData;
        currentPositionIdx = 0;
        selectedPart = BodyPart.HEAD;
        selectedPartIndex = 0;
    }

    public static void exportToFile() {
        if (!active || editedPoseData == null) return;
        // Toutes les poses touchées pendant la session sont exportées, pas seulement la courante
        Map<String, PoseData> allPoses = new HashMap<>(PosesRegistry.getAllPoses());
        allPoses.putAll(editedPoses);

        Path exportPath = FMLPaths.GAMEDIR.get().resolve("cucubany_poses_export.json");
        try (Writer writer = Files.newBufferedWriter(exportPath, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(PosesRegistry.serialize(allPoses), writer);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.sendMessage(
                    new TextComponent("§a[Hitbox Editor] Exporté : " + exportPath.toAbsolutePath()),
                    UUID.randomUUID()
                );
            }
        } catch (IOException e) {
            LOGGER.error("[HitboxDevEditor] Failed to export poses: {}", e.getMessage());
        }
    }

    // --- Getters ---

    public static boolean isActive() { return active; }
    public static Vec3 getGhostPos() { return ghostPos; }
    public static float getGhostYBodyRot() { return ghostYBodyRot; }
    public static float getGhostYHeadRot() { return ghostYHeadRot; }
    public static DummyPlayer getGhostEntity() { return ghostEntity; }
    public static String getCurrentPoseName() { return currentPoseName; }
    public static PoseData getEditedPoseData() { return editedPoseData; }
    public static BodyPart getSelectedPart() { return selectedPart; }
    public static int getSelectedPartIndex() { return selectedPartIndex; }
    public static int getSelectedAxis() { return selectedAxis; }
}
