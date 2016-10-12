package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.util.Vector3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public abstract class SavablePlayerData extends AbstractPlayerJoinQuitClass {
	/** An ArrayList containing ALL of the currently instantiated
	 * SavablePlayerData objects that have not been disposed. */
	public static final ArrayList<SavablePlayerData>	instances					= new ArrayList<>();
	/** The file extension to use when creating, loading from, or saving to a
	 * new
	 * file. */
	public static final String							fileExt						= ".yml";
	
	public boolean										saveAndLoadWithSuperCmds	= true;
	
	/** @return */
	public static final ArrayList<SavablePlayerData> getAllInstances() {
		return new ArrayList<>(SavablePlayerData.instances);
	}
	
	protected boolean			hasRunPlayerJoinEvt	= false;
	protected boolean			hasRunPlayerQuitEvt	= false;
	
	protected boolean			isLoadedFromFile	= false;
	
	/** The Universally Unique Identifier of this player. Should always match up
	 * with the UUID returned from {@link Player#getUniqueId()}. */
	public final UUID			uuid;
	/** The last known username that this player is using. */
	public String				name;
	
	private boolean				isDisposed			= false;
	
	protected YamlConfiguration	config;
	
	/** @return An array of File objects(folders in this case) used for backing
	 *         up
	 *         configuration files. */
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
	
	public final File getSaveFileNoCreate() {
		return new File(this.getSaveFolder(), this.uuid.toString() + SavablePlayerData.fileExt);
	}
	
	/** @return The file that will be used to save/load this player's data. */
	public File getSaveFile() {
		File file = this.getSaveFileNoCreate();
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
	
	public final ConfigurationSection getRootDataSection() {
		if(this.config == null) {
			return null;
		}
		ConfigurationSection memSection = this.config.getConfigurationSection("data");
		if(memSection == null) {
			Main.sendConsoleMessage(Main.pluginName + "&eCreating " + this.getSaveFolderName() + " file for player \"&f" + this.name + "&r&a\"...");
			memSection = this.config.createSection("data");
			if(this.uuid != null) {
				memSection.set("uuid", this.uuid.toString());
			}
			if(this.name == null || this.name.isEmpty()) {
				this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
			}
			memSection.set("name", this.name);
			this.onFirstLoad();
			this.firstLoadWasRun = true;
			this.isLoadedFromFile = true;
			this.saveToFile();
		}
		return memSection;
	}
	
	/** Loads saved player data from file.
	 * 
	 * @return Whether or not the data was successfully loaded from file. */
	public boolean loadFromFile() {
		this.isLoadedFromFile = false;
		this.config = SavablePlayerData.getConfig();
		File file = this.getSaveFile();
		try {
			this.config.load(file);
			ConfigurationSection memSection = this.getRootDataSection();
			if(this.isLoadedFromFile) {
				return true;
			}
			Main.DEBUG("Loading " + this.getSaveFolderName() + " file for player \"&f" + this.name + "&r&a\"...");
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
	public boolean saveToFile() {
		if(!this.isLoadedFromFile()) {
			return false;
		}
		if(this.name == null || this.name.isEmpty()) {
			this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
		}
		if(!Main.isLoaded) {
			Main.DEBUG("Saving " + this.getSaveFolderName() + " file for player \"&f" + this.name + "&r&a\"...");
		}
		final File file = this.getSaveFile();
		this.config = SavablePlayerData.getConfig();
		ConfigurationSection memSection = this.config.getConfigurationSection("data");
		if(memSection == null) {
			memSection = this.config.createSection("data");
		}
		memSection.set("uuid", this.uuid.toString());
		memSection.set("name", this.name);
		this.saveToConfig(memSection);
		new Thread(new Runnable() {
			@Override
			public final void run() {
				try {
					SavablePlayerData.this.config.save(file);
				} catch(IOException e) {
					Main.sendConsoleMessage(Main.pluginName + "&eUnable to save data to " + SavablePlayerData.this.getSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
				}
			}
		}).start();
		/*try {
			this.config.save(file);
			return true;
		} catch(IOException e) {
			Main.sendConsoleMessage(Main.pluginName + "&eUnable to save data to " + this.getSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
			return false;
		}*/
		return true;
	}
	
	protected volatile boolean firstLoadWasRun = false;
	
	/** Run when this data attempts to load from file, only to find that the
	 * save
	 * file doesn't exist yet. */
	public abstract void onFirstLoad();
	
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
	
	/** @return This SavablePlayerData's associated player
	 * @see Server#getPlayer(UUID) */
	public Player getPlayer() {
		return Main.server.getPlayer(this.uuid);
	}
	
	/** @return Whether or not {@link SavablePlayerData#getPlayer()} returns a
	 *         player object. */
	public boolean isPlayerOnline() {
		return this.getPlayer() != null;
	}
	
	/** Saves player data to file and disposes of this SavablePlayerData object
	 * regardless of whether or not the player is online. Should only be used if
	 * the player is not online. If this SavablePlayerData object is already
	 * disposed, this does nothing. */
	public final void saveToFileAndDispose() {
		if(this.isDisposed()) {
			return;
		}
		this.saveToFile();
		this.dispose();
	}
	
	/** Saves player data to file and disposes of this SavablePlayerData object
	 * if the player is not online.
	 * 
	 * @return Whether or not this method successfully saved data to file.
	 * @see SavablePlayerData#dispose()
	 * @see SavablePlayerData#disposeIfPlayerNotOnline()
	 * @see SavablePlayerData#isDisposed() */
	public final boolean saveAndDisposeIfPlayerNotOnline() {
		if(this.isDisposed()) {
			return false;
		}
		boolean saved = this.saveToFile();
		this.disposeIfPlayerNotOnline();
		return saved;
	}
	
	/** Disposes of this SavablePlayerData object if its player is not online.
	 * No
	 * data is saved to file.
	 * 
	 * @return True if this method disposed of this SavablePlayerData object
	 * @see SavablePlayerData#saveAndDisposeIfPlayerNotOnline()
	 * @see SavablePlayerData#dispose()
	 * @see SavablePlayerData#isDisposed() */
	public boolean disposeIfPlayerNotOnline() {
		if(this.isDisposed() || this.isPlayerOnline()) {
			return false;
		}
		this.dispose();
		return true;
	}
	
	/** @return The name of this player.
	 * @see SavablePlayerData#name
	 * @see SavablePlayerData#isPlayerOnline()
	 * @see SavablePlayerData#name */
	public final String getPlayerName() {
		return(this.isPlayerOnline() ? this.getPlayer().getName() : this.name);
	}
	
	/** @return The display name of this player if they are online, or their
	 *         last
	 *         known name otherwise.
	 * @see SavablePlayerData#name
	 * @see SavablePlayerData#isPlayerOnline() */
	public final String getPlayerDisplayName() {
		return(this.isPlayerOnline() ? this.getPlayer().getDisplayName() : this.name);
	}
	
	/** Updates the reference to this player's name
	 * 
	 * @see SavablePlayerData#name */
	public final void updateName() {
		if(!this.isPlayerOnline()) {
			this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
		} else {
			this.name = this.getPlayer().getName();
		}
	}
	
	/** @return This player's nick name.
	 * @see PlayerChat#getNickName() */
	public final String getPlayerNickName() {
		this.updateName();
		PlayerChat chat = PlayerChat.getPlayerChat(this.uuid);
		String nickname = chat.getNickName();
		chat.disposeIfPlayerNotOnline();
		return nickname;
	}
	
	public static final String getWorldFromSavedLocation(String name, ConfigurationSection mem) {
		ConfigurationSection root = mem.getConfigurationSection(name);
		if(root == null) {
			return null;
		}
		try {
			return root.getString("world");
		} catch(Throwable e) {
			Main.DEBUG("&eFailed to load vector variable from location variable \"&f" + name + "&r&e\" into memory:\n&c" + Main.throwableToStr(e));
			return null;//Main.server.getWorlds().get(0).getSpawnLocation();
		}
	}
	
	public static final Vector3 getVector3FromSavedLocation(String name, ConfigurationSection mem) {
		ConfigurationSection root = mem.getConfigurationSection(name);
		if(root == null) {
			return null;
		}
		try {
			Vector3 vector = new Vector3(root.getDouble("x"), root.getDouble("y"), root.getDouble("z"));
			return vector;
		} catch(Throwable e) {
			Main.DEBUG("&eFailed to load vector variable from location variable \"&f" + name + "&r&e\" into memory:\n&c" + Main.throwableToStr(e));
			return null;//Main.server.getWorlds().get(0).getSpawnLocation();
		}
	}
	
	/** @param name The name to use when loading the location
	 * @param mem The {@link ConfigurationSection} to load from
	 * @return The resulting {@link Location} or null if loading failed. */
	public static final Location getLocationFromConfig(String name, ConfigurationSection mem) {
		ConfigurationSection root = mem.getConfigurationSection(name);
		if(root == null) {
			return null;
		}
		try {
			World world;
			String loadWorld = root.getString("world");
			if(Main.isStringUUID(loadWorld)) {
				world = Main.server.getWorld(UUID.fromString(loadWorld));
			} else {
				world = Main.server.getWorld(root.getString("world"));
			}
			if(world == null) {
				return null;
			}
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
	
	/** @param name The name to use when saving the location
	 * @param loc The {@link Location} that will be saved
	 * @param mem The {@link ConfigurationSection} to save to
	 * @param saveWorldAsUUIDOrName If true, the world will be saved using its
	 *            {@link UUID}. If false, the world will be saved using its
	 *            name. */
	public static final void saveLocationToConfig(String name, Location loc, ConfigurationSection mem, boolean saveWorldAsUUIDOrName) {
		ConfigurationSection root = mem.getConfigurationSection(name);
		if(root == null) {
			root = mem.createSection(name);
		}
		if(loc != null && loc.getWorld() != null) {
			if(saveWorldAsUUIDOrName) {
				root.set("world", loc.getWorld().getUID().toString());
			} else {
				root.set("world", loc.getWorld().getName());
			}
			root.set("x", new Double(loc.getX()));
			root.set("y", new Double(loc.getY()));
			root.set("z", new Double(loc.getZ()));
			root.set("yaw", new Float(loc.getYaw()));
			root.set("pitch", new Float(loc.getPitch()));
		}
	}
	
	/** @param player1 The first player to check
	 * @param player2 The second player to check
	 * @return Whether or not the UUID from the first player matches the UUID
	 *         from the second player. */
	public static final boolean playerEquals(Player player1, Player player2) {
		if(player1 == null || player2 == null) {
			return false;
		}
		return player1.getUniqueId().toString().equals(player2.getUniqueId().toString());
	}
	
	/** Disposes of this SavablePlayerData object. No data is saved to file.
	 * 
	 * @see SavablePlayerData#saveAndDisposeIfPlayerNotOnline()
	 * @see SavablePlayerData#disposeIfPlayerNotOnline()
	 * @see SavablePlayerData#isDisposed() */
	public void dispose() {
		SavablePlayerData.instances.remove(this);
		this.isDisposed = true;
	}
	
	/** @return Whether or not this SavablePlayerData object has been disposed.
	 * @see SavablePlayerData#saveAndDisposeIfPlayerNotOnline()
	 * @see SavablePlayerData#dispose()
	 * @see SavablePlayerData#disposeIfPlayerNotOnline() */
	public final boolean isDisposed() {
		return this.isDisposed;
	}
	
}
