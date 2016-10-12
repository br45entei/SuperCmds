package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.yml.YamlMgmtClass;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.IllegalPluginAccessException;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class PlayerPermissions extends SavablePlayerData {
	protected static final ArrayList<PlayerPermissions> permInstances = new ArrayList<>();
	
	public static final ArrayList<PlayerPermissions> getInstances() {
		return new ArrayList<>(PlayerPermissions.permInstances);
	}
	
	public final ArrayList<String>	permissions	= new ArrayList<>();
	public Group					group;
	public boolean					isOperator	= false;
	
	PermissionAttachment			attachment	= null;
	
	public final PermissionAttachment getAttachment() {
		if(this.attachment == null && this.isPlayerOnline()) {
			try {
				this.attachment = this.getPlayer().addAttachment(Main.getInstance());
			} catch(Throwable ignored) {
			}
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
	public final String toString() {
		return "&3PlayerChat:\n" + //
				"&3# of permissions: \"&f" + this.permissions.size() + "&r&3\"\n" + //
				"&3# of inherited permissions: \"&f" + (this.group != null ? this.group.getAllPermissions().size() : 0) + "&r&3\"\n" + //
				"&3Group: \"&f" + (this.group != null ? this.group.displayName : "null") + "&r&3\"\n" + //
				"&3isOperator: \"&f" + this.isOperator + "&r&3\"";
	}
	
	@Override
	public String getSaveFolderName() {
		return "PlayerPermissions";
	}
	
	@Override
	public final void onFirstLoad() {
		
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		if(this.isDisposed()) {
			return;
		}
		this.permissions.clear();
		List<String> permissions = mem.getStringList("permissions");
		if(permissions == null) {
			mem.set("permissions", new ArrayList<String>());
			Main.sendConsoleMessage(Main.pluginName + "&5Created data.permissions entry in player \"&f" + this.name + "&r&5\"'s file!");
		} else {
			for(String perm : permissions) {
				this.permissions.add(perm);
				Main.DEBUG("&aLoaded player permission \"&f" + perm + "&r&a\" for player \"&f" + this.name + "&r&a\"!");
			}
		}
		this.group = Group.getGroupByName(mem.getString("group"));
		if(this.group != null) {
			Main.DEBUG("&dGroup for player \"&f" + this.getPlayerDisplayName() + "&r&d\" is: \"&f" + this.group.displayName + "&r&d\"!");
		} else {
			Main.sendConsoleMessage(Main.pluginName + "&5Failed to load group \"&f" + mem.getString("group") + "&r&5\" for player \"&f" + this.getPlayerDisplayName() + "&r&5\"!");
		}
		if(this.isPlayerOnline()) {
			this.isOperator = this.getPlayer().isOp();
			PermissionAttachment perms = this.getAttachment();
			if(perms != null) {
				long startTime = System.currentTimeMillis();
				this.loadPermissionsFromGroup(this.group);
				addPermissionsToAttachment(perms, this.permissions);
				System.out.println("ElapsedTime: " + StringUtil.getElapsedTime(System.currentTimeMillis() - startTime, true));
			}
		} else {
			this.isOperator = mem.getBoolean("isOperator");
		}
	}
	
	private static final void addPermissionsToAttachment(PermissionAttachment perms, List<String> permissions) {
		try {
			Field p = PermissionAttachment.class.getDeclaredField("permissions");
			p.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, Boolean> map = (Map<String, Boolean>) p.get(perms);
			for(String perm : permissions) {
				if(!perm.startsWith("-")) {
					map.put(perm, Boolean.TRUE);
				} else {
					map.put(perm.substring(1), Boolean.FALSE);
				}
			}
			Field permissibleGet = PermissionAttachment.class.getDeclaredField("permissible");
			permissibleGet.setAccessible(true);
			Permissible permissible = (Permissible) permissibleGet.get(perms);
			permissible.recalculatePermissions();
		} catch(Throwable e) {
			e.printStackTrace();
			for(String perm : permissions) {
				if(!perm.startsWith("-")) {
					perms.setPermission(perm, true);
				} else {
					perms.setPermission(perm.substring(1), false);
				}
			}
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
		try {
			for(PlayerPermissions perm : PlayerPermissions.permInstances) {
				perm.reloadFromConfigAndSaveToFile();
			}
		} catch(IllegalArgumentException ignored) {
		} catch(Throwable e) {
			Main.sendConsoleMessage(Main.pluginName + "&eAn error occurred while refreshing player permissions:&z&c" + Main.throwableToStr(e));
		}
	}
	
	public final boolean changeGroup(Group newGroup) {
		if(newGroup == this.group) {
			return false;
		}
		this.group = newGroup;
		return this.reloadFromConfigAndSaveToFile();
	}
	
	public final void loadPermissionsFromGroup(Group group) {
		if(group == null) {
			return;
		}
		PermissionAttachment perms = this.getAttachment();
		if(perms != null) {
			addPermissionsToAttachment(perms, group.getAllPermissions());
		}
	}
	
	public final void reloadPlayerPermissions() {
		for(String perm : this.permissions) {
			this.getAttachment().setPermission(perm, true);
			//Main.perm.playerAdd(this.getPlayer(), perm);
		}
		this.loadPermissionsFromGroup(this.group);
	}
	
	public final boolean reloadFromConfigAndSaveToFile() {
		if(!this.isPlayerOnline()) {
			return this.saveToFile();
		}
		if(this.getAttachment() != null) {
			this.getAttachment().remove();
			this.attachment = null;
			if(this.getAttachment() != null) {// This is not dead code. Read it again.
				this.loadPermissionsFromGroup(this.group);
				this.reloadPlayerPermissions();
				return this.saveToFile();
			}
		} else {
			this.loadPermissionsFromGroup(this.group);
			return this.saveToFile();
		}
		return false;
	}
	
	public static final PlayerPermissions getPlayerPermissions(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerPermissions.getPlayerPermissions(player.getUniqueId());
	}
	
	public static final PlayerPermissions getPlayerPermissions(UUID uuid) {
		if(uuid == null || uuid.toString().equals(Main.consoleUUID.toString())) {
			return null;
		}
		for(PlayerPermissions perms : PlayerPermissions.getInstances()) {
			if(perms.uuid.toString().equals(uuid.toString())) {
				return perms;
			}
		}
		PlayerPermissions perms = new PlayerPermissions(uuid);
		perms.loadFromFile();
		return perms;
	}
	
	@Override
	//@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(PlayerJoinEvent event) {
		System.out.println("PlayerPermissions.onPlayerJoin():");
		final long startTime = System.currentTimeMillis();
		final Player player = event.getPlayer();
		this.hasRunPlayerQuitEvt = false;
		if(this.hasRunPlayerJoinEvt) {
			return;
		}
		this.hasRunPlayerJoinEvt = true;
		if(this.isDisposed()) {
			return;
		}
		Main.DEBUG("PlayerPermissions: onPlayerJoinEvent: " + this.name);
		if(SavablePlayerData.playerEquals(this.getPlayer(), player)) {
			this.getAttachment();
			this.loadFromFile();
			if(this.group == null) {
				this.group = Group.getDefaultGroup();
				if(this.group != null) {
					String msg = Main.pluginName + Main.playerPromotedMessage.replace("PLAYERNAME", player.getDisplayName()).replace("GROUPNAME", this.group.displayName);
					for(Player p : Main.server.getOnlinePlayers()) {
						if(!p.getUniqueId().toString().equals(player.getUniqueId().toString())) {
							Main.sendMessage(p, msg);
						}
					}
					Main.sendConsoleMessage(msg);
					Main.sendMessage(player, Main.pluginName + "&aYou have been promoted to the \"&f" + this.group.displayName + "&r&a\" rank!");
				}
			}
		}
		System.out.println("End of PlayerPermissions.onPlayerJoin(): " + StringUtil.getElapsedTime(System.currentTimeMillis() - startTime, true));
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerQuit(PlayerQuitEvent event) {
		this.hasRunPlayerJoinEvt = false;
		if(this.hasRunPlayerQuitEvt) {
			return;
		}
		this.hasRunPlayerQuitEvt = true;
		if(this.isDisposed()) {
			HandlerList.unregisterAll(this);
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
		HandlerList.unregisterAll(this);
	}
	
	//============================================================================================
	
	/** @return An ArrayList containing all of the permissions that this player
	 *         has(including any permissions inherited from groups etc). */
	public final ArrayList<String> getAllPermissions() {
		ArrayList<String> perms = new ArrayList<>(this.permissions);
		if(this.group != null) {
			for(String perm : this.group.getAllPermissions()) {
				if(!perms.contains(perm)) {
					perms.add(perm);
				}
			}
		}
		return perms;
	}
	
	public final boolean isAMemberOfGroup(final Group group) {
		if(this.group == null || group == null) {
			return false;
		}
		if(this.group == group || this.group.name.equalsIgnoreCase(group.name)) {
			return true;
		}
		return this.group.isDescendantOf(group);
	}
	
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
		if(uuid.toString().equals(Main.consoleUUID.toString())) {
			return true;
		}
		PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
		boolean rtrn = perms.hasPermission(permission);
		if(!perms.isPlayerOnline()) {
			perms.dispose();
		}
		return rtrn;
	}
	
	private final boolean hasPerm(String perm) {
		for(String permission : this.getAllPermissions()) {
			if(permission.equalsIgnoreCase(perm)) {
				return true;
			}
		}
		return false;
	}
	
	public final boolean hasPermissionExplicit(String permission) {
		if(this.isPlayerOnline()) {
			if(this.isOperator != this.getPlayer().isOp()) {
				this.isOperator = this.getPlayer().isOp();
				this.saveToFile();
			}
		}
		if(permission == null) {
			return false;
		}
		if(this.hasPerm(permission)) {
			return true;
		}
		final String[] args = permission.split("\\.");
		final String[] args2 = permission.split("\\.");
		for(int i = 0; i < args.length; i++) {
			String curPerm = Main.getElementsFromStringArrayAtIndexesAsString(args2, 0, i, '.') + ".*";
			if(this.hasPerm(curPerm)) {
				return true;
			}
		}
		return false;
	}
	
	public final boolean hasPermission(String permission) {
		if(this.isPlayerOnline()) {
			if(this.isOperator != this.getPlayer().isOp()) {
				this.isOperator = this.getPlayer().isOp();
				this.saveToFile();
			}
		}
		if(permission == null) {
			return false;
		}
		if(this.isOperator) {
			return true;
		}
		if(this.hasPermissionExplicit(permission)) {
			return true;
		}
		/*if(this.group != null) {
			for(String perm : this.group.getAllPermissions()) {
				if(perm.equalsIgnoreCase(permission)) {
					return true;
				}
			}
		}*/
		return false;
	}
	
	public static final void setPermission(Player player, String permission, boolean allowed) {
		if(player == null || permission == null) {
			return;
		}
		PlayerPermissions.setPermission(player.getUniqueId(), permission, allowed);
	}
	
	public static final void setPermission(UUID uuid, String permission, boolean allowed) {
		if(uuid == null || uuid.toString().equals(Main.consoleUUID.toString()) || permission == null) {
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
		final boolean didAnything;
		if(allowed) {
			didAnything = this.permissions.add(permission);
		} else {
			didAnything = this.permissions.remove(permission);
		}
		if(didAnything) {
			if(this.getAttachment() != null) {
				this.getAttachment().setPermission(permission, allowed);
			}
			this.saveToFile();//this.saveAndDisposeIfPlayerNotOnline();
		}
		return didAnything;
	}
	
	/** Player groups
	 * 
	 * @author Brian_Entei */
	public static final class Group extends SavablePlayerData {//TODO make this extend savable plugin data, that would make things easier... ...and maybe put this in its own file as well.
		/** An ArrayList containing ALL of the currently instantiated Group
		 * objects that haven't been disposed. */
		public static final ArrayList<Group>	groups	= new ArrayList<>();
		public static YamlConfiguration			config;
		
		public static final ArrayList<Group> getInstances() {
			return new ArrayList<>(Group.groups);
		}
		
		public boolean					isDefault;
		public String					displayName;
		public final ArrayList<Group>	inheritances	= new ArrayList<>();
		public Group					nextGroup;
		public double					costToRankup;
		public String					moneyOrCredit	= "NONE";
		public final ArrayList<String>	permissions		= new ArrayList<>();
		
		public static final Group getDefaultGroup() {
			for(Group group : new ArrayList<>(Group.groups)) {
				if(group.isDefault) {
					return group;
				}
			}
			return null;
		}
		
		@Override
		public final String toString() {
			String rtrn = "&3Group \"&3" + this.name + "&r&3\":\n" + //
					"Default Group: &e" + this.isDefault + "&3\n" + //
					"Display Name: \"&f" + this.displayName + "&r&3\"\n" + //
					"# of inherited groups: &e" + this.inheritances.size() + "&3\n" + //
					"Next Group(Rankup): &e" + (this.nextGroup != null ? this.nextGroup.name : "null") + "&3\n" + //
					"Requires: &f" + Main.decimal.format(this.costToRankup) + "&3\n" + //
					"Requirement Type: &f" + this.moneyOrCredit.toLowerCase() + "&3\n" + //
					"Total # of permissions(including inherited ones): &e" + this.getAllPermissions().size() + "&3\n" + //
					"Total # of permissions(this group only): &e" + this.permissions.size();
			return Main.formatColorCodes(rtrn);
		}
		
		public final boolean setDefault(boolean isDefault) {
			if(Group.getDefaultGroup() != null && Group.getDefaultGroup() != this) {
				return false;
			}
			boolean wasDefault = this.isDefault;
			this.isDefault = isDefault;
			PlayerPermissions.refreshPlayerPermissions();
			return(wasDefault != this.isDefault);
		}
		
		protected final boolean hasPerm(String perm) {
			for(String permission : this.permissions) {
				if(permission.equalsIgnoreCase(perm)) {
					return true;
				}
			}
			return false;
		}
		
		public final ArrayList<String> getAllPermissions() {
			ArrayList<String> rtrn = new ArrayList<>();
			rtrn.addAll(this.permissions);
			if(!this.inheritances.isEmpty()) {
				for(Group inheritance : this.inheritances) {
					for(String perm : inheritance.getAllPermissions()) {
						if(!rtrn.contains(perm)) {
							rtrn.add(perm);
						}
					}
				}
			}
			return rtrn;
		}
		
		public final boolean canRankup() {
			return this.nextGroup != null;
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
				Main.sendConsoleMessage(Main.pluginName + "&aLoaded &f" + Group.groups.size() + "&a groups from file.");
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
		
		/** This actually saves ALL loaded groups to file; this can be deceptive
		 * and will likely be changed in the future. */
		@Override
		public final boolean saveToFile() {
			return Group.saveToStaticFile();
		}
		
		public static final boolean saveToStaticFile() {
			Main.sendConsoleMessage(Main.pluginName + "&aSaving groups to groups.yml file...");
			Group.config = null;//Makes the files get wiped before saving. Comment this line if things begin disappearing, then find out why they are disappearing and un-comment.
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
			Group check = Group.getGroupByName(groupName);
			if(check != null) {
				return check;
			}
			ConfigurationSection memSection = Group.getConfig().getConfigurationSection("groups");
			if(memSection == null) {
				memSection = Group.getConfig().createSection("groups");
			}
			Group newGroup = new Group(groupName, memSection);
			newGroup.displayName = "&f" + Main.capitalizeFirstLetter(newGroup.name);
			return newGroup;
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
		public final void onFirstLoad() {
			
		}
		
		@Override
		public void loadFromConfig(ConfigurationSection mem) {
			this.permissions.clear();
			final ConfigurationSection group = mem.getConfigurationSection(this.name);
			if(group != null) {
				Main.DEBUG("&aLoading group \"&f" + this.name + "&r&a\" from groups.yml file...");
				this.setDefault(group.getBoolean("isDefault"));
				this.displayName = group.getString("displayName");
				if(this.displayName == null || this.displayName.isEmpty()) {
					this.displayName = "&f" + Main.capitalizeFirstLetter(this.name);
				}
				final List<String> inheritedGroupNames = group.getStringList("inheritances");
				final String nextGroupName = group.getString("nextGroup", "");
				this.costToRankup = group.getDouble("costToRankup");
				this.moneyOrCredit = group.getString("moneyOrCredit");
				if(this.moneyOrCredit == null || this.moneyOrCredit.isEmpty()) {
					this.moneyOrCredit = "NONE";
				}
				this.moneyOrCredit = this.moneyOrCredit.toUpperCase();
				if(group.getStringList("permissions") == null) {
					group.set("permissions", this.permissions);
					Main.DEBUG("&5Created group.yml: groups." + this.name + ".permissions entry!");
				} else {
					Main.DEBUG("&5group.yml: groups." + this.name + ".permissions already existed! Let's load from it...");
				}
				List<String> permissions = group.getStringList("permissions");
				Main.DEBUG("&dgroup.getStringList(\"permissions\").size(): \"&f" + permissions.size() + "&r&d\"!");
				for(String perm : permissions) {
					this.permissions.add(perm);
					Main.DEBUG("&aLoaded group permission \"&f" + perm + "&r&a\" for group \"&f" + this.name + "&r&a\"!");
				}
				final Group THIS = this;
				Main.server.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
					@Override
					public void run() {//This waits for a total of 25 seconds before giving up.
						int tries = 0;
						while(THIS.nextGroup == null) {
							if(nextGroupName == null || nextGroupName.isEmpty()) {
								break;
							}
							THIS.nextGroup = Group.getGroupByName(nextGroupName);
							tries++;
							try {
								Thread.sleep(250);
							} catch(Throwable ignored) {
							}
							if(tries >= 100) {
								Main.sendConsoleMessage(Main.pluginName + "&cUnable to load variable nextGroup for group \"&f" + THIS.name + "&r&c\"!");
								break;
							}
						}
					}
				});
				Main.server.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
					@Override
					public void run() {//same here.
						int tries = 0;
						while(THIS.inheritances.size() != inheritedGroupNames.size()) {
							if(inheritedGroupNames.isEmpty()) {
								break;
							}
							for(int i = 0; i < inheritedGroupNames.size(); i++) {
								Group loadedGroup = Group.getGroupByName(inheritedGroupNames.get(i));
								if(loadedGroup != null) {
									THIS.inheritances.add(loadedGroup);
								}
							}
							tries++;
							try {
								Thread.sleep(250);
							} catch(Throwable ignored) {
							}
							if(tries >= 100) {
								Main.sendConsoleMessage(Main.pluginName + "&cUnable to load variable inheritance for group \"&f" + THIS.name + "&r&c\"!");
								break;
							}
						}
					}
				});
			}
		}
		
		@Override
		public void saveToConfig(ConfigurationSection mem) {
			ConfigurationSection group = mem.getConfigurationSection(this.name);
			if(group == null) {
				group = mem.createSection(this.name);
			}
			Main.DEBUG("&aSaving group \"&f" + this.name + "&r&a\" to groups.yml file...");
			group.set("isDefault", new Boolean(this.isDefault));
			group.set("displayName", this.displayName);
			group.set("inheritances", this.getInheritedGroupNames());
			group.set("nextGroup", (this.nextGroup != null ? this.nextGroup.name : ""));
			group.set("costToRankup", new Double(this.costToRankup));
			group.set("moneyOrCredit", this.moneyOrCredit);
			group.set("permissions", this.permissions);
		}
		
		public final boolean isAncestorOf(Group group) {
			if(group == null || group.isDisposed()) {
				return false;
			}
			return group.isDescendantOf(this);
		}
		
		public final boolean isDescendantOf(Group group) {
			if(group == null || group.isDisposed()) {
				return false;
			}
			if(group == this) {
				return true;
			}
			for(Group inheritance : this.inheritances) {
				if(group == inheritance || inheritance.name.equalsIgnoreCase(group.name)) {
					return true;
				}
				if(inheritance.isDescendantOf(group)) {
					return true;
				}
			}
			return false;
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
		
		public final boolean addInheritance(Group group) {
			int oldSize = this.inheritances.size();
			if(group != null) {
				if(!group.isDescendantOf(this)) {//prevents stackoverflow errors etc
					if(!this.inheritances.contains(group)) {
						this.inheritances.add(group);
					}
				}
			}
			PlayerPermissions.refreshPlayerPermissions();
			return oldSize != this.inheritances.size();
		}
		
		public final boolean removeInheritance(Group group) {
			int oldSize = this.inheritances.size();
			if(group != null) {
				if(this.inheritances.contains(group)) {
					this.inheritances.remove(group);
				}
			}
			PlayerPermissions.refreshPlayerPermissions();
			return oldSize != this.inheritances.size();
		}
		
		public final Group getInheritedGroupByName(String name) {
			for(Group group : this.inheritances) {
				if(group.name.equalsIgnoreCase(name)) {
					return group;
				}
			}
			return null;
		}
		
		public final boolean setNextGroup(Group group) {
			Group oldNextGroup = this.nextGroup;
			if(group == this) {
				group = oldNextGroup;
			}
			this.nextGroup = group;
			PlayerPermissions.refreshPlayerPermissions();
			return this.nextGroup != oldNextGroup;
		}
		
		public final ArrayList<String> getInheritedGroupNames() {
			ArrayList<String> rtrn = new ArrayList<>();
			for(Group group : this.inheritances) {
				rtrn.add(group.name);
			}
			return rtrn;
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
			this.dispose(false);
		}
		
		public void dispose(boolean removeFromOtherGroups) {
			if(removeFromOtherGroups) {
				for(PlayerPermissions perm : new ArrayList<>(PlayerPermissions.permInstances)) {
					if(perm.group == this) {
						perm.changeGroup(null);
					}
				}
				for(Group group : new ArrayList<>(Group.groups)) {
					if(group != this && group != null) {
						if(group.nextGroup == this) {
							group.setNextGroup(null);
						}
						group.removeInheritance(this);
					}
				}
			}
			super.dispose();
			Group.groups.remove(this);
			if(removeFromOtherGroups) {
				try {
					Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
						@Override
						public void run() {
							PlayerPermissions.refreshPlayerPermissions();
						}
					});
				} catch(IllegalPluginAccessException ignored) {
				}
			}
		}
		
	}
	
}
