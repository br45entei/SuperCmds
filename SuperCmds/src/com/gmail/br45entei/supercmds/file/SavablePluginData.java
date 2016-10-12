package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public abstract class SavablePluginData extends AbstractPlayerJoinQuitClass {
	public static final ArrayList<SavablePluginData>	instances					= new ArrayList<>();
	public static final String							fileExt						= ".yml";
	protected boolean									isLoadedFromFile			= false;
	public boolean										saveAndLoadWithSuperCmds	= true;
	
	public static final ArrayList<SavablePluginData> getAllInstances() {
		return SavablePluginData.instances;
	}
	
	public final String			name;
	private boolean				isDisposed	= false;
	
	protected YamlConfiguration	config;
	
	/** Create a new SavablePluginData instance. */
	public SavablePluginData(String name) {
		this.name = name;
		SavablePluginData.instances.add(this);
		Main.server.getPluginManager().registerEvents(this, Main.getInstance());
	}
	
	public final String getName() {
		return this.name;
	}
	
	/** The name of the folder in which plugin data will be saved should be set
	 * here.<br>
	 * Setting this to null will cause the plugin to use it's data folder
	 * instead.
	 * 
	 * @return The name of the folder in which plugin data will be saved. */
	public abstract String getSaveFolderName();
	
	/** Gets the folder in which plugin data will be saved.
	 * 
	 * @return The folder in which plugin data will be saved. */
	public File getSaveFolder() {
		if(this.getSaveFolderName() == null || this.getSaveFolderName().isEmpty()) {
			return Main.dataFolder;
		}
		File folder = new File(Main.dataFolder, this.getSaveFolderName());
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	public final File getSaveFile() {
		File file = new File(this.getSaveFolder(), this.name + SavablePluginData.fileExt);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Throwable e) {
				Main.sendConsoleMessage(Main.pluginName + "&eUnable to create new " + this.name + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
			}
		}
		return file;
	}
	
	/** @return A new Yaml Configuration. */
	public YamlConfiguration getConfig() {
		if(this.config == null) {
			this.config = new YamlConfiguration();
		}
		return this.config;
	}
	
	/** @return Whether or not the method loadFromFile() was executed and
	 *         completed successfully. */
	public final boolean isLoadedFromFile() {
		return this.isLoadedFromFile;
	}
	
	/** Loads saved plugin data from file.
	 * 
	 * @return Whether or not the data was successfully loaded from file. */
	public boolean loadFromFile() {
		this.isLoadedFromFile = false;
		this.config = null;
		this.getConfig();
		File file = this.getSaveFile();
		try {
			this.config.load(file);
			this.config.set("name", this.name);
			ConfigurationSection memSection = this.config.getConfigurationSection("data");
			if(memSection == null) {
				Main.sendConsoleMessage(Main.pluginName + "&eCreating " + this.name + " file for plugin data \"&f" + this.name + "&r&a\"...");
				memSection = this.config.createSection("data");
				this.saveToFile();
				this.isLoadedFromFile = true;
				return true;
			}
			Main.sendConsoleMessage(Main.pluginName + "&aLoading " + this.name + " file for plugin data \"&f" + this.name + "&r&a\"...");
			this.loadFromConfig(memSection);
			this.isLoadedFromFile = true;
			return true;
		} catch(Throwable e) {
			Main.sendConsoleMessage(Main.pluginName + "&eUnable to load data from " + this.name + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
			return false;
		}
	}
	
	/** Saves plugin data to file from memory.
	 * 
	 * @return Whether or nor the data was successfully saved to file. */
	public boolean saveToFile() {
		if(!Main.isLoaded) {
			return false;
		}
		Main.sendConsoleMessage(Main.pluginName + "&aSaving " + this.name + " file for plugin data \"&f" + this.name + "&r&a\"...");
		File file = this.getSaveFile();
		this.config = null;
		this.getConfig().set("name", this.name);
		ConfigurationSection memSection = this.config.getConfigurationSection("data");
		if(memSection == null) {
			memSection = this.config.createSection("data");
		}
		this.saveToConfig(memSection);
		try {
			this.config.save(file);
			return true;
		} catch(IOException e) {
			Main.sendConsoleMessage(Main.pluginName + "&eUnable to save data to " + this.name + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
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
	
	public void delete() {
		File saveFile = this.getSaveFile();
		if(saveFile != null && saveFile.isFile()) {
			saveFile.delete();
		}
	}
	
	public void dispose() {
		SavablePluginData.instances.remove(this);
		this.isDisposed = true;
	}
	
	public final boolean isDisposed() {
		return this.isDisposed;
	}
	
	//==========================================================================
	
	public static final ItemStack[] getItemsFromConfig(String name, ConfigurationSection root) {
		if(name == null || name.isEmpty() || root == null) {
			return new ItemStack[0];
		}
		ConfigurationSection items = root.getConfigurationSection(name);
		if(items == null) {
			return new ItemStack[0];
		}
		ArrayList<ItemStack> its = new ArrayList<>();
		for(String key : items.getKeys(false)) {
			ItemStack item = items.getItemStack(key, new ItemStack(Material.AIR));
			its.add(item);
		}
		ItemStack[] rtrn = new ItemStack[its.size()];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = its.get(i);
		}
		return rtrn;
	}
	
	public static final boolean saveItemsToConfig(String name, ItemStack[] its, ConfigurationSection root) {
		if(name == null || name.isEmpty() || root == null) {
			return false;
		}
		ConfigurationSection items = root.getConfigurationSection(name);
		if(items == null) {
			items = root.createSection(name);
		}
		int i = 0;
		for(ItemStack item : its) {
			items.set("ItemStack_" + i, item);
			i++;
		}
		return true;
	}
	
}
