package com.gmail.br45entei.supercmds.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.gmail.br45entei.supercmds.Main;

/** @author Brian_Entei */
public abstract class SavablePlayerData extends AbstractPlayerJoinQuitClass {
	public static final ArrayList<SavablePlayerData>	instances			= new ArrayList<>();
	public static final String							fileExt				= ".yml";
	
	protected boolean									hasRunPlayerJoinEvt	= false;
	protected boolean									hasRunPlayerQuitEvt	= false;
	
	protected boolean									isLoadedFromFile	= false;
	
	public final UUID									uuid;
	public String										name;
	
	private boolean										isDisposed			= false;
	
	private YamlConfiguration							config;
	
	public static final File[] getStaticSaveFolders() {
		File folder1 = new File(Main.dataFolder, "PlayerCmdData");
		if(!folder1.exists()) {
			folder1.mkdirs();
		}
		File folder2 = new File(Main.dataFolder, "PlayerEcoData");
		if(!folder2.exists()) {
			folder2.mkdirs();
		}
		File folder3 = new File(Main.dataFolder, "PlayerChatData");
		if(!folder3.exists()) {
			folder3.mkdirs();
		}
		File folder4 = new File(Main.dataFolder, "PlayerPermissions");
		if(!folder4.exists()) {
			folder4.mkdirs();
		}
		File folder5 = new File(Main.dataFolder, "PlayerGroups");
		if(!folder5.exists()) {
			folder5.mkdirs();
		}
		return new File[] {folder1, folder2, folder3, folder4, folder5};
	}
	
	/** Create a new SavablePlayerData instance for this player.
	 * 
	 * @param player The player whose data we will be saving */
	public SavablePlayerData(Player player) {
		this(player.getUniqueId(), player.getName());
	}
	
	/** Create a new SavablePlayerData instance for the given UUID and name.
	 * 
	 * @param uuid The uuid of the player whose data we will be saving
	 * @param name The name of the player whose data we will be saving */
	public SavablePlayerData(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
		SavablePlayerData.instances.add(this);
		Main.server.getPluginManager().registerEvents(this, Main.getInstance());
	}
	
	/** The name of the folder in which player data will be saved should be set
	 * here.
	 * 
	 * @return The name of the folder in which player data will be saved. */
	public abstract String getSaveFolderName();
	
	/** Gets the folder in which player data will be saved.
	 * 
	 * @return The folder in which player data will be saved. */
	public File getSaveFolder() {
		File folder = new File(Main.dataFolder, this.getSaveFolderName());
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	public File getSaveFile() {
		File file = new File(this.getSaveFolder(), this.uuid.toString() + SavablePlayerData.fileExt);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Throwable e) {
				Main.sendConsoleMessage(Main.pluginName + "&eUnable to create new " + this.getSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
			}
		}
		return file;
	}
	
	/** @return A new Yaml Configuration. */
	public static YamlConfiguration getConfig() {
		YamlConfiguration config = new YamlConfiguration();
		return config;
	}
	
	/** @return Whether or not the method loadFromFile() was executed and
	 *         completed successfully. */
	public final boolean isLoadedFromFile() {
		return this.isLoadedFromFile;
	}
	
