package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.BodyHealthProvider;
import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.network.hitbox.SyncBodyHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID)
public class DamageDistributionHandler {

    private static final Random rand = new Random();

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level.isClientSide) return;

        DamageSource source = event.getSource();
        float amount = event.getAmount();

        // VÉRIFICATION DU FLAG (Éviter double dégâts)
        // Si BodyPartEntity.hurt() a déjà été exécuté pour ce tick/event, on ne fait rien.
        if (player.getPersistentData().getBoolean("cucubany_part_hit")) {
            player.getPersistentData().remove("cucubany_part_hit"); // On consomme le flag
            return; // Dégâts déjà gérés localement, on arrête là.
        }

        player.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(health -> {
            boolean damageApplied = false;

            // --- CAS 1 : CHUTE (Jambes) ---
            if (source == DamageSource.FALL) {
                distributeDamage(health, amount, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT);
                damageApplied = true;
            }
            // --- CAS 2 : NOYADE / ÉTOUFFEMENT (Tête/Torse) ---
            else if (source == DamageSource.DROWN || source == DamageSource.IN_WALL) {
                distributeDamage(health, amount, BodyPart.HEAD, BodyPart.TORSO);
                damageApplied = true;
            }
            // --- CAS 3 : FEU / MAGIE (Aléatoire) ---
            else if (source.isFire() || source == DamageSource.MAGIC || source == DamageSource.WITHER) {
                distributeRandomly(health, amount);
                damageApplied = true;
            }
            // --- CAS 4 : COUP DIRECT (Fallback) ---
            // Si on est ici, c'est que le joueur a pris un coup physique (Zombie, Joueur...)
            // MAIS que le coup n'a pas touché une BodyPartEntity (sinon le flag serait true).
            // Cela arrive si on tape dans la hitbox "Vanilla" invisible qui dépasse parfois.
            // ACTION : On applique les dégâts au TORSE par défaut.
            else {
                health.hurt(BodyPart.TORSO, amount);
                damageApplied = true;
            }

            // --- SYNCHRONISATION (CRITIQUE) ---
            // C'est ce qui manquait pour voir les dégâts de noyade !
            if (damageApplied && player instanceof ServerPlayer serverPlayer) {
                SyncBodyHealthPacket.sendHealthSync(serverPlayer, health.serializeNBT());
            }

            // --- MORT ---
            if (health.getHealth(BodyPart.HEAD) <= 0 || health.getHealth(BodyPart.TORSO) <= 0) {
                player.setHealth(0);
            }
        });
    }

    private static void distributeDamage(fr.cucubany.cucubanymod.capabilities.IBodyHealth health, float totalAmount, BodyPart... parts) {
        float splitAmount = totalAmount / parts.length;
        for (BodyPart part : parts) {
            health.hurt(part, splitAmount);
        }
    }

    private static void distributeRandomly(fr.cucubany.cucubanymod.capabilities.IBodyHealth health, float amount) {
        BodyPart[] allParts = BodyPart.values();
        BodyPart target = allParts[rand.nextInt(allParts.length)];
        health.hurt(target, amount);
    }
}