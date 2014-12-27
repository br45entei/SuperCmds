package com.gmail.br45entei.supercmds.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.yml.YamlMgmtClass;

/** @author Brian_Entei */
public class PlayerPermissions extends SavablePlayerData {
	protected static final ArrayList<PlayerPermissions>	permInstances	= new ArrayList<>();
	
	public static final ArrayList<PlayerPermissions> getInstances() {
		return new ArrayList<>(PlayerPermissions.permInstances);
	}
	
	public final ArrayList<String>	permissions	= new ArrayList<>();
	public Group					group;
	public boolean					isOperator	= false;
	
	PermissionAttachment			attachment	= null;
	
	public final PermissionAttachment getAttachment() {
		if(this.attachment == null && this.isPlayerOnline()) {
			this.attachment = this.getPlayer().addAttachment(Main.getInstance());
		}
		return this.attachment;
	}
	
	private PlayerPermissions(UUID uuid) {
		super(uuid, Main.uuidMasterList.getPlayerNameFromUUID(uuid));
		PlayerPermissions.permInstances.add(this);
	}
	
	private PlayerPermissions(Player player) {
		super(player);
		PlayerPermissions.permInstances.add(this);
	}
	
	@Override
	public String getSaveFolderName() {
		return "PlayerPermissions";
	}
	
