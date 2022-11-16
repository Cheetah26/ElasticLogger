package com.shanemongan.elasticlogger;

import org.bukkit.Server;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class ServerMetrics implements Runnable {
    @Override
    public void run() {
        Server server = getServer();
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> data = GetBasicServerInfo();

        data.put("version", server.getVersion());
        data.put("tps", server.getTPS()[0]);
        data.put("worlds", server.getWorlds().size());
        data.put("loadedPlugins", server.getPluginManager().getPlugins().length);
        data.put("onlinePlayers", server.getOnlinePlayers().size());
        data.put("playerBans", server.getBannedPlayers().size());
        data.put("ipBans", server.getIPBans().size());

        data.put("totalMemory", runtime.totalMemory());
        data.put("usedMemory", Math.abs(runtime.totalMemory() - runtime.freeMemory()));

        Map<String, Object> metric = new HashMap<>();
        metric.put("server", data);

        ElasticClient.InsertMetric(metric);
    }

    public static Map<String, Object> GetBasicServerInfo() {
        Server server = getServer();
        Map<String, Object> data = new HashMap<>();
        data.put("name", ElasticClient.serverName);
        return data;
    }

}
