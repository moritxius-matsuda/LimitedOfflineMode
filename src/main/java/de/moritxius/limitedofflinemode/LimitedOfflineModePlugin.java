package de.moritxius.limitedofflinemode;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
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
import java.util.UUID;
import java.util.Collections;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Plugin(
        id = "limited-offline-mode",
        name = "LimitedOfflineMode",
        version = "1.0-SNAPSHOT",
        description = "Allows specific usernames to join in offline mode while server is in online mode"
)
public class LimitedOfflineModePlugin {

    private final Set<String> allowedUsers = new HashSet<>();
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
    }

    private void loadAllowedUsers() {
        try {
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
                Files.write(configPath, "# Add usernames that should be allowed to join in offline mode\n".getBytes());
            }
        } catch (IOException e) {
            logger.error("Failed to load allowed users configuration", e);
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) {
        // This is now handled by PreLoginEvent and GameProfileRequestEvent
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();
        if (username != null && allowedUsers.contains(username.toLowerCase(Locale.ROOT))) {
            // CRITICAL FIX: Force offline mode for allowed users
            event.setResult(PreLoginComponentResult.forceOfflineMode());
            logger.info("Forcing offline mode for user: {}", username);
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        String username = event.getUsername();
        if (username != null && allowedUsers.contains(username.toLowerCase(Locale.ROOT))) {
            UUID offlineUuid = UuidUtils.generateOfflinePlayerUuid(username);
            GameProfile offlineProfile = new GameProfile(
                offlineUuid, 
                username, 
                Collections.emptyList()
            );
            
            event.setGameProfile(offlineProfile);
            logger.info("Using offline profile for user: {}", username);
        }
    }
}