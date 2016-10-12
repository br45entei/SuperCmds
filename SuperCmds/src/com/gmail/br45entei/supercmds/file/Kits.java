package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerPermissions.Group;

import java.util.ArrayList;
import java.util.Date;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class Kits extends SavablePluginData {
	public static final ArrayList<Kit> kits = new ArrayList<>();
	
	public static final ArrayList<Kit> getAllKits() {
		return new ArrayList<>(Kits.kits);
	}
	
	public static final void registerKit(Kit kit) {
		if(kit == null) {
			return;
		}
		if(Kits.getKitByName(kit.name) == null) {
			if(!Kits.kits.contains(kit)) {
				Kits.kits.add(kit);
				Kits.getInstance().saveToFile();
			}
		}
	}
	
	public static final void unregisterKit(Kit kit) {
		if(kit == null) {
			return;
		}
		if(Kits.kits.contains(kit)) {
			Kits.kits.remove(kit);
			Kits.getInstance().saveToFile();
		}
	}
	
	private static Kits instance;
	
	public static final Kits getInstance() {
		if(Kits.instance == null) {
			Kits.instance = new Kits();
		}
		return Kits.instance;
	}
	
	public static final Kit getKitByName(String name) {
		for(Kit kit : Kits.getAllKits()) {
			if(kit.name.equalsIgnoreCase(name)) {
				return kit;
			}
		}
		return null;
	}
	
	public static final Kit createKit(String name) {
		if(Kits.getKitByName(name) != null) {
			return Kits.getKitByName(name);
		}
		Kit newKit = new Kit(name);
		Kits.kits.add(newKit);
		return newKit;
	}
	
	/** @param name The name to use. */
	private Kits() {
		super("Kits");
		Kits.instance = this;
	}
	
	@Override
	public String getSaveFolderName() {
		return null;//Defaults to main plugin's data folder
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		for(String key : mem.getKeys(false)) {
			ConfigurationSection kit = mem.getConfigurationSection(key);
			if(kit != null) {
				Kit k = Kit.getFromConfig(kit);
				if(k != null) {
					Kits.kits.add(k);
					Main.DEBUG("&aSuccessfully loaded kit from file:&z&3" + k.toString());
				} else {
					Main.sendConsoleMessage(Main.pluginName + "&cKit \"&f" + key + "&r&c\" did not load correctly!");
				}
			} else {
				Main.sendConsoleMessage(Main.pluginName + "&cConfiguration section \"&f" + key + "&r&c\" was null! Cannot load that kit...");
			}
		}
		Main.sendConsoleMessage(Main.pluginName + "&aLoaded &f" + Kits.kits.size() + "&a kits from file.");
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		for(Kit kit : Kits.getAllKits()) {
			kit.saveToConfig(mem);
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
	
	public static final class Kit {
		public final String	name;
		public long			obtainInterval;							//in seconds!
		public double		rewardMoney			= 0.0;
		public Group		requiredGroup		= null;
		public String		requiredPermission	= null;
		public ItemStack[]	items				= new ItemStack[0];
		
		@Override
		public final String toString() {
			return "&3Name: \"&f" + this.name + "&r&3\";\n" + //
					"&3Obtain interval(HH:mm:ss): \"&f" + Main.hourFormatter.format(new Date(this.obtainInterval / 1000)) + "&3\"\n" + //
					"&3Reward money: \"&f" + Main.decimal.format(this.rewardMoney) + "&3\"\n" + //
					"&3Required group: \"&f" + (this.requiredGroup != null ? this.requiredGroup.displayName : "null") + "&r&3\"\n" + //
					"&3Required permission: \"&f" + this.requiredPermission + "&r&3\"\n" + //
					"&3# of items: &f" + this.items.length + "&3;";
		}
		
		protected Kit(String name) {
			this.name = name;
		}
		
		protected static final Kit getFromConfig(ConfigurationSection kit) {
			if(kit == null) {
				return null;
			}
			String name = kit.getString("name");
			if(name == null) {
				return null;
			}
			Kit rtrn = Kits.getKitByName(name);
			if(rtrn == null) {
				rtrn = new Kit(name);
			}
			if(!rtrn.loadFromConfig(kit)) {
				Main.sendConsoleMessage(Main.pluginName + "&eThe kit \"&f" + rtrn.name + "&r&e\" did not get loaded from the config file correctly!");
			}
			return rtrn;
		}
		
		public final boolean loadFromConfig(ConfigurationSection kit) {
			if(kit == null) {
				return false;
			}
			this.obtainInterval = kit.getLong("obtainInterval");
			this.rewardMoney = kit.getDouble("rewardMoney");
			if(Main.handlePermissions) {
				this.requiredGroup = Group.getGroupByName(kit.getString("requiredGroup"));
			}
			this.requiredPermission = kit.getString("requiredPermission");
			this.items = SavablePluginData.getItemsFromConfig("items", kit);
			return true;
		}
		
		public final boolean saveToConfig(ConfigurationSection mem) {
			ConfigurationSection kit = mem.getConfigurationSection(this.name);
			if(kit == null) {
				kit = mem.createSection(this.name);
			}
			kit.set("name", this.name);
			kit.set("obtainInterval", new Long(this.obtainInterval));
			kit.set("rewardMoney", new Double(this.rewardMoney));
			if(Main.handlePermissions) {
				kit.set("requiredGroup", (this.requiredGroup != null ? this.requiredGroup.name : ""));
			}
			kit.set("requiredPermission", this.requiredPermission);
			SavablePluginData.saveItemsToConfig("items", this.items, kit);
			return true;
		}
		
		public final void dispose() {
			Kits.unregisterKit(this);
			Kits.getInstance().saveToFile();
		}
		
	}
	
}
