package fr.cucubany.cucubanymod.items.advanced;

import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.roleplay.Identity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IdentityCardItem extends Item {
    public IdentityCardItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        if(!level.isClientSide && player.isShiftKeyDown()) {

            ItemStack card = player.getItemInHand(pUsedHand);
            if(card.hasTag() && card.getTag().contains("Identity")) {
                return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(pUsedHand));
            }

            Vec3 lookVec = player.getLookAngle();
            AABB box = player.getBoundingBox().expandTowards(lookVec.scale(5)); // 5 est la portée maximale pour viser une entité

            List<Entity> entities = player.level.getEntities(player, box, (entity) -> true); // Récupère toutes les entités dans la boîte

            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    updateIdentityCard(player, target, player.getItemInHand(pUsedHand));
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(pUsedHand));
                }
            }

            player.sendMessage(new TranslatableComponent("message.cucubanymod.identity_card.no_player_found").withStyle(ChatFormatting.RED), player.getUUID());

        }
        return super.use(level, player, pUsedHand);
    }

    private void updateIdentityCard(Player p, Player targetedPlayer, ItemStack card) {
        // Obtenez la capacité Identity du joueur
        LazyOptional<IIdentityCapability> identityCap = targetedPlayer.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);

        identityCap.ifPresent(cap -> {
            Identity identity = cap.getIdentity();

            if (identity == null || identity.getFirstName().isEmpty() || identity.getLastName().isEmpty()) return;

            // Crée un nouveau tag pour stocker les informations de l'identité
            CompoundTag identityTag = new CompoundTag();
            identityTag.putString("FirstName", identity.getFirstName());
            identityTag.putString("LastName", identity.getLastName());

            int identityNumber = IdentityCardNumberManager.getNextIdentityNumber();
            identityTag.putInt("IdentityNumber", identityNumber);


            // Ajoute le tag à la carte
            card.getOrCreateTag().put("Identity", identityTag);
            card.getOrCreateTag().putBoolean("IdentityLinked", true);

            p.sendMessage(new TranslatableComponent("message.cucubanymod.identity_card.linked", identity.getFullName()).withStyle(ChatFormatting.GREEN), p.getUUID());
        });

    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        if (stack.hasTag() && stack.getTag().contains("Identity")) {
            CompoundTag identityTag = stack.getTag().getCompound("Identity");
            String firstName = identityTag.getString("FirstName");
            String lastName = identityTag.getString("LastName");
            int identityNumber = identityTag.getInt("IdentityNumber");

            tooltipComponents.add(new TranslatableComponent("tooltip.cucubanymod.identity_card.first_name", firstName).withStyle(ChatFormatting.GREEN));
            tooltipComponents.add(new TranslatableComponent("tooltip.cucubanymod.identity_card.last_name", lastName).withStyle(ChatFormatting.GREEN));
            tooltipComponents.add(new TranslatableComponent("tooltip.cucubanymod.identity_card.identity_number", identityNumber).withStyle(ChatFormatting.GOLD));
        }
    }
}
