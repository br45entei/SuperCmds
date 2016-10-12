package com.gmail.br45entei.supercmds;

import com.gmail.br45entei.supercmds.api.Chat;
import com.gmail.br45entei.supercmds.api.Economy_SuperCmds;
import com.gmail.br45entei.supercmds.api.Permissions;
import com.gmail.br45entei.supercmds.api.credits.ItemLibrary;
import com.gmail.br45entei.supercmds.cmds.MainCmdListener;
import com.gmail.br45entei.supercmds.file.Kits;
import com.gmail.br45entei.supercmds.file.PlayerChat;
import com.gmail.br45entei.supercmds.file.PlayerPermissions;
import com.gmail.br45entei.supercmds.file.PlayerPermissions.Group;
import com.gmail.br45entei.supercmds.file.PlayerStatus;
import com.gmail.br45entei.supercmds.file.SavablePlayerData;
import com.gmail.br45entei.supercmds.file.SavablePluginData;
import com.gmail.br45entei.supercmds.file.TicketData;
import com.gmail.br45entei.supercmds.file.Warps;
import com.gmail.br45entei.supercmds.thread.CmdThread;
import com.gmail.br45entei.supercmds.util.CodeUtils;
import com.gmail.br45entei.supercmds.yml.YamlMgmtClass;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.RoundingMode;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class Main extends JavaPlugin implements Listener, PluginInfo {
	
	private static final UUID pluginUUID = UUID.fromString("dcad5c46-4638-4d65-aeac-f52604c15a4e");
	
	public static final UUID getESPUUID() {
		return UUID.fromString("a4c003a4-ebaa-4775-a9d9-ce8dd9f1b15c");
	}
	
	public static final boolean isESPPresent() {
		final UUID espUUID = Main.getESPUUID();
		boolean hasESP = false;
		for(JavaPlugin plugin : Main.pluginsUsingMe) {
			if(plugin instanceof PluginInfo) {
				PluginInfo info = (PluginInfo) plugin;
				if(espUUID.toString().equals(info.getPluginUUID().toString())) {
					hasESP = true;
					break;
				}
			}
		}
		return hasESP;
	}
	
	protected static Main					instance;
	public Main								plugin				= this;
	public static final boolean				forceDebugMsgs		= false;
	
	public static final DecimalFormat		decimal				= new DecimalFormat("#0.00");
	public static final DecimalFormat		decimalRoundUp		= new DecimalFormat("#");
	public static final DecimalFormat		decimalRoundDown	= new DecimalFormat("#");
	public static final DateFormat			hourFormatter		= new SimpleDateFormat("HH:mm:ss");
	
	public static final UUID				consoleUUID			= UUID.fromString("c1d9e7cb-3dd7-4b52-a3c2-239ba94c8b4d");	//This is just a random uuid that I got from https://www.uuidgenerator.net/, don't worry.
	
	public static final HashSet<Material>	transparent			= new HashSet<>();
	
	static {
		transparent.add(Material.AIR);
		Main.decimal.setRoundingMode(RoundingMode.HALF_EVEN);
		Main.decimalRoundUp.setRoundingMode(RoundingMode.UP);
		Main.decimalRoundDown.setRoundingMode(RoundingMode.DOWN);
	}
	
	//public static final int					fNumberOfThreads	= 20;
	//private static final ThreadPoolExecutor	fThreadPool			= ((ThreadPoolExecutor) Executors.newFixedThreadPool(fNumberOfThreads/*, namedThreadFactory*/));
	
	public static final void registerEvents(Listener listener, Plugin plugin) {
		Main.server.getPluginManager().registerEvents(listener, plugin);
	}
	
	public static final void registerEvents() {
		registerEvents(Main.instance, Main.instance);
		registerEvents(MainCmdListener.getInstance(), Main.instance);
		registerEvents(new Economy_SuperCmds(Main.isVaultInstalled() ? Main.getVault() : Main.instance), Main.instance);
		registerEvents(new Chat(), Main.instance);
		registerEvents(new Permissions(), Main.instance);
		registerEvents(Main.uuidMasterList, Main.getInstance());
		Warps.getInstance();//registers events for Warps.java in the SavablePluginData class' constructor.
		Kits.getInstance();//same as warps
		TicketData.getInstance();//same as warps
	}
	
	public static final Main getInstance() {
		return Main.instance;
	}
	
	public static void DEBUG(String str) {
		if(Main.showDebugMsgs) {
			Main.sendConsoleMessage(Main.pluginName + Main.formatColorCodes("&eDebug: " + str));
		}
	}
	
	public static File getPluginFile() {
		return Main.pluginFile;
	}
	
	public static final String getStringFromListClosestTo(String str, String... list) {
		return getStringFromListClosestTo(str, -1, list);
	}
	
	public static final String getStringFromListClosestTo(String str, int likenessLimit, String... list) {
		ArrayList<String> l = new ArrayList<>();
		for(String s : list) {
			if(s != null) {
				l.add(s);
			}
		}
		return getStringFromListClosestTo(l, str, likenessLimit);
	}
	
	public static final String getStringFromListClosestTo(List<String> list, String str) {
		return getStringFromListClosestTo(list, str, -1);
	}
	
	public static final String getStringFromListClosestTo(List<String> list, String str, int likenessLimit) {
		String closestStr = null;
		if(str != null && list != null && !list.isEmpty()) {
			int closestMatch = Integer.MAX_VALUE;
			for(String check : list) {
				int compare = Math.abs(StringUtil.ALPHABETICAL_ORDER.compare(str, check));
				if(compare < closestMatch && (likenessLimit >= 0 ? compare <= likenessLimit : true)) {
					closestStr = check;
				}
			}
		}
		return closestStr;
	}
	
	public static final String		rwhite		= ChatColor.RESET + "" + ChatColor.WHITE;
	public static final ChatColor	aqua		= ChatColor.AQUA;
	public static final ChatColor	black		= ChatColor.BLACK;
	public static final ChatColor	blue		= ChatColor.BLUE;
	public static final ChatColor	bold		= ChatColor.BOLD;
	public static final ChatColor	daqua		= ChatColor.DARK_AQUA;
	public static final ChatColor	dblue		= ChatColor.DARK_BLUE;
	public static final ChatColor	dgray		= ChatColor.DARK_GRAY;
	public static final ChatColor	dgreen		= ChatColor.DARK_GREEN;
	public static final ChatColor	dpurple		= ChatColor.DARK_PURPLE;
	public static final ChatColor	dred		= ChatColor.DARK_RED;
	public static final ChatColor	gold		= ChatColor.GOLD;
	public static final ChatColor	gray		= ChatColor.GRAY;
	public static final ChatColor	green		= ChatColor.GREEN;
	public static final ChatColor	italic		= ChatColor.ITALIC;
	public static final ChatColor	lpurple		= ChatColor.LIGHT_PURPLE;
	public static final ChatColor	magic		= ChatColor.MAGIC;
	public static final ChatColor	red			= ChatColor.RED;
	public static final ChatColor	reset		= ChatColor.RESET;
	public static final ChatColor	striken		= ChatColor.STRIKETHROUGH;
	public static final ChatColor	underline	= ChatColor.UNDERLINE;
	public static final ChatColor	white		= ChatColor.WHITE;
	public static final ChatColor	yellow		= ChatColor.YELLOW;
	public static String			pluginName	= Main.white + "[" + Main.dred + "Super Cmds" + Main.white + "] ";
	
	public static String broadcast(String str) {
		str = Main.formatColorCodes(str);
		Bukkit.getServer().broadcastMessage(str);
		return str;
	}
	
	public static String formatColorCodes(String str, boolean formatMagicCode) {
		return Main.formatColorCodes(formatMagicCode ? str : str.replaceAll("(?i)&k", ""));
	}
	
	/** @param str The string to format
	 * @return The given string with ChatColor. */
	public static String formatColorCodes(String str) {
		return str.replaceAll("(?i)&w", Main.white + "").replaceAll("(?i)&_", Main.rwhite).replaceAll("(?i)&b", Main.aqua + "").replaceAll("(?i)&0", Main.black + "").replaceAll("(?i)&9", Main.blue + "").replaceAll("(?i)&l", Main.bold + "").replaceAll("(?i)&3", Main.daqua + "").replaceAll("(?i)&1", Main.dblue + "").replaceAll("(?i)&8", Main.dgray + "").replaceAll("(?i)&2", Main.dgreen + "").replaceAll("(?i)&5", Main.dpurple + "").replaceAll("(?i)&4", Main.dred + "").replaceAll("(?i)&6", Main.gold + "").replaceAll("(?i)&7", Main.gray + "").replaceAll("(?i)&a", Main.green + "").replaceAll("(?i)&o", Main.italic + "").replaceAll("(?i)&d", Main.lpurple + "").replaceAll("(?i)&k", Main.magic + "").replaceAll("(?i)&c", Main.red + "").replaceAll("(?i)&m", Main.striken + "").replaceAll("(?i)&n", Main.underline + "").replaceAll("(?i)&f", Main.white + "").replaceAll("(?i)&e", Main.yellow + "").replaceAll("(?i)&r", Main.reset + "").replaceAll("(?i)§w", Main.white + "").replaceAll("(?i)§_", Main.rwhite).replaceAll("(?i)§b", Main.aqua + "").replaceAll("(?i)§0", Main.black + "").replaceAll("(?i)§9", Main.blue + "").replaceAll("(?i)§l", Main.bold + "").replaceAll("(?i)§3", Main.daqua + "").replaceAll("(?i)§1", Main.dblue + "").replaceAll("(?i)§8", Main.dgray + "").replaceAll("(?i)§2", Main.dgreen + "").replaceAll("(?i)§5", Main.dpurple + "").replaceAll("(?i)§4", Main.dred + "").replaceAll("(?i)§6", Main.gold + "").replaceAll("(?i)§7", Main.gray + "").replaceAll("(?i)§a", Main.green + "").replaceAll("(?i)§o", Main.italic + "").replaceAll("(?i)§d", Main.lpurple + "").replaceAll("(?i)§k", Main.magic + "").replaceAll("(?i)§c", Main.red + "").replaceAll("(?i)§m", Main.striken + "").replaceAll("(?i)§n", Main.underline + "").replaceAll("(?i)§f", Main.white + "").replaceAll("(?i)§e", Main.yellow + "").replaceAll("(?i)§r", Main.reset + "");
	}
	
	/** @param str String
	 * @return The given string without ChatColor. Does not remove the ChatColor
	 *         codes(i.e. "&f" or "§4"). */
	public static String escapeColorCodes(String str) {
		return str.replaceAll("(?i)&w", "&&rw").replaceAll("(?i)&b", "&&rb").replaceAll("&0", "&&r0").replaceAll("&9", "&&r9").replaceAll("(?i)&l", "&&rl").replaceAll("&3", "&&r3").replaceAll("&1", "&&r1").replaceAll("&8", "&&r8").replaceAll("&2", "&&r2").replaceAll("&5", "&&r5").replaceAll("&4", "&&r4").replaceAll("&6", "&&r6").replaceAll("&7", "&&r7").replaceAll("(?i)&a", "&&ra").replaceAll("(?i)&o", "&&ro").replaceAll("(?i)&d", "&&rd").replaceAll("(?i)&k", "&&rk").replaceAll("(?i)&c", "&&rc").replaceAll("(?i)&m", "&&rm").replaceAll("(?i)&n", "&&rn").replaceAll("(?i)&f", "&&rf").replaceAll("(?i)&e", "&&re").replaceAll("(?i)&r", "&&rr").//
				replaceAll("(?i)\u00A7w", "&&rw").replaceAll("(?i)\u00A7b", "&&rb").replaceAll("\u00A70", "&&r0").replaceAll("\u00A79", "&&r9").replaceAll("(?i)\u00A7l", "&&rl").replaceAll("\u00A73", "&&r3").replaceAll("\u00A71", "&&r1").replaceAll("\u00A78", "&&r8").replaceAll("\u00A72", "&&r2").replaceAll("\u00A75", "&&r5").replaceAll("\u00A74", "&&r4").replaceAll("\u00A76", "&&r6").replaceAll("\u00A77", "&&r7").replaceAll("(?i)\u00A7a", "&&ra").replaceAll("(?i)\u00A7o", "&&ro").replaceAll("(?i)\u00A7d", "&&rd").replaceAll("(?i)\u00A7k", "&&rk").replaceAll("(?i)\u00A7c", "&&rc").replaceAll("(?i)\u00A7m", "&&rm").replaceAll("(?i)\u00A7n", "&&rn").replaceAll("(?i)\u00A7f", "&&rf").replaceAll("(?i)\u00A7e", "&&re").replaceAll("(?i)\u00A7r", "&&rr");
	}
	
	/** @param str String
	 * @return The given string without ChatColor. Does not remove the ChatColor
	 *         codes(i.e. "&f" or "§4"). */
	public static String stripColorCodes(String str) {
		return str.replaceAll("(?i)\u00A7w", "").replaceAll("(?i)\u00A7b", "").replaceAll("\u00A70", "").replaceAll("\u00A79", "").replaceAll("(?i)\u00A7l", "").replaceAll("\u00A73", "").replaceAll("\u00A71", "").replaceAll("\u00A78", "").replaceAll("\u00A72", "").replaceAll("\u00A75", "").replaceAll("\u00A74", "").replaceAll("\u00A76", "").replaceAll("\u00A77", "").replaceAll("(?i)\u00A7a", "").replaceAll("(?i)\u00A7o", "").replaceAll("(?i)\u00A7d", "").replaceAll("(?i)\u00A7k", "").replaceAll("(?i)\u00A7c", "").replaceAll("(?i)\u00A7m", "").replaceAll("(?i)\u00A7n", "").replaceAll("(?i)\u00A7f", "").replaceAll("(?i)\u00A7e", "").replaceAll("(?i)\u00A7r", "");/*return Pattern.compile("(?i)"+String.valueOf("\u00A7")+"[0-9A-FK-OR]+").matcher(str).replaceAll("");*/
	}
	
	public static String getFirstLetterOfGameMode(GameMode gm) {
		if(gm == null) {
			return "";
		}
		return gm.name().substring(0, 1).toUpperCase();
	}
	
	public static String limitStringToNumOfChars(String str, int limit) {
		return(str != null ? (str.length() >= 1 ? (str.substring(0, (str.length() >= limit ? limit : str.length()))) : "") : "");
	}
	
	/** @param stackTraceElements The elements to convert
	 * @return The resulting string */
	public static final String stackTraceElementsToStr(StackTraceElement[] stackTraceElements) {
		String str = "";
		for(StackTraceElement stackTrace : stackTraceElements) {
			str += (!stackTrace.toString().startsWith("Caused By") ? "     at " : "") + stackTrace.toString() + "\r\n";
		}
		return str;
	}
	
	/** @param t The Throwable to convert
	 * @return The resulting string */
	public static final String throwableToStr(Throwable t) {
		String str = t.getClass().getName() + ": ";
		if((t.getMessage() != null) && !t.getMessage().isEmpty()) {
			str += t.getMessage() + "\r\n";
		} else {
			str += "\r\n";
		}
		str += Main.stackTraceElementsToStr(t.getStackTrace());
		if(t.getCause() != null) {
			str += "Caused by:\r\n" + Main.throwableToStr(t.getCause());
		}
		return str;
	}
	
	public static File					pluginFile		= null;
	public static PluginDescriptionFile	pdffile;
	public static ConsoleCommandSender	console;
	public static Server				server			= null;
	public static BukkitScheduler		scheduler		= null;
	public static File					dataFolder		= null;
	public static String				dataFolderName	= "";
	public static boolean				YamlsAreLoaded	= false;
	/** Yes, I set a FileConfiguration object as a static member. Problem?
	 * 
	 * @see <a
	 *      href=
	 *      "http://wiki.bukkit.org/Configuration_API_Reference#The_Configuration_Object">http://wiki.bukkit.org/Configuration_API_Reference#The_Configuration_Object</a> */
	public static FileConfiguration		config;
	public static File					configFile		= null;
	public static String				configFileName	= "config.yml";
	public static String				consoleSayFormat;
	public static CommandSender			rcon;
	
	public static final UUIDMasterList	uuidMasterList	= new UUIDMasterList();
	
	public static boolean isStringUUID(String str) {
		try {
			return UUID.fromString(str) != null;
		} catch(Exception e) {
			return false;
		}
	}
	
	public static final World getWorld(String str) {
		if(str != null) {
			for(World world : Main.server.getWorlds()) {
				if(world.getName().equalsIgnoreCase(str)) {
					return world;
				}
			}
		}
		return null;
	}
	
	public static final World getWorld(UUID uuid) {
		if(uuid != null) {
			for(World world : Main.server.getWorlds()) {
				if(world.getUID().toString().equals(uuid.toString())) {
					return world;
				}
			}
		}
		return null;
	}
	
	public static final Player getPlayer(UUID uuid) {
		if(uuid != null) {
			for(Player player : new ArrayList<>(Main.server.getOnlinePlayers())) {
				if(player.getUniqueId().toString().equals(uuid.toString())) {
					return player;
				}
			}
		}
		return null;
	}
	
	public static Player getPlayer(String name) {
		return Main.getPlayer(name, false);
	}
	
	public static Player getPlayer(String name, boolean partialNameSearch) {
		Player rtrn = null;
		for(Player curPlayer : Main.server.getOnlinePlayers()) {
			if(curPlayer.getName().equals(name)) {
				return curPlayer;
			}
		}
		if(partialNameSearch) {
			for(Player curPlayer : Main.server.getOnlinePlayers()) {
				if(curPlayer.getName().startsWith(name)) {
					return curPlayer;
				}
			}
		}
		return rtrn;
	}
	
	public static final ItemStack getSkullFor(Player player) {
		SkullMeta skullMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);// island home
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		skullMeta.setOwner(player.getName());
		item.setItemMeta(skullMeta);
		return item;
	}
	
	public static boolean				enabled							= true;
	public static boolean				isLoaded						= false;
	public static final String			getPunctuationChars				= "\\p{Punct}+";
	public static final String			getWhiteSpaceChars				= "\\s+"/*"\\p{Space}+"*/;
	public static final String			getAlphaNumericChars			= "\\p{Alnum}+";
	public static final String			getAlphabetChars				= "\\p{Alpha}+";
	public static final String			getNumberChars					= "\\p{Digit}+";
	public static final String			getUpperCaseChars				= "\\p{Lower}+";
	public static final String			getLowerCaseChars				= "\\p{Upper}+";
	
	public static final String			vaultFileName					= "Vault-1.5.6_MC_1.8.1.jar";														//"Vault-1.5.3.jar";//"Vault-1.5.2.jar";
	
	/** The variable used to store messages that are meant to be displayed only
	 * once.
	 * 
	 * @see <a
	 *      href=
	 *      "http://enteisislandsurvival.no-ip.org/javadoc/index.html">Main.sendOneTimeMessage()</a>
	 * @see <a
	 *      href="http://enteisislandsurvival.no-ip.org/javadoc/index.html">Java
	 *      Documentation for EnteisCommands</a> */
	private static ArrayList<String>	oneTimeMessageList				= new ArrayList<>();
	
	// TODO To be loaded from config.yml
	public static boolean				backupOnStartup					= false;
	
	public static boolean				enableDownloadUpdates			= false;
	
	public static boolean				showDebugMsgs					= false;
	public static String				noPerm							= "";
	
	public static boolean				handleEconomy					= false;
	public static String				moneyTerm						= "money";
	public static String				creditTerm						= "credits";
	public static String				broadcastPrefix					= "&f[&4Broadcast&f]";
	public static boolean				handleChat						= false;
	public static boolean				handlePermissions				= false;
	
	public static String				playerPromotedMessage			= "&aPlayer &f\"PLAYERNAME\"&r&a was just promoted to the &3GROUPNAME&r&a rank!";
	public static boolean				displayNicknameBrackets			= true;
	
	public static boolean				displayOperatorJoinQuitMessages	= true;
	public static boolean				teleportToSpawnOnVoid			= true;
	private static volatile Location	spawnLocation					= null;
	
	public static final String getWorldStrFromSpawnLocationInConfig() {
		ConfigurationSection root = Main.config.getConfigurationSection("spawnLocation");
		if(root != null) {
			return root.getString("world");
		}
		return null;
	}
	
	public static final World getWorldFromConfigStr() {
		String str = getWorldStrFromSpawnLocationInConfig();
		if(str != null) {
			if(Main.isStringUUID(str)) {
				return Main.server.getWorld(UUID.fromString(str));
			}
			return Main.server.getWorld(str);
		}
		return null;
	}
	
	public static String						configVersion	= "";
	
	//==========================
	
	public static final ArrayList<JavaPlugin>	pluginsUsingMe	= new ArrayList<>();
	
	//=====================
	
	public static final void savePluginData() {
		for(SavablePlayerData savable : SavablePlayerData.instances) {
			if(!(savable instanceof Group)) {
				if(savable.saveAndLoadWithSuperCmds) {
					savable.saveToFile();
				}
			}
		}
		Group.saveToStaticFile();
		//Warps.getInstance().saveToFile();
		ItemLibrary.saveAllLibrariesToFile();//XXX To remove or not to remove; that is the question...
		for(SavablePluginData savable : SavablePluginData.getAllInstances()) {
			if(savable.saveAndLoadWithSuperCmds) {
				savable.saveToFile();
			}
		}
		YamlMgmtClass.saveYamls();
	}
	
	@Override
	public void onDisable() {
		Main.uuidMasterList.onDisable();
		Main.isLoaded = false;
		//Group.disposeAll();
		savePluginData();
		Main.sendConsoleMessage(Main.pluginName + "&eVersion " + Main.pdffile.getVersion() + " is now disabled!");
	}
	
	@Override
	public void onLoad() {
		Main.isLoaded = false;
		Main.instance = this;
		Main.pdffile = this.getDescription();
		Main.server = Bukkit.getServer();
		Main.scheduler = Main.server.getScheduler();
		//server.getPluginManager().registerEvents(this, this);
		Main.console = Main.server.getConsoleSender();
		Main.dataFolder = this.getDataFolder();
		if(!(Main.dataFolder.exists())) {
			Main.dataFolder.mkdir();
		}
		Main.pluginFile = this.getFile();
		try {
			Main.dataFolderName = this.getDataFolder().getAbsolutePath();
		} catch(SecurityException e) {
			FileMgmt.LogCrash(e, "onEnable()", "Failed to get the full directory of this library's folder(\"" + Main.dataFolderName + "\")!", true, Main.dataFolderName);
		}
	}
	
	public static final boolean isVaultInstalled() {
		return getVault() != null;
	}
	
	public static final Plugin getVault() {
		return Bukkit.getServer().getPluginManager().getPlugin("Vault");
	}
	
	public static final void setSpawnLocation(Location loc, boolean allowNull) {
		if(!allowNull) {
			Main.setSpawnLocation(loc);
			return;
		}
		Main.spawnLocation = loc;
	}
	
	public static final void setSpawnLocation(Location loc) {
		if(loc == null) {
			loc = getServerDefaultSpawnLocation();
		}
		Main.spawnLocation = loc;
	}
	
	public static final Location getSpawnLocation() {
		if(Main.spawnLocation == null) {
			World world = getWorldFromConfigStr();
			if(world != null) {
				Main.spawnLocation = SavablePlayerData.getLocationFromConfig("spawnLocation", Main.config);
				Main.sendConsoleMessage(Main.pluginName + "&aSuccessfully loaded spawn location from config after the missing world loaded/was detected!");
			}
		}
		return Main.spawnLocation;
	}
	
	public static final Location getServerDefaultSpawnLocation() {
		return Main.server.getWorlds().get(0).getSpawnLocation();
	}
	
	public static final Location getSpawnOrDefaultSpawnLocationIfNull() {
		Location loc = Main.getSpawnLocation();
		if(loc == null) {
			loc = Main.getServerDefaultSpawnLocation();
		}
		return loc;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onWorldLoadEvent(WorldLoadEvent event) {
		World loaded = event.getWorld();
		World check = getWorldFromConfigStr();
		if(loaded != null && check != null) {
			if(loaded.getUID().toString().equals(check.getUID().toString())) {
				Main.spawnLocation = SavablePlayerData.getLocationFromConfig("spawnLocation", Main.config);
				Main.sendConsoleMessage(Main.pluginName + "&aSuccessfully loaded spawn location from config after the missing world loaded!");
			}
		}
	}
	
	@Override
	public void onEnable() {
		Main.isLoaded = false;
		if(!Main.isVaultInstalled()) {
			File vault = YamlMgmtClass.getResourceFromStreamAsFile(Main.dataFolder, Main.vaultFileName, false);
			try {
				Plugin v = Main.server.getPluginManager().loadPlugin(vault);
				if(v != null) {
					Main.server.getPluginManager().enablePlugin(v);
					Main.sendConsoleMessage(Main.pluginName + "&eVault could not be found, so " + Main.pluginName + "&eloaded it manually.");
				} else {
					Main.sendConsoleMessage(Main.pluginName + "&eVault could not be found and Bukkit was unable to load it manually. A server restart should fix the issue.");
				}
			} catch(Throwable ignored) {
				Main.sendConsoleMessage(Main.pluginName + "&eVault could not be found and " + Main.pluginName + "&efailed to load it manually.");
			}
		}
		Main.console = Main.server.getConsoleSender();
		Main.consoleSayFormat = "&f[&5" + Main.console.getName() + "&f] ";
		Main.showDebugMsg(Main.pluginName + "The dataFolderName variable is: \"" + Main.dataFolderName + "\"!", Main.showDebugMsgs);
		// TODO Loading Files, plugins, etc.
		YamlMgmtClass.LoadConfig();
		if(Main.spawnLocation == null) {
			if(Main.getWorldFromConfigStr() == null) {
				Main.spawnLocation = Main.server.getWorlds().get(0).getSpawnLocation();
			} else {
				Main.getSpawnLocation();
			}
		}
		Main.uuidMasterList.onEnable();
		// TODO End of Loading Files, plugins, etc.
		if(Main.enabled) {
			CmdThread.getInstance();
			/*if(Main.setupPermissions()) {
				Main.sendConsoleMessage(Main.pluginName + "&2Vault Permissions API Successful!");
			} else {
				Main.sendConsoleMessage(Main.pluginName + "&cVault Permissions API was not successful.");
			}*/
			Main.registerEvents();
			for(SavablePluginData data : SavablePluginData.getAllInstances()) {
				try {
					if(data != null && data.saveAndLoadWithSuperCmds) {
						data.loadFromFile();
					}
				} catch(Throwable e) {
					Main.sendConsoleMessage(Main.pluginName + "&eAn error occurred while loading \"&f" + (data != null ? data.getName() : "&4{!Unknown SavablePluginData Type!}") + "&r&e\" from file:&z&c" + Main.throwableToStr(e));
				}
			}
			ItemLibrary.loadAllLibrariesFromFile();
			TicketData.getInstance().loadFromFile();
			try {
				Group.reloadFromFile();
			} catch(Throwable e) {
				Main.sendConsoleMessage(Main.pluginName + "&cUnable to load from groups.yml file:&z&4" + Main.throwableToStr(e));
			}
			Main.sendConsoleMessage(Main.pluginName + "&aVersion " + Main.pdffile.getVersion() + " is now enabled!");
			//Back up the config files:
			if(Main.backupOnStartup) {
				new Thread(new Runnable() {
					@Override
					public final void run() {
						File[] playerFolders = SavablePlayerData.getStaticSaveFolders();
						try {
							final String date = CodeUtils.getSystemTime(false, true, true);//Date only, file system safe
							File backupsFolder = new File(Main.dataFolder, "Config_Backups");
							backupsFolder.mkdirs();
							File datedFolder = new File(backupsFolder, date);
							datedFolder.mkdirs();
							for(File playerFolder : playerFolders) {
								File playerZip = new File(datedFolder, FilenameUtils.getName(playerFolder.getAbsolutePath()) + ".zip");
								try {
									playerZip.delete();
								} catch(Throwable ignored) {
								}
								FileMgmt.zipDir(playerFolder, playerZip);
							}
							for(SavablePluginData data : SavablePluginData.getAllInstances()) {
								File zipFile = new File(datedFolder, FilenameUtils.getName(data.getSaveFile().getAbsolutePath()) + ".zip");
								try {
									zipFile.delete();
								} catch(Throwable ignored) {
								}
								FileMgmt.zipFile(data.getSaveFile(), zipFile);
							}
						} catch(Throwable e) {
							final String throwable = Main.throwableToStr(e);
							Main.sendConsoleMessage(Main.pluginName + "&cUnable to automatically back up configuration files:&z&4" + throwable);
							if(throwable.contains("FileMgmt.copy(FileMgmt.java:174)")) {
								Main.sendConsoleMessage(Main.pluginName + "&eDid you /reload the server instead of &3/save-all&e and &3/stop {message...}&e?&z&eIf so, consider shutting down the server and starting it up properly.");
							}
						}
					}
				}).start();
			}
			try {
				YamlMgmtClass.getResourceFromStreamAsFile(Main.dataFolder, "permissions.txt", true);
			} catch(NullPointerException e) {
				final String throwable = Main.throwableToStr(e);
				Main.sendConsoleMessage(Main.pluginName + "&cUnable to create permissions.txt reference file:&z&4" + throwable);
				if(throwable.contains("FileMgmt.copy(FileMgmt.java:174)")) {
					Main.sendConsoleMessage(Main.pluginName + "&eDid you /reload the server instead of &3/save-all&e and &3/stop {message...}&e?&z&eIf so, consider shutting down the server and starting it up properly.");
				}
			}
			Runnables.updatePlayerStatesTask.runTaskTimer(Main.getInstance(), 20 * 5, 20 * 30);//Main.scheduler.scheduleSyncRepeatingTask(Main.getInstance(), Runnables.updatePlayerStatesTask, 0, 20 * 30);
			Main.isLoaded = true;
		} else {
			Main.uuidMasterList.onDisable();
			this.onDisable();
		}
	}
	
	/*@Deprecated
	public static Permission perm;
	
	@Deprecated
	public static boolean setupPermissions() {
		if(Main.server.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Permission> rsp = Main.server.getServicesManager().getRegistration(Permission.class);
		if(rsp != null) {
			if(rsp.getProvider() != null) {
				Main.perm = rsp.getProvider();
			}
		}
		return Main.perm != null;
	}*/
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static final void onServerPingPlayerListEvent(ServerListPingEvent event) {
		Main.sendConsoleMessage("Received server ping from: " + event.getAddress().toString());
	}
	
	public static final String getItemStackDisplayName(ItemStack item) {
		return item == null ? null : (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? Main.formatColorCodes(item.getItemMeta().getDisplayName()) : capitalizeFirstLetterOfEachWordIn(item.getType().name()));
	}
	
	@SuppressWarnings("deprecation")
	public static final ItemStack getItemStackFromBlock(Block block) {
		if(block == null) {
			return null;
		}
		return new ItemStack(block.getType(), 1, (short) 0, Byte.valueOf(block.getData()));
	}
	
	public static final String getItemStackName(Block block) {
		return getItemStackName(getItemStackFromBlock(block));
	}
	
	public static final String getItemStackName(ItemStack item) {
		return item == null ? null : (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? Main.formatColorCodes(item.getItemMeta().getDisplayName()) : Main.getItemStackUserFriendlyName(item));
	}
	
	public static final String getItemStackUserFriendlyName(ItemStack item) {
		ItemInfo info = item == null ? null : Items.itemByStack(item);
		return info == null ? getItemStackDisplayName(item) : info.getName();
	}
	
	private static final int	SECOND	= 1000;
	private static final int	MINUTE	= 60 * Main.SECOND;
	private static final int	HOUR	= 60 * Main.MINUTE;
	private static final int	DAY		= 24 * Main.HOUR;
	private static final int	YEAR	= 365 * Main.DAY;
	
	public static final String getLengthOfTime(long milliseconds) {
		StringBuffer text = new StringBuffer("");
		boolean yearsDaysOrHours = false;
		if(milliseconds >= Main.YEAR) {//dang. That is a long time. Haha lol I made an accidental funny.
			long years = milliseconds / Main.YEAR;
			text.append(years).append(years == 1L ? " year " : " years ");//I don't think that a Java long value can hold more than one year tbh... oh well. To think I wanted to add decades and centuries too XD
			milliseconds %= Main.YEAR;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= Main.DAY) {
			long days = milliseconds / Main.DAY;
			text.append(days).append(days == 1 ? " day " : " days ");
			milliseconds %= Main.DAY;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= Main.HOUR) {
			long hours = milliseconds / Main.HOUR;
			text.append(hours).append(hours == 1 ? " hours " : " hours ");
			milliseconds %= Main.HOUR;
			yearsDaysOrHours = true;
		}
		boolean minutes = false;
		boolean plural = true;
		if(milliseconds >= Main.MINUTE) {
			if(yearsDaysOrHours) {
				text.append("and ");
			} else {
				text.append("00:");
			}
			long mins = milliseconds / Main.MINUTE;
			text.append((mins >= 10 ? "" : "0") + mins).append(":");
			milliseconds %= Main.MINUTE;
			minutes = true;
			plural = mins != 1;
		} else {
			if(yearsDaysOrHours) {
				text.append("and 00:");
			} else {
				text.append("00:00:");
			}
		}
		if(milliseconds >= Main.SECOND) {
			long sec = milliseconds / Main.SECOND;
			text.append((sec >= 10 ? "" : "0") + sec);
			milliseconds %= Main.SECOND;
			plural = !minutes ? sec != 1 : plural;
		} else {
			text.append("00");
		}
		text.append(plural ? (minutes ? " minutes" : " seconds") : (minutes ? " minute" : " second"));
		return text.toString();
	}
	
	public static final String getTimeAndDate(long time) {
		return new SimpleDateFormat("MMM dd, yyyy @ HH:mm:ss").format(new Date(time));
	}
	
	public static String getSystemTime(boolean getTimeOnly) {
		return new SimpleDateFormat(getTimeOnly ? "HH:mm:ss" : "MM/dd/yyyy HH:mm:ss").format(new Date());
	}
	
	public final static Chunk getChunkAtWorldCoords(World world, int x, int z) {
		Chunk chunk;
		if(x < 0 && z >= 0) {//X is negative
			chunk = world.getChunkAt((x / 16) - 1, z / 16);
		} else if(x >= 0 && z < 0) {//Z is negative
			chunk = world.getChunkAt(x / 16, (z / 16) - 1);
		} else if(x < 0 && z < 0) {//X and Z are negative
			chunk = world.getChunkAt((x / 16) - 1, (z / 16) - 1);
		} else {//X and Z are positive, do things normally
			chunk = world.getChunkAt(x / 16, z / 16);
		}
		return chunk;
	}
	
	/** Checks str1 against str2(and vice versa) to see if they either
	 * equal(case
	 * ignored), if str1 starts with str2 and str1 is less than 6 characters(and
	 * vice versa), and removes all non-alpha-numeric characters and performs
	 * the checks again.
	 * 
	 * @param str1 String
	 * @param str2 String
	 * @return True if str1 is equal to, starts with, or ends with str1, and
	 *         vice versa; false otherwise */
	public static boolean isSimilarTo(String str1, String str2) {
		if(str1 == null || str2 == null || str1.equals("") || str2.equals("")) return false;
		boolean rtrn = (str1.equals(str2)) || (str1.equalsIgnoreCase(str2)) || (str1.startsWith(str2) && str1.length() + 1 >= (str2.length())) || (str1.endsWith(str2) && str1.length() + 1 >= str2.length() && str2.length() <= 6) || (str2.startsWith(str1) && str2.length() + 1 >= (str1.length())) || (str2.endsWith(str1) && str2.length() + 1 >= str1.length() && str1.length() <= 6);
		if(rtrn == true) {
			return true;
		}
		str1 = str1.replaceAll("[^\\p{Alnum}\\p{Space}]+", "");
		str2 = str2.replaceAll("[^\\p{Alnum}\\p{Space}]+", "");//Removes everything except AaBbCc, 123, and whitespace)
		if(str1.equals("") || str2.equals("")) return false;
		rtrn = (str1.equals(str2)) || (str1.equalsIgnoreCase(str2)) || (str1.startsWith(str2) && str1.length() + 1 >= (str2.length())) || (str1.endsWith(str2) && str1.length() + 1 >= str2.length() && str2.length() <= 6) || (str2.startsWith(str1) && str2.length() + 1 >= (str1.length())) || (str2.endsWith(str1) && str2.length() + 1 >= str1.length() && str1.length() <= 6);
		return false;
	}
	
	public static String ignoreCase(String str) {
		return "(?i)" + Pattern.quote(str);
	}
	
	@SuppressWarnings("boxing")
	public static double toNumber(String str) {
		if(Main.checkIsNumber(str)) {
			return Double.valueOf(str);
		}
		return Double.NaN;
	}
	
	public static boolean checkIsNumber(String str) {
		final String Digits = "(\\p{Digit}+)";
		final String HexDigits = "(\\p{XDigit}+)";
		final String Exp = "[eE][+-]?" + Digits;
		final String fpRegex = ("[\\x00-\\x20]*[+-]?(NaN|Infinity|(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|(\\.(" + Digits + ")(" + Exp + ")?)|" + "(((0[xX]" + HexDigits + "(\\.)?)|(0[xX]" + HexDigits + "?(\\.)" + HexDigits + "))[pP][+-]?" + Digits + "))[fFdD]?))[\\x00-\\x20]*");
		if(Pattern.matches(fpRegex, str)) {
			return true;
		}
		return false;
	}
	
	public static boolean checkIsInteger(String str) {
		boolean rtrn = true;
		try {
			Integer.valueOf(str);
		} catch(NumberFormatException e) {
			rtrn = false;
		}
		return rtrn;
	}
	
	public static boolean checkIsFloat(String str) {
		boolean rtrn = true;
		try {
			Float.valueOf(str);
		} catch(NumberFormatException e) {
			rtrn = false;
		}
		return rtrn;
	}
	
	public static boolean checkIsDouble(String str) {
		boolean rtrn = true;
		try {
			Double.valueOf(str);
		} catch(NumberFormatException e) {
			rtrn = false;
		}
		return rtrn;
	}
	
	public static boolean checkIsLong(String str) {
		boolean rtrn = true;
		try {
			Long.valueOf(str);
		} catch(NumberFormatException e) {
			rtrn = false;
		}
		return rtrn;
	}
	
	public static void onRconEvent(RemoteServerCommandEvent evt) {
		Main.rcon = evt.getSender();
	}
	
	public static boolean hasRconConnectedYet() {
		return(Main.rcon != null);
	}
	
	public static CommandSender getRcon() {
		if(Main.hasRconConnectedYet()) {
			return Main.rcon;
		}
		return null;
	}
	
	public static final List<String> getStringsBetweenStrings(String input, String pattern1, String pattern2) {
		List<String> strings = Arrays.asList(input.replaceAll("^.*?(?i)" + pattern1 + "", "").split("(?i)" + pattern2 + ".*?(" + pattern1 + "|$)"));
		return strings;
	}
	
	/** @param str The entire command string
	 * @return The command used */
	public static String getCommandFromMsg(String str) {
		if(!(str.isEmpty())) {
			if(str.length() == 1 || str.contains(" ") == false) {
				return str;
			} else if(str.indexOf(" ") >= 1) {
				String command = str.substring(0, str.indexOf(" "));
				return command;
			} else {
				return str;
			}
		}
		return "null";
	}
	
	/** Takes the given command string(cmd, which is everything you type) and
	 * cuts the actual commandLabel(the command you used) out, resulting in only
	 * the arguments.
	 * 
	 * @param cmd
	 * @return The arguments of the given command, in String form.
	 * @since 4.9
	 * @see Main#getCommandFromMsg(String) */
	public static String[] getArgumentsFromCommand(String cmd) {// TODO Move this to the Main
		String[] args = {""};
		String getArgs = "";
		//String command = Main.getCommandFromMsg(cmd);
		if(cmd.indexOf(" ") >= 1) {
			getArgs = cmd.trim().substring(cmd.indexOf(" ")).trim();
			args = getArgs.split(Main.getWhiteSpaceChars);
			//showDebugMsg("&aDebug: The command(\"" + command + "\") has the following arguments: " + args, showDebugMsgs);
		} else {
			//showDebugMsg("&aDebug: The command(\"" + command + "\") had no arguments.", showDebugMsgs);
		}
		return args;
	}
	
	public static final String getElementsFromStringArrayAtIndexesAsString(String[] array, int startIndex, int endIndex) {
		return Main.getElementsFromStringArrayAtIndexesAsString(array, startIndex, endIndex, ' ');
	}
	
	public static final String getElementsFromStringArrayAtIndexesAsString(String[] array, int startIndex, int endIndex, char seperatorChar) {
		if(array == null || array.length == 0) {
			return null;
		}
		if(startIndex < 0 || startIndex > array.length || endIndex < 0 || endIndex > array.length) {
			return null;
		}
		String rtrn = "";
		if(startIndex > endIndex) {
			for(int i = startIndex; i < endIndex; i--) {
				rtrn += seperatorChar + array[i];
			}
		} else {
			for(int i = startIndex; i < endIndex; i++) {
				rtrn += seperatorChar + array[i];
			}
		}
		if(rtrn.startsWith(seperatorChar + "")) {
			rtrn = rtrn.substring(1);
		}
		return rtrn.trim();
	}
	
	public static final String getElementsFromStringArrayAtIndexAsString(String[] array, int index) {
		return Main.getElementsFromStringArrayAtIndexAsString(array, index, ' ');
	}
	
	public static final String getElementsFromStringArrayAtIndexAsString(String[] array, int index, char seperatorChar) {
		if(array == null || index >= array.length) {
			return "";
		}
		String mkArgs = "";
		for(int i = index; i < array.length; i++) {
			mkArgs += array[i] + seperatorChar;
		}
		return mkArgs.trim();
	}
	
	public static final boolean doesArrayContainAnyNullObjects(Object[] array) {
		for(int i = 0; i < array.length; i++) {
			if(array[i] == null) {
				return true;
			}
		}
		return false;
	}
	
	public static final int getNextFreeIndexInArray(Object[] array) {
		if(array == null || !Main.doesArrayContainAnyNullObjects(array)) {
			return -1;
		}
		for(int i = 0; i < array.length; i++) {
			if(array[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	public static String sendConsoleMessage(String message) {
		if(message == null || message.isEmpty()) return "";
		message = Main.formatColorCodes(message);
		if(StringUtils.containsIgnoreCase(message, "&z")) {
			String[] msgs = message.split("(?i)&z");
			for(String msg : msgs) {
				Main.console.sendMessage(msg.replaceAll("(?i)&z", "").trim());
			}
			return message.trim();
		}
		Main.console.sendMessage(message.trim());
		return message.trim();
	}
	
	public static String sendMessage(Player target, String message) {
		return Main.sendMessage((CommandSender) target, message);
	}
	
	public static String sendMessage(CommandSender target, String message) {
		if(message == null || message.isEmpty() || target == null) return "";
		message = Main.formatColorCodes(message);
		if(StringUtils.containsIgnoreCase(message, "&z")) {
			String[] msgs = message.split("(?i)&z");
			for(String msg : msgs) {
				target.sendMessage(msg.replaceAll("(?i)&z", "").trim());
			}
			return message;
		}
		target.sendMessage(message.trim());
		return message.trim();
	}
	
	public static final Group getAdministratorGroup() {
		Group admin = Group.getGroupByName("admin");
		admin = admin == null ? Group.getGroupByName("admins") : admin;
		admin = admin == null ? Group.getGroupByName("administrator") : admin;
		admin = admin == null ? Group.getGroupByName("administrators") : admin;
		return admin;
	}
	
	public static final boolean isPlayerAStaffMember(Player player) {
		return player != null ? player.isOp() || PlayerPermissions.getPlayerPermissions(player).isAMemberOfGroup(getAdministratorGroup()) : false;
	}
	
	public static final String sendMessageToOnlineStaffWith(CommandSender target, String message) {
		boolean sentToTargetAlready = target == null;
		if(!sentToTargetAlready && !(target instanceof Player)) {
			sentToTargetAlready = true;
			sendMessage(target, message);
		}
		for(Player player : server.getOnlinePlayers()) {
			if(player.isOp()) {
				if(!sentToTargetAlready) {
					if(SavablePlayerData.playerEquals(player, (Player) target)) {
						sentToTargetAlready = true;
					}
				}
				sendMessage(player, message);
			}
		}
		Group admin = getAdministratorGroup();
		if(admin != null) {
			for(Player player : server.getOnlinePlayers()) {
				if(!player.isOp()) {//we just sent the message to all opped players, no need to send it twice
					if(PlayerPermissions.getPlayerPermissions(player).isAMemberOfGroup(admin)) {
						if(!sentToTargetAlready) {
							if(SavablePlayerData.playerEquals(player, (Player) target)) {
								sentToTargetAlready = true;
							}
						}
						sendMessage(player, message);
					}
				}
			}
		}
		return formatColorCodes(message).trim();
	}
	
	public static final String sendMessageToOnlineStaff(String message) {
		for(Player player : server.getOnlinePlayers()) {
			if(player.isOp()) {
				sendMessage(player, message);
			}
		}
		Group admin = getAdministratorGroup();
		if(admin != null) {
			for(Player player : server.getOnlinePlayers()) {
				if(!player.isOp()) {//we just sent the message to all opped players, no need to send it twice
					if(PlayerPermissions.getPlayerPermissions(player).isAMemberOfGroup(admin)) {
						sendMessage(player, message);
					}
				}
			}
		}
		return formatColorCodes(message).trim();
	}
	
	public static String fixPluralWord(int number, String word) {
		// TODO Auto-generated method stub
		String newWord = "";
		if(number != 1) {
			if(word.equalsIgnoreCase("those") || word.equalsIgnoreCase("that")) {
				return "those";
			}
			if(word.length() <= 3 && word.equalsIgnoreCase("is")) {
				word = "are";
				return word;
			}
			if(word.length() >= 4) {
				String beginningOfWord = word.substring(0, word.length() - 3);
				String endOfWord = word.substring(word.length() - 3, word.length());
				List<String> suffixes = new ArrayList<>();
				suffixes.add("x");
				suffixes.add("sh");
				suffixes.add("ch");
				suffixes.add("z");
				suffixes.add("ss");
				Iterator<String> it = suffixes.iterator();
				boolean replacedSuffix = false;
				while(it.hasNext()) {
					if(endOfWord.contains(it.next()) && replacedSuffix == false) {
						endOfWord = endOfWord + "es";
						replacedSuffix = true;
					}
				}
				if(replacedSuffix == true) {
					newWord = beginningOfWord + endOfWord;
					return newWord;
				}
				return word + (word.endsWith("s") ? "" : "s");
			}
			return word;
		} else if(word.equalsIgnoreCase("are")) {
			word = "is";
			return word;
		} else if(word.equalsIgnoreCase("those") || word.equalsIgnoreCase("that")) {
			return "that";
		} else {
			if(word.endsWith("s") && word.length() >= 2) {
				return word.substring(0, word.length() - 1);
			}
			return word;
		}
	}
	
	public static boolean showDebugMsg(String str, boolean Override) {
		if(Override == true) {
			Main.sendConsoleMessage(str);
			return true;
		}
		return false;
	}
	
	public static String replaceWord(String str, String regex, String replacement, boolean case_sensitive, String dataFolderName) {
		try {
			if(case_sensitive == true) {
				return str.replaceAll("\\b" + regex + "\\b", replacement);
			}
			return str.replaceAll("(?i)\\b" + regex + "\\b", replacement);
		} catch(PatternSyntaxException e) {
			FileMgmt.LogCrash(e, "replaceWord(\"" + str + "\", \"" + regex + "\", \"" + replacement + "\", " + case_sensitive + ")", "A bad regex was used.", false, dataFolderName);
			Main.showDebugMsg("A bad regex was used when running function \"replaceWord()\". Check the server log or \"" + dataFolderName + "\\crash-reports.txt\" to solve the problem.", false);
		}
		return str;
	}
	
	public static String GrammarEnforcement(String msg, String dataFolderName) {
		try {
			msg = Main.capitalizeFirstLetter(msg);
			msg = Main.replaceWord(msg, "(\\w+)(\\s+\\1)+", "$1", false, dataFolderName);
			msg = Main.replaceWord(msg, "(\\w+)(\\.*\\4)+", "$1", false, dataFolderName);
			/**/msg = Main.replaceWord(msg, "\\p{Alnum}\\1{4,}", "$1", false, dataFolderName);
			//msg = msg.replaceAll("\\w+(\\.\\w+)+", "");//This SHOULD get rid of spammy letters like helloooooooooooooooo, but I'm not sure...
			msg = Main.replaceWord(msg, "lol", "*Laughs out loud*", false, dataFolderName);
			msg = Main.replaceWord(msg, "Jk", "Just joking", true, dataFolderName);
			msg = Main.replaceWord(msg, "jk", "just joking", false, dataFolderName);
			msg = Main.replaceWord(msg, "Np", "No problem", true, dataFolderName);
			msg = Main.replaceWord(msg, "np", "no problem", false, dataFolderName);
			msg = Main.replaceWord(msg, "Wtf", "What the-", true, dataFolderName);
			msg = Main.replaceWord(msg, "wtf", "what the-", false, dataFolderName);
			msg = Main.replaceWord(msg, "Wtc", "What the heck", true, dataFolderName);
			msg = Main.replaceWord(msg, "wtc", "what the heck", false, dataFolderName);
			msg = Main.replaceWord(msg, "Wth", "What the heck", true, dataFolderName);
			msg = Main.replaceWord(msg, "wth", "what the heck", false, dataFolderName);
			msg = Main.replaceWord(msg, "lmao", "*Laughing my butt off*", false, dataFolderName);
			msg = Main.replaceWord(msg, "lmfao", "*Laughing my frekin' butt off*", false, dataFolderName);
			msg = Main.replaceWord(msg, "ikr", "I know, right", false, dataFolderName);
			msg = Main.replaceWord(msg, "idk", "I don't know", false, dataFolderName);
			msg = Main.replaceWord(msg, "U", "You", true, dataFolderName);
			msg = Main.replaceWord(msg, "u", "you", false, dataFolderName);
			msg = Main.replaceWord(msg, "i", "I", true, dataFolderName);
			msg = Main.replaceWord(msg, "Budder", "Gold", true, dataFolderName);//XD
			msg = Main.replaceWord(msg, "budder", "gold", false, dataFolderName);//XD
			msg = Main.replaceWord(msg, "Buder", "Gold", true, dataFolderName);//XD
			msg = Main.replaceWord(msg, "bud.der", "gold", false, dataFolderName);//XD
			msg = Main.replaceWord(msg, "buder", "gold", false, dataFolderName);//XD
			msg = Main.replaceWord(msg, "Brb", "Be right back", true, dataFolderName);
			msg = Main.replaceWord(msg, "brb", "be right back", false, dataFolderName);
			msg = Main.replaceWord(msg, "Gtg", "Got to go", true, dataFolderName);
			msg = Main.replaceWord(msg, "gtg", "got to go", false, dataFolderName);
			msg = Main.replaceWord(msg, "Liek", "Like", true, dataFolderName);
			msg = Main.replaceWord(msg, "liek", "like", false, dataFolderName);
			msg = Main.replaceWord(msg, "Y", "Why", true, dataFolderName);
			msg = Main.replaceWord(msg, "y", "why", false, dataFolderName);
			msg = Main.replaceWord(msg, "Plz", "Please", true, dataFolderName);
			msg = Main.replaceWord(msg, "plz", "please", false, dataFolderName);
			msg = Main.replaceWord(msg, "Pzl", "Please", true, dataFolderName);
			msg = Main.replaceWord(msg, "pzl", "please", false, dataFolderName);
			msg = Main.replaceWord(msg, "Yo", "Hey", true, dataFolderName);
			msg = Main.replaceWord(msg, "yo", "hey", false, dataFolderName);
			msg = Main.replaceWord(msg, "http://", "", false, dataFolderName);
			msg = Main.replaceWord(msg, "https://", "", false, dataFolderName);
			//msg = replaceWord(msg, "(?i)(\\w+)(\\s+\\4)+", "$1", true, dataFolderName);
			msg = Main.replaceWord(msg, "Gangsta", "Gang member", true, dataFolderName);
			msg = Main.replaceWord(msg, "gangsta", "gang member", false, dataFolderName);
			msg = Main.replaceWord(msg, "Gangster", "Gang member", true, dataFolderName);
			msg = Main.replaceWord(msg, "gangster", "gang member", false, dataFolderName);
			msg = Main.replaceWord(msg, "Wut", "What", true, dataFolderName);
			msg = Main.replaceWord(msg, "wut", "what", false, dataFolderName);
			msg = Main.replaceWord(msg, "Wat", "What", true, dataFolderName);
			msg = Main.replaceWord(msg, "wat", "what", false, dataFolderName);
			msg = Main.replaceWord(msg, "ik", "I know", false, dataFolderName);
			msg = Main.replaceWord(msg, "gotta", "have to", false, dataFolderName);
			msg = Main.replaceWord(msg, "K", " Okay", true, dataFolderName);
			msg = Main.replaceWord(msg, "k", " okay", false, dataFolderName);
			msg = Main.replaceWord(msg, "ill", "I'll", false, dataFolderName);
			msg = Main.replaceWord(msg, "Shure", "Sure", true, dataFolderName);
			msg = Main.replaceWord(msg, "shure", "sure", false, dataFolderName);
			msg = Main.replaceWord(msg, "KK", "Okay, I understand", true, dataFolderName);
			msg = Main.replaceWord(msg, "Kk", "Okay, I understand", true, dataFolderName);
			msg = Main.replaceWord(msg, "kk", "okay, I understand", false, dataFolderName);
			msg = Main.replaceWord(msg, "Nvm", "Nevermind", true, dataFolderName);
			msg = Main.replaceWord(msg, "nvm", "nevermind", false, dataFolderName);
			msg = Main.replaceWord(msg, "Wanna", "Want to", true, dataFolderName);
			msg = Main.replaceWord(msg, "wanna", "want to", false, dataFolderName);
			msg = Main.replaceWord(msg, "Gimme", "Give me", true, dataFolderName);
			msg = Main.replaceWord(msg, "gimme", "give me", false, dataFolderName);
			msg = Main.replaceWord(msg, "Sec", "Second", true, dataFolderName);
			msg = Main.replaceWord(msg, "sec", "second", false, dataFolderName);
			msg = Main.replaceWord(msg, "Promo", "Promotion", true, dataFolderName);
			msg = Main.replaceWord(msg, "promo", "promotion", false, dataFolderName);
			msg = Main.replaceWord(msg, "Yah", "Yes", true, dataFolderName);
			msg = Main.replaceWord(msg, "yah", "yes", false, dataFolderName);
			msg = Main.replaceWord(msg, "Ya", "You", true, dataFolderName);
			msg = Main.replaceWord(msg, "ya", "you", false, dataFolderName);
			msg = Main.replaceWord(msg, "im", "I'm", true, dataFolderName);
			msg = Main.replaceWord(msg, "imma", "I'll", true, dataFolderName);
			msg = Main.replaceWord(msg, "Yur", "Your", true, dataFolderName);
			msg = Main.replaceWord(msg, "yur", "your", false, dataFolderName);
			msg = Main.replaceWord(msg, "bc", "because", false, dataFolderName);
			msg = Main.replaceWord(msg, "Dont", "Don't", true, dataFolderName);
			msg = Main.replaceWord(msg, "dont", "don't", false, dataFolderName);
			msg = Main.replaceWord(msg, "L8ter", "Later", true, dataFolderName);
			msg = Main.replaceWord(msg, "l8ter", "later", false, dataFolderName);
			msg = Main.replaceWord(msg, "Gg", "Good game", true, dataFolderName);
			msg = Main.replaceWord(msg, "GG", "Good game", true, dataFolderName);
			msg = Main.replaceWord(msg, "gg", "good game", false, dataFolderName);
			msg = Main.replaceWord(msg, "Teh", "The", true, dataFolderName);
			msg = Main.replaceWord(msg, "teh", "the", false, dataFolderName);
			msg = Main.replaceWord(msg, "pussy cat", "^%GH*D@", true, dataFolderName);//Workaround
			msg = Main.replaceWord(msg, "Pussy cat", "^%GH*D#", true, dataFolderName);//Workaround
			msg = Main.replaceWord(msg, "Pussy", "Wimp", true, dataFolderName);
			msg = Main.replaceWord(msg, "pussy", "wimp", false, dataFolderName);
			msg = Main.replaceWord(msg, "^%GH*D@", "pussy cat", true, dataFolderName);//Workaround
			msg = Main.replaceWord(msg, "^%GH*D#", "Pussy cat", true, dataFolderName);//Workaround
			msg = Main.replaceWord(msg, "Rofl", "*Rolling on the floor*", true, dataFolderName);
			msg = Main.replaceWord(msg, "rofl", "*rolling on the floor*", false, dataFolderName);
			msg = Main.replaceWord(msg, "Roflol", "*Rolling on the floor laughing out loud*", true, dataFolderName);
			msg = Main.replaceWord(msg, "roflol", "*rolling on the floor laughing out loud*", false, dataFolderName);
			msg = Main.replaceWord(msg, "Omg", "Oh my gosh", true, dataFolderName);
			msg = Main.replaceWord(msg, "omg", "oh my gosh", false, dataFolderName);
			msg = Main.replaceWord(msg, "Wierd", "Weird", true, dataFolderName);
			msg = Main.replaceWord(msg, "wierd", "weird", false, dataFolderName);
			msg = Main.replaceWord(msg, "Nm", "Not much", true, dataFolderName);
			msg = Main.replaceWord(msg, "nm", "not much", false, dataFolderName);
			msg = Main.replaceWord(msg, "Min", "Minute", true, dataFolderName);
			msg = Main.replaceWord(msg, "min", "minute", false, dataFolderName);
			msg = Main.replaceWord(msg, "mc", "Minecraft", false, dataFolderName);
			msg = Main.replaceWord(msg, "Tbh", "To be honest", true, dataFolderName);
			msg = Main.replaceWord(msg, "tbh", "to be honest", false, dataFolderName);
			msg = Main.replaceWord(msg, "Btw", "By the way", true, dataFolderName);
			msg = Main.replaceWord(msg, "btw", "by the way", false, dataFolderName);
			msg = Main.replaceWord(msg, "Cya", "See you", true, dataFolderName);
			msg = Main.replaceWord(msg, "cya", "see you", false, dataFolderName);
			msg = Main.replaceWord(msg, "Atm", "At the moment", true, dataFolderName);
			msg = Main.replaceWord(msg, "atm", "at the moment", true, dataFolderName);
			msg = Main.replaceWord(msg, "Imo", "In my opinion", true, dataFolderName);
			msg = Main.replaceWord(msg, "imo", "in my opinion", false, dataFolderName);
			msg = Main.replaceWord(msg, "Dood", "Dude", true, dataFolderName);
			msg = Main.replaceWord(msg, "dood", "dude", false, dataFolderName);
			msg = Main.replaceWord(msg, "Dude", "Man", true, dataFolderName);
			msg = Main.replaceWord(msg, "dude", "man", false, dataFolderName);
			msg = Main.replaceWord(msg, "Woot", "Hurrah", true, dataFolderName);
			msg = Main.replaceWord(msg, "woot", "hurrah", false, dataFolderName);
			/*msg = replaceWord(msg, "", "", true, dataFolderName);
			msg = replaceWord(msg, "", "", false, dataFolderName);*/
			msg = msg.replaceAll("(?i)\\bbud der\\b", "gold");
		} catch(ArrayIndexOutOfBoundsException e) {
			FileMgmt.LogCrash(e, "GrammarEnforcement()", "A bad regex was used in the function \"replaceWord(String msg(\"" + msg + "\"), String dataFolderName(\"" + dataFolderName + "\"))\"!", true, dataFolderName);
		}
		return msg.trim();
	}
	
	public static final String capitalizeFirstLetterOfEachWordIn(String msg) {
		msg = msg.replace("_", " ");
		String rtrn = "";
		int i = 0;
		String[] split = msg.split(getWhiteSpaceChars);
		for(String s : split) {
			rtrn += capitalizeFirstLetter(s.toLowerCase()) + (i + 1 == split.length ? "" : " ");
			i++;
		}
		return rtrn;
	}
	
	/** @param msg String
	 * @return The given String, with the first letter capitalized. If the first
	 *         character is not a letter, then this function only returns the
	 *         same String. */
	public static String capitalizeFirstLetter(String msg) {
		if(msg.length() >= 1) {
			msg = msg.substring(0, 1).toUpperCase() + msg.substring(1, msg.length());
		}
		return msg;
	}
	
	public static int getRandomIntValBetween(int Low, int High) {
		SecureRandom r = new SecureRandom();
		int L = Math.min(Low, High);
		int H = Math.max(Low, High);
		int R = r.nextInt(H - L) + L;
		return R;
	}
	
	public static boolean LocationPreciseEquals(Location loc1, Location loc2) {
		boolean x = (loc1.getX() == loc2.getX());
		boolean y = (loc1.getY() == loc2.getY());
		boolean z = (loc1.getZ() == loc2.getZ());
		return x && y && z;
	}
	
	public static boolean LocationEquals(Location loc1, Location loc2) {
		if(loc1 == null || loc2 == null) {
			return false;
		}
		boolean x = (loc1.getBlockX() == loc2.getBlockX());
		boolean y = (loc1.getBlockY() == loc2.getBlockY());
		boolean z = (loc1.getBlockZ() == loc2.getBlockZ());
		return x && y && z;
	}
	
	/** <pre>
	 * public static String unSpecifiedVarWarning(final String warningVar, final String fileName, final String pluginName) {
	 * 	sendConsoleMessage(pluginName + &quot;&amp;eWarning! \&quot;&quot; + warningVar + &quot;\&quot; was not specified(or was set incorrectly) in the \&quot;&quot; + fileName + &quot;\&quot; file!&amp;z&amp;eHas the existing \&quot;&quot; + fileName + &quot;\&quot; file not been updated from a past version?&quot;);
	 * 	return warningVar;
	 * }
	 * </pre>
	 * 
	 * @param warningVar The variable in question
	 * @param fileName The name of the config.yml file used
	 * @param pluginName The name of the plugin in question
	 * @return The variable in question */
	public static String unSpecifiedVarWarning(final String warningVar, final String fileName, final String pluginName) {
		Main.sendConsoleMessage(pluginName + "&eWarning! \"" + warningVar + "\" was not specified(or was set incorrectly) in the \"" + fileName + "\" file!&z&eHas the existing \"" + fileName + "\" file not been updated from a past version?");
		return warningVar;
	}
	
	/** Sends the specified target the message if the message hasn't been sent
	 * before during this server session. If no target is specified, or the
	 * target that is specified does not exist, then this function broadcasts
	 * the parameter str.
	 * 
	 * @param str String
	 * @param target String
	 * @return True if a message was sent, false otherwise. */
	public static boolean sendOneTimeMessage(String str, String target) {
		if(!StringUtils.isNotEmpty(str)) {
			return false;
		}
		boolean hasMessageBeenSentBefore = false;
		for(String curMsg : Main.oneTimeMessageList) {
			if(str.equalsIgnoreCase(curMsg)) {
				hasMessageBeenSentBefore = true;
				break;
			}
		}
		if(hasMessageBeenSentBefore == false) {
			Player plyr = Main.getPlayer(target);
			if(plyr != null) {
				Main.sendMessage(plyr, str);
			} else if(target.equalsIgnoreCase("console") || target.equals("!")) {
				Main.sendConsoleMessage(str);
			} else {
				Main.server.broadcastMessage(Main.formatColorCodes(str));
			}
			return true;
		}
		return false;
	}
	
	/** @param event The InventoryClickEvent to use
	 * @return What inventory was clicked based on the given information
	 * @see FunctionClass#whatInventoryWasClicked(InventoryView, int, int) */
	public static String whatInventoryWasClicked(InventoryClickEvent event) {
		return Main.whatInventoryWasClicked(event.getView(), event.getSlot(), event.getRawSlot());
	}
	
	public static String topOrBottom(InventoryClickEvent event) {
		return Main.topOrBottom(event.getView(), event.getSlot(), event.getRawSlot());
	}
	
	/** @param slot unused */
	public static String topOrBottom(InventoryView view, int slot, int rawSlot) {
		Inventory topInv = view.getTopInventory();
		if(rawSlot >= topInv.getSize()) {
			return "BOTTOM";
		}
		return "TOP";
	}
	
	/** @param view The InventoryView
	 * @param slot The slot
	 * @param rawSlot The rawSlot
	 * @return What inventory was clicked based on the given information */
	public static String whatInventoryWasClicked(InventoryView view, int slot, int rawSlot) {
		String rtrn = "UNDETERMINED_" + view.getTopInventory().getType().name() + "_SIZE_" + view.getTopInventory().getSize();
		Inventory topInv = view.getTopInventory();
		//Inventory bottomInv = view.getBottomInventory();
		if(topInv.getType().equals(InventoryType.ANVIL)) {
			if(rawSlot >= 0 && rawSlot <= 2) {//Top anvil slots
				rtrn = "ANVIL_" + rawSlot;
			} else if(rawSlot >= 3 && rawSlot <= 29) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 30 && rawSlot <= 38) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.BEACON)) {
			if(rawSlot == 0) {//Beacon slot
				rtrn = "BEACON_" + rawSlot;
			} else if(rawSlot >= 1 && rawSlot <= 27) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 28 && rawSlot <= 36) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.BREWING)) {
			if(rawSlot >= 0 && rawSlot <= 3) {//Top brewing slots
				rtrn = "BREWING_" + rawSlot;
			} else if(rawSlot >= 4 && rawSlot <= 30) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 31 && rawSlot <= 39) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.CHEST)) {
			if(topInv.getSize() == 2) {//Horse inventory(Why didn't they just make another type?)
				if(rawSlot >= 0 && rawSlot <= 1) {//Top horse slots
					rtrn = "HORSE_" + rawSlot;
				} else if(rawSlot >= 2 && rawSlot <= 28) {//Inventory
					rtrn = "BOTTOM_" + slot;
				} else if(rawSlot >= 29 && rawSlot <= 37) {//Hotbar
					rtrn = "HOTBAR_" + slot;
				}
			} else if(topInv.getSize() == 9) {
				if(rawSlot >= 0 && rawSlot <= 8) {//Top chest slots
					rtrn = "CHEST_" + rawSlot;
				} else if(rawSlot >= 9 && rawSlot <= 35) {//Inventory
					rtrn = "BOTTOM_" + slot;
				} else if(rawSlot >= 36 && rawSlot <= 44) {//Hotbar
					rtrn = "HOTBAR_" + slot;
				}
			} else if(topInv.getSize() == 18) {
				if(rawSlot >= 0 && rawSlot <= 17) {//Top chest slots
					rtrn = "CHEST_" + rawSlot;
				} else if(rawSlot >= 18 && rawSlot <= 44) {//Inventory
					rtrn = "BOTTOM_" + slot;
				} else if(rawSlot >= 45 && rawSlot <= 53) {//Hotbar
					rtrn = "HOTBAR_" + slot;
				}
			} else if(topInv.getSize() == 27) {
				if(rawSlot >= 0 && rawSlot <= 26) {//Top chest slots
					rtrn = "CHEST_" + rawSlot;
				} else if(rawSlot >= 27 && rawSlot <= 53) {//Inventory
					rtrn = "BOTTOM_" + slot;
				} else if(rawSlot >= 54 && rawSlot <= 62) {//Hotbar
					rtrn = "HOTBAR_" + slot;
				}
			} else if(topInv.getSize() == 36) {
				if(rawSlot >= 0 && rawSlot <= 35) {//Top chest slots
					rtrn = "CHEST_" + rawSlot;
				} else if(rawSlot >= 36 && rawSlot <= 62) {//Inventory
					rtrn = "BOTTOM_" + slot;
				} else if(rawSlot >= 63 && rawSlot <= 71) {//Hotbar
					rtrn = "HOTBAR_" + slot;
				}
			} else if(topInv.getSize() == 45) {
				if(rawSlot >= 0 && rawSlot <= 44) {//Top chest slots
					rtrn = "CHEST_" + rawSlot;
				} else if(rawSlot >= 45 && rawSlot <= 71) {//Inventory
					rtrn = "BOTTOM_" + slot;
				} else if(rawSlot >= 72 && rawSlot <= 80) {//Hotbar
					rtrn = "HOTBAR_" + slot;
				}
			} else if(topInv.getSize() == 54) {
				if(rawSlot >= 0 && rawSlot <= 53) {//Top chest slots
					rtrn = "CHEST_" + rawSlot;
				} else if(rawSlot >= 54 && rawSlot <= 80) {//Inventory
					rtrn = "BOTTOM_" + slot;
				} else if(rawSlot >= 81 && rawSlot <= 89) {//Hotbar
					rtrn = "HOTBAR_" + slot;
				}
			}
		}
		if(topInv.getType().equals(InventoryType.CRAFTING)) {
			if(rawSlot >= 0 && rawSlot <= 4) {//Top crafting slots
				rtrn = "CRAFTING_" + rawSlot;
			} else if(rawSlot >= 9 && rawSlot <= 35) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 36 && rawSlot <= 44) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			} else if(rawSlot >= 5 && rawSlot <= 8) {//Armour
				rtrn = "ARMOUR_" + (rawSlot == 5 ? "HELMET" : (rawSlot == 6 ? "CHESTPLATE" : (rawSlot == 7 ? "LEGGINGS" : (rawSlot == 8 ? "BOOTS" : "UNKNOWN"))));
			}
		}
		if(topInv.getType().equals(InventoryType.CREATIVE)) {//Not needed, covered by InventoryType.PLAYER
			/*
			 * if(rawSlot >= && rawSlot <= ) {//Top creative slots
			 * rtrn = "_" + rawSlot;
			 * } else if(rawSlot >= && rawSlot <= ) {//Inventory
			 * rtrn = "BOTTOM_" + slot;
			 * } else if(rawSlot >= && rawSlot <= ) {//Hotbar
			 * rtrn = "HOTBAR_" + slot;
			 * }
			 */
		}
		if(topInv.getType().equals(InventoryType.DISPENSER)) {
			if(rawSlot >= 0 && rawSlot <= 8) {//Top dispenser slots
				rtrn = "DISPENSER_" + rawSlot;
			} else if(rawSlot >= 9 && rawSlot <= 35) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 36 && rawSlot <= 44) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.DROPPER)) {
			if(rawSlot >= 0 && rawSlot <= 8) {//Top dropper slots
				rtrn = "DROPPER_" + rawSlot;
			} else if(rawSlot >= 9 && rawSlot <= 35) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 36 && rawSlot <= 44) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.ENCHANTING)) {
			if(rawSlot == 0) {//Enchanting slot
				rtrn = "ENCHANTING_" + rawSlot;
			} else if(rawSlot >= 1 && rawSlot <= 27) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 28 && rawSlot <= 36) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.ENDER_CHEST)) {
			if(rawSlot >= 0 && rawSlot <= 26) {//Top ender chest slots
				rtrn = "ENDERCHEST_" + rawSlot;
			} else if(rawSlot >= 27 && rawSlot <= 53) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 54 && rawSlot <= 62) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.FURNACE)) {
			if(rawSlot >= 0 && rawSlot <= 2) {//Top furnace slots
				rtrn = "FURNACE_" + rawSlot;
			} else if(rawSlot >= 3 && rawSlot <= 29) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 30 && rawSlot <= 38) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.HOPPER)) {
			if(rawSlot >= 0 && rawSlot <= 4) {//Top hopper slots
				rtrn = "HOPPER_" + rawSlot;
			} else if(rawSlot >= 5 && rawSlot <= 31) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 32 && rawSlot <= 40) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.MERCHANT)) {
			if(rawSlot >= 0 && rawSlot <= 2) {//Top villager slots
				rtrn = "MERCHANT_" + rawSlot;
			} else if(rawSlot >= 3 && rawSlot <= 29) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 30 && rawSlot <= 38) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		if(topInv.getType().equals(InventoryType.PLAYER)) {
			if(rawSlot >= 0 && rawSlot <= 26) {//Top Player slots
				rtrn = "PLAYER_" + rawSlot;
			} else if(rawSlot >= 27 && rawSlot <= 53) {//Inventory
				rtrn = "PLAYER_" + slot;//BOTTOM_
			} else if(rawSlot >= 54 && rawSlot <= 62) {//Hotbar
				rtrn = "PLAYER_" + slot;//HOTBAR_
			}
		}
		if(topInv.getType().equals(InventoryType.WORKBENCH)) {
			if(rawSlot >= 0 && rawSlot <= 9) {//Top crafting slots
				rtrn = "WORKBENCH_" + rawSlot;
			} else if(rawSlot >= 10 && rawSlot <= 36) {//Inventory
				rtrn = "BOTTOM_" + slot;
			} else if(rawSlot >= 37 && rawSlot <= 45) {//Hotbar
				rtrn = "HOTBAR_" + slot;
			}
		}
		return rtrn;
	}
	
	/** Takes the given command string(cmd, which is everything you type) and
	 * cuts the actual commandLabel(the command you used) out, resulting in only
	 * the arguments.
	 * 
	 * @param cmd
	 * @return The arguments of the given command, in String form.
	 * @since 4.9
	 * @see CommandMgmt#getCommandFromMsg(String) */
	public static String getStringArgumentsFromCommand(String cmd) {// TODO Move this to the Main
		String args = "";
		//String command = Main.getCommandFromMsg(cmd);
		if(cmd.indexOf(" ") >= 1) {
			args = cmd.trim().substring(cmd.indexOf(" ")).trim();
			//showDebugMsg("&aDebug: The command(\"" + command + "\") has the following arguments: " + args, showDebugMsgs);
		} else {
			//showDebugMsg("&aDebug: The command(\"" + command + "\") had no arguments.", showDebugMsgs);
		}
		return args;
	}
	
	/** Takes the given command string(cmd, which is everything you type) and
	 * cuts the actual commandLabel(the command you used) out, resulting in only
	 * the arguments.
	 * 
	 * @param cmd
	 * @return The arguments of the given command, in String form.
	 * @since 4.9
	 * @see CommandMgmt#getCommandFromMsg(String) */
	public static String getStringArgumentsFromCommand(String[] args) {// TODO Move this to the Main
		String strArgs = "";
		for(String curArg : args) {
			strArgs += curArg + " ";
		}
		strArgs = strArgs.trim();
		return strArgs;
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static final boolean startsWithIgnoreCase(String str1, String str2) {
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		return str1.startsWith(str2);
	}
	
	public static final boolean endsWithIgnoreCase(String str1, String str2) {
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		return str1.endsWith(str2);
	}
	
	/** @param array The list to read from
	 * @param c The character to use as a separator
	 * @return The resulting string */
	public static final String stringArrayToString(char c, List<String> array) {
		if(array == null) {
			return "null";
		}
		String rtrn = "";
		for(String element : array) {
			rtrn += element + c;
		}
		return rtrn.trim();
	}
	
	/** @param array The array/list/strings to read from
	 * @param c The character to use as a separator
	 * @return The resulting string */
	public static final String stringArrayToString(char c, String... array) {
		if(array == null) {
			return "null";
		}
		String rtrn = "";
		for(String element : array) {
			rtrn += element + c;
		}
		return rtrn.length() >= 2 ? rtrn.substring(0, rtrn.length() - 1) : rtrn;
	}
	
	/** @param array The String[] array to convert
	 * @param c The separator character to use
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, char c) {
		return stringArrayToString(array, c, 0);
	}
	
	/** @param array The String[] array to convert
	 * @param c The separator character to use
	 * @param startIndex The index to start at
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, char c, int startIndex) {
		return stringArrayToString(array, c + "", startIndex);
	}
	
	/** @param array The array/list/strings to read from
	 * @param c The character to use as a separator
	 * @param startIndex The index to start at
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, String c, int startIndex) {
		if(array == null || startIndex >= array.length) {
			return "null";
		}
		String rtrn = "";
		int i = 0;
		for(String element : array) {
			if(i >= startIndex) {
				rtrn += element + (i + 1 == array.length ? "" : c);
			}
			i++;
		}
		return rtrn;
	}
	
	/** @param array The String[] array to convert
	 * @param c The separator character to use
	 * @param startIndex The index to start at
	 * @param endIndex The index to stop short at
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, char c, int startIndex, int endIndex) {
		return stringArrayToString(array, c + "", startIndex, endIndex);
	}
	
	/** @param array The array/list/strings to read from
	 * @param c The character to use as a separator
	 * @param startIndex The index to start at
	 * @param endIndex The index to stop short at
	 * @return The resulting string. If startIndex is greater than or equal to
	 *         the array's size, endIndex is greater than the array's size,
	 *         startIndex is greater than or equal to endIndex, and/or either
	 *         startIndex or endIndex are negative, "null" is returned. */
	public static final String stringArrayToString(String[] array, String c, int startIndex, int endIndex) {
		if(array == null || startIndex >= array.length || endIndex > array.length || startIndex >= endIndex || startIndex < 0 || endIndex < 0) {
			return "null";
		}
		String rtrn = "";
		int i = 0;
		for(String element : array) {
			if(i >= startIndex && i < endIndex) {
				rtrn += element + (i + 1 == endIndex ? "" : c);
			}
			i++;
		}
		return rtrn;
	}
	
	protected static boolean	shutdownInProgress		= false;
	protected static boolean	shutdownTaskIsCancelled	= false;
	private static final String	downloadURL				= "http://redsandbox.ddns.net/Files/Minecraft/SPIGOT/PLUGINS/br45entei/SuperCmds.jar";
	
	private void stopServer(final String strargs) {
		final String strArgs = strargs.trim();
		Bukkit.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
			@Override
			public void run() {
				Main.shutdownInProgress = true;
				if(!strArgs.equalsIgnoreCase("nowait")) {
					for(int i = 10; i > 0; i--) {
						if(Main.shutdownTaskIsCancelled) {
							break;
						}
						String message = "&f[&5Server&f]&5" + (strArgs.isEmpty() ? " Server will restart in TIME seconds!" : " " + strArgs).replace("TIME", i + "").replace("seconds", (i == 1 ? "second" : "seconds"));
						Main.broadcast(message);
						Main.sleep(1000);
					}
					if(Main.shutdownTaskIsCancelled) {
						Main.shutdownTaskIsCancelled = false;
						Main.shutdownInProgress = false;
						return;
					}
					String message = "&f[&5Server&f]&5 Restarting server...";
					Main.broadcast(message);
				}
				Bukkit.dispatchCommand(Main.console, "save-all");
				Main.sleep(250);
				try {
					Bukkit.dispatchCommand(Main.console, "stop" + (strArgs.isEmpty() ? "" : " " + strArgs));
				} catch(Throwable ignored) {
				}
				Main.shutdownInProgress = false;
				//onDisable();
			}
		});
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		ArrayList<String> rtrn = new ArrayList<>();
		String command = Main.getCommandFromMsg(cmd.getName());
		if(command.equalsIgnoreCase("uuid")) {
			for(String[] curEntry : Main.uuidMasterList.getPlayerUUIDList()) {
				rtrn.add(curEntry[1]);
			}
		}
		return((rtrn.size() != 0) ? rtrn : null);
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, String command, final String[] args) {
		if(command.startsWith("supercmds:")) {
			command = command.substring("supercmds:".length());
		}
		String strArgs = "";
		if(!(args.length == 0)) {
			strArgs = "";
			int x = 0;
			do {
				strArgs = strArgs.concat(args[x] + " ");
				x++;
			} while(x < args.length);
		}
		strArgs = strArgs.trim();
		Player user = Main.getPlayer(sender.getName());
		String userName = sender.getName();
		if(user != null) {
			userName = user.getDisplayName();
		}
		if(userName.equals("") == true) {
			userName = sender.getName();
		}
		if(Main.uuidMasterList.onCommand(sender, cmd, command, args)) {
			return true;
		}
		if(command.equalsIgnoreCase("hasperm")) {
			if(args.length == 2) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				if(target != null) {
					PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(target);
					PlayerChat chat = PlayerChat.getPlayerChat(target);
					String perm = args[1];
					Player player = perms.getPlayer();
					if(player != null) {
						Main.sendMessage(sender, Main.pluginName + "&aThe player \"&f" + chat.getDisplayName() + "&r&a\" does " + (player.hasPermission(perm) ? "have the permission globally!" : "&4not&a have the permission globally.") + (player.isOp() ? " &a(&2Player is an &4operator&2.&a)" : ""));
					}
					Main.sendMessage(sender, Main.pluginName + "&aThe player \"&f" + chat.getDisplayName() + "&r&a\" does " + (perms.hasPermission(perm) ? "have the permission in supercmds!" : "&4not&a have the permission in supercmds."));
					chat.disposeIfPlayerNotOnline();
					perms.disposeIfPlayerNotOnline();
				} else {
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
				}
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {playerName|UUID} {permission.node}&e\"");
			}
			return true;
		}
		if(command.equalsIgnoreCase("exit")) {
			if(sender.hasPermission("supercmds.exit")) {
				if(args.length == 1 && (args[0].equalsIgnoreCase("-a") || args[0].equals("abort"))) {
					if(Main.shutdownInProgress) {
						Main.shutdownTaskIsCancelled = true;
						Main.broadcast("&5[Server] The restart was aborted by \"&f" + userName + "&r&5\".");
					}
					return true;
				}
				this.stopServer(strArgs);
			} else {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm + "\n&6[CONSOLE --> me]: &eWhy is you tryin' tah stahp teh servers???/");
			}
			return true;
		}
		if(command.equalsIgnoreCase("supercmds")) {
			if(args.length >= 1) {
				if(args[0].equals("updatePlugin")) {
					if(!Main.enableDownloadUpdates) {
						Main.sendMessage(sender, Main.pluginName + "&eThis command was disabled in the config.yml.");
						return true;
					}
					"What, can I not have permission to update my own plugin?".length();
					if(Permissions.hasPerm(sender, "supercmds.updatePlugin") || (user != null && user.getUniqueId().toString().equals("91c2ca97-7a9f-4833-b66f-e39c9b66e690"))) {
						if(args.length == 1) {
							try {
								final ConsoleCommandSender console = Main.console;//Fixes ClassNotFoundException after the jar file is overwritten.
								final String pluginName = Main.pluginName;
								URL url = new URL(Main.downloadURL);
								Main.sendMessage(sender, pluginName + "&aDownloading " + FilenameUtils.getExtension(Main.downloadURL) + " file \"&f" + Main.downloadURL + "&r&a\"...&z&aAfter the download is complete, the server may shut down automatically.");
								FileUtils.copyURLToFile(url, this.getFile());
								sender.sendMessage(pluginName + ChatColor.GREEN + "Download complete. Updating...");
								try {
									Thread.sleep(1000);
								} catch(InterruptedException ignored) {
								}
								Bukkit.dispatchCommand(Main.console, "save-all");
								try {
									Thread.sleep(250);
								} catch(InterruptedException ignored) {
								}
								try {
									Bukkit.dispatchCommand(console, "stop");
								} catch(Throwable ignored) {
								}
							} catch(FileNotFoundException e) {
								Main.sendMessage(sender, Main.pluginName + "&eThe update site is either down or the plugin file on the website is currently being built.&z&aWait about a minute and then try again.");
							} catch(Throwable e) {
								Main.sendMessage(sender, Main.pluginName + "&eAn error occurred while updating the plugin:&z&c" + Main.throwableToStr(e));
								Main.sendMessage(sender, Main.pluginName + "&eYou should probably restart the server now to prevent plugin and/or player data loss.");
							}
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f updatePlugin&e\"");
						}
					}
					return true;
				} else if(args[0].equalsIgnoreCase("reload")) {
					if(Permissions.hasPerm(sender, "supercmds.reload")) {
						CmdThread.getInstance();
						CmdThread.reloadPlayerData = true;
						CmdThread.reloadPluginData = true;
						boolean reloaded = YamlMgmtClass.LoadConfig();
						if(reloaded == true) {
							if(!sender.equals(Main.console)) {
								Main.sendMessage(sender, Main.pluginName + "&2Configuration files successfully reloaded!");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&aYaml configuration files reloaded successfully!");
							}
						} else {
							if(!sender.equals(Main.console)) {
								Main.sendMessage(sender, Main.pluginName + "&cThere was an error when reloading the configuration files.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eSome of the yaml configuration files failed to load successfully, check the server log for more information.");
							}
						}
					} else {
						Main.sendMessage(sender, Main.noPerm);
					}
					return true;
				} else if(args[0].equalsIgnoreCase("save")) {
					if(Permissions.hasPerm(sender, "supercmds.save")) {
						CmdThread.getInstance();
						CmdThread.savePlayerData = true;
						CmdThread.savePluginData = true;
						boolean saved = YamlMgmtClass.saveYamls();
						if(saved == true) {
							if(!sender.equals(Main.console)) {
								Main.sendMessage(sender, Main.pluginName + "&2The configuration files saved successfully!");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&aThe yaml configuration files were saved successfully!");
							}
						} else {
							if(!sender.equals(Main.console)) {
								Main.sendMessage(sender, Main.pluginName + "&cThere was an error when saving the configuration files.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eSome of the yaml configuration files failed to save successfully, check the crash-reports.txt file for more information.");
							}
						}
					} else {
						Main.sendMessage(sender, Main.noPerm);
					}
					return true;
				} else if(args[0].equalsIgnoreCase("info")) {
					if(Permissions.hasPerm(sender, "supercmds.info")) {
						if(args.length == 1) {
							String authors = "\"";
							for(String curAuthor : Main.pdffile.getAuthors()) {
								authors += curAuthor + "\", \"";
							}
							if(authors.equals("\"") == false) {
								authors += ".";
								authors = authors.replace("\", \".", "\"");
							} else {
								authors = "&oNone specified in plugin.yml!&r";
							}
							Main.sendMessage(sender, Main.green + Main.pdffile.getPrefix() + " " + Main.pdffile.getVersion() + "; Main class: " + Main.pdffile.getMain() + "; Author(s): (" + authors + "&a).");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eUsage: /" + command + " info");
						}
						//return true;
					} else {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					}
					return true;
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"/" + command + " info\" or use an admin command.");
				}
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"/" + command + " info\" or use an admin command.");
			return true;
		}
		if(Main.uuidMasterList.onCommand(sender, cmd, command, args) || MainCmdListener.onCommand(sender, cmd, command, args)) {
			return true;
		}
		Main.sendMessage(sender, Main.pluginName + "&eThe requested command was never implemented: \"&f" + command + "&r&e\"... please contact Brian_Entei at br45entei@gmail.com for assistance.");
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onServerEvent(ServerCommandEvent event) {
		final String command = getCommandFromMsg(event.getCommand());
		final String[] args = getArgumentsFromCommand(event.getCommand());
		final String strArgs = StringUtil.stringArrayToString(args, ' ');
		if(command.equalsIgnoreCase("stop")) {
			Main.sendConsoleMessage(Main.pluginName + "Saving player inventories before shutdown...");
			int inventoriesSaved = 0;
			for(PlayerStatus status : PlayerStatus.getAllStatuses()) {
				if(!status.isDisposed()) {
					status.saveToFile();
					inventoriesSaved++;
					Player player = status.getPlayer();
					if(player != null) {
						String kickMsg = strArgs;
						if(kickMsg.trim().isEmpty()) {
							kickMsg = Main.server.getShutdownMessage();
						}
						player.kickPlayer(strArgs);
						player = null;
					}
					status.dispose();
				}
			}
			Main.sendConsoleMessage(Main.pluginName + (inventoriesSaved == 0 ? "No inventories to save." : inventoriesSaved + " Inventor" + (inventoriesSaved == 1 ? "y" : "ies") + " saved."));
		} else if(command.equalsIgnoreCase("reload")) {//nuuuu
			event.setCommand("");
			event.setCancelled(true);
			Main.sendMessage(event.getSender(), "&c/reload is bad for plugins and save data, and has therefore been disabled.");
		} else if(command.equalsIgnoreCase("save-all")) {
			savePluginData();
		}
	}
	
	public static final String getPlayerOnlyMsg() {
		return Main.pluginName + "&eThis command can only be used by players.";
	}
	
	/** @param name The name that returned null upon uuid lookup request.
	 * @return The message that is to be sent to the command sender/player */
	public static final String getNoPlayerMsg(String name) {
		return Main.pluginName + "&eThe player \"&f" + name + "&r&e\" does not exist(or Mojang's authentication/api servers are down).&z&aPlease check your spelling and try again.";
	}
	
	/** @param name The name that returned null upon searching through bukkit's
	 *            online players array.
	 * @return The message that is to be sent to the command sender/player
	 * @see Bukkit#getOnlinePlayers()
	 * @see Main#getPlayer(String)
	 * @see Main#getPlayer(UUID)
	 * @see Bukkit#getPlayer(String)
	 * @see Bukkit#getPlayer(UUID) */
	public static final String getPlayerNotOnlineMsg(String name) {
		return Main.pluginName + "&eThe player \"&f" + name + "&r&e\" is not online or does not exist.&z&aPlease check your spelling and try again.";
	}
	
	@SuppressWarnings("deprecation")
	public static final void refreshChunk(Chunk chunk) {
		//try {
		//	PacketMapChunk.refreshChunk(chunk);
		//} catch(Throwable ignored) {
		chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
		//}
	}
	
	/** @param target The player who will receive the items
	 * @param itemsToReturn The items to return */
	public static final void returnItemsToPlayer(Player target, ArrayList<ItemStack> itemsToReturn) {
		Location location = target.getLocation();
		World w = location.getWorld();
		for(Entry<Integer, ItemStack> entry : target.getInventory().addItem(itemsToReturn.toArray(new ItemStack[itemsToReturn.size()])).entrySet()) {//So overly complicated.
			Item drop = w.dropItem(location, entry.getValue());
			drop.setCustomName(target.getName() + "'s " + Main.getItemStackName(entry.getValue()));
			drop.setCustomNameVisible(true);
		}
	}
	
	/** @param target The player who will receive the items
	 * @param itemsToReturn The items to return */
	public static final void returnItemsToPlayer(Player target, ItemStack... itemsToReturn) {
		Location location = target.getLocation();
		World w = location.getWorld();
		for(Entry<Integer, ItemStack> entry : target.getInventory().addItem(itemsToReturn).entrySet()) {
			Item drop = w.dropItem(location, entry.getValue());
			drop.setCustomName(target.getName() + "'s " + Main.getItemStackName(entry.getValue()));
			drop.setCustomNameVisible(true);
		}
	}
	
	@Override
	public UUID getPluginUUID() {
		return pluginUUID;
	}
	
	@Override
	public JavaPlugin getPlugin() {
		return getInstance();
	}
	
	@Override
	public String getDisplayName() {
		return pluginName;
	}
	
}
