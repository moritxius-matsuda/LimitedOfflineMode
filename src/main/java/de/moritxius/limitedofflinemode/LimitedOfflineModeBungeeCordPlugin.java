package de.moritxius.limitedofflinemode;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BungeeCord plugin for LimitedOfflineMode
 */
public class LimitedOfflineModeBungeeCordPlugin extends Plugin implements Listener {

    private final Set<String> allowedUsers = new HashSet<>();
    private final Map<String, Set<String>> playerGroups = new HashMap<>();
    private final Set<String> enabledGroups = new HashSet<>();
    private Path dataDirectory;

    @Override
    public void onLoad() {
        getLogger().info("Loading LimitedOfflineMode BungeeCord plugin...");
    }

    @Override
    public void onEnable() {
        dataDirectory = getDataFolder().toPath();
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            getLogger().severe("Failed to create data directory: " + e.getMessage());
        }

        loadAllowedUsers();
        loadPlayerGroups();
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new GroupCommand());
        getLogger().info("LimitedOfflineMode BungeeCord plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LimitedOfflineMode BungeeCord plugin disabled!");
    }

    private void loadAllowedUsers() {
        try {
            allowedUsers.clear();
            Path configPath = dataDirectory.resolve("allowed-users.txt");
            if (Files.exists(configPath)) {
                Files.readAllLines(configPath, StandardCharsets.UTF_8).forEach(line -> {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        allowedUsers.add(trimmed.toLowerCase(Locale.ROOT));
                    }
                });
                getLogger().info("Loaded " + allowedUsers.size() + " allowed users");
            } else {
                getLogger().info("Creating default allowed-users.txt");
                Files.write(configPath, "# Add usernames that should be allowed to join in offline mode\n".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            getLogger().severe("Failed to load allowed users configuration: " + e.getMessage());
        }
    }

    private void loadPlayerGroups() {
        try {
            playerGroups.clear();
            enabledGroups.clear();

            Path groupsPath = dataDirectory.resolve("player-groups.txt");
            if (!Files.exists(groupsPath)) {
                Files.write(groupsPath, List.of(
                        "# Format: groupName|enabled|player1,player2",
                        "admins|true|ServerAdmin,HeadDeveloper",
                        "testers|false|TestUser"
                ), StandardCharsets.UTF_8);
                getLogger().info("Created default player-groups.txt");
                return;
            }

            Files.readAllLines(groupsPath, StandardCharsets.UTF_8).forEach(line -> {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    return;
                }

                String[] parts = trimmed.split("\\|", -1);
                if (parts.length < 3) {
                    getLogger().warning("Skipping invalid group line: " + trimmed);
                    return;
                }

                String groupName = normalizeGroupName(parts[0]);
                boolean enabled = Boolean.parseBoolean(parts[1].trim());
                Set<String> members = Arrays.stream(parts[2].split(","))
                        .map(this::normalizeUsername)
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.toCollection(HashSet::new));

                if (groupName.isEmpty()) {
                    return;
                }

                playerGroups.put(groupName, members);
                if (enabled) {
                    enabledGroups.add(groupName);
                }
            });

            getLogger().info("Loaded " + playerGroups.size() + " player groups (" + enabledGroups.size() + " enabled)");
        } catch (IOException e) {
            getLogger().severe("Failed to load player groups: " + e.getMessage());
        }
    }

    private void savePlayerGroups() {
        Path groupsPath = dataDirectory.resolve("player-groups.txt");
        List<String> lines = playerGroups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String members = entry.getValue().stream().sorted().collect(Collectors.joining(","));
                    return entry.getKey() + "|" + enabledGroups.contains(entry.getKey()) + "|" + members;
                })
                .collect(Collectors.toList());

        lines.add(0, "# Format: groupName|enabled|player1,player2");

        try {
            Files.write(groupsPath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            getLogger().severe("Failed to save player groups: " + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();
        String username = connection.getName();

        if (username != null && isUserAllowed(username)) {
            connection.setOnlineMode(false);
            getLogger().info("Allowing offline mode for allowed user: " + username);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String username = player.getName();

        if (isUserAllowed(username)) {
            getLogger().info("Offline mode player connected: " + username + " (" + player.getUniqueId() + ")");
        }
    }

    private boolean isUserAllowed(String username) {
        String normalized = normalizeUsername(username);
        if (allowedUsers.contains(normalized)) {
            return true;
        }

        for (String enabledGroup : enabledGroups) {
            Set<String> members = playerGroups.get(enabledGroup);
            if (members != null && members.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeGroupName(String groupName) {
        return groupName == null ? "" : groupName.trim().toLowerCase(Locale.ROOT);
    }

    private class GroupCommand extends Command {
        GroupCommand() {
            super("lomgroup", "limitedofflinemode.admin", "limitedofflinemode");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 2 || !"group".equalsIgnoreCase(args[0])) {
                sendHelp(sender);
                return;
            }

            String action = args[1].toLowerCase(Locale.ROOT);
            String groupName = args.length > 2 ? normalizeGroupName(args[2]) : "";

            switch (action) {
                case "add" -> {
                    if (args.length < 4) {
                        sendMessage(sender, "Usage: /lomgroup group add <group> <player1,player2,...>");
                        return;
                    }

                    Set<String> players = Arrays.stream(args[3].split(","))
                            .map(LimitedOfflineModeBungeeCordPlugin.this::normalizeUsername)
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toCollection(HashSet::new));

                    if (groupName.isEmpty() || players.isEmpty()) {
                        sendMessage(sender, "Invalid group or players.");
                        return;
                    }

                    playerGroups.computeIfAbsent(groupName, key -> new HashSet<>()).addAll(players);
                    savePlayerGroups();
                    sendMessage(sender, "Group '" + groupName + "' updated with " + players.size() + " players.");
                }
                case "enable", "disable", "toggle" -> {
                    if (groupName.isEmpty() || !playerGroups.containsKey(groupName)) {
                        sendMessage(sender, "Unknown group: " + groupName);
                        return;
                    }

                    boolean enable = "enable".equals(action) || ("toggle".equals(action) && !enabledGroups.contains(groupName));
                    if (enable) {
                        enabledGroups.add(groupName);
                    } else {
                        enabledGroups.remove(groupName);
                    }
                    savePlayerGroups();
                    sendMessage(sender, "Group '" + groupName + "' " + (enable ? "enabled" : "disabled") + ".");
                }
                case "list" -> {
                    sendMessage(sender, "Groups:");
                    playerGroups.forEach((name, members) ->
                            sendMessage(sender, "- " + name + " [" + (enabledGroups.contains(name) ? "ON" : "OFF") + "] " + members)
                    );
                }
                default -> sendHelp(sender);
            }
        }

        private void sendHelp(CommandSender sender) {
            sendMessage(sender, "/lomgroup group add <group> <player1,player2,...>");
            sendMessage(sender, "/lomgroup group enable <group>");
            sendMessage(sender, "/lomgroup group disable <group>");
            sendMessage(sender, "/lomgroup group toggle <group>");
            sendMessage(sender, "/lomgroup group list");
        }

        private void sendMessage(CommandSender sender, String message) {
            sender.sendMessage(new TextComponent(message));
        }
    }
}
