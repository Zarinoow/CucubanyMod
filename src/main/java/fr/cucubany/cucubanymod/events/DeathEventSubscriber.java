package fr.cucubany.cucubanymod.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.config.CucubanyServerConfigs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeathEventSubscriber {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if(event.getEntityLiving().getCommandSenderWorld().isClientSide()) {
            return;
        }

        if (event.getEntityLiving() instanceof Player p) {
            String webhookToken = CucubanyServerConfigs.DEATH_WEBHOOK_URL.get();
            if(webhookToken.equals("your-webhook-url-here")) {
                return;
            }

            // Obtenez la source du dommage qui a causé la mort
            DamageSource damageSource = event.getSource();

            // Obtenez le message de mort localisé
            Component deathMessageComponent = damageSource.getLocalizedDeathMessage(p);

            // Convertissez le Component en String
            String deathMessage = deathMessageComponent.getString();

            new Thread(() -> {
                try {
                    sendDeathMessageToDiscord(p, deathMessage, webhookToken);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();

        }
    }

    private static void sendDeathMessageToDiscord(Player player, String deathMessage, String webhookToken) throws Exception {
        // Code pour envoyer le message de mort à Discord
        JsonObject json = new JsonObject();

        json.addProperty("username", "CucubanyDeath");

        // Make an embed
        JsonArray embeds = new JsonArray();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "Mort de " + player.getDisplayName().getString());
        embed.addProperty("description", deathMessage);
        embed.addProperty("color", 16711680); // Rouge
        // Convert timestamp to ISO8601 format
        embed.addProperty("timestamp", java.time.OffsetDateTime.now().toString());

        JsonObject thumbnail = new JsonObject();
        thumbnail.addProperty("url", "https://mc-heads.net/head/" + player.getName().getString());

        JsonObject footer = new JsonObject();
        footer.addProperty("text", player.getName().getString());
        footer.addProperty("icon_url", "https://mc-heads.net/avatar/" + player.getName().getString());

        embed.add("thumbnail", thumbnail);
        embed.add("footer", footer);

        embeds.add(embed);
        json.add("embeds", embeds);

        URL url = new URL(webhookToken);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.addRequestProperty("User-Agent", "Minecraft-Server");
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        OutputStream os = con.getOutputStream();
        os.write(json.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        con.getInputStream().close();
        con.disconnect();
    }

}
