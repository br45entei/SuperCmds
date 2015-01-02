package com.gmail.br45entei.supercmds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.br45entei.supercmds.api.Permissions;
import com.gmail.br45entei.supercmds.yml.YamlMgmtClass;

/** @author Brian_Entei */
public class UUIDMasterList implements Listener {
	
	public void DEBUG(String str) {
		if(this.showDebugMsgs) {
			Main.sendConsoleMessage(this.pluginName + Main.formatColorCodes("&eDebug: " + str));
		}
	}
	
	public final String					pluginName			= Main.white + "[" + Main.gold + "UUID Master List" + Main.white + "] ";
	public String						dataFolderName		= "";
	public File							dataFolder			= null;
	public boolean						enabled				= false;
	
	public boolean						showDebugMsgs;
	public String						configVersion		= "";
	
	public boolean						YamlsAreLoaded		= false;
	public FileConfiguration			config;
	public File							configFile			= null;
	public String						configFileName		= "uuidConfig.yml";
	
	public FileConfiguration			uuidConfig;
	public File							uuidConfigFile		= null;
	public String						uuidConfigFileName	= "UUID_MASTER_LIST.yml";
	
	private final ArrayList<String[]>	uuidMasterList		= new ArrayList<>();
	
	public ArrayList<String[]> getPlayerUUIDList() {
		return this.uuidMasterList;
	}
	
	public ArrayList<UUID> getAllListedUUIDS() {
		ArrayList<UUID> rtrn = new ArrayList<>();
		for(String[] entry : this.uuidMasterList) {
			rtrn.add(UUID.fromString(entry[0]));
		}
		return rtrn;
	}
	
	private void printMasterListToScreen() {
		int index = 0;
		for(String[] entry : this.uuidMasterList) {
			Main.sendConsoleMessage(this.pluginName + "&a[" + index + "/" + (this.uuidMasterList.size() - 1) + "]:&z&auuid: \"&f" + entry[0] + "&r&a\";&z&aname: \"&f" + entry[1] + "&r&a\"");
		}
		index++;
	}
	
	private boolean loadMasterListFromConfig() {
		boolean success = false;
		ConfigurationSection section = this.uuidConfig.getConfigurationSection("UUID_LIST");
		if(section == null) {
			section = this.uuidConfig.createSection("UUID_LIST");
		}
		Map<String, Object> configList = section.getValues(false);
		for(Map.Entry<String, Object> entry : configList.entrySet()) {
			String uuid = entry.getKey().replaceAll("_", "-");//UUID
			String name = (String) entry.getValue();//String
			this.uuidMasterList.add(new String[] {uuid, name});
		}
		return success;
	}
	
	private boolean saveMasterListToConfig() {
		this.DEBUG("&0_____&cpublic boolean &6saveMasterListToConfig&f() {");
		boolean success = true;
		ConfigurationSection section = this.uuidConfig.getConfigurationSection("UUID_LIST");
		if(section == null) {
			section = this.uuidConfig.createSection("UUID_LIST");
		}
		try {
			for(String[] curEntry : this.uuidMasterList) {
				String uuid = curEntry[0].replaceAll("-", "_");
				String name = curEntry[1];
				MemorySection.createPath(section, uuid);
				this.uuidConfig.set("UUID_LIST." + uuid, name);
			}
			this.uuidConfig.save(this.uuidConfigFile);
		} catch(Exception e) {
			e.printStackTrace();
			success = false;
		}
		this.DEBUG("&0_____&f}");
		return success;
	}
	
	public String getPlayerNameFromUUID(UUID uuid) {
		if(uuid == null) {
			return "";
		}
		return this.getPlayerNameFromUUID(uuid.toString());
	}
	
	public String getPlayerNameFromUUID(String uuid) {
		int uuidIndexInList = this.getUUIDIndexInUUIDList(uuid);
		if(uuidIndexInList != -1) {
			String[] entry = this.uuidMasterList.get(uuidIndexInList);
			if(entry != null) {
				return entry[1];
			}
		}
		return "";
	}
	
	public int getUUIDIndexInUUIDList(UUID uuid) {
		return this.getUUIDIndexInUUIDList(uuid.toString());
	}
	
