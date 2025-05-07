package ru.he3hauka.helytraswap.config;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

@Getter
public class Config {
    private final JavaPlugin plugin;
    public List<String> disabled_worlds;
    public int settings_cooldown;
    public String replacers_chestplate;
    public String replacers_elytra;
    public String mysql_host;
    public int mysql_port;
    public String mysql_database;
    public String mysql_username;
    public String mysql_password;
    public boolean mysql_enable;
    public String enable_replace;
    public String disable_replace;
    public List<String> perms_actions;
    public List<String> cooldown_actions;
    public List<String> cooldown_expired_actions;
    public List<String> swapper_actions;
    public List<String> toggle_actions;
    public List<String> disabled_world_actions;
    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.settings_cooldown = config.getInt("settings.cooldown", 10);

        this.disabled_worlds = config.getStringList("settings.disabled-worlds");

        this.mysql_host = config.getString("mysql.host", "localhost");
        this.mysql_port = config.getInt("mysql.port", 3306);
        this.mysql_database = config.getString("mysql.database", "minecraft");
        this.mysql_username = config.getString("mysql.username", "user");
        this.mysql_password = config.getString("mysql.password", "password");
        this.mysql_enable = config.getBoolean("mysql.enable", false);

        this.enable_replace = config.getString("settings.replacers.enable", "включено");
        this.disable_replace = config.getString("settings.replacers.disable", "выключено");

        this.replacers_chestplate = config.getString("settings.replacers.chestplate", "нагрудник");
        this.replacers_elytra = config.getString("settings.replacers.elytra", "элитру");

        this.perms_actions = config.getStringList("actions.perms");
        this.cooldown_actions = config.getStringList("actions.cooldown");
        this.cooldown_expired_actions = config.getStringList("actions.cooldown-expired");
        this.swapper_actions = config.getStringList("actions.swapper");
        this.toggle_actions = config.getStringList("actions.toggle");
        this.disabled_world_actions = config.getStringList("actions.disabled-world");
    }
}