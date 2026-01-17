package fr.cucubany.cucubanymod.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BodyHealthProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<IBodyHealth> BODY_HEALTH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private BodyHealth backend = null;
    private final LazyOptional<IBodyHealth> optional = LazyOptional.of(this::createBodyHealth);

    private BodyHealth createBodyHealth() {
        if (this.backend == null) {
            this.backend = new BodyHealth();
        }
        return this.backend;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BODY_HEALTH_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createBodyHealth().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createBodyHealth().deserializeNBT(nbt);
    }
}