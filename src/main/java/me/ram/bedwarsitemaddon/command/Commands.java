package me.ram.bedwarsitemaddon.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.network.UpdateCheck;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("bedwarsitemaddon")) {
            if (args.length == 0) {
                sender.sendMessage("§f===========================================================");
                sender.sendMessage("");
                sender.sendMessage("§b                     BedwarsItemAddon");
                sender.sendMessage("");
                sender.sendMessage("§f  " + Main.getInstance().getLocaleConfig().getLanguage("version") + ": §a" + Main.getVersion());
                sender.sendMessage("");
                sender.sendMessage("§f  " + Main.getInstance().getLocaleConfig().getLanguage("author") + ": §aRam");
                sender.sendMessage("");
                sender.sendMessage("§f===========================================================");
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§f=====================================================");
                sender.sendMessage("");
                sender.sendMessage("§b§l BedwarsItemAddon §fv" + Main.getVersion() + "  §7by Ram");
                sender.sendMessage("");
                Config.getLanguageList("commands.help").forEach(sender::sendMessage);
                sender.sendMessage("");
                sender.sendMessage("§f=====================================================");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("bedwarsitemaddon.reload")) {
                    sender.sendMessage(Config.getLanguage("commands.message.prefix") + Config.getLanguage("commands.message.no_permission"));
                    return true;
                }
                Config.loadConfig();
                sender.sendMessage(Config.getLanguage("commands.message.prefix") + Config.getLanguage("commands.message.reloaded"));
                return true;
            }
            if (args[0].equalsIgnoreCase("upcheck")) {
                if (!sender.hasPermission("bedwarsitemaddon.updatecheck")) {
                    sender.sendMessage(Config.getLanguage("commands.message.prefix") + Config.getLanguage("commands.message.no_permission"));
                    return true;
                }
                UpdateCheck.upCheck(sender);
                return true;
            }
        }
        return false;
    }
}
