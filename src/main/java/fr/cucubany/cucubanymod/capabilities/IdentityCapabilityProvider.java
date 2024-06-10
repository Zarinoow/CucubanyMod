package fr.cucubany.cucubanymod.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IdentityCapabilityProvider implements ICapabilitySerializable<Tag> {
    public static Capability<IIdentityCapability> IDENTITY_CAPABILITY;

    private LazyOptional<IIdentityCapability> instance = LazyOptional.of(IdentityCapability::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == IDENTITY_CAPABILITY ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public Tag serializeNBT() {
        return instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")).writeTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")).readTag(nbt);
    }
}