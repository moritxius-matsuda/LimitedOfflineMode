# LimitedOfflineMode - Velocity & BungeeCord Plugin

## 📖 Overview

This plugin allows offline accounts to join online-mode proxy servers using specific whitelisted usernames in offline mode. Supports both **Velocity** and **BungeeCord** proxy platforms. This is designed exclusively for testing and administrative purposes, eliminating the need for separate testing accounts.

## 🚀 Intended Use Cases

- **Administrative Testing**: Test server functionality without requiring a separate Minecraft account

- **Plugin Development**: Debug plugins using your main admin account in offline mode

- **Emergency Access**: Maintain access during Mojang session server outages

- **Network Diagnostics**: Troubleshoot network issues with controlled offline access

## ⚠️ Legal & Ethical Disclaimer

This plugin is intended solely for server administrators and developers to perform testing, debugging, and maintenance tasks.
It must not be used to bypass Mojang’s authentication, allow cracked/pirated clients, or enable any form of unauthorized access.

### Allowed Use

- ✅ Internal server testing

- ✅ Plugin and infrastructure development

- ✅ Emergency access during authentication server outages

### Prohibited Use

- ❌ Enabling access for regular or public players

- ❌ Using on public or commercial networks without restrictions

- ❌ Circumventing Mojang/Microsoft authentication for non-administrative purposes

### Responsibility

By using this plugin, you acknowledge and agree that you:

- comply with all Mojang/Microsoft rules and policies,
- use this tool only in controlled administrative environments,
- implement proper security measures (whitelisting, IP restrictions, logging),
- assume full responsibility for any misuse, violations, or resulting consequences.

This plugin is provided **“as is”**, without warranty. The developers are not liable for damages, security issues, or EULA violations resulting from improper use.

## ⚙️ Installation

1. Download the plugin JAR

2. Place in your proxy's `plugins` folder

3. Start the server to generate configuration files

4. Edit `allowed-users.txt` in the plugin's directory

5. Restart the server

## 📝 Configuration

Edit `allowed-users.txt` in your plugin's directory:

```txt
# Use for server testing and development ONLY
ServerAdmin
HeadDeveloper
```

## 🔄 Platform Support

| Feature | Velocity | BungeeCord |
|---------|----------|-----------|
| Offline Mode Whitelist | ✅ Full Support | ✅ Full Support |
| Configuration Reload | ✅ Yes | ✅ Yes (manual) |
| Player UUID Handling | ✅ Offline UUIDs | ✅ Standard |
| Authentication Bypass | ✅ Yes | ✅ Yes |
