package com.essencewars.debug;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancedDebugLogger {

    private final Plugin plugin;
    private final File debugFile;
    private final boolean consoleOutput;
    private final boolean fileOutput;
    private final int maxLogLines;

    private final Map<String, Long> operationTimings = new ConcurrentHashMap<>();
    private final Map<String, Integer> eventCounts = new ConcurrentHashMap<>();

    private final Set<UUID> debuggedPlayers = new HashSet<>();
    private final Map<UUID, List<String>> playerLogs = new ConcurrentHashMap<>();

    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        CRITICAL
    }

    private LogLevel currentLogLevel = LogLevel.DEBUG;

    public AdvancedDebugLogger(Plugin plugin, boolean consoleOutput, boolean fileOutput) {
        this.plugin = plugin;
        this.consoleOutput = consoleOutput;
        this.fileOutput = fileOutput;
        this.maxLogLines = 10000;

        File debugDir = new File(plugin.getDataFolder(), "debug");
        if (!debugDir.exists()) {
            debugDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        this.debugFile = new File(debugDir, "debug_" + timestamp + ".log");

        log(LogLevel.INFO, "SYSTEM", "Debug Logger initialized at " + timestamp);
        log(LogLevel.INFO, "SYSTEM", "Console output: " + consoleOutput);
        log(LogLevel.INFO, "SYSTEM", "File output: " + fileOutput + " (" + debugFile.getName() + ")");
    }

    public void log(LogLevel level, String category, String message) {
        if (level.ordinal() < currentLogLevel.ordinal()) {
            return;
        }

        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String logLine = String.format("[%s] [%s] [%s] %s", timestamp, level.name(), category, message);

        if (consoleOutput) {
            String coloredLog = colorizeLog(level, logLine);
            Bukkit.getConsoleSender().sendMessage(coloredLog);
        }

        if (fileOutput) {
            writeToFile(logLine);
        }

        eventCounts.merge(category, 1, Integer::sum);
    }

    public void log(LogLevel level, String category, String message, Object... args) {
        log(level, category, String.format(message, args));
    }

    public void logPlayer(Player player, LogLevel level, String message) {
        UUID uuid = player.getUniqueId();
        String playerInfo = String.format("%s [%s]", player.getName(), uuid);

        log(level, "PLAYER", playerInfo + " - " + message);

        if (debuggedPlayers.contains(uuid)) {
            playerLogs.computeIfAbsent(uuid, k -> new ArrayList<>())
                    .add(String.format("[%s] %s", level.name(), message));
        }
    }

    public void enablePlayerDebug(Player player) {
        UUID uuid = player.getUniqueId();
        debuggedPlayers.add(uuid);
        playerLogs.put(uuid, new ArrayList<>());
        log(LogLevel.INFO, "SYSTEM", "Enabled debug tracking for " + player.getName());
    }

    public void disablePlayerDebug(Player player) {
        UUID uuid = player.getUniqueId();
        debuggedPlayers.remove(uuid);
        log(LogLevel.INFO, "SYSTEM", "Disabled debug tracking for " + player.getName());
    }

    public List<String> getPlayerLogs(Player player) {
        return playerLogs.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public void logEvent(String eventName, Player player, Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("EVENT: %s | Player: %s", eventName, player.getName()));

        if (data != null && !data.isEmpty()) {
            sb.append(" | Data: {");
            data.forEach((key, value) -> sb.append(String.format("%s=%s, ", key, value)));
            sb.setLength(sb.length() - 2);
            sb.append("}");
        }

        log(LogLevel.DEBUG, "EVENT", sb.toString());
        logPlayer(player, LogLevel.DEBUG, "Triggered event: " + eventName);
    }

    public void logPacket(String packetType, Player player, String action, Map<String, Object> packetData) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PACKET: %s | Player: %s | Action: %s", packetType, player.getName(), action));

        if (packetData != null && !packetData.isEmpty()) {
            sb.append(" | Data: ");
            packetData.forEach((key, value) -> sb.append(String.format("%s=%s ", key, value)));
        }

        log(LogLevel.TRACE, "PACKET", sb.toString());
    }

    public void logAbility(String abilityName, Player player, String status, String reason) {
        String message = String.format("Ability: %s | Status: %s | Reason: %s", abilityName, status, reason);
        log(LogLevel.DEBUG, "ABILITY", player.getName() + " - " + message);
        logPlayer(player, LogLevel.DEBUG, message);
    }

    public void logAbilityStart(String abilityName, Player player) {
        logAbility(abilityName, player, "STARTED", "User triggered");
        startTiming(abilityName + "_" + player.getUniqueId());
    }

    public void logAbilityEnd(String abilityName, Player player, boolean success) {
        long duration = stopTiming(abilityName + "_" + player.getUniqueId());
        String status = success ? "SUCCESS" : "FAILED";
        logAbility(abilityName, player, status, "Duration: " + duration + "ms");
    }

    public void logCooldown(String abilityName, Player player, long remainingMs, String action) {
        String message = String.format("Cooldown: %s | Remaining: %dms | Action: %s", abilityName, remainingMs, action);
        log(LogLevel.DEBUG, "COOLDOWN", player.getName() + " - " + message);
    }

    public void logStateChange(String component, String oldState, String newState, String reason) {
        String message = String.format("State Change: %s | %s -> %s | Reason: %s", component, oldState, newState, reason);
        log(LogLevel.INFO, "STATE", message);
    }

    public void logPlayerState(Player player, Map<String, Object> state) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Player State: %s | ", player.getName()));
        state.forEach((key, value) -> sb.append(String.format("%s=%s, ", key, value)));
        log(LogLevel.TRACE, "STATE", sb.toString());
    }

    public void startTiming(String operationName) {
        operationTimings.put(operationName, System.nanoTime());
    }

    public long stopTiming(String operationName) {
        Long startTime = operationTimings.remove(operationName);
        if (startTime == null) {
            log(LogLevel.WARN, "PERFORMANCE", "No start time found for: " + operationName);
            return -1;
        }
        long duration = (System.nanoTime() - startTime) / 1_000_000L;
        log(LogLevel.TRACE, "PERFORMANCE", operationName + " took " + duration + "ms");
        return duration;
    }

    public void logPerformanceWarning(String operation, long durationMs, long thresholdMs) {
        if (durationMs > thresholdMs) {
            log(LogLevel.WARN, "PERFORMANCE",
                    String.format("SLOW OPERATION: %s took %dms (threshold: %dms)", operation, durationMs, thresholdMs));
        }
    }

    public void logError(String component, String message, Throwable throwable) {
        log(LogLevel.ERROR, component, message);
        log(LogLevel.ERROR, component, "Exception: " + throwable.getClass().getName());
        log(LogLevel.ERROR, component, "Message: " + throwable.getMessage());
        for (StackTraceElement element : throwable.getStackTrace()) {
            log(LogLevel.ERROR, component, "  at " + element.toString());
        }
        if (throwable.getCause() != null) {
            log(LogLevel.ERROR, component, "Caused by: " + throwable.getCause().getMessage());
        }
    }

    public void logConfigLoad(Map<String, Object> configValues) {
        log(LogLevel.INFO, "CONFIG", "Configuration loaded:");
        configValues.forEach((key, value) -> log(LogLevel.INFO, "CONFIG", "  " + key + " = " + value));
    }

    public void logConfigChange(String key, Object oldValue, Object newValue, String changedBy) {
        log(LogLevel.INFO, "CONFIG",
                String.format("Config changed: %s | %s -> %s | Changed by: %s", key, oldValue, newValue, changedBy));
    }

    public void logStatistics() {
        log(LogLevel.INFO, "STATS", "=== Event Statistics ===");
        eventCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> log(LogLevel.INFO, "STATS", entry.getKey() + ": " + entry.getValue() + " times"));
    }

    private String colorizeLog(LogLevel level, String message) {
        String colorCode;
        switch (level) {
            case TRACE:
                colorCode = "§8";
                break;
            case DEBUG:
                colorCode = "§7";
                break;
            case INFO:
                colorCode = "§f";
                break;
            case WARN:
                colorCode = "§e";
                break;
            case ERROR:
                colorCode = "§c";
                break;
            case CRITICAL:
                colorCode = "§4§l";
                break;
            default:
                colorCode = "§f";
        }
        return colorCode + message;
    }

    private void writeToFile(String message) {
        try (FileWriter fw = new FileWriter(debugFile, true); PrintWriter pw = new PrintWriter(fw)) {
            pw.println(message);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to write to debug log: " + e.getMessage());
        }
    }

    public void setLogLevel(LogLevel level) {
        this.currentLogLevel = level;
        log(LogLevel.INFO, "SYSTEM", "Log level changed to: " + level.name());
    }

    public void clearFile() {
        try {
            new FileWriter(debugFile, false).close();
            log(LogLevel.INFO, "SYSTEM", "Debug file cleared");
        } catch (IOException e) {
            log(LogLevel.ERROR, "SYSTEM", "Failed to clear debug file: " + e.getMessage());
        }
    }

    public File getDebugFile() {
        return debugFile;
    }
}
