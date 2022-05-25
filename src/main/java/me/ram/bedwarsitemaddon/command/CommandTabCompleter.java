package me.ram.bedwarsitemaddon.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> suggest = getSuggest(sender, args);
        String last = args[args.length - 1];
        if (!last.equals("")) {
            List<String> list = new ArrayList<>();
            suggest.forEach(s -> {
                if (s.startsWith(last)) {
                    list.add(s);
                }
            });
            return list;
        }
        return suggest;
    }

    private List<String> getSuggest(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "reload", "upcheck");
        }
        return new ArrayList<>();
    }
}
