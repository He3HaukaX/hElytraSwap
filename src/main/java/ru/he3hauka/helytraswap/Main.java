package ru.he3hauka.helytraswap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.he3hauka.helytraswap.actions.ActionExecutor;
import ru.he3hauka.helytraswap.command.CommandHandler;
import ru.he3hauka.helytraswap.config.Config;
import ru.he3hauka.helytraswap.softdepend.PlaceholderDepend;
import ru.he3hauka.helytraswap.storage.Database;
import ru.he3hauka.helytraswap.storage.MySQLDatabase;
import ru.he3hauka.helytraswap.storage.SQLiteDatabase;
import ru.he3hauka.helytraswap.swap.Elytra;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main extends JavaPlugin {
    private Database database;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config config = new Config(this);
        config.init();

        try {
            if (config.mysql_enable) {
                database = new MySQLDatabase(
                        config.mysql_host,
                        config.mysql_port,
                        config.mysql_database,
                        config.mysql_username,
                        config.mysql_password
                );
                getLogger().info("Usage MySQL database");
            } else {
                database = new SQLiteDatabase();
                getLogger().warning("Usage SQLite database");
            }
        } catch (Exception e) {
            getLogger().severe("DataBase error: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ActionExecutor actionExecutor = new ActionExecutor();
        CommandHandler commandHandler = new CommandHandler(config, database, actionExecutor);

        Elytra elytra = new Elytra(this, config, commandHandler, actionExecutor);
        getServer().getPluginManager().registerEvents(elytra, this);
        getCommand("helytraswap").setExecutor(commandHandler);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && database != null) {
            new PlaceholderDepend(database, config, commandHandler).register();
        }

        if (getConfig().getBoolean("settings.update", true)) {
            new ru.he3hauka.hnear.update.UpdateChecker(this).checkForUpdates();
        }

        authorInfo();
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
    }

    public void authorInfo(){
        File file = new File(getDataFolder(), "info.txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.copy(getResource("info.txt"), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}