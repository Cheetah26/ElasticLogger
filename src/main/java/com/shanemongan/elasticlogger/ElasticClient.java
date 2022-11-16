package com.shanemongan.elasticlogger;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class ElasticClient {
    static ElasticsearchClient client;
    static String serverName;

    public static boolean Connect() throws IOException {
        Properties properties = new Properties();
        String configFile = Paths.get(
                getServer().getPluginsFolder().getAbsolutePath(), "ElasticLogger.conf"
        ).toString();

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (FileNotFoundException ex) {
            getLogger().severe("Could not find config file at " + configFile);
//            getLogger().severe("Could not locate config file for ElasticLogger.\nFile named \"ElasticLogger.conf\" should be placed in the plugin folder!");
            return false;
        } catch (IOException ex) {
            getLogger().severe(ex.getMessage());
            return false;
        }

        String host = properties.getProperty("host");
        int port = Integer.parseInt(properties.getProperty("port"));
        String fingerprint = properties.getProperty("ssl_fingerprint");
        String keyID = properties.getProperty("key_id");
        String key = properties.getProperty("key");
        serverName = properties.getProperty("server_name");

        // Setup encryption
        SSLContext sslContext = TransportUtils
                .sslContextFromCaFingerprint(fingerprint);

        // Setup Auth
//        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
//        credsProv.setCredentials(
//                AuthScope.ANY, new UsernamePasswordCredentials("elastic", "mongo626")
//        );
        String apiKeyAuth = Base64.getEncoder().encodeToString((keyID + ":" + key).getBytes(StandardCharsets.UTF_8));

        // Create the low-level client
        RestClient restClient = RestClient
                .builder(new HttpHost(host, port, "https"))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization",
                                "ApiKey " + apiKeyAuth)
                })
                .setHttpClientConfigCallback(hc -> hc
                                .setSSLContext(sslContext)
//                        .setDefaultCredentialsProvider(credsProv)
                )
                .build();


        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient client = new ElasticsearchClient(transport);
        ElasticClient.client = client;
        if (client.ping().value()) {
            ElasticClient.client = client;
            return true;
        }
        return false;
    }

    public static void InsertLog(Map<String, Object> data) {
        // Create the timestamp field
        String timestamp = DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now());
        data.put("@timestamp", timestamp);

        data.put("server", ServerMetrics.GetBasicServerInfo());

        // Try to index the data, or log errors
        try {
            client.index(i -> i
                    .index("logs-minecraft-default")
                    .document(data)
            );
        } catch (Exception e) {
            getLogger().severe(e.getLocalizedMessage() + "\n\t" + e.getCause() + "\n\t" + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void InsertMetric(Map<String, Object> data) {
        // Create the timestamp field
        String timestamp = DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now());
        data.put("@timestamp", timestamp);

        // Try to index the data, or log errors
        try {
            client.index(i -> i
                    .index("metrics-minecraft-default")
                    .document(data)
            );
        } catch (Exception e) {
            getLogger().severe(e.getLocalizedMessage() + "\n\t" + e.getCause() + "\n\t" + Arrays.toString(e.getStackTrace()));
        }
    }
}
