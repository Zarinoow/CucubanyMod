package fr.cucubany.cucubanymod.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CucubanyCreativeModeTab {

    public static final CreativeModeTab CUCUBANY_TAB = new CreativeModeTab("cucubanytab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(CucubanyItems.IDENTITY_CARD.get());
        }
    };
}
