package cz.jeme.programu.doorman;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class Doorman extends JavaPlugin implements Listener {

	private String joinMessage = null;
	private String leaveMessage = null;

	public static final Map<String, String> CORRECT_ARGS = new HashMap<String, String>();

	// Yaml configuration variables
	public static final String CONFIG_FILE_NAME = "config.yml";
	public static final String SECTION_NAME = "Messages";
	public static final String JOIN_MESSAGE_KEY = "Join Message";
	private static final String LEAVE_MESSAGE_KEY = "Leave Message";
	private File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);
	private FileConfiguration configFileYaml = YamlConfiguration.loadConfiguration(configFile);
	private ConfigurationSection section = configFileYaml.getConfigurationSection(SECTION_NAME);

	private static final String PLUGIN_PREFIX = ChatColor.GOLD + "Doorman: " + ChatColor.AQUA;

	{
		CORRECT_ARGS.put("reload", "reload");
		CORRECT_ARGS.put("getJoin", "get-join-message");
		CORRECT_ARGS.put("getLeave", "get-leave-message");
		CORRECT_ARGS.put("setJoin", "set-join-message");
		CORRECT_ARGS.put("setLeave", "set-leave-message");
	}

	@Override
	public void onEnable() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(this, this);
		File dataDir = getDataFolder();

		if (!dataDir.exists()) {
			dataDir.mkdir();
		}
		// Tab completer for command
		getCommand("doorman").setTabCompleter(new PluginTabCompleter());
		prepareConfig();
	}

	@Override
	public void onDisable() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			player = null;
		}
		if (command.getName().equalsIgnoreCase("doorman")) { // When command called
			if (args.length < 1) { // When no arguments, print usage
				sender.sendMessage(PLUGIN_PREFIX + "\n/trme reloadconfig\n/trme setjoinmessage\n/trme setleavemessage");
				return true;
			}
			if (args[0].equals(CORRECT_ARGS.get("reload"))) { // Reload config
				prepareConfig();
				sender.sendMessage(PLUGIN_PREFIX + "Successfully reloaded!");
				return true;

			} else if (args[0].equals(CORRECT_ARGS.get("getJoin"))) { // Get join message
				getJoinMessage(sender, player);
				return true;
			} else if (args[0].equals(CORRECT_ARGS.get("getLeave"))) { // Get leave message
				getLeaveMessage(sender, player);
				return true;
			} else if (args[0].equals(CORRECT_ARGS.get("setJoin"))) { // Set join message
				if (args.length < 2) { // Not enough arguments
					sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + "This action reqiuires more arguments!\n"
							+ PLUGIN_PREFIX + "Usage: /trme " + CORRECT_ARGS.get(3) + " <your join message>");
					return true;
				}
				StringBuffer message = new StringBuffer(10); // StringBuffer for all arguments except the first one
				for (int i = 1; i < args.length; i++) {
					message.append(args[i]);
					if (i != args.length - 1) {
						message.append(" ");
					}
				}
				setJoinMessage(message.toString(), sender);
				return true;
			}
		}
		sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "WHOPS! " + ChatColor.RED
				+ "Unexpected end of process:");
		sender.sendMessage(ChatColor.BLUE + args[0]);
		return true;
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		event.setJoinMessage(renderMessage(event.getPlayer(), joinMessage));
	}

	@EventHandler
	public void onPlayerLeaveEvent(PlayerQuitEvent event) {
		event.setQuitMessage(renderMessage(event.getPlayer(), leaveMessage));
	}

	private void getJoinMessage(CommandSender sender, Player player) {
		sender.sendMessage(ChatColor.AQUA + "Your join message is curently set to:\n");
		if (player == null) {
			sender.sendMessage(renderMessage(player, joinMessage));
		} else {
			TextComponent tc = new TextComponent();
			tc.setText(joinMessage);
			tc.setColor(ChatColor.GREEN);
			tc.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, tc.getText()));
			player.spigot().sendMessage(tc);
			sender.sendMessage("(Click to copy ↑)");
		}
		sender.sendMessage(ChatColor.AQUA + "Which will be displayed as:\n" + ChatColor.WHITE
				+ renderMessage(player, joinMessage));
	}

	private void setJoinMessage(String msg, CommandSender sender) {
		section.set(JOIN_MESSAGE_KEY, msg);
		saveConfigFile();
		prepareConfig();
		sender.sendMessage("Configuration saved!");
	}

	private void getLeaveMessage(CommandSender sender, Player player) {
		sender.sendMessage(ChatColor.AQUA + "Your leave message is curently set to:\n");
		if (player == null) {
			sender.sendMessage(renderMessage(player, leaveMessage));
		} else {
			TextComponent tc = new TextComponent();
			tc.setText(leaveMessage);
			tc.setColor(ChatColor.GREEN);
			tc.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, tc.getText()));
			player.spigot().sendMessage(tc);
			sender.sendMessage("(Click to copy ↑)");
		}
		sender.sendMessage(ChatColor.AQUA + "Which will be displayed as:\n" + ChatColor.WHITE
				+ renderMessage(player, leaveMessage));
	}

	private void prepareConfig() {
		if (!(configFile.exists())) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				serverLog(Level.SEVERE, "Doorman: Cannot create file \"" + CONFIG_FILE_NAME + "\"!" + e);
			}
		}
		if (section == null) {
			section = configFileYaml.createSection(SECTION_NAME);
		}
		Set<String> sectionKeys = section.getKeys(false);
		if (sectionKeys.size() == 0) {
			section.set(JOIN_MESSAGE_KEY, "&6{PLAYERNAME} &ejoined the game");
			section.set(LEAVE_MESSAGE_KEY, "&6{PLAYERNAME} &eleft the game");
		}
		joinMessage = section.getString("Join Message").replace('§', '&');
		leaveMessage = section.getString("Leave Message").replace('§', '&');
		section.set(JOIN_MESSAGE_KEY, joinMessage);
		section.set(LEAVE_MESSAGE_KEY, leaveMessage);

		saveConfigFile();

	}

	private void saveConfigFile() {
		try {
			configFileYaml.save(configFile);
		} catch (IOException e) {
			serverLog(Level.SEVERE, "Doorman: Cannot save file \"" + CONFIG_FILE_NAME + "\"!" + e);
		}
	}

	private String renderMessage(Player player, String msg) {
		String playerName = null;
		String replacedPlayerName = null;
		if (player != null) {
			playerName = player.getName();
			replacedPlayerName = (msg.replaceAll("\\{PLAYERNAME\\}", playerName));
		} else {
			replacedPlayerName = (msg.replaceAll("\\{PLAYERNAME\\}", "Steve"));
		}
		String colorsTranslated = ChatColor.translateAlternateColorCodes('&', replacedPlayerName);
		return colorsTranslated;
	}

	private void serverLog(Level lvl, String msg) {
		Bukkit.getServer().getLogger().log(lvl, msg);
	}
}