package com.gmail.br45entei.supercmds.file;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerPermissions.Group;

/** @author Brian_Entei */
public class Warps extends SavablePluginData {
	public static final ArrayList<Warp>	warps	= new ArrayList<>();
	
	public static final ArrayList<Warp> getAllWarps() {
		return new ArrayList<>(Warps.warps);
	}
	
	private static Warps	instance;
	
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
					Warps.warps.add(w);
					Main.DEBUG("&aSuccessfully loaded warp from file:&z&3" + w.toString());
				} else {
					Main.sendConsoleMessage(Main.pluginName + "&cWarp \"&f" + key + "&r&c\" did not load correctly!");
				}
			} else {
				Main.sendConsoleMessage(Main.pluginName + "&cConfiguration section \"&f" + key + "&r&c\" was null! Cannot load that warp...");
			}
		}
		Main.sendConsoleMessage(Main.pluginName + "&aLoaded &f" + Warps.warps.size() + "&a warps from file.");
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
	
	public static final class Warp {
		public final String	name;
		public Location		location;
		public Group		requiredGroup		= null;
		public String		requiredPermission	= null;
		
		@Override
		public final String toString() {
			return "&3Name: \"&f" + this.name + "&r&3\";\n" + //
			"&3Location: \"&f" + this.location.toString() + "&3\"\n" + //
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
				return null;
			}
			String name = warp.getString("name");
			if(name == null) {
				return null;
			}
			Warp rtrn = new Warp(name);
			rtrn.loadFromConfig(warp);
			return rtrn;
		}
		
		public final boolean loadFromConfig(ConfigurationSection warp) {
			if(warp == null) {
				return false;
			}
			if(warp.getString("name") == null || !warp.getString("name").equalsIgnoreCase(this.name)) {
				return false;
			}
			this.location = SavablePlayerData.getLocationFromConfig("location", warp);
			if(Main.handlePermissions) {
				this.requiredGroup = Group.getGroupByName(warp.getString("requiredGroup"));
			}
			this.requiredPermission = warp.getString("requiredPermission");
			return true;
		}
		
		public final boolean saveToConfig(ConfigurationSection mem) {
			ConfigurationSection warp = mem.getConfigurationSection(this.name);
			if(warp == null) {
				warp = mem.createSection(this.name);
			}
			warp.set("name", this.name);
			SavablePlayerData.saveLocationToConfig("location", this.location, warp);
			if(Main.handlePermissions) {
				warp.set("requiredGroup", (this.requiredGroup != null ? this.requiredGroup.name : ""));
			}
			warp.set("requiredPermission", this.requiredPermission);
			return true;
		}
		
		public final void dispose() {
			Warps.warps.remove(this);
			Warps.getInstance().saveToFile();
		}
		
	}
	
}
