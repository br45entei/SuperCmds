package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerPermissions.Group;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class Warps extends SavablePluginData {
	public static final ArrayList<Warp> warps = new ArrayList<>();
	
	public static final ArrayList<Warp> getAllWarps() {
		return new ArrayList<>(Warps.warps);
	}
	
	public static final void registerWarp(Warp warp) {
		if(warp == null) {
			return;
		}
		if(Warps.getWarpByName(warp.name) == null) {
			if(!Warps.warps.contains(warp)) {
				Warps.warps.add(warp);
				Warps.getInstance().saveToFile();
			}
		}
	}
	
	public static final void unregisterWarp(Warp warp) {
		if(warp == null) {
			return;
		}
		if(Warps.warps.contains(warp)) {
			Warps.warps.remove(warp);
			Warps.getInstance().saveToFile();
		}
	}
	
	private static Warps instance;
	
	public static final Warps getInstance() {
		if(Warps.instance == null) {
			Warps.instance = new Warps();
		}
		return Warps.instance;
	}
	
	public static final Warp getWarpByName(String name) {
		for(Warp warp : Warps.getAllWarps()) {
			if(warp.name.equalsIgnoreCase(name)) {
				return warp;
			}
		}
		return null;
	}
	
	/** @param name The name to use. */
	private Warps() {
		super("Warps");
		Warps.instance = this;
	}
	
	@Override
	public String getSaveFolderName() {
		return null;//Null will default to the main plugin data folder.
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		for(String key : mem.getKeys(false)) {
			ConfigurationSection warp = mem.getConfigurationSection(key);
			if(warp != null) {
				Warp w = Warp.getFromConfig(warp);
				if(w != null) {
					Warps.registerWarp(w);
					if(w.location == null) {
						Main.sendConsoleMessage(Main.pluginName + "&aSuccessfully loaded warp from file:&z&3" + w.toString());
					} else {
						Main.DEBUG("&aSuccessfully loaded warp from file:&z&3" + w.toString());
					}
				} else {
					Main.sendConsoleMessage(Main.pluginName + "&cWarp \"&f" + key + "&r&c\" did not load correctly!");
				}
			} else {
				Main.sendConsoleMessage(Main.pluginName + "&cConfiguration section \"&f" + key + "&r&c\" was null! Cannot load that warp...");
			}
		}
		Main.sendConsoleMessage(Main.pluginName + "&aLoaded &f" + Warps.getAllWarps().size() + "&a warps from file.");
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		for(Warp warp : Warps.getAllWarps()) {
			warp.saveToConfig(mem);
		}
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		// TODO Auto-generated method stub (42!)
		
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		// TODO Auto-generated method stub (42!)
		
	}
	
	public static final Warp createWarp(String name, Location loc, Group requiredGroup, String requiredPerm) {
		for(Warp warp : Warps.getAllWarps()) {
			if(warp.name.equalsIgnoreCase(name)) {
				return warp;
			}
		}
		Warp warp = new Warp(name, loc);
		warp.requiredGroup = requiredGroup;
		warp.requiredPermission = requiredPerm;
		Warps.warps.add(warp);
		Warps.getInstance().saveToFile();
		return warp;
	}
	
	protected static final ArrayList<Warp> warpsWaitingOnWorldsToLoad = new ArrayList<>();
	
	/** @param event Called when a World is loaded */
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onWorldLoadEvent(WorldLoadEvent event) {
		boolean didAnything = false;
		for(Warp warp : new ArrayList<>(Warps.warpsWaitingOnWorldsToLoad)) {
			if(warp.location == null) {
				ConfigurationSection mem = Warps.getInstance().config.getConfigurationSection("data");
				if(mem != null) {
					for(String w : mem.getKeys(false)) {
						if(w.equalsIgnoreCase(warp.name)) {
							ConfigurationSection warpSection = mem.getConfigurationSection(w);
							Location loc = SavablePlayerData.getLocationFromConfig("location", warpSection);
							if(loc != null) {
								warp.location = loc;
								Warps.warpsWaitingOnWorldsToLoad.remove(warp);
								Main.sendConsoleMessage(Main.pluginName + "&aSuccessfully loaded warp \"&6/warp " + warp.name + "&r&a\"'s location from config after waiting on its world to load!");
								didAnything = true;
							}
						}
					}
				}
			}
		}
		if(Warps.warpsWaitingOnWorldsToLoad.size() > 0 && didAnything) {
			Main.sendConsoleMessage(Main.pluginName + "&eThere are still other warps that are waiting on their respective worlds to load! If the world does not load, then the warp will be un-usable.");
		}
	}
	
	public static final class Warp {
		public final String	name;
		public Location		location;
		public Group		requiredGroup		= null;
		public String		requiredPermission	= null;
		
		@Override
		public final String toString() {
			return "&3Name: \"&f" + this.name + "&r&3\";\n" + //
					"&3Location: \"&f" + (this.location != null ? this.location.toString() : "null - waiting on load...") + "&3\"\n" + //
					"&3Required group: \"&f" + (this.requiredGroup != null ? this.requiredGroup.displayName : "null") + "&r&3\"\n" + //
					"&3Required permission: \"&f" + this.requiredPermission + "&r&3\";";
		}
		
		private Warp(String name) {
			this.name = name;
		}
		
		protected Warp(String name, Location loc) {
			this(name);
			this.location = loc;
		}
		
		protected static final Warp getFromConfig(ConfigurationSection warp) {
			if(warp == null) {
				Main.sendConsoleMessage(Main.pluginName + "&4Cannot load a warp from a null configuration section.");
				return null;
			}
			String name = warp.getString("name");
			if(name == null || name.isEmpty()) {
				Main.sendConsoleMessage(Main.pluginName + "&4Cannot load a warp from a configuration section with a null or empty name.");
				return null;
			}
			Warp rtrn = Warps.getWarpByName(name);
			if(rtrn == null) {
				rtrn = new Warp(name);
			}
			if(rtrn.loadFromConfig(warp)) {
				return rtrn;
			}
			Main.sendConsoleMessage(Main.pluginName + "&eThe warp \"&f" + rtrn.name + "&r&e\" did not get loaded from the config file correctly!");
			return rtrn;
		}
		
		public final boolean loadFromConfig(ConfigurationSection warp) {
			if(warp == null) {
				return false;
			}
			this.location = SavablePlayerData.getLocationFromConfig("location", warp);
			if(Main.handlePermissions) {
				this.requiredGroup = Group.getGroupByName(warp.getString("requiredGroup"));
			}
			this.requiredPermission = warp.getString("requiredPermission");
			if(this.location == null) {
				Warps.warpsWaitingOnWorldsToLoad.add(this);
			}
			return true;
		}
		
		public final boolean saveToConfig(ConfigurationSection mem) {
			ConfigurationSection warp = mem.getConfigurationSection(this.name);
			if(warp == null) {
				warp = mem.createSection(this.name);
			}
			warp.set("name", this.name);
			SavablePlayerData.saveLocationToConfig("location", this.location, warp, false);
			if(Main.handlePermissions) {
				warp.set("requiredGroup", (this.requiredGroup != null ? this.requiredGroup.name : ""));
			}
			warp.set("requiredPermission", this.requiredPermission);
			return true;
		}
		
		public final void dispose() {
			Warps.unregisterWarp(this);
		}
		
	}
	
}
