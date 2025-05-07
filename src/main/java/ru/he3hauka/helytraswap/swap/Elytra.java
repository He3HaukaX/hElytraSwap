package ru.he3hauka.helytraswap.swap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.he3hauka.helytraswap.actions.ActionExecutor;
import ru.he3hauka.helytraswap.command.CommandHandler;
import ru.he3hauka.helytraswap.config.Config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Elytra implements Listener {
    private final JavaPlugin plugin;
    private final Config config;
    public static final Map<Player, Long> cooldowns = new HashMap<>();
    private final CommandHandler commandHandler;
    private final ActionExecutor actionExecutor;

    public Elytra(JavaPlugin plugin,
                  Config config,
                  CommandHandler commandHandler,
                  ActionExecutor actionExecutor) {
        this.plugin = plugin;
        this.config = config;
        this.commandHandler = commandHandler;
        this.actionExecutor = actionExecutor;
    }

    @EventHandler
    public void useElytra(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        commandHandler.getToggleStatus(player.getUniqueId()).thenAccept(toggle -> {
            if (toggle) {
                return;
            }

            ItemStack itemInHand = event.getItem();
            if (itemInHand == null) {
                return;
            }

            Material type = itemInHand.getType();
            if (type != Material.ELYTRA && !type.name().endsWith("_CHESTPLATE")) {
                return;
            }

            ItemStack chestplate = player.getInventory().getChestplate();
            boolean isElytraInHand = type == Material.ELYTRA;

            if (isElytraInHand && chestplate == null) {
                return;
            }
            if (isElytraInHand && chestplate.getType() == Material.ELYTRA) {
                return;
            }
            if (!isElytraInHand && (chestplate == null || chestplate.getType() != Material.ELYTRA)) {
                return;
            }

            if (!player.hasPermission("helytraswap.toggle")) {
                actionExecutor.execute(player, config.perms_actions, "NONE", "UNKNOWN", "OFF");
                return;
            }

            if (config.disabled_worlds.contains(player.getWorld().getName())) {
                actionExecutor.execute(player, config.disabled_world_actions, "NONE", "UNKNOWN", "ON");
                return;
            }

            if (cooldowns.containsKey(player)) {
                long cooldownEndTime = cooldowns.get(player) + config.settings_cooldown * 1000L;
                long timeLeft = cooldownEndTime - System.currentTimeMillis();
                if (timeLeft > 0) {
                    double cooldownSeconds = timeLeft / 1000.0;
                    String formattedCooldown = String.format(Locale.US, "%.1f", cooldownSeconds);
                    actionExecutor.execute(player, config.cooldown_actions, formattedCooldown, "UNKNOWN", "ON");
                    return;
                }
            }

            event.setCancelled(true);
            handleItemSwap(player, itemInHand, chestplate, isElytraInHand);
        });
    }

    private void handleItemSwap(Player player, ItemStack itemInHand, ItemStack chestplate, boolean isElytraInHand) {
        ItemStack itemToEquip = itemInHand.clone();
        itemToEquip.setAmount(1);

        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        if (chestplate != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(chestplate);
            } else {
                player.getWorld().dropItem(player.getLocation(), chestplate);
            }
        }

        player.getInventory().setChestplate(itemToEquip);
        String itemName = isElytraInHand ? config.replacers_elytra : config.replacers_chestplate;

        player.updateInventory();
        cooldowns.put(player, System.currentTimeMillis());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            actionExecutor.execute(player, config.cooldown_expired_actions, "EXPIRED", "NONE", "ON");
            cooldowns.remove(player);
        }, config.settings_cooldown * 20L);

        actionExecutor.execute(player, config.swapper_actions, String.valueOf(config.settings_cooldown), itemName, "ON");
    }
}
