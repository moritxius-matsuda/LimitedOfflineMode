package de.moritxius.limitedofflinemode;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * BungeeCord plugin for LimitedOfflineMode
 * 
 * Allows specific whitelisted usernames to join in offline mode
 * while the proxy is in online mode.
 */
public class LimitedOfflineModeBungeeCordPlugin extends Plugin implements Listener {

    private final Set<String> allowedUsers = new HashSet<>();
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
        getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("LimitedOfflineMode BungeeCord plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LimitedOfflineMode BungeeCord plugin disabled!");
    }

    /**
     * Load allowed users from configuration file
     */
    private void loadAllowedUsers() {
        try {
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
                Files.createDirectories(dataDirectory);
                Files.write(configPath, "# Add usernames that should be allowed to join in offline mode\n".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            getLogger().severe("Failed to load allowed users configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle pre-login event - verify if user is in allowed list
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();
        String username = connection.getName();
        
        if (username != null && allowedUsers.contains(username.toLowerCase(Locale.ROOT))) {
            // Allow offline mode login for whitelisted users
            connection.setOnlineMode(false);
            getLogger().info("Allowing offline mode for whitelisted user: " + username);
        }
    }

    /**
     * Handle post-login event - log successful connections
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String username = player.getName();
        
        if (allowedUsers.contains(username.toLowerCase(Locale.ROOT))) {
            getLogger().info("Offline mode player connected: " + username + " (" + player.getUniqueId() + ")");
        }
    }

    /**
     * Reload configuration - can be called by admin commands if needed
     */
    public void reloadConfiguration() {
        allowedUsers.clear();
        loadAllowedUsers();
        getLogger().info("Configuration reloaded!");
    }

    /**
     * Get the set of allowed users
     */
    public Set<String> getAllowedUsers() {
        return new HashSet<>(allowedUsers);
    }

    /**
     * Add a user to the allowed list
     */
    public void addAllowedUser(String username) {
        allowedUsers.add(username.toLowerCase(Locale.ROOT));
    }

    /**
     * Remove a user from the allowed list
     */
    public void removeAllowedUser(String username) {
        allowedUsers.remove(username.toLowerCase(Locale.ROOT));
    }

    /**
     * Check if a user is in the allowed list
     */
    public boolean isUserAllowed(String username) {
        return allowedUsers.contains(username.toLowerCase(Locale.ROOT));
    }
}
