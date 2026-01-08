package fr.cucubany.cucubanymod.effects.advanced;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TrippyEffect extends MobEffect {

    public TrippyEffect() {
        super(MobEffectCategory.HARMFUL, 0x8800FF); // couleur violette/psyché
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            // effets visuels + contrôle inversé à faire côté client
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
