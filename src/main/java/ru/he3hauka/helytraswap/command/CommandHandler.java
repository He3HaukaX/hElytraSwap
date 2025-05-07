package ru.he3hauka.helytraswap.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.he3hauka.helytraswap.actions.ActionExecutor;
import ru.he3hauka.helytraswap.config.Config;
import ru.he3hauka.helytraswap.storage.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Config config;
    private final Database database;
    private final Map<UUID, Boolean> localToggleMap = new ConcurrentHashMap<>();
    private final ActionExecutor actionExecutor;
    public CommandHandler(Config config,
                          Database database,
                          ActionExecutor actionExecutor) {
        this.config = config;
        this.database = database;
        this.actionExecutor = actionExecutor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("helytraswap.admin") || !sender.isOp()) {
            sender.sendMessage("§7[§x§F§B§9§C§0§8hElytraSwap§7] §fYou don't have enough rights!");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("§x§F§B§9§C§0§8╔");
            sender.sendMessage("§x§F§B§9§C§0§8╠ §f/" + label + " reload §7(§x§F§B§9§C§0§8Reloads plugin config§7)");
            sender.sendMessage("§x§F§B§9§C§0§8╠ §f/" + label + " toggle §7(§x§F§B§9§C§0§8Enable/Disable ElytraSwap for yourself§7)");
            sender.sendMessage("§x§F§B§9§C§0§8╚");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
            case "reboot": {
                sender.sendMessage("§7[§x§F§B§9§C§0§8hElytraSwap§7] §fAn attempt to §x§F§B§9§C§0§8restart§f the plugin...!");
                long start = System.currentTimeMillis();
                config.init();
                long reloadTime = System.currentTimeMillis() - start;
                sender.sendMessage("§7[§x§F§B§9§C§0§8hElytraSwap§7] §fPlugin refreshed in §x§F§B§9§C§0§8" + reloadTime + "§f ms!");
                return true;
            }

            case "toggle": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command!");
                    return true;
                }

                Player player = (Player) sender;

                getToggleStatus(player.getUniqueId()).thenAccept(currentStatus -> {
                    boolean newStatus = !currentStatus;
                    setToggleStatus(player.getUniqueId(), newStatus).thenRun(() -> {
                        String toggleStatus = newStatus ? config.disable_replace : config.enable_replace;
                        actionExecutor.execute(player, config.toggle_actions, "NONE", "UNKNOWN", toggleStatus);
                    });
                });
                return true;
            }

            default:
                sender.sendMessage("§x§F§B§9§C§0§8╔");
                sender.sendMessage("§x§F§B§9§C§0§8╠ §f/" + label + " reload §7(§x§F§B§9§C§0§8Reloads plugin config§7)");
                sender.sendMessage("§x§F§B§9§C§0§8╠ §f/" + label + " toggle §7(§x§F§B§9§C§0§8Enable/Disable ElytraSwap for yourself§7)");
                sender.sendMessage("§x§F§B§9§C§0§8╚");
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "reboot", "toggle")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public CompletableFuture<Boolean> getToggleStatus(UUID uuid) {
        if (database != null) {
            return database.getToggleStatus(uuid);
        }
        return CompletableFuture.completedFuture(localToggleMap.getOrDefault(uuid, true));
    }

    private CompletableFuture<Void> setToggleStatus(UUID uuid, boolean status) {
        if (database != null) {
            return database.setToggleStatus(uuid, status);
        }
        localToggleMap.put(uuid, status);
        return CompletableFuture.completedFuture(null);
    }

    public boolean getLocalToggleStatus(UUID uuid) {
        return localToggleMap.getOrDefault(uuid, true);
    }
}