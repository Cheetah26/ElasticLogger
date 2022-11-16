package com.shanemongan.elasticlogger;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("player", GetPlayerData(event.getPlayer()));
        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("reason", event.getReason());
        long playtimeMillis = Math.abs(event.getPlayer().getLastLogin() - System.currentTimeMillis());
        Duration playtime = Duration.ofMillis(playtimeMillis);
        data.put("playtime", playtime.toString());
        data.put("player", GetPlayerData(event.getPlayer()));
        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("reason", event.getCause());
        data.put("player", GetPlayerData(event.getPlayer()));
        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        if (event.getPlayer().getLastDamageCause() != null)
            data.put("reason", event.getPlayer().getLastDamageCause().getCause());
        data.put("itemsDropped", event.getDrops().size());
        if (event.getPlayer().getKiller() != null) data.put("killer", event.getPlayer().getKiller().getName());
        data.put("player", GetPlayerData(event.getPlayer()));
        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("player", GetPlayerData(event.getPlayer()));
        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncChatEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        PlainTextComponentSerializer componentSerializer = PlainTextComponentSerializer.plainText();
        data.put("message", componentSerializer.serialize(event.message()));
        data.put("player", GetPlayerData(event.getPlayer()));
        ElasticClient.InsertLog(data);
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerItemPickup(EntityPickupItemEvent event) {
//        if (event.getEntity() instanceof Player) {
//            Map<String, Object> data = new HashMap<>();
//            data.put("type", event.getEventName());
//            data.put("item", event.getItem().getName());
//            data.put("player", GetPlayerData((Player) event.getEntity()));
//            ElasticClient.InsertLog(data);
//        }
//    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerItemDrop(PlayerDropItemEvent event) {
//        Map<String, Object> data = new HashMap<>();
//        data.put("type", event.getEventName());
//        data.put("quantity", event.getItemDrop().getItemStack().getAmount());
//        data.put("item", event.getItemDrop().getName());
//        data.put("player", GetPlayerData(event.getPlayer()));
//        ElasticClient.InsertDocument(data);
//    }
//
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("item", event.getItem().getType().toString());
        data.put("player", GetPlayerData(event.getPlayer()));
        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerReceiveDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "PlayerReceiveDamage");
            data.put("damage", event.getFinalDamage());
            data.put("reason", event.getCause().toString());
            data.put("player", GetPlayerData((Player) event.getEntity()));
            ElasticClient.InsertLog(data);
        }
    }

    @EventHandler
    public void onPlayerDealDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "PlayerDealDamage");
            data.put("damage", event.getFinalDamage());
            data.put("reason", event.getCause().toString());
            if (event.getEntity() instanceof Player) {
                data.put("pvp", true);
                data.put("receiver", GetPlayerData((Player) event.getEntity()));
                data.put("player", GetPlayerData((Player) event.getDamager()));
            } else {
                data.put("pvp", false);
                data.put("receiver", EntityEventListener.GetEntityData(event.getEntity()));
            }
            ElasticClient.InsertLog(data);
        }
    }

    public static Map<String, Object> GetPlayerData(Player player) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", player.getName());
        data.put("ip", Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress());
        data.put("ping", player.getPing());
        String location = String.format("%d, %d, %d", player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        data.put("location", location);
        return data;
    }
}