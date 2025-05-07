package ru.he3hauka.hnear.update;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String repo = "He3HaukaX/hElytraSwap";
    private final Logger logger;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + repo + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("User-Agent", "Mozilla/5.0");

                if (conn.getResponseCode() == 200) {
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(new InputStreamReader(conn.getInputStream()));

                    String latestVersion = (String) json.get("tag_name");
                    String currentVersion = plugin.getDescription().getVersion();

                    if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                        logger.warning("A new update is available! Latest version: " + latestVersion + ", current version: " + currentVersion);
                        logger.warning("Download it here: https://github.com/" + repo + "/releases/tag/" + latestVersion);
                    } else {
                        logger.info("You are running the latest version: " + currentVersion);
                    }
                } else {
                    logger.warning("Update check failed: HTTP response code " + conn.getResponseCode());
                }

                conn.disconnect();
            } catch (Exception e) {
                logger.warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }
}
