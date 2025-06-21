# LimitedOfflineMode - Velocity Plugin

Ein Velocity-Plugin, das es bestimmten Benutzern erlaubt, im Offline-Modus beizutreten, obwohl der Server im Online-Modus läuft. Ideal für Testzwecke.

## Features

- ✅ Erlaubt spezifischen Benutzern den Offline-Beitritt
- ✅ Konfigurierbare Benutzerliste
- ✅ **Schöne interaktive Commands** mit klickbaren Elementen
- ✅ **Add/Remove Commands** für einfache Benutzerverwaltung
- ✅ **Tab-Completion** für alle Commands
- ✅ **Farbige Nachrichten** mit Emojis und Formatierung
- ✅ Hot-Reload der Konfiguration
- ✅ Automatische Speicherung in Konfigurationsdatei
- ✅ Detailliertes Logging

## Installation

1. Kompiliere das Plugin:
   ```bash
   ./gradlew shadowJar
   ```

2. Kopiere die JAR-Datei aus `build/libs/LimitedOfflineMode-1.0.0.jar` in den `plugins` Ordner deines Velocity-Servers.

3. Starte den Server neu.

## Konfiguration

Nach dem ersten Start wird automatisch eine Konfigurationsdatei erstellt:
`plugins/limitedofflinemode/allowed-users.txt`

### Beispiel-Konfiguration:
```
# LimitedOfflineMode Configuration
# Liste der Benutzernamen, die im Offline-Modus beitreten dürfen
# Ein Benutzername pro Zeile
# Zeilen die mit # beginnen sind Kommentare

TestUser1
TestUser2
OfflinePlayer
MeinTestAccount
```

## Commands

Das Plugin bietet eine schöne, interaktive Command-Oberfläche mit klickbaren Elementen und farbigen Nachrichten.

### `/offlinemode` oder `/offlinemode help`
- **Permission:** `limitedofflinemode.admin`
- **Beschreibung:** Zeigt eine schöne Hilfe-Übersicht mit allen verfügbaren Commands

### `/offlinemode list`
- **Permission:** `limitedofflinemode.admin`
- **Beschreibung:** Zeigt alle erlaubten Offline-Benutzer in einer schönen Liste an
- **Features:** 
  - Nummerierte Liste mit Emojis
  - Klickbare "Remove"-Links für jeden Benutzer
  - Zeigt Anzahl der Benutzer an

### `/offlinemode add <username>`
- **Permission:** `limitedofflinemode.admin`
- **Beschreibung:** Fügt einen Benutzer zur Offline-Whitelist hinzu
- **Features:**
  - Automatische Speicherung in die Konfigurationsdatei
  - Schöne Erfolgs-/Fehlermeldungen
  - Prüfung auf bereits existierende Benutzer

### `/offlinemode remove <username>`
- **Permission:** `limitedofflinemode.admin`
- **Beschreibung:** Entfernt einen Benutzer aus der Offline-Whitelist
- **Features:**
  - Tab-Completion mit existierenden Benutzern
  - Automatische Speicherung in die Konfigurationsdatei
  - Schöne Erfolgs-/Fehlermeldungen

### `/offlinemode reload`
- **Permission:** `limitedofflinemode.admin`
- **Beschreibung:** Lädt die Konfiguration neu, ohne Server-Neustart
- **Features:** Zeigt Anzahl der geladenen Benutzer vor und nach dem Reload

## Command-Beispiele

```
/offlinemode help
# Zeigt eine schöne Hilfe-Übersicht mit klickbaren Commands

/offlinemode add TestUser123
# ✅ SUCCESS
#    User TestUser123 has been added to the offline whitelist!
#    They can now join in offline mode.

/offlinemode list
# ═══════════════════════════════════════
#         Offline Whitelist
# ═══════════════════════════════════════
# 
# ✅ Allowed offline users (2):
# 
#  1. 👤 TestUser123 [Remove]
#  2. 👤 OfflinePlayer [Remove]

/offlinemode remove TestUser123
# ✅ SUCCESS
#    User TestUser123 has been removed from the offline whitelist!
#    They now require online authentication.
```

## Permissions

- `limitedofflinemode.admin` - Zugriff auf alle Plugin-Commands

## Wie es funktioniert

1. Das Plugin überwacht alle Login-Versuche über das `PreLoginEvent`
2. Wenn ein Benutzername in der `allowed-users.txt` steht, wird der Login erlaubt
3. Alle anderen Benutzer müssen weiterhin die normale Online-Authentifizierung durchlaufen

## Sicherheitshinweise

⚠️ **WICHTIG:** Dieses Plugin ist nur für Testzwecke gedacht!

- Offline-Benutzer können ihre Identität nicht verifizieren
- Jeder kann sich als ein erlaubter Offline-Benutzer ausgeben
- Verwende dies NIEMALS auf einem Produktionsserver
- Entferne das Plugin nach den Tests

## Entwicklung

### Voraussetzungen
- Java 11 oder höher
- Gradle

### Build
```bash
./gradlew build
```

### Development Build
```bash
./gradlew shadowJar
```

## Troubleshooting

### Plugin lädt nicht
- Überprüfe die Velocity-Version (benötigt 3.2.0+)
- Überprüfe die Java-Version (benötigt 11+)
- Schaue in die Server-Logs für Fehlermeldungen

### Benutzer können nicht beitreten
- Überprüfe die `allowed-users.txt` Datei
- Stelle sicher, dass der Benutzername exakt übereinstimmt (case-insensitive)
- Verwende `/offlinemode list` um die geladenen Benutzer zu überprüfen

### Konfiguration wird nicht geladen
- Verwende `/offlinemode reload` nach Änderungen
- Überprüfe die Dateiberechtigungen
- Schaue in die Server-Logs für Fehlermeldungen

## Lizenz

Dieses Plugin ist für Testzwecke erstellt und steht unter der MIT-Lizenz.