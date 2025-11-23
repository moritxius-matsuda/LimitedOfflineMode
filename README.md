# LimitedOfflineMode - Velocity & BungeeCord Plugin

## 📖 Overview

This plugin allows offline accounts to join online-mode proxy servers using specific whitelisted usernames in offline mode. Supports both **Velocity** and **BungeeCord** proxy platforms. This is designed exclusively for testing and administrative purposes, eliminating the need for separate testing accounts.

## 🚀 Intended Use Cases

- **Administrative Testing**: Test server functionality without requiring a separate Minecraft account

- **Plugin Development**: Debug plugins using your main admin account in offline mode

- **Emergency Access**: Maintain access during Mojang session server outages

- **Network Diagnostics**: Troubleshoot network issues with controlled offline access

## ⚠️ IMPORTANT LEGAL AND ETHICAL NOTICE

**THIS PLUGIN IS STRICTLY PROHIBITED FOR USE IN SCENARIOS THAT ENABLE OR ENCOURAGE UNAUTHORIZED ACCESS TO MINECRAFT SERVERS:**

- ❌ Do not use to allow cracked/pirated Minecraft clients

- ❌ Do not enable for regular players or public access

- ❌ Do not use to bypass Mojang's authentication system for non-administrative purposes

**Approved usage is limited to:**

- ✅ Server administrators testing their own infrastructure

- ✅ Plugin developers debugging their creations

- ✅ Trusted staff members performing maintenance tasks

Any other use violates Mojang's EULA and the terms of this software license. Violators risk legal action and account termination by Mojang/Microsoft.

## ⚙️ Installation

### Velocity Installation

1. Download the plugin JAR from the Releases section

2. Place in your Velocity proxy's `plugins` folder

3. Start the server to generate configuration files

4. Edit `allowed-users.txt` in the plugin's directory

5. Restart the server

### BungeeCord Installation

1. Download the plugin JAR from the Releases section

2. Place in your BungeeCord proxy's `plugins` folder

3. Start the server to generate configuration files

4. Edit `allowed-users.txt` in the `plugins/LimitedOfflineMode/` directory

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



## LimitedOfflineMode - Velocity Plugin

**LEGAL AND ETHICAL USAGE NOTICE**

### 1. Strict Usage Policy

This plugin **MUST ONLY** be used by server administrators for:

- Testing server configurations

- Debugging plugins and network issues

- Emergency maintenance during authentication server outages

**ANY OTHER USE IS EXPRESSLY PROHIBITED**

### 2. Anti-Piracy Stance

The developers:

- ❌ DO NOT support software piracy

- ❌ DO NOT condone cracked Minecraft clients

- ❌ DO NOT permit this tool for unauthorized access

This plugin **MUST NOT** be used to enable or encourage:

- Access to paid Minecraft features without authorization

- Circumvention of Mojang's authentication systems

- Use of illegally obtained Minecraft copies

### 3. Security Responsibilities

Administrators using this tool MUST:

- Maintain tight access controls

- Use strong unique passwords for offline accounts

- Regularly audit access logs

- Restrict to essential personnel only

- Implement IP whitelisting where possible

### 4. Mojang EULA Compliance

Users are responsible for ensuring compliance with:

- [Minecraft Commercial Use Guidelines](https://www.minecraft.net/en-us/terms)

- [Mojang Account Terms](https://account.mojang.com/terms)

- [Microsoft Services Agreement](https://www.microsoft.com/servicesagreement)

### 5. License Restrictions

Use of this software constitutes agreement to:

- Use exclusively for authorized administrative testing

- Never enable access for unauthorized players

- Implement all reasonable security measures

- Accept full liability for misuse

**VIOLATORS WILL HAVE THEIR LICENSE TERMINATED AND MAY FACE LEGAL ACTION**

### 6. No Warranty

THIS PLUGIN IS PROVIDED "AS IS" WITHOUT WARRANTY. THE AUTHORS SHALL NOT BE LIABLE FOR:

- Security breaches from policy violations

- EULA violations by users

- Legal consequences of misuse

- Account suspensions by Mojang/Microsoft

**By using this software, you affirm that you are a server administrator using it solely for authorized testing purposes and accept full responsibility for preventing unauthorized access.**

```
