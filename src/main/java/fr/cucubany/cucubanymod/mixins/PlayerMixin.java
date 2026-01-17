package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;
import fr.cucubany.cucubanymod.hitbox.IMultiPartPlayer;
import fr.cucubany.cucubanymod.hitbox.PartTransform;
import fr.cucubany.cucubanymod.hitbox.PoseManager;
import fr.cucubany.cucubanymod.hitbox.poses.IPartPoseHandler;
import fr.cucubany.cucubanymod.hitbox.poses.StandingPoseHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IMultiPartPlayer {

    // Map : 1 Partie Logique -> Liste de Parties Physiques
    @Unique private Map<BodyPart, List<BodyPartEntity>> bodyPartMap;
    // Tableau plat pour Forge/Vanilla
    @Unique private BodyPartEntity[] partsArray;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) { super(type, level); }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.level.isClientSide) {
            System.out.println("CLIENT: Initialisation des hitboxes pour " + player.getName().getString());
        }
        this.bodyPartMap = new EnumMap<>(BodyPart.class);
        List<BodyPartEntity> allPartsList = new ArrayList<>();

        // Utiliser Standing pour l'initialisation des tailles par défaut
        IPartPoseHandler defaultPose = new StandingPoseHandler();

        for (BodyPart partType : BodyPart.values()) {
            List<BodyPartEntity> entities = new ArrayList<>();
            // On récupère les transforms initiaux pour savoir combien d'entités créer
            PartTransform[] transforms = defaultPose.getTransforms(player, partType);

            // Si l'Enum dit qu'il faut 2 parties (Torso), on en crée 2
            for (int i = 0; i < partType.getPartCount(); i++) {
                // Sécurité : si le handler ne renvoie pas assez de transforms, on fallback
                float w = (i < transforms.length) ? transforms[i].size().width : 0.25F;
                float h = (i < transforms.length) ? transforms[i].size().height : 0.25F;

                BodyPartEntity entity = new BodyPartEntity(player, partType, w, h);
                entities.add(entity);
                allPartsList.add(entity);
            }
            this.bodyPartMap.put(partType, entities);
        }

        this.partsArray = allPartsList.toArray(new BodyPartEntity[0]);
    }

    @Override public boolean isMultipartEntity() { return true; }
    @Override public PartEntity<?>[] getParts() { return this.getBodyParts(); }
    @Override public BodyPartEntity[] getBodyParts() { return this.partsArray != null ? this.partsArray : new BodyPartEntity[0]; }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        if (this.partsArray != null) updatePartPositions();
    }

    @Unique
    private void updatePartPositions() {
        Player player = (Player) (Object) this;
        IPartPoseHandler handler = PoseManager.getHandler(player);

        for (BodyPart partType : BodyPart.values()) {
            List<BodyPartEntity> entities = this.bodyPartMap.get(partType);
            if (entities == null || entities.isEmpty()) continue;

            // Récupère les positions cibles (ex: 2 transforms pour Torso)
            PartTransform[] targets = handler.getTransforms(player, partType);

            for (int i = 0; i < entities.size(); i++) {
                if (i >= targets.length) break;

                BodyPartEntity entity = entities.get(i);
                PartTransform target = targets[i];

                // 1. Appliquer Taille
                entity.resize(target.size().width, target.size().height);

                // 2. Appliquer Position
                entity.xo = entity.getX(); entity.yo = entity.getY(); entity.zo = entity.getZ();
                entity.xOld = entity.getX(); entity.yOld = entity.getY(); entity.zOld = entity.getZ();
                entity.setPos(target.position().x, target.position().y, target.position().z);

                // --- AJOUT CRUCIAL : FORCER LA BOUNDING BOX ---
                // Comme l'entité a noPhysics=true, setPos ne déplace pas toujours la boîte de collision.
                // On la force manuellement ici.
                entity.setBoundingBox(entity.getDimensions(net.minecraft.world.entity.Pose.STANDING)
                        .makeBoundingBox(target.position().x, target.position().y, target.position().z));
            }
        }
    }
}