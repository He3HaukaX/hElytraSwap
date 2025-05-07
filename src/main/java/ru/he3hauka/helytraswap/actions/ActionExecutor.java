package ru.he3hauka.helytraswap.actions;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.he3hauka.helytraswap.utils.HexSupport.format;

public class ActionExecutor {

    private static final Pattern HOVER_PATTERN = Pattern.compile("\\{HoverText:cmd (.*?), text: (.*?)}");
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();

    public void execute(Player player, List<String> actions, String cooldownTime, String item, String toggle) {
        for (String action : actions) {
            if (!action.contains(" ")) continue;
            String type = action.substring(0, action.indexOf(" "));
            String content = action.substring(type.length()).trim();
            switch (type) {
                case "[Message]" -> sendMessage(player, content, cooldownTime, item, toggle);
                case "[Sound]" -> playSound(player, content);
                case "[Title]" -> sendTitle(player, content, cooldownTime, item, toggle);
                case "[Console]" -> runConsoleCommand(player, content, cooldownTime, item, toggle);
                case "[Broadcast]" -> broadcastMessage(player, content, cooldownTime, item, toggle);
                case "[Actionbar]" -> sendActionBar(player, content, cooldownTime, item, toggle);
                case "[Bossbar]" -> sendBossBar(player, content, cooldownTime, item, toggle);
                default -> player.sendMessage(SERIALIZER.deserialize("§cUnknown action type: " + type));
            }
        }
    }

    private void sendMessage(Player player, String raw, String cooldownTime, String item, String toggle) {
        if (player == null) return;
        String message = formatWithPlaceholders(raw, player, cooldownTime, item, toggle);
        Matcher matcher = HOVER_PATTERN.matcher(message);
        if (matcher.find()) {
            String command = matcher.group(1);
            String hover = matcher.group(2);
            String mainText = message.substring(0, matcher.start()) + message.substring(matcher.end());
            TextComponent component = SERIALIZER.deserialize(mainText)
                    .hoverEvent(HoverEvent.showText(SERIALIZER.deserialize(formatWithPlaceholders(hover, player, cooldownTime, item, toggle))))
                    .clickEvent(ClickEvent.runCommand(command));
            player.sendMessage(component);
        } else {
            player.sendMessage(SERIALIZER.deserialize(message));
        }
    }

    private void broadcastMessage(Player source, String raw, String cooldownTime, String item, String toggle) {
        String message = formatWithPlaceholders(raw, source, cooldownTime, item, toggle);
        Matcher matcher = HOVER_PATTERN.matcher(message);
        if (matcher.find()) {
            String command = matcher.group(1);
            String hover = matcher.group(2);
            String mainText = message.substring(0, matcher.start()) + message.substring(matcher.end());
            TextComponent component = SERIALIZER.deserialize(mainText)
                    .hoverEvent(HoverEvent.showText(SERIALIZER.deserialize(formatWithPlaceholders(hover, source, cooldownTime, item, toggle))))
                    .clickEvent(ClickEvent.runCommand(command));
            Bukkit.broadcast(component);
        } else {
            Component component = SERIALIZER.deserialize(message);
            Bukkit.broadcast(component);
        }
    }

    private void playSound(Player player, String content) {
        if (player == null) return;
        try {
            String[] parts = content.split(":");
            Sound sound = Sound.valueOf(parts[0].trim().toUpperCase());
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            player.sendMessage(SERIALIZER.deserialize("§cInvalid sound or parameters"));
        }
    }

    private void sendTitle(Player player, String raw, String cooldownTime, String item, String toggle) {
        if (player == null) return;
        String formatted = formatWithPlaceholders(raw, player, cooldownTime, item, toggle);
        String[] parts = formatted.split("&&");
        Component title = SERIALIZER.deserialize(parts.length > 0 ? parts[0].trim() : "");
        Component subtitle = SERIALIZER.deserialize(parts.length > 1 ? parts[1].trim() : "");
        player.showTitle(Title.title(title, subtitle, Title.Times.of(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(1000))));
    }

    private void sendActionBar(Player player, String raw, String cooldownTime, String item, String toggle) {
        if (player == null) return;
        String message = formatWithPlaceholders(raw, player, cooldownTime, item, toggle);
        Component component = SERIALIZER.deserialize(message);
        player.sendActionBar(component);
    }

    private void sendBossBar(Player player, String raw, String cooldownTime, String item, String toggle) {
        if (player == null) return;
        String formatted = formatWithPlaceholders(raw, player, cooldownTime, item, toggle);
        String[] parts = formatted.split(":", 5);
        if (parts.length < 2) {
            player.sendMessage(SERIALIZER.deserialize("§cFormat is incorrect. Expected: Text:Duration:Color:Style"));
            return;
        }
        String text = parts[0].trim();
        long durationTicks;
        try {
            durationTicks = Long.parseLong(parts[1].trim());
        } catch (NumberFormatException e) {
            player.sendMessage(SERIALIZER.deserialize("§cInvalid duration in bossbar"));
            return;
        }
        Color color = Color.PURPLE;
        if (parts.length > 2) {
            try {
                color = Color.valueOf(parts[2].trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        Overlay overlay = Overlay.PROGRESS;
        if (parts.length > 3) {
            try {
                overlay = Overlay.valueOf(parts[3].trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        float progress = 1.0f;
        if (parts.length > 4) {
            try {
                float val = Float.parseFloat(parts[4].trim());
                if (val >= 0.0f && val <= 1.0f) {
                    progress = val;
                }
            } catch (Exception ignored) {}
        }
        BossBar bossBar = BossBar.bossBar(SERIALIZER.deserialize(text), progress, color, overlay);
        if (activeBossBars.containsKey(player.getUniqueId())) {
            player.hideBossBar(activeBossBars.get(player.getUniqueId()));
        }
        activeBossBars.put(player.getUniqueId(), bossBar);
        player.showBossBar(bossBar);
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("hElytraSwap"), () -> {
            player.hideBossBar(bossBar);
            activeBossBars.remove(player.getUniqueId());
        }, durationTicks);
    }

    private void runConsoleCommand(Player player, String command, String cooldownTime, String item, String toggle) {
        String parsed = formatWithPlaceholders(command, player, cooldownTime, item, toggle);
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, parsed);
    }

    private String formatWithPlaceholders(String text, Player player, String cooldownTime, String item, String toggle) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        text = text.replace("%player%", player.getName());
        text = text.replace("%cooldown%", cooldownTime);
        text = text.replace("%item%", item);
        text = text.replace("%toggle%", toggle);
        text = format(text);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}
