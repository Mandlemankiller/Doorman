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

		boolean setJoin = args[0].equals(Doorman.CORRECT_ARGS.get("setJoin"));
		boolean setLeave = args[0].equals(Doorman.CORRECT_ARGS.get("setLeave"));
		boolean playernameAlreadyUsed = Arrays.stream(args).anyMatch("{PLAYERNAME}"::equals);
		boolean lastArgEmpty = args[args.length - 1].equals("");
		boolean playernameStartsWithLastArg = Doorman.PLAYERNAME_VAR.toLowerCase()
				.startsWith(args[args.length - 1].toLowerCase());

		if ((setJoin || setLeave) && !playernameAlreadyUsed && (lastArgEmpty || playernameStartsWithLastArg)) {
			setList.add(Doorman.PLAYERNAME_VAR);
		}

		return setList;
	}

}
