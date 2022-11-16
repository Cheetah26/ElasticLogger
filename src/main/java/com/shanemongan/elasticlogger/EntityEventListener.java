package com.shanemongan.elasticlogger;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.HashMap;
import java.util.Map;

public class EntityEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", event.getEventName());
            data.put("entity", GetEntityData(event.getEntity()));
            ElasticClient.InsertLog(data);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", event.getEventName());
            data.put("entity", GetEntityData(event.getEntity()));
            if (event.getEntity().getKiller() != null) {
                data.put("killedByPlayer", true);
                data.put("player", PlayerEventListener.GetPlayerData(event.getEntity().getKiller()));
            } else {
                data.put("killedByPlayer", false);
            }
            ElasticClient.InsertLog(data);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplosion(EntityExplodeEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("blocksDestroyed", event.blockList().size());
        data.put("blocksDropped", Math.round(event.getYield() * event.blockList().size()));
        data.put("entity", GetEntityData(event.getEntity()));
        ElasticClient.InsertLog(data);
    }

    public static Map<String, Object> GetEntityData(Entity entity) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", entity.getType());
        data.put("name", entity.getName());
        String location = String.format("%d, %d, %d",
                entity.getLocation().getBlockX(),
                entity.getLocation().getBlockY(),
                entity.getLocation().getBlockZ());
        data.put("location", location);
        return data;
    }
}
