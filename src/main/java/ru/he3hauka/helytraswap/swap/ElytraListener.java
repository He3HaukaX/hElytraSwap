package ru.he3hauka.helytraswap.swap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.he3hauka.helytraswap.actions.ActionExecutor;
import ru.he3hauka.helytraswap.command.CommandHandler;
import ru.he3hauka.helytraswap.config.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElytraListener implements Listener {
    private final SwapHandler swapHandler;
    private final CommandHandler commandHandler;
    private final Config config;
    private final Map<Player, Long> shortCooldown = new ConcurrentHashMap<>();
    private final ActionExecutor actionExecutor;
    public ElytraListener(SwapHandler swapHandler,
                          CommandHandler commandHandler,
                          Config config,
                          ActionExecutor actionExecutor) {
        this.swapHandler = swapHandler;
        this.commandHandler = commandHandler;
        this.config = config;
        this.actionExecutor = actionExecutor;
    }

    @EventHandler
    public void Use(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var now = System.currentTimeMillis();

        if (shortCooldown.containsKey(player) && now - shortCooldown.get(player) < 400) return;
        shortCooldown.put(player, now);

        commandHandler.getToggleStatus(player.getUniqueId()).thenAccept(toggle -> {
            if (toggle) return;

            var handItem = event.getItem();
            if (handItem == null || !handItem.equals(player.getInventory().getItemInMainHand())) return;

            var type = handItem.getType();
            if (type != Material.ELYTRA && !type.name().endsWith("_CHESTPLATE")) return;

            var chestplate = player.getInventory().getChestplate();
            boolean isElytra = type == Material.ELYTRA;

            if ((isElytra && (chestplate == null || chestplate.getType() == Material.ELYTRA)) ||
                    (!isElytra && (chestplate == null || chestplate.getType() != Material.ELYTRA)))
                return;

            if (!player.hasPermission("helytraswap.toggle")) {
                actionExecutor.execute(player, config.perms_actions, "NONE", "UNKNOWN", "OFF");
                return;
            }

            if (config.disabled_worlds.contains(player.getWorld().getName())) {
                actionExecutor.execute(player, config.disabled_world_actions, "NONE", "UNKNOWN", "ON");
                return;
            }

            event.setCancelled(true);
            swapHandler.swap(player, handItem, chestplate, isElytra);
        });
    }
}
