package ru.he3hauka.helytraswap.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.he3hauka.helytraswap.command.CommandHandler;
import ru.he3hauka.helytraswap.config.Config;
import ru.he3hauka.helytraswap.storage.Database;

public class ElytraPlaceholder extends PlaceholderExpansion {
    private final Database database;
    private final Config config;
    private final CommandHandler commandHandler;
    public ElytraPlaceholder(Database database,
                             Config config,
                             CommandHandler commandHandler) {
        this.database = database;
        this.config = config;
        this.commandHandler = commandHandler;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "helytraswap";
    }

    @Override
    public @NotNull String getAuthor() {
        return "He3Hauka";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        if (params.equalsIgnoreCase("toggle")) {
            boolean status = database != null
                    ? database.getToggleStatus(player.getUniqueId()).join()
                    : commandHandler.getLocalToggleStatus(player.getUniqueId());
            return status ? config.disable_replace : config.enable_replace;
        }
        return null;
    }
}