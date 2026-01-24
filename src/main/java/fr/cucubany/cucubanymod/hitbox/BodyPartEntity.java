package fr.cucubany.cucubanymod.hitbox;

import fr.cucubany.cucubanymod.capabilities.BodyHealthProvider;
import fr.cucubany.cucubanymod.network.hitbox.SyncBodyHealthPacket;
import fr.cucubany.cucubanymod.server.PartRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.PartEntity;

public class BodyPartEntity extends PartEntity<Player> {
    public final BodyPart logicalPart; // Lien vers l'Enum (ex: TORSO)
    private EntityDimensions dimensions;

    public BodyPartEntity(Player parent, BodyPart logicalPart, float width, float height) {
        super(parent);
        this.logicalPart = logicalPart;
        this.dimensions = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
        this.noPhysics = true;

        if (!parent.level.isClientSide) {
            PartRegistry.add(this);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        // DÉSINSCRIPTION
        if (!this.level.isClientSide) {
            PartRegistry.remove(this);
        }
    }

    // --- Permet le redimensionnement dynamique ---
    public void resize(float width, float height) {
        if (this.dimensions.width != width || this.dimensions.height != height) {
            this.dimensions = EntityDimensions.scalable(width, height);
            this.refreshDimensions();
        }
    }

    @Override public EntityDimensions getDimensions(Pose pose) { return this.dimensions; }

    // --- Système de Dégâts ---
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level.isClientSide) {
            this.getParent().getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(health -> {
                // 1. Appliquer les dégâts à la partie spécifique
                health.hurt(this.logicalPart, amount);

                // 2. Synchroniser
                if (this.getParent() instanceof ServerPlayer serverPlayer) {
                    SyncBodyHealthPacket.sendHealthSync(serverPlayer, health.serializeNBT());
                }

                // 3. SIGNALER QUE LA TOUCHE EST TRAITÉE
                // On met un flag pour que DamageDistributionHandler sache qu'on a déjà géré ce coup
                this.getParent().getPersistentData().putBoolean("cucubany_part_hit", true);
            });
        }

        return this.getParent().hurt(source, amount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // On redirige le clic droit vers le joueur parent
        return this.getParent().interact(player, hand);
    }

    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag nbt) {}
    @Override protected void addAdditionalSaveData(CompoundTag nbt) {}
    @Override public boolean isPickable() { return true; }
    @Override public boolean is(Entity entity) { return this == entity || this.getParent() == entity; }
    @Override public Packet<?> getAddEntityPacket() { return null; }
    @Override public boolean isNoGravity() { return true; }
    @Override
    public boolean isAttackable() {
        return true;
    }
}