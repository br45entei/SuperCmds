package com.gmail.br45entei.supercmds.yml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.br45entei.supercmds.FileMgmt;
import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.SavablePlayerData;

/** @author Brian_Entei */
public class YamlMgmtClass {
	
	/** Attempts to load a file based on the given file name and parent folder.<br>
	 * If the file does not exist, this will try to create the file and create
	 * it's data from the jar's assets folder.<br>
	 * If the file does exist, this will return that file as is.
	 * 
	 * @param folder File
	 * @param path String
	 * @return The resulting file */
	public static File getResourceFromStreamAsFile(File folder, String path, boolean overwriteExisting) {
		File output = new File(folder, FilenameUtils.getName(path));
		try {
			if(!output.exists()) {
				output.createNewFile();
			} else if(!overwriteExisting) {
				return output;
			}
			@SuppressWarnings("resource")
			OutputStream out = new FileOutputStream(output);
			FileMgmt.copy(Main.class.getResourceAsStream("/" + path), out);
			try {
				out.close();
			} catch(Throwable ignored) {
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public static boolean LoadConfig() {
		//plugin.saveDefaultConfig();
		Main.config = new YamlConfiguration();
		Main.configFile = YamlMgmtClass.getResourceFromStreamAsFile(Main.dataFolder, Main.configFileName, false);
		//NEWCONFIG = new YamlConfiguration();
		//NEWCONFIGFile = new java.io.File(dataFolderName, NEWCONFIGFileName);
		try {
			YamlMgmtClass.loadResourceFiles();
		} catch(Exception e) {
			e.printStackTrace();
		}
		Main.YamlsAreLoaded = YamlMgmtClass.reloadFiles(true);
		if(Main.YamlsAreLoaded == true) {
			Main.DEBUG(Main.pluginName + "&aAll YAML Configration Files loaded successfully!");
		} else {
			Main.sendConsoleMessage(Main.pluginName + "&cError: Some YAML Files failed to load successfully! Check the server log or \"" + Main.dataFolderName + "\\crash-reports.txt\" to solve the problem.");
		}
		return Main.YamlsAreLoaded;
	}
	
	private static void loadResourceFiles() throws Exception {
		if(!Main.configFile.exists()) {
			Main.configFile.getParentFile().mkdirs();
			YamlMgmtClass.getResourceFromStreamAsFile(Main.dataFolder, Main.configFileName, true);
		}
		/*if(!Main.NEWCONFIGFile.exists()) {
			Main.NEWCONFIGFile.getParentFile().mkdirs();
			YamlMgmtClass.getResourceFromStreamAsFile(Main.dataFolder, Main.NEWCONFIGFileName);
		}*/
	}
	
	public static boolean reloadFiles(boolean ShowStatus) {
		Main.YamlsAreLoaded = false;
		boolean loadedAllVars = false;
		String unloadedFiles = "\"";
		Exception e1 = null;
		try {
			Main.config.load(Main.configFile);
		} catch(Exception e) {
			e1 = e;
			unloadedFiles = unloadedFiles + Main.configFileName + "\" ";
		}
		//Exception e2 = null;try{NEWCONFIG.load(NEWCONFIGFile);} catch (Exception e) {e2 = e;unloadedFiles = unloadedFiles + NEWCONFIGFileName + "\" ";}
		//Exception e2 = null;try{UUIDMgmt.uuidConfig.load(UUIDMgmt.uuidConfigFile);} catch (Exception e) {e2 = e;unloadedFiles += UUIDMgmt.uuidConfigFileName + "\" ";}
		try {
			if(unloadedFiles.equals("\"")) {
				Main.YamlsAreLoaded = true;
				loadedAllVars = YamlMgmtClass.loadYamlVariables();
				if(loadedAllVars == true) {
					if(ShowStatus) {
						Main.sendConsoleMessage(Main.pluginName + "&aAll of the yaml configuration files loaded successfully!");
					}
				} else {
					if(ShowStatus) {
						Main.sendConsoleMessage(Main.pluginName + "&aSome of the settings did not load correctly from the configuration files! Check the server log to solve the problem.");
					}
				}
				return true;
			}
			String Causes = "";
			if(e1 != null) {
				Causes = Causes.concat(Causes + "\n" + Main.throwableToStr(e1));
			}
			//if(e2 != null) {Causes = Causes.concat(Causes + "\r" + e2.toString());}
			//if(e2 != null) {Causes = Causes.concat(Causes + "\r" + e2.toString());}
			throw new InvalidYamlException(Causes);
		} catch(InvalidYamlException e) {
			FileMgmt.LogCrash(e, "reloadFiles()", "Failed to load one or more of the following YAML files: " + unloadedFiles, false, Main.dataFolderName);
			Main.DEBUG(Main.pluginName + "&cThe following YAML files failed to load properly! Check the server log or \"" + Main.dataFolderName + "\\crash-reports.txt\" to solve the problem: (" + unloadedFiles + ")");
			//logger.severe(e.toString());//A test
			return false;
		}
	}
	
	public static boolean saveYamls() {
		String unSavedFiles = "\"";
		//The following tries to save the FileConfigurations to their Files:
		Exception e1 = null;
		try {
			SavablePlayerData.saveLocationToConfig("spawnLocation", Main.spawnLocation, Main.config);
			Main.config.save(Main.configFile);
		} catch(Exception e) {
			e1 = e;
			unSavedFiles += Main.configFileName + "\" ";
		}
		//Exception e2 = null;try{NEWCONFIG.save(NEWCONFIGFile);} catch (Exception e) {e2 = e;unSavedFiles = unSavedFiles + NEWCONFIGFileName + "\" ";}
		try {
			if(unSavedFiles.equals("\"")) {
				Main.DEBUG(Main.pluginName + "&aAll of the yaml configuration files were saved successfully!");
				return true;
			}
			String Causes = "";
			if(e1 != null) {
				Causes = Causes.concat(Causes + "\r" + e1.toString());
			}
			//if(e2 != null) {Causes = Causes.concat(Causes + "\r" + e2.toString());}
			if(!Causes.isEmpty()) {
				throw new InvalidYamlException(Causes);
			}
		} catch(InvalidYamlException e) {
			FileMgmt.LogCrash(e, "saveYamls()", "Failed to save one or more of the following YAML files: (" + unSavedFiles + ")", false, Main.dataFolderName);
			Main.DEBUG(Main.pluginName + "&cThe following YAML files failed to get saved properly! Check the server log or \"" + Main.dataFolderName + "\\crash-reports.txt\" to solve the problem: (" + unSavedFiles + ")");
			//logger.severe(e.toString());//A test
			return false;
		}
		return true;
	}
	
	public static boolean loadYamlVariables() {
		boolean loadedAllVars = true;
		try {
			Main.configVersion = Main.formatColorCodes(Main.config.getString("version"));
			if(Main.configVersion.equals(Main.pdffile.getVersion())) {
				Main.DEBUG(Main.pluginName + "&aThe " + Main.configFileName + "'s version matches this plugin's version(&f" + Main.pdffile.getVersion() + "&a)!");
			} else {
				Main.DEBUG(Main.pluginName + "&eThe " + Main.configFileName + "'s version does NOT match this plugin's version(&f" + Main.pdffile.getVersion() + "&e)! Make sure that you update the " + Main.configFileName + " from this plugin's latest version!&e.");
			}
		} catch(Exception e) {
			Main.unSpecifiedVarWarning("version", "config.yml", Main.pluginName);
			Main.sendConsoleMessage(Main.pluginName + "&cInvalid configuration settings detected! Disabling this plugin to prevent bad settings from corrupting saved player data...");
			Main.server.getPluginManager().disablePlugin(Main.getInstance());
			Main.enabled = false;
			return false;
		}
		final String oldPluginName = Main.white + "[" + Main.dred + "Super Cmds" + Main.white + "] ";
		try {
			Main.pluginName = Main.formatColorCodes(Main.config.getString("pluginName"));
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("pluginName", "config.yml", oldPluginName);
			Main.pluginName = oldPluginName;
		}
		try {
			Main.backupOnStartup = (Boolean.valueOf(Main.formatColorCodes(Main.config.getString("backupOnStartup")))).booleanValue() == true;
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("backupOnStartup", "config.yml", Main.pluginName);
		}
		try {
			Main.showDebugMsgs = (Main.forceDebugMsgs || (Boolean.valueOf(Main.formatColorCodes(Main.config.getString("showDebugMsgs")))).booleanValue() == true);
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("showDebugMsgs", "config.yml", Main.pluginName);
		}
		try {
			Main.noPerm = Main.formatColorCodes(Main.config.getString("noPermission"));
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("noPermission", "config.yml", Main.pluginName);
		}
		
		try {
			Main.handleEconomy = (Boolean.valueOf(Main.formatColorCodes(Main.config.getString("handleEconomy")))).booleanValue() == true;
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("handleEconomy", "config.yml", Main.pluginName);
		}
		try {
			Main.moneyTerm = Main.formatColorCodes(Main.config.getString("moneyTerm"));
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("moneyTerm", "config.yml", Main.pluginName);
		}
		try {
			Main.handleChat = (Boolean.valueOf(Main.formatColorCodes(Main.config.getString("handleChat")))).booleanValue() == true;
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("handleChat", "config.yml", Main.pluginName);
		}
		try {
			Main.handlePermissions = (Boolean.valueOf(Main.formatColorCodes(Main.config.getString("handlePermissions")))).booleanValue() == true;
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("handlePermissions", "config.yml", Main.pluginName);
		}
		
		try {
			Main.playerPromotedMessage = Main.formatColorCodes(Main.config.getString("playerPromotedMessage"));
		} catch(Exception e) {
			loadedAllVars = false;
			Main.unSpecifiedVarWarning("playerPromotedMessage", "config.yml", Main.pluginName);
		}
		
		try {
			Main.spawnLocation = SavablePlayerData.getLocationFromConfig("spawnLocation", Main.config);
		} catch(Exception e) {
			loadedAllVars = false;
			Main.sendConsoleMessage("&eWarning! The \"&fspawnLocation&r&e\" option in the config.yml file was set incorrectly! Please set it with a valid set of coords and world UUID or use the in-game /setspawn command.");
		}
		
		return loadedAllVars;
	}
}
