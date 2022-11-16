package com.shanemongan.elasticlogger;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;

public class BlockEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTntPrimed(TNTPrimeEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("reason", event.getReason());
        if (event.getPrimerEntity() instanceof Player) {
            data.put("primedByPlayer", true);
            data.put("player", EntityEventListener.GetEntityData(event.getPrimerEntity()));
        } else {
            data.put("primedByPlayer", false);
        }
        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("block", event.getBlock().getType().toString());
        data.put("player", PlayerEventListener.GetPlayerData(event.getPlayer()));
        String location = String.format("%d, %d, %d", event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ());
        data.put("location", location);

        ElasticClient.InsertLog(data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", event.getEventName());
        data.put("block", event.getBlock().getType().toString());
        data.put("player", PlayerEventListener.GetPlayerData(event.getPlayer()));
        String location = String.format("%d, %d, %d", event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ());
        data.put("location", location);

        ElasticClient.InsertLog(data);
    }
}