	/** Loads saved player data from file.
	 * 
	 * @return Whether or not the data was successfully loaded from file. */
	public boolean loadFromFile() {
		this.config = SavablePlayerData.getConfig();
		File file = this.getSaveFile();
		try {
			this.config.load(file);
			ConfigurationSection memSection = this.config.getConfigurationSection("data");
			if(memSection == null) {
				Main.sendConsoleMessage(Main.pluginName + "&eCreating " + this.getSaveFolderName() + " file for player \"&f" + this.name + "&r&a\"...");
				memSection = this.config.createSection("data");
				memSection.set("uuid", this.uuid.toString());
				if(this.name == null || this.name.isEmpty()) {
					this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
				}
				memSection.set("name", this.name);
				this.saveToFile();
				return true;
			}
			Main.sendConsoleMessage(Main.pluginName + "&aLoading " + this.getSaveFolderName() + " file for player \"&f" + this.name + "&r&a\"...");
			this.name = memSection.getString("name");
			if(this.name == null || this.name.isEmpty()) {
				this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
			}
			this.loadFromConfig(memSection);
			this.isLoadedFromFile = true;
			return true;
		} catch(Throwable e) {
			Main.sendConsoleMessage(Main.pluginName + "&eUnable to load data from " + this.getSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
			return false;
		}
	}
	
	/** Saves player data to file from memory.
	 * 
	 * @return Whether or not the data was successfully saved to file. */
	//Lol that typo XD
	public boolean saveToFile() {
		Main.sendConsoleMessage(Main.pluginName + "&aSaving " + this.getSaveFolderName() + " file for player \"&f" + this.name + "&r&a\"...");
		File file = this.getSaveFile();
		this.config = SavablePlayerData.getConfig();
		ConfigurationSection memSection = this.config.getConfigurationSection("data");
		if(memSection == null) {
			memSection = this.config.createSection("data");
		}
		memSection.set("uuid", this.uuid.toString());
		if(this.name == null || this.name.isEmpty()) {
			this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
		}
		memSection.set("name", this.name);
		this.saveToConfig(memSection);
		try {
			this.config.save(file);
			return true;
		} catch(IOException e) {
			Main.sendConsoleMessage(Main.pluginName + "&eUnable to save data to " + this.getSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
			return false;
		}
	}
	
	/** Here is where you read custom variables from the configuration and store
	 * them for use later in your class.
	 * 
	 * @param mem The configuration that you should read from */
	public abstract void loadFromConfig(ConfigurationSection mem);
	
	/** Here is where you take custom variables from your class and store them
	 * into the configuration for saving.
	 * 
	 * @param mem The configuration that you should save to */
	public abstract void saveToConfig(ConfigurationSection mem);
	
	//==========================================================================
	
	public Player getPlayer() {
		return Main.server.getPlayer(this.uuid);
	}
	
	public boolean isPlayerOnline() {
		return this.getPlayer() != null;
	}
	
	public final boolean saveAndDisposeIfPlayerNotOnline() {
		if(this.isDisposed()) {
			return false;
		}
		boolean saved = this.saveToFile();
		if(!this.isPlayerOnline()) {
			this.dispose();
		}
		return saved;
	}
	
	public final boolean disposeIfPlayerNotOnline() {
		if(this.isDisposed() || this.isPlayerOnline()) {
			return false;
		}
		this.dispose();
		return true;
	}
	
	public final String getPlayerDisplayName() {
		return(this.isPlayerOnline() ? this.getPlayer().getDisplayName() : this.name);
	}
	
	public static final Location getLocationFromConfig(String name, ConfigurationSection mem) {
		ConfigurationSection root = mem.getConfigurationSection(name);
		if(root == null) {
			return null;
		}
		try {
			World world = Main.server.getWorld(UUID.fromString(root.getString("world")));
			double x = root.getDouble("x");
			double y = root.getDouble("y");
			double z = root.getDouble("z");
			float yaw = new Double(root.getDouble("yaw")).floatValue();
			float pitch = new Double(root.getDouble("pitch")).floatValue();
			Location loc = new Location(world, x, y, z);
			loc.setYaw(yaw);
			loc.setPitch(pitch);
			return loc;
		} catch(Throwable e) {
			Main.DEBUG("&eFailed to load location variable \"&f" + name + "&r&e\" into memory:\n&c" + Main.throwableToStr(e));
			return null;//Main.server.getWorlds().get(0).getSpawnLocation();
		}
	}
	
	public static final void saveLocationToConfig(String name, Location loc, ConfigurationSection mem) {
		ConfigurationSection root = mem.getConfigurationSection(name);
		if(root == null) {
			root = mem.createSection(name);
		}
		if(loc != null && loc.getWorld() != null) {
			root.set("world", loc.getWorld().getUID().toString());
			root.set("x", new Double(loc.getX()));
			root.set("y", new Double(loc.getY()));
			root.set("z", new Double(loc.getZ()));
			root.set("yaw", new Float(loc.getYaw()));
			root.set("pitch", new Float(loc.getPitch()));
		}
	}
	
	public static final boolean playerEquals(Player player1, Player player2) {
		if(player1 == null || player2 == null) {
			return false;
		}
		return player1.getUniqueId().toString().equals(player2.getUniqueId().toString());
	}
	
	public void dispose() {
		SavablePlayerData.instances.remove(this);
		this.isDisposed = true;
	}
	
	public final boolean isDisposed() {
		return this.isDisposed;
	}
	
}
