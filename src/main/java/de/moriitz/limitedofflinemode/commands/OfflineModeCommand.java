package de.moriitz.limitedofflinemode.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.moriitz.limitedofflinemode.LimitedOfflineModePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            source.sendMessage(Component.text("Usage: /offlinemode <list|reload>", NamedTextColor.RED));
            return;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
                var allowedUsers = plugin.getAllowedOfflineUsers();
                if (allowedUsers.isEmpty()) {
                    source.sendMessage(Component.text("No offline users configured.", NamedTextColor.YELLOW));
                } else {
                    source.sendMessage(Component.text("Allowed offline users:", NamedTextColor.GREEN));
                    for (String user : allowedUsers) {
                        source.sendMessage(Component.text("- " + user, NamedTextColor.GRAY));
                    }
                }
                break;
                
            case "reload":
                plugin.reloadConfiguration();
                source.sendMessage(Component.text("Configuration reloaded!", NamedTextColor.GREEN));
                break;
                
            default:
                source.sendMessage(Component.text("Unknown subcommand. Use: list, reload", NamedTextColor.RED));
                break;
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("limitedofflinemode.admin");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length <= 1) {
            return CompletableFuture.completedFuture(List.of("list", "reload"));
        }
        
        return CompletableFuture.completedFuture(List.of());
    }
}