package me.ram.bedwarsitemaddon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.bstats.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.ram.bedwarsitemaddon.command.CommandTabCompleter;
import me.ram.bedwarsitemaddon.command.Commands;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.config.LocaleConfig;
import me.ram.bedwarsitemaddon.items.BridgeEgg;
import me.ram.bedwarsitemaddon.items.CompactTower;
import me.ram.bedwarsitemaddon.items.EnderPearlChair;
import me.ram.bedwarsitemaddon.items.ExplosionProof;
import me.ram.bedwarsitemaddon.items.FireBall;
import me.ram.bedwarsitemaddon.items.LightTNT;
import me.ram.bedwarsitemaddon.items.MagicMilk;
import me.ram.bedwarsitemaddon.items.Parachute;
import me.ram.bedwarsitemaddon.items.TNTLaunch;
import me.ram.bedwarsitemaddon.items.TeamIronGolem;
import me.ram.bedwarsitemaddon.items.TeamSilverFish;
import me.ram.bedwarsitemaddon.items.Trampoline;
import me.ram.bedwarsitemaddon.items.WalkPlatform;
import me.ram.bedwarsitemaddon.listener.EventListener;
import me.ram.bedwarsitemaddon.manage.NoFallManage;
import me.ram.bedwarsitemaddon.network.UpdateCheck;

/**
 * @author Ram
 * @version 1.7.0
 */
public class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private NoFallManage noFallManage;
    @Getter
    private LocaleConfig localeConfig;

    public static String getVersion() {
        return "1.7.0";
    }

    @Override
    public FileConfiguration getConfig() {
        FileConfiguration config = Config.getConfig();
        return config == null ? super.getConfig() : config;
    }

    public void onEnable() {
        if (!getDescription().getName().equals("BedwarsItemAddon") || !getDescription().getVersion().equals(getVersion()) || !getDescription().getAuthors().contains("Ram")) {
            try {
                new Exception("Please don't edit plugin.yml!").printStackTrace();
            } catch (Exception ignored) {
            }
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        noFallManage = new NoFallManage();
        localeConfig = new LocaleConfig();
        getLocaleConfig().loadLocaleConfig();
        Bukkit.getConsoleSender().sendMessage("§f========================================");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage("            §bBedwarsItemAddon");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage(" §f" + getLocaleConfig().getLanguage("version") + ": §a" + getVersion());
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage(" §f" + getLocaleConfig().getLanguage("author") + ": §aRam");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage("§f========================================");
        Config.loadConfig();
        Bukkit.getPluginCommand("bedwarsitemaddon").setExecutor(new Commands());
        Bukkit.getPluginCommand("bedwarsitemaddon").setTabCompleter(new CommandTabCompleter());
        if (Bukkit.getPluginManager().getPlugin("BedwarsRel") != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getPluginManager().isPluginEnabled("BedwarsRel")) {
                        cancel();
                        registerEvents();
                    }
                }
            }.runTaskTimer(this, 0L, 0L);
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
        }
        try {
            new Metrics(this).addCustomChart(new Metrics.SimplePie("language", () -> localeConfig.getPluginLocale().getName()));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onLoad() {
        try {
            // 允许飞行，防止使用道具时踢出服务器
            Path path = Paths.get(getDataFolder().getParentFile().getAbsolutePath()).getParent().resolve("server.properties");
            boolean reboot = false;
            List<String> lines = Files.readAllLines(path);
            if (lines.contains("allow-flight=false")) {
                lines.remove("allow-flight=false");
                lines.add("allow-flight=true");
                reboot = true;
            }
            Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
            if (reboot) {
                Bukkit.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getPluginManager().registerEvents(new UpdateCheck(), this);
        Bukkit.getPluginManager().registerEvents(new FireBall(), this);
        Bukkit.getPluginManager().registerEvents(new LightTNT(), this);
        Bukkit.getPluginManager().registerEvents(new BridgeEgg(), this);
        Bukkit.getPluginManager().registerEvents(new Parachute(), this);
        Bukkit.getPluginManager().registerEvents(new TNTLaunch(), this);
        Bukkit.getPluginManager().registerEvents(new MagicMilk(), this);
        Bukkit.getPluginManager().registerEvents(new Trampoline(), this);
        Bukkit.getPluginManager().registerEvents(new CompactTower(), this);
        Bukkit.getPluginManager().registerEvents(new WalkPlatform(), this);
        Bukkit.getPluginManager().registerEvents(new TeamIronGolem(), this);
        Bukkit.getPluginManager().registerEvents(new TeamSilverFish(), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionProof(), this);
        Bukkit.getPluginManager().registerEvents(new EnderPearlChair(), this);
    }
}
