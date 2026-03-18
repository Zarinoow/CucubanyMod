package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;

public class ShoesBoxClosetBlock extends ClosetBlock {

    public ShoesBoxClosetBlock(Properties pProperties) {
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

}
