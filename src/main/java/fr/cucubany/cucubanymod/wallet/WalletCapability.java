package fr.cucubany.cucubanymod.wallet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class WalletCapability implements IWalletCapability {
    private final SimpleContainer container = new SimpleContainer(SIZE);

    @Override public SimpleContainer getContainer() { return container; }

    @Override
    public CompoundTag writeNBT() {
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag slot = new CompoundTag();
                slot.putByte("Slot", (byte) i);
                stack.save(slot);
                list.add(slot);
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", list);
        return tag;
    }

    @Override
    public void readNBT(CompoundTag tag) {
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slot = list.getCompound(i);
            int index = slot.getByte("Slot") & 0xFF;
            if (index < container.getContainerSize()) {
                container.setItem(index, ItemStack.of(slot));
            }
        }
    }
}
