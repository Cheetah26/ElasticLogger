package com.shanemongan.elasticlogger;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class ElasticLogger extends JavaPlugin {

    // Plugin startup logic
    @Override
    public void onEnable() {
        // Initialize Elastic Client
        try {
            if (!ElasticClient.Connect()) {
                getLogger().severe("ElasticLogger was unable connect to server!");
                return;
            }
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
            return;
        }

        getLogger().info("ElasticLogger successfully connected!");

        // Register event handler
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
        getServer().getPluginManager().registerEvents(new EntityEventListener(), this);
        getServer().getPluginManager().registerEvents(new BlockEventListener(), this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new ServerMetrics(), 100L, 10L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}