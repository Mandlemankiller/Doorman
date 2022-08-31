package cz.jeme.programu.doorman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class PluginTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			return new ArrayList<String>(Doorman.CORRECT_ARGS.values());
		}
		List<String> setList = new ArrayList<String>();
		if (((args[0].equals(Doorman.CORRECT_ARGS.get("setJoin")))
				|| (args[0].equals(Doorman.CORRECT_ARGS.get("setLeave"))))
				&& !Arrays.stream(args).anyMatch("{PLAYERNAME}"::equals)) {
			setList.add("{PLAYERNAME}");
		}
		return setList;
	}

}
