package me.ram.bedwarsitemaddon.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.utils.ColorUtil;

public class Config {

    private static FileConfiguration file_config;
    private static FileConfiguration language_config;
    public static boolean update_check_enabled;
    public static boolean update_check_report;
    public static String message_cooling;
    public static boolean items_fireball_enabled;
    public static boolean items_fireball_ejection_enabled;
    public static boolean items_fireball_ejection_no_fall;
    public static boolean items_tnt_enabled;
    public static boolean items_tnt_ejection_enabled;
    public static boolean items_tnt_ejection_no_fall;
    public static boolean items_parachute_enabled;
    public static boolean items_trampoline_enabled;
    public static boolean items_bridge_egg_enabled;
    public static boolean items_ender_pearl_chair_enabled;
    public static boolean items_team_iron_golem_enabled;
    public static boolean items_team_silver_fish_enabled;
    public static boolean items_explosion_proof_enabled;
    public static boolean items_walk_platform_enabled;
    public static boolean items_tnt_launch_enabled;
    public static boolean items_tnt_launch_ejection_enabled;
    public static boolean items_tnt_launch_ejection_no_fall;
    public static boolean items_magic_milk_enabled;
    public static boolean items_compact_tower_enabled;
    public static int items_walk_platform_break_time;
    public static int items_fireball_damage;
    public static int items_tnt_damage;
    public static int items_tnt_launch_damage;
    public static int items_tnt_fuse_ticks;
    public static int items_tnt_launch_fuse_ticks;
    public static int items_magic_milk_duration;
    public static double items_fireball_cooldown;
    public static double items_tnt_cooldown;
    public static double items_parachute_cooldown;
    public static double items_trampoline_cooldown;
    public static double items_bridge_egg_cooldown;
    public static double items_ender_pearl_chair_cooldown;
    public static double items_team_iron_golem_cooldown;
    public static double items_walk_platform_cooldown;
    public static double items_tnt_launch_cooldown;
    public static double items_magic_milk_cooldown;
    public static double items_compact_tower_cooldown;
    public static double items_fireball_ejection_velocity;
    public static double items_tnt_ejection_velocity;
    public static double items_parachute_velocity;
    public static double items_parachute_landing_velocity;
    public static double items_parachute_gliding_velocity;
    public static double items_trampoline_velocity;
    public static double items_tnt_launch_launch_velocity;
    public static double items_tnt_launch_ejection_velocity;
    public static double items_fireball_range;
    public static double items_tnt_range;
    public static double items_tnt_launch_range;
    public static int items_trampoline_size;
    public static int items_trampoline_staytime;
    public static int items_team_iron_golem_staytime;
    public static int items_team_iron_golem_health;
    public static int items_team_iron_golem_damage;
    public static int items_team_silver_fish_staytime;
    public static int items_team_silver_fish_health;
    public static int items_team_silver_fish_damage;
    public static int items_bridge_egg_maxblock;
    public static String items_parachute_item;
    public static String items_team_iron_golem_item;
    public static String items_team_iron_golem_name;
    public static String items_team_silver_fish_name;
    public static String items_trampoline_item;
    public static String items_trampoline_lack_space;
    public static String items_walk_platform_item;
    public static String items_tnt_launch_item;
    public static String items_compact_tower_item;

