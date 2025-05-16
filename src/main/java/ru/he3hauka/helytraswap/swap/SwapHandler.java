package ru.he3hauka.helytraswap.swap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.he3hauka.helytraswap.actions.ActionExecutor;
import ru.he3hauka.helytraswap.config.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SwapHandler {
    private final JavaPlugin plugin;
    private final Config config;
    private final ActionExecutor executor;
    private final Map<Player, Long> cooldowns = new ConcurrentHashMap<>();

    public SwapHandler(JavaPlugin plugin,
                       Config config,
                       ActionExecutor executor) {
        this.plugin = plugin;
        this.config = config;
        this.executor = executor;
    }

    public void swap(Player player,
                     ItemStack handItem,
                     ItemStack chestplate,
                     boolean isElytra) {
        var equip = new ItemStack(handItem.getType());
        equip.setItemMeta(handItem.getItemMeta());

        if (handItem.getAmount() > 1) handItem.setAmount(handItem.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);

        if (chestplate != null) {
            var inv = player.getInventory();
            if (inv.firstEmpty() != -1) inv.addItem(chestplate);
            else player.getWorld().dropItemNaturally(player.getLocation(), chestplate);
        }

        player.getInventory().setChestplate(equip);
        player.updateInventory();

        cooldowns.put(player, System.currentTimeMillis());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            executor.execute(player, config.cooldown_expired_actions, "EXPIRED", "NONE", "ON");
            cooldowns.remove(player);
        }, config.settings_cooldown * 20L);

        var replacer = isElytra ? config.replacers_elytra : config.replacers_chestplate;
        executor.execute(player, config.swapper_actions, String.valueOf(config.settings_cooldown), replacer, "ON");
    }
}
