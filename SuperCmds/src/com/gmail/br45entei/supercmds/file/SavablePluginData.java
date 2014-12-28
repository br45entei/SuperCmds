package com.gmail.br45entei.supercmds.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.br45entei.supercmds.Main;

/** @author Brian_Entei */
public abstract class SavablePluginData extends AbstractPlayerJoinQuitClass {
	public static final ArrayList<SavablePluginData>	instances			= new ArrayList<>();
	public static final String							fileExt				= ".yml";
	protected boolean									isLoadedFromFile	= false;
	
	public static final ArrayList<SavablePluginData> getAllInstances() {
		return SavablePluginData.instances;
	}
	
	public final String			name;
	private boolean				isDisposed	= false;
	
	private YamlConfiguration	config;
	
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
	public final File getSaveFolder() {
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
		Main.sendConsoleMessage(Main.pluginName + "&aSaving " + this.name + " file for plugin data \"&f" + this.name + "&r&a\"...");
		File file = this.getSaveFile();
		this.config.set("name", this.name);
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
	
	public void dispose() {
		SavablePluginData.instances.remove(this);
		this.isDisposed = true;
	}
	
	public final boolean isDisposed() {
		return this.isDisposed;
	}
	
}