    public static void loadConfig() {
        File folder = new File(Main.getInstance().getDataFolder(), "/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Main.getInstance().getLocaleConfig().loadLocaleConfig();
        File config_file = new File(folder.getAbsolutePath() + "/config.yml");
        File items_file = new File(folder.getAbsolutePath() + "/items.yml");
        if (!config_file.exists()) {
            Main.getInstance().getLocaleConfig().saveResource("config.yml");
        }
        if (!items_file.exists()) {
            Main.getInstance().getLocaleConfig().saveResource("items.yml");
        }
        file_config = YamlConfiguration.loadConfiguration(config_file);
        language_config = YamlConfiguration.loadConfiguration(getLanguageFile());
        FileConfiguration items_config = YamlConfiguration.loadConfiguration(items_file);
        FileConfiguration config = Main.getInstance().getConfig();
        update_check_enabled = config.getBoolean("update_check.enabled");
        update_check_report = config.getBoolean("update_check.report");
        message_cooling = getLanguage("item.cooling");
        items_fireball_enabled = items_config.getBoolean("fireball.enabled");
        items_fireball_ejection_enabled = items_config.getBoolean("fireball.ejection.enabled");
        items_fireball_ejection_no_fall = items_config.getBoolean("fireball.ejection.no_fall");
        items_tnt_enabled = items_config.getBoolean("tnt.enabled");
        items_tnt_ejection_enabled = items_config.getBoolean("tnt.ejection.enabled");
        items_tnt_ejection_no_fall = items_config.getBoolean("tnt.ejection.no_fall");
        items_fireball_ejection_velocity = items_config.getDouble("fireball.ejection.velocity");
        items_tnt_ejection_velocity = items_config.getDouble("tnt.ejection.velocity");
        items_tnt_launch_ejection_velocity = items_config.getDouble("tnt_launch.ejection.velocity");
        items_fireball_range = items_config.getDouble("fireball.range");
        items_tnt_range = items_config.getDouble("tnt.range");
        items_tnt_launch_range = items_config.getDouble("tnt_launch.range");
        items_tnt_launch_launch_velocity = items_config.getDouble("tnt_launch.launch_velocity");
        items_parachute_enabled = items_config.getBoolean("parachute.enabled");
        items_trampoline_enabled = items_config.getBoolean("trampoline.enabled");
        items_bridge_egg_enabled = items_config.getBoolean("bridge_egg.enabled");
        items_ender_pearl_chair_enabled = items_config.getBoolean("ender_pearl_chair.enabled");
        items_team_iron_golem_enabled = items_config.getBoolean("team_iron_golem.enabled");
        items_team_silver_fish_enabled = items_config.getBoolean("team_silver_fish.enabled");
        items_explosion_proof_enabled = items_config.getBoolean("explosion_proof.enabled");
        items_walk_platform_enabled = items_config.getBoolean("walk_platform.enabled");
        items_tnt_launch_enabled = items_config.getBoolean("tnt_launch.enabled");
        items_tnt_launch_ejection_enabled = items_config.getBoolean("tnt_launch.ejection.enabled");
        items_tnt_launch_ejection_no_fall = items_config.getBoolean("tnt_launch.ejection.no_fall");
        items_magic_milk_enabled = items_config.getBoolean("magic_milk.enabled");
        items_compact_tower_enabled = items_config.getBoolean("compact_tower.enabled");
        items_walk_platform_break_time = items_config.getInt("walk_platform.break_time");
        items_fireball_damage = items_config.getInt("fireball.damage");
        items_tnt_damage = items_config.getInt("tnt.damage");
        items_tnt_launch_damage = items_config.getInt("tnt_launch.damage");
        items_tnt_fuse_ticks = items_config.getInt("tnt.fuse_ticks");
        items_tnt_launch_fuse_ticks = items_config.getInt("tnt_launch.fuse_ticks");
        items_magic_milk_duration = items_config.getInt("magic_milk.duration");
        items_fireball_cooldown = items_config.getDouble("fireball.cooldown");
        items_tnt_cooldown = items_config.getDouble("tnt.cooldown");
        items_parachute_cooldown = items_config.getDouble("parachute.cooldown");
        items_trampoline_cooldown = items_config.getDouble("trampoline.cooldown");
        items_bridge_egg_cooldown = items_config.getDouble("bridge_egg.cooldown");
        items_ender_pearl_chair_cooldown = items_config.getDouble("ender_pearl_chair.cooldown");
        items_team_iron_golem_cooldown = items_config.getDouble("team_iron_golem.cooldown");
        items_walk_platform_cooldown = items_config.getDouble("walk_platform.cooldown");
        items_magic_milk_cooldown = items_config.getDouble("magic_milk.cooldown");
        items_compact_tower_cooldown = items_config.getDouble("compact_tower.cooldown");
        items_tnt_launch_cooldown = items_config.getDouble("tnt_launch.cooldown");
        items_parachute_velocity = items_config.getDouble("parachute.velocity");
        items_parachute_landing_velocity = items_config.getDouble("parachute.landing_velocity");
        items_parachute_gliding_velocity = items_config.getDouble("parachute.gliding_velocity");
        items_trampoline_velocity = items_config.getDouble("trampoline.velocity");
        items_trampoline_size = items_config.getInt("trampoline.size");
        items_trampoline_staytime = items_config.getInt("trampoline.staytime");
        items_team_iron_golem_staytime = items_config.getInt("team_iron_golem.staytime");
        items_team_iron_golem_health = items_config.getInt("team_iron_golem.health");
        items_team_iron_golem_damage = items_config.getInt("team_iron_golem.damage");
        items_team_silver_fish_staytime = items_config.getInt("team_silver_fish.staytime");
        items_team_silver_fish_health = items_config.getInt("team_silver_fish.health");
        items_team_silver_fish_damage = items_config.getInt("team_silver_fish.damage");
        items_bridge_egg_maxblock = items_config.getInt("bridge_egg.maxblock");
        items_parachute_item = items_config.getString("parachute.item");
        items_team_iron_golem_item = items_config.getString("team_iron_golem.item");
        items_team_iron_golem_name = ColorUtil.color(items_config.getString("team_iron_golem.name"));
        items_team_silver_fish_name = ColorUtil.color(items_config.getString("team_silver_fish.name"));
        items_trampoline_item = items_config.getString("trampoline.item");
        items_trampoline_lack_space = ColorUtil.color(items_config.getString("trampoline.lack_space"));
        items_walk_platform_item = items_config.getString("walk_platform.item");
        items_tnt_launch_item = items_config.getString("tnt_launch.item");
        items_compact_tower_item = items_config.getString("compact_tower.item");
    }

    public static FileConfiguration getConfig() {
        return file_config;
    }

    public static String getLanguage(String path) {
        return ColorUtil.color(language_config.getString(path, "null"));
    }

    public static List<String> getLanguageList(String path) {
        if (language_config.contains(path) && language_config.isList(path)) {
            return ColorUtil.colorList(language_config.getStringList(path));
        }
        return Arrays.asList("null");
    }

    private static File getLanguageFile() {
        File folder = new File(Main.getInstance().getDataFolder(), "/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + "/language.yml");
        if (!file.exists()) {
            Main.getInstance().getLocaleConfig().saveResource("language.yml");
        }
        return file;
    }
}
