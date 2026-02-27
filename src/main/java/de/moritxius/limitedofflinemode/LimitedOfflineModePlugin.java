package de.moritxius.limitedofflinemode;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.UuidUtils;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Plugin(
        id = "limited-offline-mode",
        name = "LimitedOfflineMode",
        version = "1.0-SNAPSHOT",
        description = "Allows specific usernames to join in offline mode while server is in online mode"
)
public class LimitedOfflineModePlugin {

    private final Set<String> allowedUsers = new HashSet<>();
    private final Map<String, Set<String>> playerGroups = new HashMap<>();
    private final Set<String> enabledGroups = new HashSet<>();
    private final Logger logger;
    private final ProxyServer proxy;
    private final Path dataDirectory;

    @Inject
    public LimitedOfflineModePlugin(Logger logger, ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadAllowedUsers();
        loadPlayerGroups();
        registerCommands();
    }

    private void registerCommands() {
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("lomgroup").aliases("limitedofflinemode").build(),
                new GroupCommand()
        );
    }

    private void loadAllowedUsers() {
        try {
            allowedUsers.clear();
            Path configPath = dataDirectory.resolve("allowed-users.txt");
            if (Files.exists(configPath)) {
                Files.readAllLines(configPath).forEach(line -> {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        allowedUsers.add(trimmed.toLowerCase(Locale.ROOT));
                    }
                });
                logger.info("Loaded {} allowed users", allowedUsers.size());
            } else {
                logger.info("Creating default allowed-users.txt");
                Files.createDirectories(dataDirectory);
                Files.write(configPath, "# Add usernames that should be allowed to join in offline mode\n".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            logger.error("Failed to load allowed users configuration", e);
        }
    }

    private void loadPlayerGroups() {
        try {
            playerGroups.clear();
            enabledGroups.clear();

            Path groupsPath = dataDirectory.resolve("player-groups.txt");
            if (!Files.exists(groupsPath)) {
                Files.createDirectories(dataDirectory);
                Files.write(groupsPath, List.of(
                        "# Format: groupName|enabled|player1,player2",
                        "admins|true|ServerAdmin,HeadDeveloper",
                        "testers|false|TestUser"
                ), StandardCharsets.UTF_8);
                logger.info("Created default player-groups.txt");
                return;
            }

            Files.readAllLines(groupsPath, StandardCharsets.UTF_8).forEach(line -> {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    return;
                }

                String[] parts = trimmed.split("\\|", -1);
                if (parts.length < 3) {
                    logger.warn("Skipping invalid group line: {}", trimmed);
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

            logger.info("Loaded {} player groups ({} enabled)", playerGroups.size(), enabledGroups.size());
        } catch (IOException e) {
            logger.error("Failed to load player groups", e);
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
            Files.createDirectories(dataDirectory);
            Files.write(groupsPath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to save player groups", e);
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) {
        // handled by PreLoginEvent and GameProfileRequestEvent
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();
        if (username != null && isUserAllowed(username)) {
            event.setResult(PreLoginComponentResult.forceOfflineMode());
            logger.info("Forcing offline mode for user: {}", username);
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        String username = event.getUsername();
        if (username != null && isUserAllowed(username)) {
            UUID offlineUuid = UuidUtils.generateOfflinePlayerUuid(username);
            GameProfile offlineProfile = new GameProfile(offlineUuid, username, Collections.emptyList());
            event.setGameProfile(offlineProfile);
            logger.info("Using offline profile for user: {}", username);
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

    private class GroupCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] args = invocation.arguments();

            if (!source.hasPermission("limitedofflinemode.admin")) {
                sendMessage(source, "No permission.");
                return;
            }

            if (args.length < 2 || !"group".equalsIgnoreCase(args[0])) {
                sendHelp(source);
                return;
            }

            String action = args[1].toLowerCase(Locale.ROOT);
            String groupName = args.length > 2 ? normalizeGroupName(args[2]) : "";

            switch (action) {
                case "add" -> {
                    if (args.length < 4) {
                        sendMessage(source, "Usage: /lomgroup group add <group> <player1,player2,...>");
                        return;
                    }

                    Set<String> players = Arrays.stream(args[3].split(","))
                            .map(LimitedOfflineModePlugin.this::normalizeUsername)
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toCollection(HashSet::new));

                    if (groupName.isEmpty() || players.isEmpty()) {
                        sendMessage(source, "Invalid group or players.");
                        return;
                    }

                    playerGroups.computeIfAbsent(groupName, key -> new HashSet<>()).addAll(players);
                    savePlayerGroups();
                    sendMessage(source, "Group '" + groupName + "' updated with " + players.size() + " players.");
                }
                case "enable", "disable", "toggle" -> {
                    if (groupName.isEmpty() || !playerGroups.containsKey(groupName)) {
                        sendMessage(source, "Unknown group: " + groupName);
                        return;
                    }

                    boolean enable = "enable".equals(action) || ("toggle".equals(action) && !enabledGroups.contains(groupName));
                    if (enable) {
                        enabledGroups.add(groupName);
                    } else {
                        enabledGroups.remove(groupName);
                    }
                    savePlayerGroups();
                    sendMessage(source, "Group '" + groupName + "' " + (enable ? "enabled" : "disabled") + ".");
                }
                case "list" -> {
                    sendMessage(source, "Groups:");
                    playerGroups.forEach((name, members) ->
                            sendMessage(source, "- " + name + " [" + (enabledGroups.contains(name) ? "ON" : "OFF") + "] " + members)
                    );
                }
                default -> sendHelp(source);
            }
        }

        private void sendHelp(CommandSource source) {
            sendMessage(source, "/lomgroup group add <group> <player1,player2,...>");
            sendMessage(source, "/lomgroup group enable <group>");
            sendMessage(source, "/lomgroup group disable <group>");
            sendMessage(source, "/lomgroup group toggle <group>");
            sendMessage(source, "/lomgroup group list");
        }

        private void sendMessage(CommandSource source, String message) {
            source.sendMessage(Component.text(message));
        }
    }
}
