package com.example.limitedofflinemode;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
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

    public LimitedOfflineModePlugin(Logger logger, ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadAllowedUsers();
        proxy.getEventManager().register(this, this);
    }

    private void loadAllowedUsers() {
        try {
            Path configPath = dataDirectory.resolve("allowed-users.txt");
            if (Files.exists(configPath)) {
                Files.readAllLines(configPath).forEach(line -> {
                    if (!line.trim().isEmpty()) {
                        allowedUsers.add(line.trim().toLowerCase());
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
        if (event.getResult() == ComponentResult.allowed()) {
            return;
        }
    
        String username = event.getPlayer().getUsername();
        if (username != null && allowedUsers.contains(username.toLowerCase())) {
            event.setResult(ComponentResult.allowed());
            logger.info("Allowed offline user: {}", username);
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.getResult() == PreLoginEvent.PreLoginComponentResult.allowed()) {
            return;
        }

        String username = event.getUsername();
        if (username != null && allowedUsers.contains(username.toLowerCase())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.allowed());
            logger.info("Pre-login allowed for offline user: {}", username);
        }
    }
}
