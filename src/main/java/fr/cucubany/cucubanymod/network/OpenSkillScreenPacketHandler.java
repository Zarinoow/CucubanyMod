package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.client.screen.IdentityGui;
import fr.cucubany.cucubanymod.client.screen.SkillScreen;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenSkillScreenPacketHandler {
    public static void handle(OpenSkillScreenPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openScreen(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(OpenSkillScreenPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        Player target = mc.level.getPlayerByUUID(packet.playerUUID());
        if (target != null) {
            Identity identity = IdentityProvider.getIdentity(target);
            if(identity == null) {
                mc.player.sendMessage(new TranslatableComponent("message.cucubanymod.skill.get.no_identity").withStyle(ChatFormatting.RED), mc.player.getUUID());
                return;
            }
            mc.setScreen(new SkillScreen(identity.getEducation()));
        } else {
            mc.player.sendMessage(new TranslatableComponent("message.cucubanymod.skill.get.no_player").withStyle(ChatFormatting.RED), mc.player.getUUID());
        }

    }
}

