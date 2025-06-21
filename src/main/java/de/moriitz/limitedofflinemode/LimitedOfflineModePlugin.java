package de.moriitz.limitedofflinemode;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.moriitz.limitedofflinemode.commands.OfflineModeCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Plugin(
    id = "limitedofflinemode",
    name = "LimitedOfflineMode",
    version = "1.0.0",
    description = "Allows specific users to join in offline mode while server is in online mode",
    authors = {"Moriitz"}
)
public class LimitedOfflineModePlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Set<String> allowedOfflineUsers = new HashSet<>();
    
    @Inject
    public LimitedOfflineModePlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfiguration();
        
        // Registriere den Command
        server.getCommandManager().register("offlinemode", new OfflineModeCommand(this));
        
        logger.info("LimitedOfflineMode Plugin loaded successfully!");
        logger.info("Allowed offline users: " + allowedOfflineUsers.size());
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();
        
        // Prüfe ob der Benutzer in der Liste der erlaubten Offline-Benutzer steht
        if (allowedOfflineUsers.contains(username.toLowerCase())) {
            // Erlaube dem Benutzer den Beitritt, auch wenn er nicht authentifiziert ist
            logger.info("Allowing offline user '{}' to join", username);
            
            // Setze den Benutzer als erlaubt - dies überschreibt die Online-Modus-Prüfung
            event.setResult(PreLoginEvent.PreLoginComponentResult.allowed());
            return;
        }
        
        // Für alle anderen Benutzer: Standard-Verhalten beibehalten
        // Das bedeutet, sie müssen weiterhin die Online-Authentifizierung durchlaufen
        logger.debug("User '{}' not in offline whitelist, standard authentication required", username);
    }

    private void loadConfiguration() {
        try {
            // Erstelle das Plugin-Verzeichnis falls es nicht existiert
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            
            Path configFile = dataDirectory.resolve("allowed-users.txt");
            
            // Erstelle die Konfigurationsdatei mit Beispieleinträgen falls sie nicht existiert
            if (!Files.exists(configFile)) {
                createDefaultConfig(configFile);
            }
            
            // Lade die erlaubten Benutzer aus der Datei
            List<String> lines = Files.readAllLines(configFile);
            allowedOfflineUsers.clear();
            
            for (String line : lines) {
                line = line.trim();
                // Ignoriere leere Zeilen und Kommentare
                if (!line.isEmpty() && !line.startsWith("#")) {
                    allowedOfflineUsers.add(line.toLowerCase());
                    logger.debug("Added offline user: {}", line);
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
        }
    }
    
    private void createDefaultConfig(Path configFile) throws IOException {
        String defaultConfig = """
            # LimitedOfflineMode Configuration
            # Liste der Benutzernamen, die im Offline-Modus beitreten dürfen
            # Ein Benutzername pro Zeile
            # Zeilen die mit # beginnen sind Kommentare
            
            # Beispiel-Benutzer (entferne das # um sie zu aktivieren):
            # TestUser1
            # TestUser2
            # OfflinePlayer
            
            # Füge hier deine Test-Benutzer hinzu:
            """;
        
        Files.writeString(configFile, defaultConfig);
        logger.info("Created default configuration file at: {}", configFile);
    }
    
    /**
     * Lädt die Konfiguration neu (kann für Reload-Funktionalität verwendet werden)
     */
    public void reloadConfiguration() {
        loadConfiguration();
        logger.info("Configuration reloaded. Allowed offline users: " + allowedOfflineUsers.size());
    }
    
    /**
     * Gibt die Liste der erlaubten Offline-Benutzer zurück
     */
    public Set<String> getAllowedOfflineUsers() {
        return new HashSet<>(allowedOfflineUsers);
    }
}