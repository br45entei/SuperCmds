package com.gmail.br45entei.supercmds.api.credits;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.SavablePluginData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class ItemLibrary extends SavablePluginData {
	
	public static final ConcurrentLinkedQueue<ItemLibrary> library = new ConcurrentLinkedQueue<>();
	
	public static final ArrayList<ItemLibrary> getShopItems() {
		ArrayList<ItemLibrary> rtrn = new ArrayList<>();
		for(ItemLibrary item : library) {
			if(item != NULL && item != null) {
				rtrn.add(item);
			}
		}
		return rtrn;
	}
	
	public int							price;
	public ItemStack[]					items;
	public ArrayList<String>			permissions;
	
	private static final ItemLibrary	NULL	= new ItemLibrary();
	
	static {
		NULL.dispose();
	}
	
	public static final ItemLibrary getItemFromName(String name) {
		if(name == null) {
			return null;
		}
		for(ItemLibrary item : new ArrayList<>(library)) {
			if(item.getName().equalsIgnoreCase(name)) {
				if(item.isDisposed()) {
					library.remove(item);
					continue;
				}
				return item;
			}
		}
		return null;
	}
	
	private ItemLibrary() {
		this("null");
		this.saveAndLoadWithSuperCmds = false;
		this.price = 0;
		this.items = new ItemStack[0];
		this.permissions = new ArrayList<>();
		super.dispose();
	}
	
	private ItemLibrary(String name) {
		super(name);
		if(getItemFromName(name) == null) {
			this.saveAndLoadWithSuperCmds = true;
			ItemLibrary.library.add(this);
		} else {
			this.dispose();
		}
	}
	
	public ItemLibrary(String name, final int price, ItemStack... items) {
		this(name);
		this.price = price;
		this.items = items;
		this.permissions = new ArrayList<>();
	}
	
	public ItemLibrary(String name, final int price, String... permissions) {
		this(name);
		this.price = price;
		this.items = null;
		this.permissions = new ArrayList<>(Arrays.asList(permissions == null ? new String[0] : permissions));
	}
	
	public ItemLibrary(String name, final int price, ItemStack[] items, String... permissions) {
		this(name);
		this.price = price;
		this.items = items;
		this.permissions = new ArrayList<>(Arrays.asList(permissions == null ? new String[0] : permissions));
	}
	
	public final String getItemNames() {
		String rtrn = "";
		if(this.items != null && this.items.length > 0) {
			int i = 1;
			for(ItemStack item : this.items) {
				rtrn += "&z\t\t&3[" + i + "]&f: &e\"" + item.getType().name() + "\"&3;";
				i++;
			}
		} else {
			rtrn = "&z\t\t&3No items found.";
		}
		return rtrn;
	}
	
	public final String getPermissionsAsString() {
		String rtrn = "";
		if(!this.permissions.isEmpty()) {
			int i = 1;
			for(String permission : this.permissions) {
				rtrn += "&z\t\t&3[" + i + "]&f: &8" + Main.escapeColorCodes(permission) + "&3;";
				i++;
			}
		} else {
			rtrn = "&z\t\t&3No permissions found.";
		}
		return rtrn;
	}
	
	@Override
	public final String toString() {
		return "&eShop item \"&f" + Main.escapeColorCodes(this.name) + "&r&e\":&z&eBuy price: &6" + Main.decimal.format(this.price) + "&r&f " + Main.creditTerm + "&r&e;&z"//
				+ "&eItems: " + this.getItemNames()//
				+ "&z&ePermissions: " + this.getPermissionsAsString();
	}
	
	public static final void disposeAllLibraries() {
		for(ItemLibrary library : library) {
			library.dispose();
		}
		library.clear();
	}
	
	public static final void loadAllLibrariesFromFile() {
		disposeAllLibraries();
		File loadFolder = NULL.getSaveFolder();
		for(String fileName : loadFolder.list()) {
			File file = new File(loadFolder, fileName);
			if(file.isFile() && fileName.toLowerCase().endsWith(SavablePluginData.fileExt)) {
				ItemLibrary library = new ItemLibrary(fileName.substring(0, fileName.length() - 4));
				library.permissions = new ArrayList<>();
				if(!library.loadFromFile()) {
					library.dispose();
					library = null;
				}
			}
		}
	}
	
	public static final void saveAllLibrariesToFile() {
		for(ItemLibrary library : ItemLibrary.library) {
			if(library != null) {
				library.saveToFile();
			}
		}
	}
	
	@Override
	public String getSaveFolderName() {
		return "ItemLibrary";
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		this.price = mem.getInt("price");
		
		ConfigurationSection list = mem.getConfigurationSection("items");
		if(list != null) {
			ArrayList<ItemStack> items = new ArrayList<>();
			for(String key : list.getKeys(false)) {
				ItemStack item = list.getItemStack(key);
				if(item != null) {
					items.add(item);
				}
			}
			this.items = items.toArray(new ItemStack[items.size()]);
		}
		
		List<String> perms = mem.getStringList("permissions");
		if(perms != null && !perms.isEmpty()) {
			ArrayList<String> permissions = new ArrayList<>(perms);
			Collections.sort(perms);
			this.permissions = permissions;
		}
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		mem.set("price", Integer.valueOf(this.price));
		ConfigurationSection list = mem.getConfigurationSection("items");
		if(list == null) {
			list = mem.createSection("items");
		}
		if(this.items != null) {
			int i = 0;
			for(ItemStack item : this.items) {
				list.set("item" + (i++), item);
			}
		}
		mem.set("permissions", this.permissions);
	}
	
	@Override
	public final boolean loadFromFile() {
		if(this.isDisposed() || this == NULL) {
			return false;
		}
		return super.loadFromFile();
	}
	
	@Override
	public final boolean saveToFile() {
		if(this.isDisposed() || this == NULL) {
			return false;
		}
		return super.saveToFile();
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
	}
	
	@Override
	public final void dispose() {
		if(this == NULL) {
			return;
		}
		super.dispose();
		library.remove(this);
	}
	
}