	//@SuppressWarnings("deprecation")
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		if(this.isDisposed()) {
			return;
		}
		this.permissions.clear();
		if(mem.getStringList("permissions") == null) {
			Main.sendConsoleMessage(Main.pluginName + "&5Created data.permissions entry in player \"&f" + this.name + "&r&5\"'s file!");
		}
		for(String perm : mem.getStringList("permissions")) {
			this.permissions.add(perm);
			Main.sendConsoleMessage(Main.pluginName + "&aLoaded player permission \"&f" + perm + "&r&a\" for player \"&f" + this.name + "&r&a\"!");
		}
		this.group = Group.getGroupByName(mem.getString("group"));
		if(this.group != null) {
			Main.sendConsoleMessage(Main.pluginName + "&dGroup for player \"&f" + this.getPlayerDisplayName() + "&r&d\" is: \"&f" + this.group.displayName + "&r&d\"!");
		} else {
			Main.sendConsoleMessage(Main.pluginName + "&5Failed to load group \"&f" + mem.getString("group") + "&r&5\" for player \"&f" + this.getPlayerDisplayName() + "&r&5\"!");
		}
		if(this.isPlayerOnline()) {
			this.isOperator = this.getPlayer().isOp();
			if(this.getAttachment() != null) {
				if(this.group != null) {
					for(String perm : this.group.getAllPermissions()) {
						if(!perm.startsWith("-")) {
							this.getAttachment().setPermission(perm, true);
							//Main.perm.playerAdd(this.getPlayer(), perm);
						} else {
							perm = perm.substring(1);
							this.getAttachment().setPermission(perm, false);
							//Main.perm.playerRemove(this.getPlayer(), perm);
						}
					}
				}
				for(String perm : this.permissions) {
					this.getAttachment().setPermission(perm, true);
					//Main.perm.playerAdd(this.getPlayer(), perm);
				}
			}
		} else {
			this.isOperator = mem.getBoolean("isOperator");
		}
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		if(this.isDisposed()) {
			return;
		}
		mem.set("permissions", this.permissions);
		mem.set("group", (this.group != null ? this.group.name : ""));
		mem.set("isOperator", new Boolean(this.isOperator));
	}
	
	public final void reloadFromFileAndWipeConfig() {
		this.permissions.clear();
		this.group = null;
		this.getAttachment().remove();
		this.attachment = null;
		this.isOperator = false;
		this.loadFromFile();
	}
	
	public static final void refreshPlayerPermissions() {
		for(PlayerPermissions perm : PlayerPermissions.permInstances) {
			perm.reloadFromConfigAndSaveToFile();
		}
	}
	
	public final boolean changeGroup(Group newGroup) {
		if(newGroup == this.group) {
			return false;
		}
		final Group oldGroup = this.group;
		if(oldGroup != null) {
			
		}
		return this.reloadFromConfigAndSaveToFile();
	}
	
	//@SuppressWarnings("deprecation")
	public final void loadPermissionsFromGroup(Group group) {
		if(group == null) {
			return;
		}
		if(this.getAttachment() != null) {
			for(String perm : group.getAllPermissions()) {
				if(!perm.startsWith("-")) {
					this.getAttachment().setPermission(perm, true);
					//Main.perm.playerAdd(this.getPlayer(), perm);
				} else {
					perm = perm.substring(1);
					this.getAttachment().setPermission(perm, false);
					//Main.perm.playerRemove(this.getPlayer(), perm);
				}
			}
		}
	}
	
	public final void reloadPlayerPermissions() {
		for(String perm : this.permissions) {
			this.getAttachment().setPermission(perm, true);
			//Main.perm.playerAdd(this.getPlayer(), perm);
		}
	}
	
	//@SuppressWarnings("deprecation")
	public final boolean reloadFromConfigAndSaveToFile() {
		if(!this.isPlayerOnline()) {
			return false;
		}
		this.getAttachment().remove();
		this.attachment = null;
		if(this.getAttachment() != null) {//XXX This is not dead code. Read it again.
			this.loadPermissionsFromGroup(this.group);
			this.reloadPlayerPermissions();
			return this.saveToFile();
		}
		return false;
	}
	
	public final void saveToFileAndDispose() {
		if(this.isDisposed()) {
			return;
		}
		this.saveToFile();
		this.dispose();
	}
	
	public static final PlayerPermissions getPlayerPermissions(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerPermissions.getPlayerPermissions(player.getUniqueId());
	}
	
	public static final PlayerPermissions getPlayerPermissions(UUID uuid) {
		for(PlayerPermissions eco : PlayerPermissions.getInstances()) {
			if(eco.uuid.toString().equals(uuid.toString())) {
				return eco;
			}
		}
		PlayerPermissions eco = new PlayerPermissions(uuid);
		eco.loadFromFile();
		return eco;
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(PlayerJoinEvent event) {
		if(this.isDisposed()) {
			return;
		}
		Main.sendConsoleMessage("PlayerPermissions: onPlayerJoinEvent: " + this.name);
		if(SavablePlayerData.playerEquals(this.getPlayer(), event.getPlayer())) {
			this.getAttachment();
			this.loadFromFile();
		}
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerQuit(PlayerQuitEvent event) {
		if(this.isDisposed()) {
			return;
		}
		if(SavablePlayerData.playerEquals(this.getPlayer(), event.getPlayer())) {
			this.isOperator = this.getPlayer().isOp();
			if(this.attachment != null) {
				this.attachment.remove();
				this.attachment = null;
			}
			this.saveToFileAndDispose();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if(this.attachment != null) {
			this.attachment.remove();
			this.attachment = null;
		}
		PlayerPermissions.permInstances.remove(this);
	}
	
	//============================================================================================
	
	public static final boolean hasPermission(Player player, String permission) {
		if(player == null || permission == null) {
			return false;
		}
		return PlayerPermissions.hasPermission(player.getUniqueId(), permission);
	}
	
	public static final boolean hasPermission(UUID uuid, String permission) {
		if(uuid == null || permission == null) {
			return false;
		}
		PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
		boolean rtrn = perms.hasPermission(permission);
		if(!perms.isPlayerOnline()) {
			perms.dispose();
		}
		return rtrn;
	}
	
	public final boolean hasPermission(String permission) {
		if(permission == null) {
			return false;
		}
		if(this.isOperator || (this.isPlayerOnline() ? this.getPlayer().isOp() : false)) {
			return true;
		}
		for(String perm : this.permissions) {
			if(perm.equalsIgnoreCase(permission)) {
				return true;
			}
		}
		if(this.group != null) {
			for(String perm : this.group.getAllPermissions()) {
				if(perm.equalsIgnoreCase(permission)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static final void setPermission(Player player, String permission, boolean allowed) {
		if(player == null || permission == null) {
			return;
		}
		PlayerPermissions.setPermission(player.getUniqueId(), permission, allowed);
	}
	
	public static final void setPermission(UUID uuid, String permission, boolean allowed) {
		if(uuid == null || permission == null) {
			return;
		}
		PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
		perms.setPermission(permission, allowed);
		if(!perms.isPlayerOnline()) {
			perms.saveToFileAndDispose();
		}
		PlayerPermissions.refreshPlayerPermissions();
	}
	
	public final boolean setPermission(String permission, boolean allowed) {
		if(permission == null) {
			return false;
		}
		boolean didAnything = this.permissions.remove(permission);
		if(allowed) {
			didAnything = this.permissions.add(permission);
		}
		if(this.getAttachment() != null) {
			this.getAttachment().setPermission(permission, allowed);
		}
		return didAnything;
	}
	
	public static final class Group extends SavablePlayerData {
		public static final ArrayList<Group>	groups		= new ArrayList<>();
		public static YamlConfiguration			config;
		
		public boolean							isDefault;
		public String							displayName;
		public Group							inheritance;
		public final ArrayList<String>			permissions	= new ArrayList<>();
		
		@Override
		public final String toString() {
			String rtrn = "&3Group \"&3" + this.name + "&r&3\":\n" + //
			"Default Group: &e" + this.isDefault + "&3\n" + //
			"Display Name: \"&f" + this.displayName + "&r&3\"\n" + //
			"Inherited Group: &e" + (this.inheritance != null ? this.inheritance.name : "null") + "&3\n" + //
			"Total number of permissions: &e" + this.getAllPermissions().size();
			return Main.formatColorCodes(rtrn);
		}
		
		public final boolean setDefault(boolean isDefault) {
			for(Group group : new ArrayList<>(Group.groups)) {
				if(group != this) {
					if(group.isDefault) {
						return false;
					}
				}
			}
			boolean wasDefault = this.isDefault;
			this.isDefault = isDefault;
			PlayerPermissions.refreshPlayerPermissions();
			return(wasDefault != this.isDefault);
		}
		
		public final ArrayList<String> getAllPermissions() {
			ArrayList<String> rtrn = new ArrayList<>();
			rtrn.addAll(this.permissions);
			if(this.inheritance != null) {
				for(String perm : this.inheritance.getAllPermissions()) {
					if(!rtrn.contains(perm)) {
						rtrn.add(perm);
					}
				}
			}
			return rtrn;
		}
		
		public static final void reloadFromFile() {
			Group.disposeAll();
			Group.initialize();
		}
		
		public static final boolean initialize() {
			YamlConfiguration config = Group.getConfig();
			File file = Group.getStaticSaveFile();
			try {
				config.load(file);
				ConfigurationSection memSection = Group.getConfig().getConfigurationSection("groups");
				if(memSection == null) {
					Main.sendConsoleMessage(Main.pluginName + "&eCreating new groups.yml file\"...");
					memSection = Group.config.createSection("groups");
					return true;
				}
				Main.sendConsoleMessage(Main.pluginName + "&aLoading from groups.yml file...");
				for(Map.Entry<String, Object> entry : memSection.getValues(false).entrySet()) {
					String gname = entry.getKey();
					Group.getGroup(gname, memSection);
				}
				return true;
			} catch(Throwable e) {
				Main.sendConsoleMessage(Main.pluginName + "&eUnable to load data from " + Group.getStaticSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
				return false;
			}
		}
		
		public static final YamlConfiguration getConfig() {
			if(Group.config == null) {
				Group.config = SavablePlayerData.getConfig();
			}
			return Group.config;
		}
		
		@Override
		public File getSaveFile() {
			return Group.getStaticSaveFile();
		}
		
		@Override
		public File getSaveFolder() {
			return Group.getStaticSaveFolder();
		}
		
		public static File getStaticSaveFile() {
			File file = new File(Group.getStaticSaveFolder(), "groups" + SavablePlayerData.fileExt);
			if(!file.exists()) {
				try {
					YamlMgmtClass.getResourceFromStreamAsFile(Group.getStaticSaveFolder(), "groups.yml", true);
				} catch(Throwable e) {
					Main.sendConsoleMessage(Main.pluginName + "&eUnable to create new " + Group.getStaticSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
				}
			}
			return file;
		}
		
		public static final String getStaticSaveFolderName() {
			return "PlayerGroups";
		}
		
		public static final File getStaticSaveFolder() {
			File folder = new File(Main.dataFolder, Group.getStaticSaveFolderName());
			if(!folder.exists()) {
				folder.mkdirs();
			}
			return folder;
		}
		
		@Override
		public final boolean saveToFile() {
			return Group.saveToStaticFile();
		}
		
		public static final boolean saveToStaticFile() {
			Main.sendConsoleMessage(Main.pluginName + "&aSaving groups to groups.yml file...");
			YamlConfiguration config = Group.getConfig();
			File file = Group.getStaticSaveFile();
			ConfigurationSection memSection = config.getConfigurationSection("groups");
			if(memSection == null) {
				memSection = config.createSection("groups");
			}
			for(Group group : Group.groups) {
				group.saveToConfig(memSection);
			}
			try {
				config.save(file);
				return true;
			} catch(IOException e) {
				Main.sendConsoleMessage(Main.pluginName + "&eUnable to save data to " + Group.getStaticSaveFolderName() + " file \"&f" + file.getAbsolutePath() + "&r&e\":\n&c" + Main.throwableToStr(e));
				return false;
			}
		}
		
		public static final Group createGroup(String groupName) {
			Group rtrn = Group.getGroupByName(groupName);
			if(rtrn != null) {
				return rtrn;
			}
			ConfigurationSection memSection = Group.getConfig().getConfigurationSection("groups");
			if(memSection == null) {
				memSection = Group.getConfig().createSection("groups");
			}
			rtrn = new Group(groupName, memSection);
			rtrn.displayName = "&f" + Main.capitalizeFirstLetter(rtrn.name);
			memSection = null;
			return rtrn;
		}
		
		public static final Group getGroupByName(String name) {
			if(name == null || name.isEmpty()) {
				return null;
			}
			for(Group group : Group.groups) {
				if(group.name.equalsIgnoreCase(name)) {
					return group;
				}
			}
			return null;
		}
		
		public static final Group getGroup(String name, ConfigurationSection mem) {
			if(name == null || name.isEmpty()) {
				return null;
			}
			for(Group group : Group.groups) {
				if(group.name.equalsIgnoreCase(name)) {
					return group;
				}
			}
			return new Group(name, mem);
		}
		
		private Group(String name, ConfigurationSection mem) {
			super((UUID) null, name);
			this.loadFromConfig(mem);
			Group.groups.add(this);
		}
		
		@Override
		public void loadFromConfig(ConfigurationSection mem) {
			this.permissions.clear();
			ConfigurationSection group = mem.getConfigurationSection(this.name);
			if(group != null) {
				Main.sendConsoleMessage(Main.pluginName + "&aLoading group \"&f" + this.name + "&r&a\" from groups.yml file...");
				this.setDefault(group.getBoolean("isDefault"));
				this.displayName = group.getString("displayName");
				if(this.displayName == null || this.displayName.isEmpty()) {
					this.displayName = "&f" + Main.capitalizeFirstLetter(this.name);
				}
				this.inheritance = Group.getGroupByName(group.getString("inheritance"));
				if(group.getStringList("permissions") == null) {
					group.set("permissions", this.permissions);
					Main.sendConsoleMessage(Main.pluginName + "&5Created group.yml: groups." + this.name + ".permissions entry!");
				} else {
					Main.sendConsoleMessage(Main.pluginName + "&5group.yml: groups." + this.name + ".permissions already existed! Let's load from it...");
				}
				List<String> permissions = group.getStringList("permissions");
				Main.sendConsoleMessage(Main.pluginName + "&dgroup.getStringList(\"permissions\").size(): \"&f" + permissions.size() + "&r&d\"!");
				for(String perm : permissions) {
					this.permissions.add(perm);
					Main.sendConsoleMessage(Main.pluginName + "&aLoaded group permission \"&f" + perm + "&r&a\" for group \"&f" + this.name + "&r&a\"!");
				}
			}
		}
		
		@Override
		public void saveToConfig(ConfigurationSection mem) {
			ConfigurationSection group = mem.getConfigurationSection(this.name);
			if(group == null) {
				group = mem.createSection(this.name);
			}
			Main.sendConsoleMessage(Main.pluginName + "&aSaving group \"&f" + this.name + "&r&a\" to groups.yml file...");
			group.set("isDefault", new Boolean(this.isDefault));
			group.set("displayName", this.displayName);
			group.set("inheritance", (this.inheritance != null ? this.inheritance.name : ""));
			group.set("permissions", this.permissions);
		}
		
		@Override
		public String getSaveFolderName() {
			return Group.getStaticSaveFolderName();
		}
		
		@Override
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerJoin(PlayerJoinEvent event) {
			//
		}
		
		@Override
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerQuit(PlayerQuitEvent event) {
			//
		}
		
		public final boolean setDisplayName(String newDisplayName) {
			if(newDisplayName == null || newDisplayName.isEmpty()) {
				return false;
			}
			String oldDisplayName = this.displayName;
			this.displayName = newDisplayName;
			this.saveToFile();
			return !this.displayName.equals(oldDisplayName);
		}
		
		public final boolean setPermission(String permission, boolean allowed) {
			if(permission == null) {
				return false;
			}
			boolean didAnything = this.permissions.remove(permission);
			if(allowed) {
				didAnything = this.permissions.add(permission);
			}
			PlayerPermissions.refreshPlayerPermissions();
			return didAnything;
		}
		
		public final boolean setInheritance(Group group) {
			Group oldInheritance = this.inheritance;
			if(group == this) {
				group = oldInheritance;
			}
			this.inheritance = group;
			PlayerPermissions.refreshPlayerPermissions();
			return this.inheritance != oldInheritance;
		}
		
		public static final void disposeAll() {
			for(Group group : new ArrayList<>(Group.groups)) {
				group.dispose();
			}
			Group.config = null;
			Group.getConfig();
		}
		
		@Override
		public void dispose() {
			for(PlayerPermissions perm : new ArrayList<>(PlayerPermissions.permInstances)) {
				if(perm.group == this) {
					perm.changeGroup(null);
				}
			}
			for(Group group : new ArrayList<>(Group.groups)) {
				if(group != this && group != null) {
					if(group.inheritance == this) {
						group.setInheritance(null);
					}
				}
			}
			super.dispose();
			Group.groups.remove(this);
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					PlayerPermissions.refreshPlayerPermissions();
				}
			});
		}
		
	}
	
}
