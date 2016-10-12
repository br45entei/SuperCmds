package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.api.Permissions;
import com.gmail.br45entei.supercmds.cmds.InventoryViewingInfo;
import com.gmail.br45entei.supercmds.cmds.MainCmdListener;
import com.gmail.br45entei.supercmds.file.inventory.GamemodeInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

@SuppressWarnings("javadoc")
public final strictfp class PlayerStatus extends SavablePlayerData {
	private static final ArrayList<PlayerStatus> statuses = new ArrayList<>();
	
	public static final ArrayList<PlayerStatus> getAllStatuses() {
		return new ArrayList<>(statuses);
	}
	
	public volatile boolean				isAfk					= false;
	
	public volatile Location			lastAfkLocation			= null;
	
	public volatile boolean				isThorModeOn			= false;
	
	public volatile boolean				isGodModeOn				= false;
	
	public volatile boolean				isFlyModeOn				= false;
	
	public volatile boolean				isVanishModeOn			= false;
	
	public volatile GameMode			lastGameMode			= null;
	
	public volatile UUID				lastWorld				= null;
	
	public volatile double				lastHealth				= 20.0D;
	
	public volatile double				lastMaxHealth			= 20.0D;
	
	public volatile float				lastFoodSaturation		= 5.0F;
	
	public volatile int					lastFoodLevel			= 20;
	
	public volatile int					lastXpLevel				= 0;
	
	public volatile float				lastXp					= 0.0F;
	
	public volatile Location			lastTeleportLoc			= null;
	
	public volatile boolean				hasDefaultHome			= false;
	
	public volatile Location			homeLocation			= null;
	
	public volatile boolean				wasFlyingLast			= false;
	
	public final HashMap<String, Long>	lastKitTimes			= new HashMap<>();
	
	public final ArrayList<ItemStack>	leftOverItems			= new ArrayList<>();
	
	public volatile boolean				wasGodModeOnBeforeSwap	= false;
	
	public volatile boolean				wasFlyModeOnBeforeSwap	= false;
	
	public volatile float				lastWalkSpeed			= 0.2F;
	
	public volatile float				lastFlySpeed			= 0.1F;
	
	private volatile long				lastLogoutTime			= -1L;
	
	private volatile String				lastIPAddress			= "";
	
	public volatile Location			lastKnownLocation		= null;
	
	public volatile boolean				canOtherPlayersEditInv	= true;
	
	public final ArrayList<Home>		homes					= new ArrayList<>();
	
	public volatile boolean				isSocialSpyModeOn		= false;
	
	public volatile int					hearts					= 3;
	
	public volatile double				health					= 6.0D;
	
	public volatile boolean				hasReadRules			= false;
	
	protected final ArrayList<Home>		homesWaitingOnWorlds	= new ArrayList<>();
	protected ConfigurationSection		tempHomesSection		= null;
	protected ConfigurationSection		tempDefaultHomeSection	= null;
	
	public volatile Warps.Warp			lastDeletedWarp			= null;
	
	public volatile Kits.Kit			lastDeletedKit			= null;
	
	public volatile Home				lastDeletedHome			= null;
	
	public volatile boolean				isInBed					= false;
	protected volatile int				bedSchedulerTID			= 0;
	
	public volatile long				loginTime				= 0L;
	
	public volatile long				lastVanishTime			= -1L;
	
	public volatile UUID				invOwnerUUID			= null;
	
	public volatile GameMode			invOwnerGameMode		= null;
	
	public volatile UUID				invOwnerWorld			= null;
	
	public volatile String				invTypeBeingViewed		= "NONE";
	
	public volatile boolean				oreMainEditWandOn		= false;
	
	public volatile Location			oreRegionLeftLoc		= null;
	
	public volatile Location			oreRegionRightLoc		= null;
	
	public final void setViewingInv(UUID target, GameMode mode, UUID world, String invType) {
		if((target == null) || (mode == null) || (world == null) || (invType == null) || (invType.isEmpty())) {
			this.invOwnerUUID = null;
			this.invOwnerGameMode = null;
			this.invOwnerWorld = null;
			this.invTypeBeingViewed = "NONE";
			return;
		}
		World w = Main.server.getWorld(world);
		if(w != null) {
			w = GamemodeInventory.getCorrectWorldToUse(w);
			world = w.getUID();
		}
		mode = GamemodeInventory.getCorrectGameModeToUse(mode);
		this.invOwnerUUID = target;
		this.invOwnerGameMode = mode;
		this.invOwnerWorld = world;
		this.invTypeBeingViewed = invType;
	}
	
	public final boolean isViewingInvFor(UUID owner, GameMode mode, UUID world) {
		if((this.invOwnerUUID == null) || (this.invOwnerGameMode == null) || (this.invOwnerWorld == null) || (owner == null) || (mode == null) || (world == null)) {
			return false;
		}
		World w = Main.server.getWorld(world);
		if(w != null) {
			w = GamemodeInventory.getCorrectWorldToUse(w);
			world = w.getUID();
		}
		mode = GamemodeInventory.getCorrectGameModeToUse(mode);
		return (this.invOwnerUUID.toString().equals(owner.toString())) && (this.invOwnerGameMode == mode) && (this.invOwnerWorld.toString().equals(world.toString()));
	}
	
	public final ItemStack getSavedHelmet(GameMode mode, UUID world) {
		return GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).getHelmet(world, mode);
	}
	
	public final ItemStack getSavedChestplate(GameMode mode, UUID world) {
		return GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).getChestplate(world, mode);
	}
	
	public final ItemStack getSavedLeggings(GameMode mode, UUID world) {
		return GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).getLeggings(world, mode);
	}
	
	public final ItemStack getSavedBoots(GameMode mode, UUID world) {
		return GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).getBoots(world, mode);
	}
	
	public final ItemStack getSavedShieldItem(GameMode mode, UUID world) {
		return GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).getShieldItem(world, mode);
	}
	
	public final PlayerStatus setSavedHelmet(GameMode mode, UUID world, ItemStack item) {
		GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).setHelmet(world, mode, item);
		return this;
	}
	
	public final PlayerStatus setSavedChestplate(GameMode mode, UUID world, ItemStack item) {
		GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).setChestplate(world, mode, item);
		return this;
	}
	
	public final PlayerStatus setSavedLeggings(GameMode mode, UUID world, ItemStack item) {
		GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).setLeggings(world, mode, item);
		return this;
	}
	
	public final PlayerStatus setSavedBoots(GameMode mode, UUID world, ItemStack item) {
		GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).setBoots(world, mode, item);
		return this;
	}
	
	public final PlayerStatus setSavedShieldItem(GameMode mode, UUID world, ItemStack item) {
		GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).setShieldItem(world, mode, item);
		return this;
	}
	
	public final boolean setSavedInventory(String invOrEnder, GameMode mode, UUID world, Map<Integer, ItemStack> items) {
		if((invOrEnder == null) || (invOrEnder.isEmpty()) || (mode == null) || (world == null)) {
			return false;
		}
		if(invOrEnder.equals("inv")) {
			GamemodeInventory.copyItemMapIntoInventory(items, GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).getInventory(world, mode), true);
			return true;
		}
		if(invOrEnder.equals("ender")) {
			GamemodeInventory.copyItemMapIntoInventory(items, GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid).getEnderChest(world, mode), true);
			return true;
		}
		return false;
	}
	
	public final Map<Integer, ItemStack> getSavedInventory(String invOrEnder, GameMode mode, UUID world) {
		if((invOrEnder == null) || (invOrEnder.isEmpty()) || (mode == null) || (world == null)) {
			return null;
		}
		world = GamemodeInventory.getCorrectWorldToUse(world);
		GamemodeInventory gmi = GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid);
		if(invOrEnder.equals("inv")) return GamemodeInventory.getItemMapOfInventory(gmi.getInventory(world, mode));
		if(invOrEnder.equals("ender")) {
			return GamemodeInventory.getItemMapOfInventory(gmi.getEnderChest(world, mode));
		}
		return new HashMap<>();
	}
	
	public final boolean updateSavedInventoryFromPlayer(boolean oneTickLater, boolean updateTopInventories) {
		final Player player = getPlayer();
		if(player == null) {
			return false;
		}
		if(updateTopInventories) {
			MainCmdListener.updateTopInventoriesFromOwnerChanges(player, oneTickLater);
			return true;
		}
		final PlayerStatus THIS = this;
		Runnable code = new Runnable() {
			@Override
			public final void run() {
				GameMode mode = GamemodeInventory.getCorrectGameModeToUse(player.getGameMode());
				UUID w = GamemodeInventory.getCorrectWorldToUse(player.getWorld().getUID());
				THIS.setSavedInventory("inv", mode, w, GamemodeInventory.getItemMapOfInventory(player.getInventory()));
				THIS.setSavedInventory("ender", mode, w, GamemodeInventory.getItemMapOfInventory(player.getEnderChest()));
				
				THIS.setSavedHelmet(mode, w, player.getInventory().getHelmet());
				THIS.setSavedChestplate(mode, w, player.getInventory().getChestplate());
				THIS.setSavedLeggings(mode, w, player.getInventory().getLeggings());
				THIS.setSavedBoots(mode, w, player.getInventory().getBoots());
				THIS.setSavedShieldItem(mode, w, player.getInventory().getItemInOffHand());
			}
		};
		if(oneTickLater) {
			Main.scheduler.runTask(Main.getInstance(), code);
		} else {
			code.run();
		}
		return true;
	}
	
	public final boolean updateSavedInventory(GameMode mode, World world, Map<Integer, ItemStack> sourceInv, Map<Integer, ItemStack> sourceEnder, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack shield) {
		mode = GamemodeInventory.getCorrectGameModeToUse(mode);
		UUID w = GamemodeInventory.getCorrectWorldToUse(world).getUID();
		setSavedInventory("inv", mode, w, sourceInv);
		setSavedInventory("ender", mode, w, sourceEnder);
		
		setSavedHelmet(mode, w, helmet);
		setSavedChestplate(mode, w, chestplate);
		setSavedLeggings(mode, w, leggings);
		setSavedBoots(mode, w, boots);
		setSavedShieldItem(mode, w, shield);
		return true;
	}
	
	public final void updatePlayerFromSavedInventory(boolean usePlayerUpdate) {
		Player player = getPlayer();
		if(player != null) {
			GamemodeInventory gmi = GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid);
			gmi.updatePlayerInventory(player, usePlayerUpdate);
		}
	}
	
	public final void updatePlayerFromSavedInventory(boolean usePlayerUpdate, String invType) {
		Player player = getPlayer();
		if(player != null) {
			GamemodeInventory gmi = GamemodeInventory.getGamemodeInventoryForPlayer(this.uuid);
			gmi.updatePlayerInventory(player, invType, usePlayerUpdate);
		}
	}
	
	@Override
	public String getSaveFolderName() {
		return "PlayerCmdData";
	}
	
	@Override
	public final void onFirstLoad() {
		updateFromPlayer(false, false);
		saveToFile();
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		this.isAfk = mem.getBoolean("isAfk");
		this.lastAfkLocation = SavablePlayerData.getLocationFromConfig("lastAfkLocation", mem);
		this.isThorModeOn = mem.getBoolean("isThorModeOn");
		this.isGodModeOn = mem.getBoolean("isGodModeOn");
		this.isFlyModeOn = mem.getBoolean("isFlyModeOn");
		this.isVanishModeOn = mem.getBoolean("isVanishModeOn");
		if(this.isVanishModeOn) {
			this.lastVanishTime = System.currentTimeMillis();
		}
		this.lastWorld = ((mem.getString("lastWorld") != null) && (Main.isStringUUID(mem.getString("lastWorld"))) ? UUID.fromString(mem.getString("lastWorld")) : Main.server.getWorlds().get(0).getUID());
		this.lastGameMode = (isPlayerOnline() ? getPlayer().getGameMode() : mem.getString("lastGameMode") != null ? null : isPlayerOnline() ? getPlayer().getGameMode() : mem.getString("lastGameMode").equalsIgnoreCase("spectator") ? GameMode.SPECTATOR : mem.getString("lastGameMode").equalsIgnoreCase("adventure") ? GameMode.ADVENTURE : mem.getString("lastGameMode").equalsIgnoreCase("creative") ? GameMode.CREATIVE : mem.getString("lastGameMode").equalsIgnoreCase("survival") ? GameMode.SURVIVAL : GameMode.SURVIVAL);
		this.lastHealth = mem.getDouble("lastHealth");
		this.lastMaxHealth = mem.getDouble("lastMaxHealth");
		this.lastFoodSaturation = new Double(mem.getDouble("lastFoodSaturation")).floatValue();
		this.lastFoodLevel = mem.getInt("lastFoodLevel");
		this.lastXpLevel = mem.getInt("lastXpLevel");
		this.lastXp = new Double(mem.getDouble("lastXp")).floatValue();
		this.lastTeleportLoc = SavablePlayerData.getLocationFromConfig("lastTeleportLoc", mem);
		this.hasDefaultHome = mem.getBoolean("hasHome");
		this.homeLocation = SavablePlayerData.getLocationFromConfig("homeLocation", mem);
		if(this.homeLocation == null) {
			this.tempDefaultHomeSection = mem;
		}
		this.wasFlyingLast = mem.getBoolean("wasFlyingLast");
		try {
			ConfigurationSection lastKitTimes = mem.getConfigurationSection("lastKitTimes");
			if(lastKitTimes != null) {
				for(String key : lastKitTimes.getKeys(false)) {
					this.lastKitTimes.put(key, new Long(lastKitTimes.getLong(key)));
				}
			}
		} catch(Throwable e) {
			Main.sendConsoleMessage(Main.pluginName + "&eAn error occurred while loading lastKitTimes for player \"&f" + this.name + "&r&e\":&z&c" + Main.throwableToStr(e));
		}
		
		ItemStack[] leftOverItems = SavablePluginData.getItemsFromConfig("leftOverItems", mem);
		this.leftOverItems.clear();
		for(ItemStack item : leftOverItems) {
			this.leftOverItems.add(item);
		}
		
		this.wasGodModeOnBeforeSwap = mem.getBoolean("wasGodModeOnBeforeSwap", false);
		this.wasFlyModeOnBeforeSwap = mem.getBoolean("wasFlyModeOnBeforeSwap", false);
		this.lastWalkSpeed = new Double(mem.getDouble("lastWalkSpeed", 0.2D)).floatValue();
		this.lastFlySpeed = new Double(mem.getDouble("lastFlySpeed", 0.1D)).floatValue();
		this.lastLogoutTime = mem.getLong("lastLogoutTime", -1L);
		this.lastIPAddress = mem.getString("lastIPAddress", "");
		this.lastKnownLocation = SavablePlayerData.getLocationFromConfig("lastKnownLocation", mem);
		this.canOtherPlayersEditInv = mem.getBoolean("canOtherPlayersEditInv", true);
		ConfigurationSection homesSection = mem.getConfigurationSection("homes");
		if(homesSection != null) {
			for(String key : homesSection.getKeys(false)) {
				Home home = Home.getFromConfig(key, homesSection);
				if(home.location == null) {
					this.homesWaitingOnWorlds.add(home);
					if(this.tempHomesSection == null) {
						this.tempHomesSection = homesSection;
					}
				}
				this.homes.add(home);
			}
			Main.sendConsoleMessage(Main.pluginName + "&aLoaded &f" + this.homes.size() + "&r&a extra homes from file for player \"&f" + getPlayerDisplayName() + "&r&a\".");
		}
		this.isSocialSpyModeOn = mem.getBoolean("isSocialSpyModeOn", false);
		this.hearts = mem.getInt("hearts", 3);
		this.health = mem.getDouble("health", 6.0D);
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		mem.set("isAfk", new Boolean(this.isAfk));
		SavablePlayerData.saveLocationToConfig("lastAfkLocation", this.lastAfkLocation, mem, true);
		mem.set("isThorModeOn", new Boolean(this.isThorModeOn));
		mem.set("isGodModeOn", new Boolean(this.isGodModeOn));
		mem.set("isFlyModeOn", new Boolean(this.isFlyModeOn));
		mem.set("isVanishModeOn", new Boolean(this.isVanishModeOn));
		mem.set("lastWorld", this.lastWorld != null ? this.lastWorld.toString() : getPlayer() != null ? getPlayer().getWorld().getUID().toString() : null);
		mem.set("lastGameMode", this.lastGameMode != null ? this.lastGameMode.name().toLowerCase() : (getPlayer() != null ? getPlayer().getGameMode() : GameMode.SURVIVAL).name().toLowerCase());
		mem.set("lastHealth", Double.valueOf(this.lastHealth));
		mem.set("lastMaxHealth", Double.valueOf(this.lastMaxHealth));
		mem.set("lastFoodSaturation", Float.valueOf(this.lastFoodSaturation));
		mem.set("lastFoodLevel", Integer.valueOf(this.lastFoodLevel));
		mem.set("lastXpLevel", Integer.valueOf(this.lastXpLevel));
		mem.set("lastXp", Float.valueOf(this.lastXp));
		SavablePlayerData.saveLocationToConfig("lastTeleportLoc", this.lastTeleportLoc, mem, true);
		mem.set("hasHome", new Boolean(this.hasDefaultHome));
		SavablePlayerData.saveLocationToConfig("homeLocation", this.homeLocation, mem, true);
		mem.set("wasFlyingLast", new Boolean(this.wasFlyingLast));
		
		ConfigurationSection lastKitTimes = mem.getConfigurationSection("lastKitTimes");
		if(lastKitTimes == null) {
			lastKitTimes = mem.createSection("lastKitTimes");
		}
		for(Map.Entry<String, Long> entry : this.lastKitTimes.entrySet()) {
			lastKitTimes.set(entry.getKey(), entry.getValue());
		}
		
		ItemStack[] items = new ItemStack[this.leftOverItems.size()];
		for(int i = 0; i < this.leftOverItems.size(); i++) {
			items[i] = (this.leftOverItems.get(i));
		}
		SavablePluginData.saveItemsToConfig("leftOverItems", items, mem);
		
		mem.set("wasGodModeOnBeforeSwap", Boolean.valueOf(this.wasGodModeOnBeforeSwap));
		mem.set("wasFlyModeOnBeforeSwap", Boolean.valueOf(this.wasFlyModeOnBeforeSwap));
		mem.set("lastWalkSpeed", Float.valueOf(this.lastWalkSpeed));
		mem.set("lastFlySpeed", Float.valueOf(this.lastFlySpeed));
		mem.set("lastLogoutTime", new Long(this.lastLogoutTime));
		mem.set("lastIPAddress", this.lastIPAddress);
		SavablePlayerData.saveLocationToConfig("lastKnownLocation", this.lastKnownLocation, mem, true);
		mem.set("canOtherPlayersEditInv", Boolean.valueOf(this.canOtherPlayersEditInv));
		ConfigurationSection homesSection = mem.getConfigurationSection("homes");
		if(homesSection == null) {
			homesSection = mem.createSection("homes");
		}
		for(Home home : this.homes) {
			home.saveToConfig(homesSection);
		}
		mem.set("isSocialSpyModeOn", new Boolean(this.isSocialSpyModeOn));
		mem.set("hearts", new Integer(this.hearts));
		mem.set("health", new Double(this.health));
	}
	
	public static final PlayerStatus getPlayerStatus(Player player) {
		if(player == null) {
			return null;
		}
		return getPlayerStatus(player.getUniqueId());
	}
	
	public static final PlayerStatus getPlayerStatus(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(PlayerStatus status : statuses) {
			if(status.uuid.toString().equals(uuid.toString())) {
				return status;
			}
		}
		PlayerStatus status = new PlayerStatus(uuid);
		status.loadFromFile();
		return status;
	}
	
	private PlayerStatus(UUID uuid) {
		super(uuid, Main.uuidMasterList.getPlayerNameFromUUID(uuid));
		statuses.add(this);
	}
	
	private PlayerStatus(Player player) {
		super(player);
		statuses.add(this);
	}
	
	@Override
	public void dispose() {
		if(getPlayer() != null) {
			throw new NullPointerException("What the FUCK are you doing.");
		}
		super.dispose();
		statuses.remove(this);
	}
	
	public final void onPlayerEnterBed() {
		//System.out.println("onPlayerEnterBed(" + this.getPlayerName() + ")");
		final Player player = this.getPlayer();
		this.isInBed = true;
		this.bedSchedulerTID = Main.scheduler.scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(!PlayerStatus.this.isPlayerOnline()) {
					int tid = PlayerStatus.this.bedSchedulerTID;
					PlayerStatus.this.bedSchedulerTID = 0;
					PlayerStatus.this.isInBed = false;
					Main.scheduler.cancelTask(tid);
					return;
				}
				if(player.getHealth() < player.getMaxHealth()) {
					player.setHealth(player.getHealth() + 1.0D);
				}
			}
		}, 0L, 2L);
	}
	
	public final void onPlayerLeaveBed() {
		//System.out.println("onPlayerLeaveBed(" + this.getPlayerName() + ")");
		this.isInBed = false;
		Main.scheduler.cancelTask(this.bedSchedulerTID);
	}
	
	/** @param event Unused */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onWorldLoadEvent(WorldLoadEvent event) {
		if(this.tempHomesSection != null) {
			boolean didAnything = false;
			if((this.homeLocation == null) && (this.tempDefaultHomeSection != null)) {
				this.homeLocation = SavablePlayerData.getLocationFromConfig("homeLocation", this.tempDefaultHomeSection);
				if(this.homeLocation != null) {
					didAnything = true;
					this.tempDefaultHomeSection = null;
					Main.sendConsoleMessage(Main.pluginName + "&aSuccessfully loaded player \"&f" + getPlayerNickName() + "&r&a\"'s default home location from config after waiting on its world to load!");
				}
			}
			for(Home home : new ArrayList<>(this.homesWaitingOnWorlds)) {
				if(home.location == null) {
					ConfigurationSection homeSection = this.tempHomesSection.getConfigurationSection(home.name);
					if(homeSection != null) {
						home.location = SavablePlayerData.getLocationFromConfig("location", homeSection);
						if(home.location != null) {
							Main.sendConsoleMessage(Main.pluginName + "&aSuccessfully loaded player \"&f" + getPlayerNickName() + "&r&a\"'s custom home location for home \"&f" + home.name + "&r&a\" from config after waiting on its world to load!");
							didAnything = true;
						}
					}
				} else {
					this.homesWaitingOnWorlds.remove(home);
				}
			}
			if((this.homesWaitingOnWorlds.size() > 0) && (didAnything)) {
				Main.sendConsoleMessage(Main.pluginName + "&eThere are still other homes that are waiting on their respective worlds to load for player \"&f" + this.name + "&r&a\"! If the world does not load, then the home(s) will be un-usable.");
			}
		}
	}
	
	public void updatePlayerHearts(boolean setOrUpdate) {
		if(!isPlayerOnline()) {
			return;
		}
		if(setOrUpdate) {
			getPlayer().setMaxHealth(this.hearts * 2.0D);
			getPlayer().setHealth(this.health);
		} else {
			this.health = getPlayer().getHealth();
			this.hearts = new Long(Math.round(getPlayer().getMaxHealth() / 2.0D)).intValue();
		}
	}
	
	public final ArrayList<InventoryViewingInfo> getEveryoneViewingMyInventories() {
		ArrayList<InventoryViewingInfo> list = new ArrayList<>();
		for(PlayerStatus status : new ArrayList<>(statuses)) {
			if(status != this) {
				
				if(status.isPlayerOnline()) {
					InventoryViewingInfo invInfo = status.getInventoryBeingViewed();
					if((invInfo != null) && (invInfo.owner != null) && (invInfo.owner.toString().equals(this.uuid.toString())) && (!list.contains(invInfo))) {
						list.add(invInfo);
					}
				}
			}
		}
		return list;
	}
	
	@Override
	public final boolean disposeIfPlayerNotOnline() {
		return this.getEveryoneViewingMyInventories().isEmpty() ? super.disposeIfPlayerNotOnline() : false;
	}
	
	public final boolean isViewingSomeonesInventory() {
		InventoryViewingInfo invInfo = getInventoryBeingViewed();
		String invName = invInfo != null ? invInfo.inv.getName() : "null";
		Main.DEBUG("&5TEST: invName: " + invName);
		return (invInfo != null) && (invInfo.inv.getType() != InventoryType.CRAFTING) && (!invName.equals("container.crafting"));
	}
	
	public final InventoryViewingInfo getInventoryBeingViewed() {
		if((!isPlayerOnline()) || (this.invOwnerUUID == null) || (this.invOwnerGameMode == null) || (this.invOwnerWorld == null) || (this.invTypeBeingViewed == null) || (this.invTypeBeingViewed.isEmpty())) {
			return null;
		}
		return new InventoryViewingInfo(this.invOwnerUUID, this.invOwnerGameMode, this.invOwnerWorld, this.uuid, getPlayer().getOpenInventory().getTopInventory(), this.invTypeBeingViewed);
	}
	
	public static final ArrayList<InventoryViewingInfo> getAllOpenInventoriesOfTypeForPlayer(UUID owner, GameMode mode, UUID world, String invType) {
		ArrayList<InventoryViewingInfo> rtrn = new ArrayList<>();
		if((owner == null) || (mode == null) || (world == null) || (invType == null) || (invType.isEmpty())) {
			return rtrn;
		}
		World w = Main.server.getWorld(world);
		if(w != null) {
			w = GamemodeInventory.getCorrectWorldToUse(w);
			world = w.getUID();
		}
		mode = GamemodeInventory.getCorrectGameModeToUse(mode);
		ArrayList<PlayerStatus> statuses = getAllStatuses();
		for(PlayerStatus status : statuses) {
			InventoryViewingInfo invInfo = status.getInventoryBeingViewed();
			if((status.isPlayerOnline()) && (invInfo != null) && (invInfo.equals(owner, mode, world, invType))) {
				Inventory invBeingViewed = status.getPlayer().getOpenInventory().getTopInventory();
				if((invBeingViewed != null) && (invBeingViewed.getType() != InventoryType.CRAFTING) && (!invBeingViewed.getName().equals("container.crafting"))) {
					rtrn.add(invInfo);
				}
			}
		}
		return rtrn;
	}
	
	private final ArrayList<UUID>	playersRequestingToTpToMe		= new ArrayList<>();
	private final ArrayList<UUID>	playersRequestingMeToTpToThem	= new ArrayList<>();
	
	@Override
	public final void onPlayerJoin(PlayerJoinEvent event) {
		if(!isPlayerOnline()) {
			return;
		}
		if(!SavablePlayerData.playerEquals(event.getPlayer(), getPlayer())) {
			return;
		}
		if(!isLoadedFromFile()) {
			loadFromFile();
		}
		if(this.isVanishModeOn || (getPlayer().isOp() && !Main.displayOperatorJoinQuitMessages) || Permissions.hasPerm(getPlayer(), "supercmds.quietJoin")) {
			event.setJoinMessage(null);
		}
		this.loginTime = System.currentTimeMillis();
		final PlayerStatus THIS = this;
		Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(!THIS.isPlayerOnline()) {
					return;
				}
				Player player = THIS.getPlayer();
				player.setGameMode(THIS.lastGameMode != null ? THIS.lastGameMode : THIS.getPlayer().getGameMode());
				player.setAllowFlight((THIS.lastGameMode == GameMode.CREATIVE) || (THIS.lastGameMode == GameMode.SPECTATOR) ? true : THIS.wasFlyingLast ? true : THIS.isFlyModeOn);
				player.setFlying(THIS.wasFlyingLast);
				player.setMaxHealth((THIS.lastMaxHealth > 0.0D) && (THIS.lastMaxHealth <= 2000.0D) ? THIS.lastMaxHealth : player.getMaxHealth());
				player.setHealth((THIS.lastHealth > 0.0D) && (THIS.lastHealth <= 2000.0D) ? THIS.lastHealth : player.getHealth());
				player.setSaturation(THIS.lastFoodSaturation > 0.0F ? THIS.lastFoodSaturation : player.getSaturation());
				player.setFoodLevel((THIS.lastFoodLevel > 0) && (THIS.lastFoodLevel <= 20) ? THIS.lastFoodLevel : player.getFoodLevel());
				player.setLevel(THIS.lastXpLevel > 0 ? THIS.lastXpLevel : player.getLevel());
				player.setExp(THIS.lastXp > 0.0F ? THIS.lastXp : player.getExp());
				THIS.updatePlayerHearts(false);
				//updatePlayerStateStates();
			}
		}, 5L);
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerQuit(PlayerQuitEvent event) {
		if(!isPlayerOnline()) {
			return;
		}
		if(!SavablePlayerData.playerEquals(event.getPlayer(), getPlayer())) {
			return;
		}
		this.wasFlyingLast = getPlayer().isFlying();
		if(this.isVanishModeOn || (getPlayer().isOp() && !Main.displayOperatorJoinQuitMessages) || Permissions.hasPerm(getPlayer(), "supercmds.quietQuit")) {
			event.setQuitMessage(null);
		}
		this.lastLogoutTime = System.currentTimeMillis();
		updateFromPlayer(false, false);
	}
	
	public final void updateFromPlayer(boolean updateInventory, boolean updateTopInventories) {
		if(isPlayerOnline()) {
			this.lastWorld = getPlayer().getWorld().getUID();
			this.lastGameMode = getPlayer().getGameMode();
			this.lastHealth = getPlayer().getHealth();
			this.lastMaxHealth = getPlayer().getMaxHealth();
			this.lastFoodSaturation = getPlayer().getSaturation();
			this.lastFoodLevel = getPlayer().getFoodLevel();
			this.lastXpLevel = getPlayer().getLevel();
			this.lastXp = getPlayer().getExp();
			if(updateInventory) {
				updateSavedInventoryFromPlayer(false, updateTopInventories);
			}
			this.saveToFile();
		}
	}
	
	public final boolean canGetKit(Kits.Kit kit) {
		boolean iCanHasKitz = true;
		if(kit.obtainInterval > 0L) {
			Long lastTimeIGotThisKit = this.lastKitTimes.get(kit.name);
			if(lastTimeIGotThisKit == null) {
				this.lastKitTimes.put(kit.name, new Long(0L));
				return true;
			}
			long NOW = System.currentTimeMillis();
			long nextTimeKitWillBeAvailable = lastTimeIGotThisKit.longValue() + kit.obtainInterval * 1000L;
			if(nextTimeKitWillBeAvailable >= NOW) {
				iCanHasKitz = false;
			}
		}
		return iCanHasKitz;
	}
	
	public final int getNumOfHomes() {
		return this.homes.size() + (hasHome() ? 1 : 0);
	}
	
	public final boolean canSetAnotherHome() {
		return Permissions.hasPerm(this.uuid, "supercmds.homes." + getNumOfHomes() + 1);
	}
	
	public final boolean hasHome() {
		return this.homeLocation != null;
	}
	
	public final boolean hasHome(String name) {
		return getHome(name) != null;
	}
	
	public final Home getHome(String name) {
		for(Home home : this.homes) {
			if(home.name.equalsIgnoreCase(name)) {
				return home;
			}
		}
		return null;
	}
	
	public final void toggleAfkState() {
		if(!isPlayerOnline()) {
			return;
		}
		if(!this.isVanishModeOn) {
			this.isAfk = (!this.isAfk);
			if(this.isAfk) {
				this.lastAfkLocation = getPlayer().getLocation();
			}
			Main.broadcast("&7* &f" + getPlayerDisplayName() + "&r&7: is no" + (this.isAfk ? "w afk." : " longer afk."));
		}
	}
	
	public static final Team getVanishedTeam() {
		Team vanishedTeam = Main.server.getScoreboardManager().getMainScoreboard().getTeam("SuperCmds_Vanish");
		if(vanishedTeam == null) {
			vanishedTeam = Main.server.getScoreboardManager().getMainScoreboard().registerNewTeam("SuperCmds_Vanish");
		}
		vanishedTeam.setCanSeeFriendlyInvisibles(true);
		return vanishedTeam;
	}
	
	public static final void updatePlayerStateStates() {
		updatePlayerVanishStates();
		updatePlayerFlyModeStates();
		updatePlayerGodModeStates();
		updatePlayerThorModeStates();
		for(PlayerStatus status : new ArrayList<>(statuses)) {
			if(status.isPlayerOnline()) {
				status.lastKnownLocation = status.getPlayer().getLocation();
			}
			//status.saveToFile();
		}
		//PlayerPermissions.refreshPlayerPermissions();
	}
	
	@SuppressWarnings("deprecation")
	public static final void updatePlayerVanishStates() {
		Main.DEBUG(Main.pluginName + "&3Updating player vanish states!");
		Team vanishedTeam = getVanishedTeam();
		ArrayList<Player> onlinePlayers = new ArrayList<>(Main.server.getOnlinePlayers());
		for(Player player : Main.server.getOnlinePlayers()) {
			PlayerStatus status = getPlayerStatus(player);
			if(Permissions.hasPerm(player, "supercmds.vanish.exempt")) {
				vanishedTeam.addPlayer(player);
			} else {
				vanishedTeam.removePlayer(player);
			}
			if((status.isVanishModeOn) && (Permissions.hasPerm(player, "supercmds.use.vanish"))) {
				
				for(Player p : onlinePlayers) {
					if((!Permissions.hasPerm(p, "supercmds.vanish.exempt")) && (!p.getUniqueId().toString().equals(player.getUniqueId().toString()))) {
						p.hidePlayer(player);
					} else {
						p.showPlayer(player);
					}
				}
			} else {
				if(status.isVanishModeOn) {
					status.isVanishModeOn = false;
					status.lastVanishTime = -1L;
					Main.sendMessage(player, Main.pluginName + "&aYou are now " + (status.isVanishModeOn ? "&2completely invisible&a to other players. Server operators and players with special permissions may still be able to see you." : "&fvisible&a to other players."));
				}
				
				for(Player p : onlinePlayers) {
					player.showPlayer(p);
				}
			}
		}
		onlinePlayers.clear();
	}
	
	public static final void updatePlayerFlyModeStates() {
		for(PlayerStatus status : new ArrayList<>(statuses)) {
			if(status.isPlayerOnline()) {
				if(Permissions.hasPerm(status.getPlayer(), "supercmds.use.speed")) {
					status.getPlayer().getWalkSpeed();
					
					status.getPlayer().getFlySpeed();
					
				} else {
					status.getPlayer().setWalkSpeed(0.2F);
					status.getPlayer().setFlySpeed(0.1F);
				}
				if(status.isFlyModeOn) {
					if(!Permissions.hasPerm(status.getPlayer(), "supercmds.use.fly")) {
						if((status.getPlayer().getGameMode() != GameMode.CREATIVE) && (status.getPlayer().getGameMode() != GameMode.SPECTATOR)) {
							status.isFlyModeOn = false;
							status.wasFlyModeOnBeforeSwap = false;
							status.getPlayer().setFlying(false);
							status.getPlayer().setAllowFlight(false);
							Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet fly mode to " + (status.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
						}
					} else {
						status.getPlayer().setAllowFlight(true);
						if(status.getPlayer().getGameMode() == GameMode.SPECTATOR) {
							status.getPlayer().setFlying(true);
						}
					}
				} else if((status.getPlayer().getGameMode() == GameMode.CREATIVE) || (status.getPlayer().getGameMode() == GameMode.SPECTATOR)) {
					status.wasFlyModeOnBeforeSwap = status.isFlyModeOn;
					status.getPlayer().setAllowFlight(true);
					if(status.getPlayer().getGameMode() == GameMode.SPECTATOR) {
						status.getPlayer().setFlying(true);
					}
					if(!status.isFlyModeOn) {
						status.isFlyModeOn = true;
						Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet fly mode to " + (status.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
					}
				} else if(Permissions.hasPerm(status.getPlayer(), "supercmds.use.fly")) {
					if(status.wasFlyModeOnBeforeSwap != status.isFlyModeOn) {
						status.getPlayer().setAllowFlight(status.wasFlyModeOnBeforeSwap);
						status.isFlyModeOn = status.wasFlyModeOnBeforeSwap;
						Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet fly mode back to " + (status.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
					} else if(status.isFlyModeOn) {
						status.getPlayer().setAllowFlight(true);
					} else {
						status.getPlayer().setAllowFlight(false);
					}
				} else {
					status.wasFlyingLast = false;
					status.isFlyModeOn = false;
					status.wasFlyModeOnBeforeSwap = false;
					status.getPlayer().setAllowFlight(false);
				}
			}
		}
	}
	
	public static final void updatePlayerGodModeStates() {
		for(PlayerStatus status : new ArrayList<>(statuses)) {
			if(status.isPlayerOnline()) {
				if(!Permissions.hasPerm(status.getPlayer(), "supercmds.use.god")) {
					if(status.isGodModeOn) {
						status.isGodModeOn = false;
						status.wasGodModeOnBeforeSwap = false;
						Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
					}
				} else if((status.getPlayer().getGameMode() == GameMode.CREATIVE) || (status.getPlayer().getGameMode() == GameMode.SPECTATOR)) {
					if(!status.isGodModeOn) {
						status.wasGodModeOnBeforeSwap = false;
						status.isGodModeOn = true;
						Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
					}
				} else if((status.wasGodModeOnBeforeSwap) && (!status.isGodModeOn)) {
					status.isGodModeOn = true;
					Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet god mode back to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				} else if((!status.wasGodModeOnBeforeSwap) && (status.isGodModeOn)) {
					status.isGodModeOn = false;
					Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet god mode back to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				}
			}
		}
	}
	
	public static final void updatePlayerThorModeStates() {
		for(PlayerStatus status : new ArrayList<>(statuses)) {
			if((status.isPlayerOnline()) && (!Permissions.hasPerm(status.getPlayer(), "supercmds.use.thor")) && (status.isThorModeOn)) {
				status.isThorModeOn = false;
				Main.sendMessage(status.getPlayer(), Main.pluginName + (status.isThorModeOn ? "&aThor has blessed your hammer!" : "&eThor's blessing has worn off..."));
			}
		}
	}
	
	public void addTeleportToMeRequest(Player requester) {
		this.playersRequestingToTpToMe.add(requester.getUniqueId());
		if(isPlayerOnline()) {
			Main.sendMessage(getPlayer(), Main.pluginName + requester.getDisplayName() + "&r&e is requesting to teleport to you.&z&eType &f/tpaccept&e to accept their request or type &f/tpdeny&e to deny their request.");
		}
	}
	
	public void addTeleportToThemRequest(Player requester) {
		this.playersRequestingMeToTpToThem.add(requester.getUniqueId());
		if(isPlayerOnline()) {
			Main.sendMessage(getPlayer(), Main.pluginName + requester.getDisplayName() + "&r&e is requesting for you to teleport to them.&z&eType &f/tpaccept&e to accept their request or type &f/tpdeny&e to deny their request.");
		}
	}
	
	public final void acceptTeleportRequestsFrom(ArrayList<Player> players) {
		ArrayList<UUID> getUUIDS = new ArrayList<>();
		for(Player player : players) {
			if(player != null) {
				getUUIDS.add(player.getUniqueId());
			}
		}
		acceptTeleportRequestsFrom(getUUIDS.toArray(new UUID[getUUIDS.size()]));
	}
	
	public final void acceptTeleportRequestsFrom(Player... players) {
		ArrayList<UUID> getUUIDS = new ArrayList<>();
		Player[] arrayOfPlayer;
		int j = (arrayOfPlayer = players).length;
		for(int i = 0; i < j; i++) {
			Player player = arrayOfPlayer[i];
			if(player != null) {
				getUUIDS.add(player.getUniqueId());
			}
		}
		acceptTeleportRequestsFrom(getUUIDS.toArray(new UUID[getUUIDS.size()]));
	}
	
	public void acceptTeleportRequestsFrom(UUID... players) {
		if(!isPlayerOnline()) {
			denyTeleportRequests();
			return;
		}
		UUID[] arrayOfUUID;
		int j = (arrayOfUUID = players).length;
		for(int i = 0; i < j; i++) {
			UUID uuid = arrayOfUUID[i];
			if(this.playersRequestingMeToTpToThem.contains(uuid)) {
				Player requester = Main.server.getPlayer(uuid);
				if(requester != null) {
					getPlayer().teleport(requester);
					Main.sendMessage(requester, Main.pluginName + getPlayer().getDisplayName() + "&r&e has accepted your teleport request.");
					if(requester.getUniqueId().toString().equals(getPlayer().getUniqueId().toString())) {
						Main.sendMessage(getPlayer(), Main.pluginName + "&fLol, you requested to teleport to yourself, haha.");
					}
				}
			}
			this.playersRequestingMeToTpToThem.remove(uuid);
			if(this.playersRequestingToTpToMe.contains(uuid)) {
				Player requester = Main.server.getPlayer(uuid);
				if(requester != null) {
					requester.teleport(getPlayer());
					Main.sendMessage(requester, Main.pluginName + getPlayer().getDisplayName() + "&r&e has accepted your teleport request.");
					if(requester.getUniqueId().toString().equals(getPlayer().getUniqueId().toString())) {
						Main.sendMessage(getPlayer(), Main.pluginName + "&fLol, you requested yourself to be teleported to yourself, haha.");
					}
				}
			}
			this.playersRequestingToTpToMe.remove(uuid);
		}
	}
	
	public void acceptTeleportRequests() {
		if(!isPlayerOnline()) {
			denyTeleportRequests();
			return;
		}
		int i = 0;
		for(UUID uuid : this.playersRequestingMeToTpToThem) {
			Player requester = Main.server.getPlayer(uuid);
			if(requester != null) {
				getPlayer().teleport(requester);
				Main.sendMessage(requester, Main.pluginName + getPlayer().getDisplayName() + "&r&e has accepted your teleport request" + (i < this.playersRequestingMeToTpToThem.size() - 1 ? ", but they also had other pending teleport requests" : "") + ".");
				if(requester.getUniqueId().toString().equals(getPlayer().getUniqueId().toString())) {
					Main.sendMessage(getPlayer(), Main.pluginName + "&fLol, you requested to teleport to yourself, haha.");
				}
			}
			i++;
		}
		this.playersRequestingMeToTpToThem.clear();
		for(UUID uuid : this.playersRequestingToTpToMe) {
			Player requester = Main.server.getPlayer(uuid);
			if(requester != null) {
				requester.teleport(getPlayer());
				Main.sendMessage(requester, Main.pluginName + getPlayer().getDisplayName() + "&r&e has accepted your teleport request.");
				if(requester.getUniqueId().toString().equals(getPlayer().getUniqueId().toString())) {
					Main.sendMessage(getPlayer(), Main.pluginName + "&fLol, you requested yourself to be teleported to yourself, haha.");
				}
			}
		}
		this.playersRequestingToTpToMe.clear();
	}
	
	public int denyTeleportRequests() {
		int rtrn = this.playersRequestingToTpToMe.size() + this.playersRequestingMeToTpToThem.size();
		if(!isPlayerOnline()) {
			this.playersRequestingToTpToMe.clear();
			this.playersRequestingMeToTpToThem.clear();
			return rtrn;
		}
		for(UUID uuid : this.playersRequestingMeToTpToThem) {
			Player requester = Main.server.getPlayer(uuid);
			if(requester != null) {
				getPlayer().teleport(requester);
				Main.sendMessage(requester, Main.pluginName + getPlayer().getDisplayName() + "&r&e has denied your pending teleport request.");
			}
		}
		for(UUID uuid : this.playersRequestingToTpToMe) {
			Player requester = Main.server.getPlayer(uuid);
			if(requester != null) {
				getPlayer().teleport(requester);
				Main.sendMessage(requester, Main.pluginName + getPlayer().getDisplayName() + "&r&e has denied your pending teleport request.");
			}
		}
		return rtrn;
	}
	
	public final Long getLastLogoutTime() {
		if(this.lastLogoutTime < 0) {
			return null;
		}
		return Long.valueOf(this.lastLogoutTime);
	}
	
	public final boolean hasPlayedInTheLast6Months() {
		return System.currentTimeMillis() - this.lastLogoutTime < 15778463000L;
	}
	
	public final String getIPAddress() {
		if(this.isPlayerOnline()) {
			this.lastIPAddress = this.getPlayer().getAddress().getHostString();
			this.saveToFile();
		}
		return this.lastIPAddress;
	}
	
}
