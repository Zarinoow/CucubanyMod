package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class ShoeboxClosetBlock extends ClosetBlock {

    public ShoeboxClosetBlock(Properties pProperties) {
        super(pProperties);
    }

    /*
     * Méthodes héritées de ClosetBlock
     */
    @Override
    SkinPart[] getSkinParts() {
        return new SkinPart[] {
                SkinPart.SHOES
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

}
