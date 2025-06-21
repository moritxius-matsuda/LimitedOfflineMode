package de.moriitz.limitedofflinemode.commands;

import com.velocitypowered.api.command.SimpleCommand;
import de.moriitz.limitedofflinemode.LimitedOfflineModePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OfflineModeCommand implements SimpleCommand {
    
    private final LimitedOfflineModePlugin plugin;
    
    public OfflineModeCommand(LimitedOfflineModePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        var source = invocation.source();
        String[] args = invocation.arguments();
        
        if (args.length == 0) {
            sendHelpMessage(source);
            return;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
                handleListCommand(source);
                break;
                
            case "add":
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /offlinemode add <username>", NamedTextColor.RED));
                    return;
                }
                handleAddCommand(source, args[1]);
                break;
                
            case "remove":
            case "delete":
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /offlinemode remove <username>", NamedTextColor.RED));
                    return;
                }
                handleRemoveCommand(source, args[1]);
                break;
                
            case "reload":
                handleReloadCommand(source);
                break;
                
            case "help":
                sendHelpMessage(source);
                break;
                
            default:
                source.sendMessage(Component.text("Unknown subcommand: " + args[0], NamedTextColor.RED));
                sendHelpMessage(source);
                break;
        }
    }
    
    private void sendHelpMessage(com.velocitypowered.api.command.CommandSource source) {
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.text("        LimitedOfflineMode Commands", NamedTextColor.YELLOW, TextDecoration.BOLD));
        source.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.empty());
        
        // List command
        source.sendMessage(Component.text("▶ ", NamedTextColor.GREEN)
            .append(Component.text("/offlinemode list", NamedTextColor.AQUA, TextDecoration.BOLD)
                .clickEvent(ClickEvent.suggestCommand("/offlinemode list"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to execute", NamedTextColor.GRAY))))
            .append(Component.text(" - Show all allowed offline users", NamedTextColor.WHITE)));
        
        // Add command
        source.sendMessage(Component.text("▶ ", NamedTextColor.GREEN)
            .append(Component.text("/offlinemode add <username>", NamedTextColor.AQUA, TextDecoration.BOLD)
                .clickEvent(ClickEvent.suggestCommand("/offlinemode add "))
                .hoverEvent(HoverEvent.showText(Component.text("Click to start typing", NamedTextColor.GRAY))))
            .append(Component.text(" - Add a user to offline whitelist", NamedTextColor.WHITE)));
        
        // Remove command
        source.sendMessage(Component.text("▶ ", NamedTextColor.GREEN)
            .append(Component.text("/offlinemode remove <username>", NamedTextColor.AQUA, TextDecoration.BOLD)
                .clickEvent(ClickEvent.suggestCommand("/offlinemode remove "))
                .hoverEvent(HoverEvent.showText(Component.text("Click to start typing", NamedTextColor.GRAY))))
            .append(Component.text(" - Remove a user from offline whitelist", NamedTextColor.WHITE)));
        
        // Reload command
        source.sendMessage(Component.text("▶ ", NamedTextColor.GREEN)
            .append(Component.text("/offlinemode reload", NamedTextColor.AQUA, TextDecoration.BOLD)
                .clickEvent(ClickEvent.suggestCommand("/offlinemode reload"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to execute", NamedTextColor.GRAY))))
            .append(Component.text(" - Reload configuration from file", NamedTextColor.WHITE)));
        
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.empty());
    }
    
    private void handleListCommand(com.velocitypowered.api.command.CommandSource source) {
        var allowedUsers = plugin.getAllowedOfflineUsers();
        
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.text("        Offline Whitelist", NamedTextColor.YELLOW, TextDecoration.BOLD));
        source.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.empty());
        
        if (allowedUsers.isEmpty()) {
            source.sendMessage(Component.text("❌ No offline users configured.", NamedTextColor.RED));
            source.sendMessage(Component.text("   Use ", NamedTextColor.GRAY)
                .append(Component.text("/offlinemode add <username>", NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.suggestCommand("/offlinemode add "))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to add a user", NamedTextColor.GRAY))))
                .append(Component.text(" to add users.", NamedTextColor.GRAY)));
        } else {
            source.sendMessage(Component.text("✅ Allowed offline users (" + allowedUsers.size() + "):", NamedTextColor.GREEN, TextDecoration.BOLD));
            source.sendMessage(Component.empty());
            
            int index = 1;
            for (String user : allowedUsers.stream().sorted().collect(Collectors.toList())) {
                source.sendMessage(Component.text(String.format("%2d. ", index), NamedTextColor.GRAY)
                    .append(Component.text("👤 " + user, NamedTextColor.WHITE, TextDecoration.BOLD))
                    .append(Component.text(" [", NamedTextColor.GRAY))
                    .append(Component.text("Remove", NamedTextColor.RED, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.suggestCommand("/offlinemode remove " + user))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to remove " + user, NamedTextColor.RED))))
                    .append(Component.text("]", NamedTextColor.GRAY)));
                index++;
            }
        }
        
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.empty());
    }
    
    private void handleAddCommand(com.velocitypowered.api.command.CommandSource source, String username) {
        String result = plugin.addOfflineUser(username);
        
        switch (result) {
            case "SUCCESS":
                source.sendMessage(Component.empty());
                source.sendMessage(Component.text("✅ SUCCESS", NamedTextColor.GREEN, TextDecoration.BOLD));
                source.sendMessage(Component.text("   User ", NamedTextColor.WHITE)
                    .append(Component.text(username, NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" has been added to the offline whitelist!", NamedTextColor.WHITE)));
                source.sendMessage(Component.text("   They can now join in offline mode.", NamedTextColor.GRAY));
                source.sendMessage(Component.empty());
                break;
                
            case "ALREADY_EXISTS":
                source.sendMessage(Component.empty());
                source.sendMessage(Component.text("⚠️ WARNING", NamedTextColor.YELLOW, TextDecoration.BOLD));
                source.sendMessage(Component.text("   User ", NamedTextColor.WHITE)
                    .append(Component.text(username, NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" is already in the offline whitelist!", NamedTextColor.WHITE)));
                source.sendMessage(Component.empty());
                break;
                
            case "ERROR":
                source.sendMessage(Component.empty());
                source.sendMessage(Component.text("❌ ERROR", NamedTextColor.RED, TextDecoration.BOLD));
                source.sendMessage(Component.text("   Failed to add user to the whitelist.", NamedTextColor.WHITE));
                source.sendMessage(Component.text("   Check the server console for details.", NamedTextColor.GRAY));
                source.sendMessage(Component.empty());
                break;
        }
    }
    
    private void handleRemoveCommand(com.velocitypowered.api.command.CommandSource source, String username) {
        String result = plugin.removeOfflineUser(username);
        
        switch (result) {
            case "SUCCESS":
                source.sendMessage(Component.empty());
                source.sendMessage(Component.text("✅ SUCCESS", NamedTextColor.GREEN, TextDecoration.BOLD));
                source.sendMessage(Component.text("   User ", NamedTextColor.WHITE)
                    .append(Component.text(username, NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" has been removed from the offline whitelist!", NamedTextColor.WHITE)));
                source.sendMessage(Component.text("   They now require online authentication.", NamedTextColor.GRAY));
                source.sendMessage(Component.empty());
                break;
                
            case "NOT_FOUND":
                source.sendMessage(Component.empty());
                source.sendMessage(Component.text("⚠️ WARNING", NamedTextColor.YELLOW, TextDecoration.BOLD));
                source.sendMessage(Component.text("   User ", NamedTextColor.WHITE)
                    .append(Component.text(username, NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" is not in the offline whitelist!", NamedTextColor.WHITE)));
                source.sendMessage(Component.empty());
                break;
                
            case "ERROR":
                source.sendMessage(Component.empty());
                source.sendMessage(Component.text("❌ ERROR", NamedTextColor.RED, TextDecoration.BOLD));
                source.sendMessage(Component.text("   Failed to remove user from the whitelist.", NamedTextColor.WHITE));
                source.sendMessage(Component.text("   Check the server console for details.", NamedTextColor.GRAY));
                source.sendMessage(Component.empty());
                break;
        }
    }
    
    private void handleReloadCommand(com.velocitypowered.api.command.CommandSource source) {
        int oldCount = plugin.getAllowedOfflineUsers().size();
        plugin.reloadConfiguration();
        int newCount = plugin.getAllowedOfflineUsers().size();
        
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("🔄 CONFIGURATION RELOADED", NamedTextColor.GREEN, TextDecoration.BOLD));
        source.sendMessage(Component.text("   Loaded ", NamedTextColor.WHITE)
            .append(Component.text(String.valueOf(newCount), NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text(" offline users", NamedTextColor.WHITE))
            .append(Component.text(" (was " + oldCount + ")", NamedTextColor.GRAY)));
        source.sendMessage(Component.empty());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("limitedofflinemode.admin");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length <= 1) {
            return CompletableFuture.completedFuture(List.of("list", "add", "remove", "reload", "help"));
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "remove":
                case "delete":
                    // Suggest existing offline users for removal
                    return CompletableFuture.completedFuture(new ArrayList<>(plugin.getAllowedOfflineUsers()));
                case "add":
                    // No suggestions for add (user can type any username)
                    return CompletableFuture.completedFuture(List.of());
            }
        }
        
        return CompletableFuture.completedFuture(List.of());
    }
}