	public int getUUIDIndexInUUIDList(String uuid) {
		int index = 0;
		for(String[] curEntry : this.uuidMasterList) {
			if(curEntry[0].equals(uuid)) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
	public int getPlayerIndexInUUIDList(Player player) {
		int index = 0;
		for(String[] curEntry : this.uuidMasterList) {
			if(curEntry[0].equals(player.getUniqueId().toString())) {
				return index;
			}
			index++;
		}
		//The player hasn't been added yet, let's add them now:
		String[] newEntry = new String[] {player.getUniqueId().toString(), player.getName()};
		this.uuidMasterList.add(newEntry);
		return this.uuidMasterList.size() - 1;//Returns the most recently added entry by index.
		//return -1;
	}
	
	private void updatePlayerNameInUUIDList(Player player) {
		String[] newEntry = new String[] {player.getUniqueId().toString(), player.getName()};
		int playerIndexInList = this.getPlayerIndexInUUIDList(player);
		if(playerIndexInList != -1) {
			this.uuidMasterList.set(playerIndexInList, newEntry);
		} else if(Bukkit.getServer().getOnlineMode()) {
			this.uuidMasterList.add(newEntry);
		}
	}
	
	public String getPlayerNameFromUUIDList(Player player) {
		int playerIndexInList = this.getPlayerIndexInUUIDList(player);
		String[] playerEntry = this.uuidMasterList.get(playerIndexInList);
		return playerEntry[1];
	}
	
	public static final boolean isStrUUID(String str) {
		try {
			UUID.fromString(str);
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	public UUID getUUIDFromPlayerName(String playerName) {
		if(UUIDMasterList.isStrUUID(playerName)) {
			for(String[] curEntry : this.uuidMasterList) {
				if(curEntry[0].equals(playerName)) {
					return UUID.fromString(playerName);
				}
			}
		}
		for(String[] curEntry : this.uuidMasterList) {
			if(curEntry[1].equalsIgnoreCase(playerName)) {
				return UUID.fromString(curEntry[0]);
			}
		}
		try {
			List<String> names = new ArrayList<>();
			names.add(playerName);
			UUIDFetcher fetch = new UUIDFetcher(names);
			Map<String, UUID> uuidMap = fetch.call();
			UUID rtrn = null;
			for(Map.Entry<String, UUID> entry : uuidMap.entrySet()) {
				int index = this.getUUIDIndexInUUIDList(entry.getValue());
				String[] newEntry = new String[] {entry.getValue().toString(), entry.getKey()};
				if(index == -1) {
					this.uuidMasterList.add(newEntry);
				} else {
					this.uuidMasterList.set(index, newEntry);
				}
				Main.sendConsoleMessage(Main.pluginName + "&aDownloaded and saved the UUID and username of a player who has never logged in:");
				Main.sendConsoleMessage(Main.pluginName + "&aUUID: \"&f" + newEntry[0] + "&r&a\"; Last Known Username: \"&f" + newEntry[1] + "&r&a\";!");
				rtrn = entry.getValue();
			}
			this.saveMasterListToConfig();
			return rtrn;
		} catch(Throwable ignored) {
		}
		return null;//uuid;
	}
	
	/** @param playerName
	 * @return The String UUID from the playername stored in the arraylist if
	 *         any, or an empty string(""). */
	public String getUUIDStringFromPlayerName(String playerName) {
		//int index = 0;
		for(String[] curEntry : this.uuidMasterList) {
			if(curEntry[1].equalsIgnoreCase(playerName)) {
				return curEntry[0];
			}
			//index++;
		}
		return "";
	}
	
	/** @param player
	 * @return The UUID listed in the database under the given player's name if
	 *         any, otherwise the player's UUID. */
	public String getWhatUUIDToUseForPlayer(Player player) {
		String uuid = this.getUUIDStringFromPlayerName(player.getName());
		if(uuid.isEmpty() == false) {
			return uuid;
		}
		return player.getUniqueId().toString();
	}
	
	public ArrayList<String> getAllOfflinePlayerNamesFromList() {
		ArrayList<String> rtrn = new ArrayList<>();
		for(String[] curEntry : this.uuidMasterList) {
			String playerName = curEntry[1];
			if(Main.getPlayer(curEntry[1]) == null) {
				rtrn.add(playerName);
			}
		}
		return rtrn;
	}
	
	public ArrayList<String> getAllOnlinePlayerNamesFromList() {
		ArrayList<String> rtrn = new ArrayList<>();
		for(String[] curEntry : this.uuidMasterList) {
			String playerName = curEntry[1];
			if(Main.getPlayer(curEntry[1]) != null) {
				rtrn.add(playerName);
			}
		}
		return rtrn;
	}
	
	public UUIDMasterList() {
	}
	
	public void onEnable() {
		this.dataFolder = new File(Main.getInstance().getDataFolder().getParentFile(), "UUIDMasterList");
		if(!(this.dataFolder.exists())) {
			this.dataFolder.mkdir();
		}
		try {
			this.dataFolderName = this.dataFolder.getAbsolutePath();
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		//loadconfig:
		boolean successfulLoad = this.LoadConfig();
		if(!successfulLoad) {
			Main.sendConsoleMessage(this.pluginName + "&eSomething went wrong when loading the configuration files! Please check through the server log for details.");
		}
		try {
			this.loadMasterListFromConfig();
		} catch(Exception e) {
			e.printStackTrace();
		}
		Main.sendConsoleMessage(this.pluginName + "&eVersion " + Main.pdffile.getVersion() + " is now enabled.");
		this.enabled = true;
	}
	
	public void onDisable() {
		this.saveMasterListToConfig();
		Main.sendConsoleMessage(this.pluginName + "&eVersion " + Main.pdffile.getVersion() + " is now disabled.");
		this.enabled = false;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent evt) {
		this.updatePlayerNameInUUIDList(evt.getPlayer());
		this.saveMasterListToConfig();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent evt) {
		this.updatePlayerNameInUUIDList(evt.getPlayer());
		this.saveMasterListToConfig();
	}
	
	public boolean LoadConfig() {
		this.configFile = new File(this.dataFolder, this.configFileName);
		this.config = new YamlConfiguration();
		this.uuidConfigFile = new File(this.dataFolder, this.uuidConfigFileName);
		this.uuidConfig = new YamlConfiguration();
		try {
			this.loadResourceFiles();
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.YamlsAreLoaded = this.reloadFiles(true);
		if(this.YamlsAreLoaded == true) {
			this.DEBUG(this.pluginName + "&aAll YAML Configration Files loaded successfully!");
		} else {
			Main.sendConsoleMessage(this.pluginName + "&cError: Some YAML Files failed to load successfully! Check the server log or \"" + this.dataFolderName + "\\crash-reports.txt\" to solve the problem.");
		}
		return this.YamlsAreLoaded;
	}
	
	private void loadResourceFiles() throws Exception {
		if(!this.configFile.exists()) {
			this.configFile.getParentFile().mkdirs();
			this.configFile = YamlMgmtClass.getResourceFromStreamAsFile(this.dataFolder, this.configFileName, true);
		}
		if(!this.uuidConfigFile.exists()) {
			this.uuidConfigFile.getParentFile().mkdirs();
			this.uuidConfigFile = YamlMgmtClass.getResourceFromStreamAsFile(this.dataFolder, this.uuidConfigFileName, true);
		}
	}
	
	private boolean reloadFiles(boolean ShowStatus) {
		this.YamlsAreLoaded = false;
		boolean loadedAllVars = false;
		String unloadedFiles = "\"";
		Exception e1 = null;
		try {
			this.config.load(this.configFile);
		} catch(Exception e) {
			e1 = e;
			unloadedFiles += this.configFileName + "\" ";
		}
		Exception e2 = null;
		try {
			this.uuidConfig.load(this.uuidConfigFile);
		} catch(Exception e) {
			e2 = e;
			unloadedFiles += this.uuidConfigFileName + "\" ";
		}
		try {
			if(unloadedFiles.equals("\"")) {
				this.YamlsAreLoaded = true;
				loadedAllVars = this.loadYamlVariables();
				if(loadedAllVars == true) {
					if(ShowStatus) {
						Main.sendConsoleMessage(this.pluginName + "&aAll of the yaml configuration files loaded successfully!");
					}
				} else {
					if(ShowStatus) {
						Main.sendConsoleMessage(this.pluginName + "&aSome of the settings did not load correctly from the configuration files! Check the server log to solve the problem.");
					}
				}
				return true;
			}
			String Causes = "";
			if(e1 != null) {
				Causes = Causes.concat(Causes + "\r" + e1.toString());
			}
			if(e2 != null) {
				Causes = Causes.concat(Causes + "\r" + e2.toString());
			}
			throw new Exception(Causes);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadYamlVariables() {
		boolean loadedAllVars = true;
		try {
			this.configVersion = Main.formatColorCodes(this.config.getString("version"));
			if(this.configVersion.equals(Main.pdffile.getVersion())) {
				this.DEBUG("&aThe " + this.configFileName + "'s version matches this plugin's version(&f" + Main.pdffile.getVersion() + "&a)!");
			} else {
				Main.sendConsoleMessage(this.pluginName + "&eThe " + this.configFileName + "'s version does NOT match this plugin's version(&f" + Main.pdffile.getVersion() + "&e)! Make sure that you update the " + this.configFileName + " from this plugin's latest version!");
			}
		} catch(Exception e) {
			Main.sendConsoleMessage(this.pluginName + "&eThe version in the config.yml was not set!");
			Main.sendConsoleMessage(this.pluginName + "&cInvalid configuration settings detected!");
			this.enabled = false;
			return false;
		}
		try {
			this.showDebugMsgs = (Main.forceDebugMsgs || (Boolean.valueOf(Main.formatColorCodes(Main.config.getString("showDebugMsgs")))).booleanValue() == true);
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("showDebugMsgs", "config.yml", Main.pluginName);
		}
		return loadedAllVars;
	}
	
	public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {
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
		Player user = null;
		if(sender instanceof Player) {
			user = Bukkit.getPlayer(((Player) sender).getUniqueId());
		}
		String userName = sender.getName();
		if(user != null) {
			userName = user.getDisplayName();
		}
		if(userName.equals("") == true) {
			userName = sender.getName();
		}
		if(command.equalsIgnoreCase("uuid") || command.equalsIgnoreCase("uuids")) {
			if(Permissions.hasPerm(sender, "uuid.cmd.use.uuid")) {
				if(args.length >= 1) {
					if(args[0].equalsIgnoreCase("printmasterlist") || args[0].equalsIgnoreCase("pml")) {
						if(Permissions.hasPerm(user, "uuid.cmd.use.uuid.pml")) {
							if(user != null) {
								Main.sendConsoleMessage(this.pluginName + "&ePlayer \"&f" + user.getName() + "&r&e\" initiated master list print to console. Here goes:");
								this.printMasterListToScreen();
								Main.sendMessage(user, this.pluginName + "&2Printed to console screen successfully, go and check the console.");
							} else {
								Main.sendConsoleMessage(this.pluginName + "&ePrinting master UUID list to screen:");
								this.printMasterListToScreen();
							}
						} else {
							Main.sendMessage(user, this.pluginName + Main.noPerm);
						}
						return true;
					} else if(args[0].equalsIgnoreCase("saveall")) {
						if(Permissions.hasPerm(sender, "uuid.cmd.use.uuid.saveall")) {
							if(user != null) {
								Main.sendConsoleMessage(this.pluginName + "&ePlayer \"&f" + user.getName() + "&r&e\" initiated master list save to file.");
								boolean successful = this.saveMasterListToConfig();
								Main.sendMessage(user, this.pluginName + (successful ? "&2Save was successful." : "&eSomething went wrong while saving to file! Check the server log for details."));
							} else {
								Main.sendConsoleMessage(this.pluginName + "&eSaving master UUID list to file...");
								boolean successful = this.saveMasterListToConfig();
								Main.sendConsoleMessage(this.pluginName + "&2Save complete.");
								Main.sendMessage(user, this.pluginName + (successful ? "&2Save was successful." : "&eSomething went wrong while saving to file! Check the server log for details."));
							}
						} else {
							Main.sendMessage(sender, this.pluginName + Main.noPerm);
						}
						return true;
					} else if(args[0].equalsIgnoreCase("uuid")) {
						if(Permissions.hasPerm(sender, "uuid.cmd.use.uuid.uuid")) {
							if(args.length == 2) {
								Player target = Main.getPlayer(args[1]);
								if(target != null) {
									Main.sendMessage(sender, this.pluginName + "&a\"&f" + target.getDisplayName() + "&r&a\"'s Universally Unique ID is: \"&f" + target.getUniqueId().toString() + "&r&a\"!");
								} else {
									UUID uuid = this.getUUIDFromPlayerName(args[1]);
									if(uuid != null) {
										Main.sendMessage(sender, this.pluginName + "&2[&6OFFLINE_PLAYER&2]: &a\"&f" + this.getPlayerNameFromUUID(uuid) + "&r&a\"'s Universally Unique ID is: \"&f" + this.getUUIDFromPlayerName(args[1]).toString() + "&r&a\"!");
										return true;
									}
									Main.sendMessage(sender, this.pluginName + "&cThe player \"&f" + args[0] + "&r&c\" does not exist(or Mojang's authentication/api servers are down)!");
									return true;
								}
							} else if(args.length == 1) {
								if(user != null) {
									Main.sendMessage(sender, this.pluginName + "&aYour Universally Unique ID is: \"&f" + user.getUniqueId().toString() + "&r&a\"!");
								} else {
									Main.sendMessage(sender, this.pluginName + "&eUsage: \"/" + command + " " + args[0] + "&r&e [playerName]\"; where \"&f[playerName]&r&e\" is required.");
								}
							} else {
								Main.sendMessage(sender, this.pluginName + "&eUsage: \"/" + command + " " + args[0] + "&r&e [playerName]\".");
							}
						} else {
							Main.sendMessage(sender, this.pluginName + Main.noPerm);
						}
					} else {
						Main.sendMessage(sender, this.pluginName + "&eUsage: \"/" + command + " [uuid|printmasterlist|pml|saveall]");
					}
				} else {
					Main.sendMessage(sender, this.pluginName + "&eUsage: \"/" + command + " [uuid|printmasterlist|pml|saveall]");
				}
			} else {
				Main.sendMessage(sender, this.pluginName + Main.noPerm);
			}
			return true;
		}
		return false;
	}
}
