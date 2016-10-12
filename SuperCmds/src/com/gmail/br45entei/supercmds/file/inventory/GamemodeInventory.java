package com.gmail.br45entei.supercmds.file.inventory;

import com.gmail.br45entei.supercmds.InventoryAPI;
import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerStatus;
import com.gmail.br45entei.supercmds.file.SavablePlayerData;
import com.gmail.br45entei.supercmds.util.VersionUtil;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_10_R1.IInventory;

/** @author Brian_Entei */
@SuppressWarnings({"javadoc", "unused"})
public class GamemodeInventory extends SavablePlayerData {
	
	private static final ArrayList<GamemodeInventory>	instances	= new ArrayList<>();
	
	private static Class<?>								CraftInventory;
	private static Method								getInventory;
	private static Class<?>								IInventory;
	private static Method								update;
	
	public static final void initialize() {
		//if(VersionUtil.getNMSVersion().startsWith("v1_10_R")) {
		try {
			setupReflection();
		} catch(NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		//}
	}
	
	// Stores the reflected classes & methods in variable for access later
	private static final void setupReflection() throws NoSuchMethodException, SecurityException {
		CraftInventory = VersionUtil.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftInventory");
		getInventory = CraftInventory.getDeclaredMethod("getInventory");
		IInventory = VersionUtil.getNMSClass("net.minecraft.server.%s.IInventory");
		update = IInventory.getDeclaredMethod("update");
	}
	
	/** Mimics the original {@link IInventory#update()} method in the NMS and
	 * CraftBukkit classes. */
	public static final void updateInventoryNatively(Inventory i) {
		try {
			Object iinventory = IInventory.cast(getInventory.invoke(CraftInventory.cast(i), new Object[] {}));
			update.invoke(iinventory);//update.invoke(IInventory.cast(getInventory.invoke(CraftInventory.cast(i))));
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		if(i.getHolder() != null && i.getHolder() instanceof Player) {
			((Player) i.getHolder()).updateInventory();
		}
	}
	
	public static final GamemodeInventory getGamemodeInventoryIfExistsFor(Player player) {
		if(player == null) {
			return null;
		}
		return getGamemodeInventoryIfExistsFor(player.getUniqueId());
	}
	
	public static final GamemodeInventory getGamemodeInventoryIfExistsFor(UUID player) {
		if(player == null) {
			return null;
		}
		for(GamemodeInventory gmi : instances) {
			if(gmi.equals(player)) {
				return gmi;
			}
		}
		return null;
	}
	
	public static final GamemodeInventory getGamemodeInventoryForPlayer(Player player) {
		if(player == null) {
			return null;
		}
		return getGamemodeInventoryForPlayer(player.getUniqueId());
	}
	
	public static final GamemodeInventory getGamemodeInventoryForPlayer(UUID player) {
		if(player == null) {
			return null;
		}
		GamemodeInventory check = getGamemodeInventoryIfExistsFor(player);
		if(check != null) {
			return check;
		}
		GamemodeInventory gmi = new GamemodeInventory(player);
		gmi.loadFromFile();
		return gmi;
	}
	
	private static final class GMWorldData {
		
		protected final UUID	world;
		private final UUID		player;
		
		public GMWorldData(UUID world, UUID player) {
			this.world = world;
			this.player = player;
		}
		
		public final World getWorld() {
			return Main.server.getWorld(this.world);
		}
		
		public final UUID getWorldUID() {
			return this.world;
		}
		
		public final String getWorldName() {
			World world = Main.server.getWorld(this.world);
			return world != null ? world.getName() : "null";
		}
		
		public final Player getPlayer() {
			return Main.server.getPlayer(this.player);
		}
		
		public final UUID getPlayerUID() {
			return this.player;
		}
		
		public final String getPlayerName() {
			return Main.uuidMasterList.getPlayerNameFromUUID(this.player);
		}
		
		protected final HashMap<GameMode, Inventory>	armorInvs			= new HashMap<>();
		protected final HashMap<GameMode, Inventory>	inventories			= new HashMap<>();
		protected final HashMap<GameMode, Inventory>	enderChests			= new HashMap<>();
		
		protected final HashMap<GameMode, Integer>		selectedInvSlots	= new HashMap<>();
		
		protected final HashMap<GameMode, Integer>		expLevels			= new HashMap<>();
		protected final HashMap<GameMode, Float>		exps				= new HashMap<>();
		
		protected final HashMap<GameMode, Integer>		foodLevels			= new HashMap<>();
		protected final HashMap<GameMode, Double>		healths				= new HashMap<>();
		protected final HashMap<GameMode, Double>		maxHealths			= new HashMap<>();
		protected final HashMap<GameMode, Float>		saturations			= new HashMap<>();
		
		protected final HashMap<GameMode, Float>		walkSpeeds			= new HashMap<>();
		protected final HashMap<GameMode, Float>		flySpeeds			= new HashMap<>();
		protected final HashMap<GameMode, Boolean>		canFlys				= new HashMap<>();
		
		public final void loadFromConfig(ConfigurationSection mem) {
			Main.DEBUG("&aLoading GMWorldData for player \"&f" + this.getPlayerName() + "&r&a\" in world \"&f" + this.getWorldName() + "&r&a\"...");
			this.armorInvs.put(GameMode.SURVIVAL, InventoryAPI.deserializeInventory(mem.getString("survivalArmor", "9;Survival Armor Inventory;")));
			this.armorInvs.put(GameMode.CREATIVE, InventoryAPI.deserializeInventory(mem.getString("creativeArmor", "9;Creative Armor Inventory;")));
			
			this.inventories.put(GameMode.SURVIVAL, InventoryAPI.deserializeInventory(mem.getString("survivalInventory", "36;Survival Inventory;")));
			this.inventories.put(GameMode.CREATIVE, InventoryAPI.deserializeInventory(mem.getString("creativeInventory", "36;Creative Inventory;")));
			
			this.enderChests.put(GameMode.SURVIVAL, InventoryAPI.deserializeInventory(mem.getString("survivalEnderChest", "27;Survival Ender Chest")));
			this.enderChests.put(GameMode.CREATIVE, InventoryAPI.deserializeInventory(mem.getString("creativeEnderChest", "27;Creative Ender Chest")));
			
			this.selectedInvSlots.put(GameMode.SURVIVAL, mem.get("survivalInvSlot") == null ? null : Integer.valueOf(mem.getInt("survivalInvSlot")));
			this.selectedInvSlots.put(GameMode.CREATIVE, mem.get("creativeInvSlot") == null ? null : Integer.valueOf(mem.getInt("creativeInvSlot")));
			
			if(this.getSurvivalInventory().getContents().length == 0) {
				System.err.println("survival inventory for world \"" + this.getWorld().getName() + "\" was null.");
			}
			if(this.armorInvs.get(GameMode.SURVIVAL).getContents().length == 0) {
				System.err.println("survival armor inventory for world \"" + this.getWorld().getName() + "\" was null.");
			}
			if(this.getSurvivalEnderChest().getContents().length == 0) {
				System.err.println("survival ender chest for world \"" + this.getWorld().getName() + "\" was null.");
			}
			if(this.getCreativeInventory().getContents().length == 0) {
				System.err.println("creative inventory for world \"" + this.getWorld().getName() + "\" was null.");
			}
			if(this.armorInvs.get(GameMode.CREATIVE).getContents().length == 0) {
				System.err.println("creative armor inventory for world \"" + this.getWorld().getName() + "\" was null.");
			}
			if(this.getCreativeEnderChest().getContents().length == 0) {
				System.err.println("creative ender chest for world \"" + this.getWorld().getName() + "\" was null.");
			}
			
			// ===
			
			this.expLevels.put(GameMode.SURVIVAL, mem.get("survivalExperienceLevel") == null ? null : Integer.valueOf(mem.getInt("survivalExperienceLevel")));
			this.expLevels.put(GameMode.CREATIVE, mem.get("creativeExperienceLevel") == null ? null : Integer.valueOf(mem.getInt("creativeExperienceLevel")));
			
			this.exps.put(GameMode.SURVIVAL, mem.get("survivalExperience") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("survivalExperience")).floatValue()));
			this.exps.put(GameMode.CREATIVE, mem.get("creativeExperience") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("creativeExperience")).floatValue()));
			
			this.foodLevels.put(GameMode.SURVIVAL, mem.get("survivalFoodLevel") == null ? null : Integer.valueOf(mem.getInt("survivalFoodLevel")));
			this.foodLevels.put(GameMode.CREATIVE, mem.get("creativeFoodLevel") == null ? null : Integer.valueOf(mem.getInt("creativeFoodLevel")));
			
			this.healths.put(GameMode.SURVIVAL, mem.get("survivalHealth") == null ? null : Double.valueOf(mem.getDouble("survivalHealth")));
			this.healths.put(GameMode.CREATIVE, mem.get("creativeHealth") == null ? null : Double.valueOf(mem.getDouble("creativeHealth")));
			
			this.maxHealths.put(GameMode.SURVIVAL, mem.get("survivalMaxHealth") == null ? null : Double.valueOf(mem.getDouble("survivalMaxHealth")));
			this.maxHealths.put(GameMode.CREATIVE, mem.get("creativeMaxHealth") == null ? null : Double.valueOf(mem.getDouble("creativeMaxHealth")));
			
			this.saturations.put(GameMode.SURVIVAL, mem.get("survivalSaturation") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("survivalSaturation")).floatValue()));
			this.saturations.put(GameMode.CREATIVE, mem.get("creativeSaturation") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("creativeSaturation")).floatValue()));
			
			this.walkSpeeds.put(GameMode.SURVIVAL, mem.get("survivalWalkSpeed") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("survivalWalkSpeed")).floatValue()));
			this.walkSpeeds.put(GameMode.CREATIVE, mem.get("creativeWalkSpeed") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("creativeWalkSpeed")).floatValue()));
			
			this.flySpeeds.put(GameMode.SURVIVAL, mem.get("survivalFlySpeed") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("survivalFlySpeed")).floatValue()));
			this.flySpeeds.put(GameMode.CREATIVE, mem.get("creativeFlySpeed") == null ? null : Float.valueOf(Double.valueOf(mem.getDouble("creativeFlySpeed")).floatValue()));
			
			this.canFlys.put(GameMode.SURVIVAL, mem.get("survivalCanFly") == null ? null : Boolean.valueOf(mem.getBoolean("survivalCanFly")));
			this.canFlys.put(GameMode.CREATIVE, mem.get("creativeCanFly") == null ? null : Boolean.valueOf(mem.getBoolean("creativeCanFly")));
		}
		
		public final void saveToConfig(ConfigurationSection mem) {
			mem.set("lastKnownWorldName", (this.getWorld() != null ? this.getWorldName() : null));
			
			mem.set("survivalArmor", InventoryAPI.serializeInventory(this.armorInvs.get(GameMode.SURVIVAL)));
			mem.set("creativeArmor", InventoryAPI.serializeInventory(this.armorInvs.get(GameMode.CREATIVE)));
			
			mem.set("survivalInventory", InventoryAPI.serializeInventory(this.getSurvivalInventory()));
			mem.set("creativeInventory", InventoryAPI.serializeInventory(this.getCreativeInventory()));
			
			mem.set("survivalEnderChest", InventoryAPI.serializeInventory(this.getSurvivalEnderChest()));
			mem.set("creativeEnderChest", InventoryAPI.serializeInventory(this.getCreativeEnderChest()));
			
			// ===
			
			mem.set("survivalExperienceLevel", this.expLevels.get(GameMode.SURVIVAL));
			mem.set("creativeExperienceLevel", this.expLevels.get(GameMode.CREATIVE));
			
			mem.set("survivalExperience", this.exps.get(GameMode.SURVIVAL));
			mem.set("creativeExperience", this.exps.get(GameMode.CREATIVE));
			
			mem.set("survivalFoodLevel", this.foodLevels.get(GameMode.SURVIVAL));
			mem.set("creativeFoodLevel", this.foodLevels.get(GameMode.CREATIVE));
			
			mem.set("survivalHealth", this.healths.get(GameMode.SURVIVAL));
			mem.set("creativeHealth", this.healths.get(GameMode.CREATIVE));
			
			mem.set("survivalMaxHealth", this.maxHealths.get(GameMode.SURVIVAL));
			mem.set("creativeMaxHealth", this.maxHealths.get(GameMode.CREATIVE));
			
			mem.set("survivalSaturation", this.saturations.get(GameMode.SURVIVAL));
			mem.set("creativeSaturation", this.saturations.get(GameMode.CREATIVE));
			
			mem.set("survivalWalkSpeed", this.walkSpeeds.get(GameMode.SURVIVAL));
			mem.set("creativeWalkSpeed", this.walkSpeeds.get(GameMode.CREATIVE));
			
			mem.set("survivalFlySpeed", this.flySpeeds.get(GameMode.SURVIVAL));
			mem.set("creativeFlySpeed", this.flySpeeds.get(GameMode.CREATIVE));
			
			mem.set("survivalCanFly", this.canFlys.get(GameMode.SURVIVAL));
			mem.set("creativeCanFly", this.canFlys.get(GameMode.CREATIVE));
			
		}
		
		public final ItemStack getHelmet(GameMode mode) {
			return this.armorInvs.get(getCorrectGameModeToUse(mode)).getItem(0);
		}
		
		public final ItemStack getChestplate(GameMode mode) {
			return this.armorInvs.get(getCorrectGameModeToUse(mode)).getItem(1);
		}
		
		public final ItemStack getLeggings(GameMode mode) {
			return this.armorInvs.get(getCorrectGameModeToUse(mode)).getItem(2);
		}
		
		public final ItemStack getBoots(GameMode mode) {
			return this.armorInvs.get(getCorrectGameModeToUse(mode)).getItem(3);
		}
		
		public final ItemStack getShieldItem(GameMode mode) {
			return this.armorInvs.get(getCorrectGameModeToUse(mode)).getItem(4);
		}
		
		public final GMWorldData setHelmet(GameMode mode, ItemStack item) {
			this.armorInvs.get(getCorrectGameModeToUse(mode)).setItem(0, item);
			return this;
		}
		
		public final GMWorldData setChestplate(GameMode mode, ItemStack item) {
			this.armorInvs.get(getCorrectGameModeToUse(mode)).setItem(1, item);
			return this;
		}
		
		public final GMWorldData setLeggings(GameMode mode, ItemStack item) {
			this.armorInvs.get(getCorrectGameModeToUse(mode)).setItem(2, item);
			return this;
		}
		
		public final GMWorldData setBoots(GameMode mode, ItemStack item) {
			this.armorInvs.get(getCorrectGameModeToUse(mode)).setItem(3, item);
			return this;
		}
		
		public final GMWorldData setShieldItem(GameMode mode, ItemStack item) {
			this.armorInvs.get(getCorrectGameModeToUse(mode)).setItem(4, item);
			return this;
		}
		
		public final Inventory getSurvivalInventory() {
			Inventory rtrn = this.inventories.get(GameMode.SURVIVAL);
			//CraftInventory inv = (CraftInventory) rtrn;
			//inv.getInventory().update();
			updateInventoryNatively(rtrn);
			return rtrn;
		}
		
		public final Inventory getCreativeInventory() {
			Inventory rtrn = this.inventories.get(GameMode.CREATIVE);
			//CraftInventory inv = (CraftInventory) rtrn;
			//inv.getInventory().update();
			updateInventoryNatively(rtrn);
			return rtrn;
		}
		
		public final Inventory getSurvivalEnderChest() {
			return this.enderChests.get(GameMode.SURVIVAL);
		}
		
		public final Inventory getCreativeEnderChest() {
			return this.enderChests.get(GameMode.CREATIVE);
		}
		
		public final boolean equals(World world) {
			return world != null && this.equals(world.getUID());
		}
		
		public final boolean equals(UUID world) {
			return world != null && this.world.toString().equals(world.toString());
		}
		
	}
	
	private final ArrayList<GMWorldData> data = new ArrayList<>();
	
	private final GMWorldData getDataFor(UUID world) {
		if(world == null) {
			return null;
		}
		world = getCorrectWorldToUse(world);
		for(GMWorldData data : this.data) {
			if(data.equals(world)) {
				return data;
			}
		}
		GMWorldData data = new GMWorldData(world, this.uuid);
		if(!this.data.contains(data)) {
			this.data.add(data);
		}
		data.loadFromConfig(this.getSectionFor(world));
		return data;
	}
	
	private final GMWorldData getDataFor(World world) {
		if(world == null) {
			return null;
		}
		world = getCorrectWorldToUse(world);
		for(GMWorldData data : this.data) {
			if(data.equals(world)) {
				return data;
			}
		}
		GMWorldData data = new GMWorldData(world.getUID(), this.uuid);
		if(!this.data.contains(data)) {
			this.data.add(data);
		}
		data.loadFromConfig(this.getSectionFor(world.getUID()));
		return data;
	}
	
	private final UUID player;
	
	private GamemodeInventory(UUID player) {
		super(player, Main.uuidMasterList.getPlayerNameFromUUID(player));
		if(getGamemodeInventoryIfExistsFor(player) != null) {
			throw new IllegalStateException("Duplicate instances of the same GamemodeInventory are not allowed!");
		}
		if(player == null) {
			throw new IllegalStateException("Player uuid cannot be null!");
		}
		this.saveAndLoadWithSuperCmds = true;
		this.player = player;
		Main.server.getPluginManager().registerEvents(this, Main.getInstance());
		instances.add(this);
	}
	
	protected final boolean equals(Player player) {
		return this.equals(player.getUniqueId());
	}
	
	protected final boolean equals(UUID player) {
		return this.player.toString().equals(player.toString());
	}
	
	public final void set(World world, GameMode mode, PlayerInventory playerInventory, Inventory enderChest) {
		if(mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
			this.setSurvival(world, playerInventory, enderChest);
		} else {
			this.setCreative(world, playerInventory, enderChest);
		}
	}
	
	public final void setSurvival(World world, PlayerInventory playerInventory, Inventory enderChest) {
		world = getCorrectWorldToUse(world);
		this.setArmorContents(world, GameMode.SURVIVAL, playerInventory);
		this.getSurvivalInventory(world).setContents(StringUtil.resizeArray(ItemStack.class, playerInventory.getContents(), 0, 36));
		this.getSurvivalEnderChest(world).setContents(StringUtil.resizeArray(ItemStack.class, enderChest.getContents(), 0, 27));
	}
	
	public final void setCreative(World world, PlayerInventory playerInventory, Inventory enderChest) {
		world = getCorrectWorldToUse(world);
		this.setArmorContents(world, GameMode.CREATIVE, playerInventory);
		this.getCreativeInventory(world).setContents(StringUtil.resizeArray(ItemStack.class, playerInventory.getContents(), 0, 36));
		this.getCreativeEnderChest(world).setContents(StringUtil.resizeArray(ItemStack.class, enderChest.getContents(), 0, 27));
	}
	
	public final void setArmorContents(World world, GameMode mode, PlayerInventory playerInventory) {
		GMWorldData data = this.getDataFor(world);
		data.setHelmet(mode, playerInventory.getHelmet());
		data.setChestplate(mode, playerInventory.getChestplate());
		data.setLeggings(mode, playerInventory.getLeggings());
		data.setBoots(mode, playerInventory.getBoots());
		data.setShieldItem(mode, playerInventory.getItemInOffHand());
	}
	
	// ========
	
	public static final void copyItemMapIntoInventory(Map<Integer, ItemStack> items, Inventory inv, boolean wipeBeforeCopy) {
		if(items == null || inv == null) {
			return;
		}
		if(wipeBeforeCopy) {
			inv.clear();
		}
		for(int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, items.get(Integer.valueOf(i)));
		}
	}
	
	public static final HashMap<Integer, ItemStack> getItemMapOfInventory(Inventory inv) {
		if(inv == null) {
			return null;
		}
		HashMap<Integer, ItemStack> map = new HashMap<>();
		for(int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if(item != null && item.getType() != Material.AIR) {
				Integer I = Integer.valueOf(i);
				map.put(I, inv.getItem(i));
			}
		}
		return map;
	}
	
	// ====================================================================================
	
	public final Inventory getInventory(World world, GameMode mode) {
		if(mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
			return this.getSurvivalInventory(world);
		}
		return this.getCreativeInventory(world);
	}
	
	public final Inventory getSurvivalInventory(World world) {
		return this.getDataFor(world).getSurvivalInventory();
	}
	
	public final Inventory getCreativeInventory(World world) {
		return this.getDataFor(world).getCreativeInventory();
	}
	
	public final Inventory getEnderChest(World world, GameMode mode) {
		if(mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
			return this.getSurvivalEnderChest(world);
		}
		return this.getCreativeEnderChest(world);
	}
	
	public final Inventory getSurvivalEnderChest(World world) {
		return this.getDataFor(world).getSurvivalEnderChest();
	}
	
	public final Inventory getCreativeEnderChest(World world) {
		return this.getDataFor(world).getCreativeEnderChest();
	}
	
	// ========
	
	public final Inventory getInventory(UUID world, GameMode mode) {
		if(mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
			return this.getSurvivalInventory(world);
		}
		return this.getCreativeInventory(world);
	}
	
	public final Inventory getSurvivalInventory(UUID world) {
		return this.getDataFor(world).getSurvivalInventory();
	}
	
	public final Inventory getCreativeInventory(UUID world) {
		return this.getDataFor(world).getCreativeInventory();
	}
	
	// Armor methods ===========
	
	public final ItemStack getHelmet(UUID world, GameMode mode) {
		return this.getDataFor(world).getHelmet(getCorrectGameModeToUse(mode));
	}
	
	public final ItemStack getChestplate(UUID world, GameMode mode) {
		return this.getDataFor(world).getChestplate(getCorrectGameModeToUse(mode));
	}
	
	public final ItemStack getLeggings(UUID world, GameMode mode) {
		return this.getDataFor(world).getLeggings(getCorrectGameModeToUse(mode));
	}
	
	public final ItemStack getBoots(UUID world, GameMode mode) {
		return this.getDataFor(world).getBoots(getCorrectGameModeToUse(mode));
	}
	
	public final ItemStack getShieldItem(UUID world, GameMode mode) {
		return this.getDataFor(world).getShieldItem(getCorrectGameModeToUse(mode));
	}
	
	public final ItemStack getSurvivalHelmet(UUID world) {
		return this.getBoots(world, GameMode.SURVIVAL);
	}
	
	public final ItemStack getCreativeHelmet(UUID world) {
		return this.getHelmet(world, GameMode.CREATIVE);
	}
	
	public final ItemStack getSurvivalChestplate(UUID world) {
		return this.getChestplate(world, GameMode.SURVIVAL);
	}
	
	public final ItemStack getCreativeChestplate(UUID world) {
		return this.getDataFor(world).getChestplate(GameMode.CREATIVE);
	}
	
	public final ItemStack getSurvivalLeggings(UUID world) {
		return this.getDataFor(world).getLeggings(GameMode.SURVIVAL);
	}
	
	public final ItemStack getCreativeLeggings(UUID world) {
		return this.getDataFor(world).getLeggings(GameMode.CREATIVE);
	}
	
	public final ItemStack getSurvivalBoots(UUID world) {
		return this.getDataFor(world).getBoots(GameMode.SURVIVAL);
	}
	
	public final ItemStack getCreativeBoots(UUID world) {
		return this.getDataFor(world).getBoots(GameMode.CREATIVE);
	}
	
	public final ItemStack getSurvivalShieldItem(UUID world) {
		return this.getDataFor(world).getShieldItem(GameMode.SURVIVAL);
	}
	
	public final ItemStack getCreativeShieldItem(UUID world) {
		return this.getDataFor(world).getShieldItem(GameMode.CREATIVE);
	}
	
	public final GamemodeInventory setHelmet(UUID world, GameMode mode, ItemStack item) {
		this.getDataFor(world).setHelmet(getCorrectGameModeToUse(mode), item);
		return this;
	}
	
	public final GamemodeInventory setChestplate(UUID world, GameMode mode, ItemStack item) {
		this.getDataFor(world).setChestplate(getCorrectGameModeToUse(mode), item);
		return this;
	}
	
	public final GamemodeInventory setLeggings(UUID world, GameMode mode, ItemStack item) {
		this.getDataFor(world).setLeggings(getCorrectGameModeToUse(mode), item);
		return this;
	}
	
	public final GamemodeInventory setBoots(UUID world, GameMode mode, ItemStack item) {
		this.getDataFor(world).setBoots(getCorrectGameModeToUse(mode), item);
		return this;
	}
	
	public final GamemodeInventory setShieldItem(UUID world, GameMode mode, ItemStack item) {
		this.getDataFor(world).setShieldItem(getCorrectGameModeToUse(mode), item);
		return this;
	}
	
	public final GamemodeInventory setSurvivalHelmet(UUID world, ItemStack item) {
		return this.setHelmet(world, GameMode.SURVIVAL, item);
	}
	
	public final GamemodeInventory setCreativeHelmet(UUID world, ItemStack item) {
		return this.setHelmet(world, GameMode.CREATIVE, item);
	}
	
	public final GamemodeInventory setSurvivalChestplate(UUID world, ItemStack item) {
		return this.setChestplate(world, GameMode.SURVIVAL, item);
	}
	
	public final GamemodeInventory setCreativeChestplate(UUID world, ItemStack item) {
		return this.setChestplate(world, GameMode.CREATIVE, item);
	}
	
	public final GamemodeInventory setSurvivalLeggings(UUID world, ItemStack item) {
		return this.setLeggings(world, GameMode.SURVIVAL, item);
	}
	
	public final GamemodeInventory setCreativeLeggings(UUID world, ItemStack item) {
		return this.setLeggings(world, GameMode.CREATIVE, item);
	}
	
	public final GamemodeInventory setSurvivalBoots(UUID world, ItemStack item) {
		return this.setBoots(world, GameMode.SURVIVAL, item);
	}
	
	public final GamemodeInventory setCreativeBoots(UUID world, ItemStack item) {
		return this.setBoots(world, GameMode.CREATIVE, item);
	}
	
	public final GamemodeInventory setSurvivalShieldItem(UUID world, ItemStack item) {
		return this.setShieldItem(world, GameMode.SURVIVAL, item);
	}
	
	public final GamemodeInventory setCreativeShieldItem(UUID world, ItemStack item) {
		return this.setShieldItem(world, GameMode.CREATIVE, item);
	}
	
	// =========================
	
	public final Inventory getEnderChest(UUID world, GameMode mode) {
		if(mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
			return this.getSurvivalEnderChest(world);
		}
		return this.getCreativeEnderChest(world);
	}
	
	public final Inventory getSurvivalEnderChest(UUID world) {
		return this.getDataFor(world).getSurvivalEnderChest();
	}
	
	public final Inventory getCreativeEnderChest(UUID world) {
		return this.getDataFor(world).getCreativeEnderChest();
	}
	
	// ====================================================================================
	
	@Override
	public final String getSaveFolderName() {
		return "GamemodeInventories";
	}
	
	private static final ConfigurationSection getRootSection(ConfigurationSection mem) {
		ConfigurationSection worlds = mem.getConfigurationSection("worlds");
		if(worlds == null) {
			worlds = mem.createSection("worlds");
		}
		return worlds;
	}
	
	private final ConfigurationSection getSectionFor(UUID world) {
		return getSectionFor(null, world);
	}
	
	private final ConfigurationSection getSectionFor(ConfigurationSection mem, UUID world) {
		if(mem == null) {
			mem = this.getRootDataSection();
		}
		ConfigurationSection worlds = GamemodeInventory.getRootSection(mem);
		for(String key : worlds.getKeys(false)) {
			String getUUID = key.replace("_", "-");
			if(Main.isStringUUID(getUUID)) {
				if(world.toString().equals(getUUID)) {
					ConfigurationSection worldSection = worlds.getConfigurationSection(key);
					if(worldSection != null) {
						return worldSection;
					}
				}
			}
		}
		return worlds.createSection(world.toString().replace("-", "_"));
	}
	
	private static final ArrayList<ConfigurationSection> getWorldSections(ConfigurationSection mem) {
		ArrayList<ConfigurationSection> list = new ArrayList<>();
		ConfigurationSection worlds = GamemodeInventory.getRootSection(mem);
		for(String key : worlds.getKeys(false)) {
			String getUUID = key.replace("_", "-");
			if(Main.isStringUUID(getUUID)) {
				ConfigurationSection worldSection = worlds.getConfigurationSection(key);
				if(worldSection != null) {
					list.add(worldSection);
				}
			}
		}
		return list;
	}
	
	private final ConfigurationSection getWorldSectionIfExists(UUID world) {
		ConfigurationSection worlds = GamemodeInventory.getRootSection(this.getRootDataSection());
		for(String key : worlds.getKeys(false)) {
			String getUUID = key.replace("_", "-");
			if(Main.isStringUUID(getUUID)) {
				ConfigurationSection worldSection = worlds.getConfigurationSection(key);
				if(worldSection != null) {
					if(world.toString().equals(getUUID)) {
						return worldSection;
					}
				}
			}
		}
		return null;
	}
	
	public final Map<UUID, File> checkForOldInventoryData() {
		final HashMap<UUID, File> check = new HashMap<>();
		File rootDir = this.getSaveFolder();
		for(String fileName : rootDir.list()) {
			if(fileName.startsWith("World_") && fileName.length() > 6) {
				String getUUID = fileName.substring(6);
				Main.DEBUG("===== fileName: \"" + fileName + "\"; uuid: " + getUUID);
				if(Main.isStringUUID(getUUID)) {
					Main.DEBUG("===== uuid \"" + getUUID + "\" is a valid UUID.");
					File folder = new File(rootDir, fileName);
					if(folder.isDirectory()) {
						Main.DEBUG("===== fileName: \"" + fileName + "\" is a folder.");
						UUID uuid = UUID.fromString(getUUID);
						World world = Main.server.getWorld(uuid);
						if(world != null) {
							Main.DEBUG("===== uuid \"" + getUUID + "\" points to world: " + world.getName());
							check.put(uuid, folder);
						} else {
							Main.DEBUG("===== uuid \"" + getUUID + "\" does not depict a valid world!");
							Main.DEBUG("Valid world uuids are as follows:");
							for(World w : Main.server.getWorlds()) {
								Main.DEBUG("" + w.getName() + ": " + w.getUID().toString());
							}
						}
					}
				}
			}
		}
		return check;
	}
	
	@Override
	public final void onFirstLoad() {
		this.firstLoadWasRun = true;
		this.updateName();
		if(this.data.size() == 0) {
			Main.sendConsoleMessage(Main.pluginName + "&eFound no existing inventory data for player \"&f" + this.name + "&r&e\"! Is this the first time they've logged in?");
		}
		for(Entry<UUID, File> entry : this.checkForOldInventoryData().entrySet()) {
			UUID worldUUID = entry.getKey();
			if(this.getWorldSectionIfExists(worldUUID) == null) {
				File worldFolder = entry.getValue();
				// Main.DEBUG("==========> worldUUID: " + worldUUID);
				World world = Main.server.getWorld(worldUUID);
				if(world != null) {
					// Main.DEBUG("==========> world: " + world.getName());
					File check = new File(worldFolder, this.uuid.toString() + ".yml");
					if(check.isFile()) {
						YamlConfiguration config = new YamlConfiguration();
						try {
							Main.DEBUG("===== " + Main.pluginName + "&aFound old inventory data for player \"&f" + this.name + "&r&a\" in world \"&f" + world.getName() + "&r&a\"; loading it...");
							config.load(check);
						} catch(Throwable e) {
							Main.DEBUG("===== " + Main.pluginName + "&eUnable to load old inventory data for player \"&f" + this.name + "&r&e\" in world \"&f" + world.getName() + "&r&a\": &f" + Functions.throwableToStr(e));
							continue;
						}
						ConfigurationSection mem1 = config.getConfigurationSection("data");
						if(mem1 == null) {
							Main.DEBUG("===== " + Main.pluginName + "&eUnable to load old inventory data for player \"&f" + this.name + "&r&e\" in world \"&f" + world.getName() + "&r&a\": No data found in file!");
							continue;
						}
						GMWorldData data = new GMWorldData(worldUUID, this.uuid);
						data.loadFromConfig(mem1);
						this.data.add(data);
						Main.DEBUG("===== " + Main.pluginName + "&aSuccessfully loaded old inventory data for player \"&f" + this.name + "&r&a\" in world \"&f" + world.getName() + "&r&a\"!");
					} else {
						Main.DEBUG("===== " + Main.pluginName + "&eUnable to load old inventory data for player \"&f" + this.name + "&r&e\" in world \"&f" + world.getName() + "&r&a\": Save file doesn't exist/is a directory???");
					}
				} else {
					Main.DEBUG("===== " + Main.pluginName + "&eUnable to load old inventory data for player \"&f" + this.name + "&r&e\": World with uuid \"" + worldUUID + "\" doesn't exist or is not loaded???");
				}
			}
		}
		final GamemodeInventory THIS = this;
		Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public final void run() {
				Player player = THIS.getPlayer();
				if(player != null) {
					THIS.updatePlayerInventories(player, player.getWorld(), player.getGameMode(), true, true, true);
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						THIS.saveToFile();
					}
				});
				thread.setDaemon(true);
				thread.start();
			}
		}, 5L);// 5 ticks = 1/4 of a second; 2 ticks = 0.1(1/10th) of a second, etc. 20 ticks = 1 second
	}
	
	private final ArrayList<UUID> getWorldsMissingFromConfig() {
		ArrayList<UUID> missingWorlds = new ArrayList<>();
		for(World world : Main.server.getWorlds()) {
			UUID uuid = world.getUID();
			if(this.getWorldSectionIfExists(uuid) == null) {
				missingWorlds.add(uuid);
			}
		}
		return missingWorlds;
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		for(ConfigurationSection worldSection : GamemodeInventory.getWorldSections(mem)) {
			UUID world = UUID.fromString(worldSection.getName().replace("_", "-"));
			this.getDataFor(world);// Loads the data automatically. lol
		}
		if(this.data.size() == 0) {
			if(!this.firstLoadWasRun) {
				this.onFirstLoad();
			}
			if(this.data.size() > 0) {
				this.saveToFile();
			}
		}
		if(this.getWorldsMissingFromConfig().size() > 0 && !this.firstLoadWasRun) {
			this.onFirstLoad();
			for(UUID uuid : this.getWorldsMissingFromConfig()) {// Method re-run instead of using returned map from above on purpose.
				this.getDataFor(uuid);
			}
		}
		if(this.data.size() > 0) {
			Main.sendConsoleMessage(Main.pluginName + "&aSuccessfully loaded &f" + this.data.size() + "&a inventory " + Main.fixPluralWord(this.data.size(), "datas") + " for player \"&f" + this.getPlayerDisplayName() + "&r&a\"!");
		}
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		for(GMWorldData data : this.data) {
			World world = Main.server.getWorld(data.world);
			Main.DEBUG("Saving GMWorldData for player \"" + this.name + "\" in world \"" + (world != null ? world.getName() : "null") + "\"...");
			data.saveToConfig(this.getSectionFor(mem, data.world));
		}
	}
	
	@Override
	public final void dispose() {
		super.dispose();
		this.data.clear();
		instances.remove(this);
		HandlerList.unregisterAll(this);
	}
	
	public final void saveCurrentPlayerInventory(Player player, GameMode mode, World world) {
		world = getCorrectWorldToUse(world);
		mode = getCorrectGameModeToUse(mode);// player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE ? GameMode.SURVIVAL : GameMode.CREATIVE;
		this.set(world, mode, player.getInventory(), player.getEnderChest());
		
		//===
		
		GMWorldData data = this.getDataFor(world);
		data.selectedInvSlots.put(mode, Integer.valueOf(player.getInventory().getHeldItemSlot()));
		data.expLevels.put(mode, Integer.valueOf(player.getLevel()));
		data.exps.put(mode, Float.valueOf(player.getExp()));
		data.foodLevels.put(mode, Integer.valueOf(player.getFoodLevel()));
		data.healths.put(mode, Double.valueOf(player.getHealth()));
		data.maxHealths.put(mode, Double.valueOf(player.getMaxHealth()));
		data.saturations.put(mode, Float.valueOf(player.getSaturation()));
		data.walkSpeeds.put(mode, Float.valueOf(player.getWalkSpeed()));
		data.flySpeeds.put(mode, Float.valueOf(player.getFlySpeed()));
		data.canFlys.put(mode, Boolean.valueOf(player.getAllowFlight()));
	}
	
	public static final GameMode[] getValidGameModes() {
		return new GameMode[] {GameMode.SURVIVAL, GameMode.CREATIVE};
	}
	
	public static final GameMode getCorrectGameModeToUse(GameMode gamemode) {
		return gamemode == null ? null : (gamemode == GameMode.SURVIVAL || gamemode == GameMode.ADVENTURE ? GameMode.SURVIVAL : GameMode.CREATIVE);
	}
	
	public static final UUID getCorrectWorldToUse(UUID world) {
		if(world == null) {
			return null;
		}
		World w = Main.server.getWorld(world);
		if(w != null) {
			w = getCorrectWorldToUse(w);
			world = w.getUID();
		}
		return world;
	}
	
	public static final World getCorrectWorldToUse(World world) {
		if(world == null) {
			return null;
		}
		if(world.getName().endsWith("_nether")) {
			World check = Main.server.getWorld(world.getName().replace("_nether", ""));// Allows the same inventory instance for nether and end worlds since they are the 'same world', just different dimensions etc. etc.
			if(check != null) {
				world = check;
			}
		}
		if(world.getName().endsWith("_the_end")) {
			World check = Main.server.getWorld(world.getName().replace("_the_end", ""));
			if(check != null) {
				world = check;
			}
		}
		return world;
	}
	
	public static final int getNumOfItemsIn(ItemStack[] inv) {
		int numOfItems = 0;
		for(ItemStack item : inv) {
			if(item != null && item.getType() != Material.AIR) {
				numOfItems++;
			}
		}
		return numOfItems;
	}
	
	private static final boolean isItemStackArrayEmpty(ItemStack[] array) {
		if(array == null) {
			return true;
		}
		boolean isEmpty = true;
		for(ItemStack item : array) {
			isEmpty &= (item == null || item.getType() == Material.AIR);
		}
		return isEmpty;
	}
	
	/** @param player The player whose inventory will be updated from saved data
	 *            matching their gamemode and world
	 * @param usePlayerUpdate Whether or not {@link Player#updateInventory()}
	 *            should be called after setting their inventory. */
	public final void updatePlayerInventory(final Player player, boolean usePlayerUpdate) {
		this.updatePlayerInventories(player, player.getWorld(), player.getGameMode(), true, false, usePlayerUpdate);
	}
	
	/** @param player The player whose inventory will be updated from saved data
	 *            matching their gamemode and world
	 * @param invType The inventory type to update, or &quot;all&quot; to update
	 *            all three types.
	 * @param usePlayerUpdate Whether or not {@link Player#updateInventory()}
	 *            should be called after setting their inventory. */
	public final void updatePlayerInventory(final Player player, String invType, boolean usePlayerUpdate) {
		this.updatePlayerInventory(player, invType, player.getWorld(), player.getGameMode(), true, false, usePlayerUpdate);
	}
	
	/** @param player The player whose inventory will be set from saved data
	 * @param world The world whose items will be loaded to be set on the player
	 * @param mode The gamemode of the inventory to set
	 * @param overwriteWithEmpty Whether or not the player's existing inventory
	 *            of each type should be overwritten with saved data that is
	 *            empty
	 * @param updateMisc Whether or not player levels, exp, hunger, saturation,
	 *            walkspeed, flyspeed, foodlevel, health, and max health should
	 *            be set from saved data as well
	 * @param usePlayerUpdate Whether or not {@link Player#updateInventory()}
	 *            should be called after setting their inventory. */
	public final void updatePlayerInventories(final Player player, World world, GameMode mode, boolean overwriteWithEmpty, boolean updateMisc, boolean usePlayerUpdate) {
		updatePlayerInventory(player, "all", world, mode, overwriteWithEmpty, updateMisc, usePlayerUpdate);
	}
	
	/** @param player The player whose inventory will be set from saved data
	 * @param invType The inventory type to update, or &quot;all&quot; to update
	 *            all three types.
	 * @param world The world whose items will be loaded to be set on the player
	 * @param mode The gamemode of the inventory to set
	 * @param overwriteWithEmpty Whether or not the player's existing inventory
	 *            of each type should be overwritten with saved data that is
	 *            empty
	 * @param updateMisc Whether or not player levels, exp, hunger, saturation,
	 *            walkspeed, flyspeed, foodlevel, health, and max health should
	 *            be set from saved data as well
	 * @param usePlayerUpdate Whether or not {@link Player#updateInventory()}
	 *            should be called after setting their inventory. */
	public final void updatePlayerInventory(final Player player, String invType, World world, GameMode mode, boolean overwriteWithEmpty, boolean updateMisc, boolean usePlayerUpdate) {
		Main.DEBUG("updatePlayerInventories(player=" + player.getName() + ", invType=" + invType + ", world=" + world.getName() + ", mode=" + mode.name() + ", overwriteWithEmpty=" + overwriteWithEmpty + ", updateMisc=" + updateMisc + ")");
		world = getCorrectWorldToUse(world);
		mode = getCorrectGameModeToUse(mode);
		
		final boolean doInv = invType.equals("all") || invType.equals("inv");
		final boolean doEnder = invType.equals("all") || invType.equals("ender");
		final boolean doArmor = invType.equals("all") || invType.equals("armor");
		if(!doInv && !doEnder && !doArmor) {
			return;
		}
		
		final GMWorldData data = GamemodeInventory.this.getDataFor(world);
		Inventory inventory = this.getInventory(world, mode);
		Inventory enderChest = this.getEnderChest(world, mode);
		
		/*
		 * if(mode == GameMode.SURVIVAL) {
		 * inventory =
		 * GamemodeInventory.this.getSurvivalInventory(world).getContents();
		 * armor =
		 * GamemodeInventory.this.getSurvivalArmorInventory(world).getContents
		 * ();
		 * enderChest =
		 * GamemodeInventory.this.getSurvivalEnderChest(world).getContents();
		 * } else {
		 * inventory =
		 * GamemodeInventory.this.getCreativeInventory(world).getContents();
		 * armor =
		 * GamemodeInventory.this.getCreativeArmorInventory(world).getContents
		 * ();
		 * enderChest =
		 * GamemodeInventory.this.getCreativeEnderChest(world).getContents();
		 * }
		 */
		
		if(overwriteWithEmpty) {
			if(doInv) {
				player.getInventory().clear();
				player.getInventory().setHeldItemSlot(0);
			}
			if(doEnder) {
				player.getEnderChest().clear();
			}
			if(doArmor) {
				player.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
			}
			if(usePlayerUpdate) {
				player.updateInventory();
			}
		}
		
		final PlayerInventory playerInv = player.getInventory();
		if(doInv) {
			playerInv.setItemInOffHand(data.getShieldItem(mode));
			if(!isItemStackArrayEmpty(inventory.getContents()) || overwriteWithEmpty) {
				for(int i = 0; i < inventory.getSize(); i++) {
					if(i < playerInv.getSize()) {
						playerInv.setItem(i, inventory.getItem(i));
					}
				}
			} else {
				Main.DEBUG("===== &e" + mode.name() + " Inventory for player \"" + player.getName() + "\" in world \"&f" + world.getName() + "&r&e\" was empty!");
			}
		}
		if(doArmor) {
			if(!isItemStackArrayEmpty(data.armorInvs.get(mode).getContents()) || overwriteWithEmpty) {
				playerInv.setHelmet(data.getHelmet(mode));
				playerInv.setChestplate(data.getChestplate(mode));
				playerInv.setLeggings(data.getLeggings(mode));
				playerInv.setBoots(data.getBoots(mode));
			} else {
				Main.DEBUG("===== &e" + mode.name() + " Armor Inventory for player \"" + player.getName() + "\" in world \"&f" + world.getName() + "&r&e\" was empty!");
			}
		}
		if(doEnder) {
			if(!isItemStackArrayEmpty(enderChest.getContents()) || overwriteWithEmpty) {
				final Inventory enderInv = player.getEnderChest();
				for(int i = 0; i < enderChest.getSize(); i++) {
					if(i < enderInv.getSize()) {
						enderInv.setItem(i, enderChest.getItem(i));
					}
				}
			} else {
				Main.DEBUG("===== &e" + mode.name() + " Ender Chest for player \"" + player.getName() + "\" in world \"&f" + world.getName() + "&r&e\" was empty!");
			}
		}
		if(doInv) {
			Integer selectedSlot = data.selectedInvSlots.get(mode);
			if(selectedSlot != null) {
				playerInv.setHeldItemSlot(selectedSlot.intValue());
			}
		}
		if(usePlayerUpdate) {
			player.updateInventory();
		}
		
		// ===
		if(updateMisc) {
			Integer level = data.expLevels.get(mode);
			Float exp = data.exps.get(mode);
			Integer foodLevel = data.foodLevels.get(mode);
			Double maxHealth = data.maxHealths.get(mode);
			Double health = data.healths.get(mode);
			Float saturation = data.saturations.get(mode);
			Float walkSpeed = data.walkSpeeds.get(mode);
			Float flySpeed = data.flySpeeds.get(mode);
			Boolean canFly = data.canFlys.get(mode);
			if(level != null) {
				player.setLevel(level.intValue());
			}
			if(exp != null) {
				player.setExp(exp.floatValue());
			}
			if(foodLevel != null) {
				player.setFoodLevel(foodLevel.intValue());
			}
			if(maxHealth != null) {
				player.setMaxHealth(maxHealth.doubleValue());
			}
			if(health != null) {
				player.setHealth(health.doubleValue());
			}
			if(saturation != null) {
				player.setSaturation(saturation.floatValue());
			}
			if(walkSpeed != null) {
				player.setWalkSpeed(walkSpeed.floatValue());
			}
			if(canFly != null) {
				player.setAllowFlight(canFly.booleanValue());
			}
			if(flySpeed != null) {
				player.setFlySpeed(flySpeed.floatValue());
			}
		}
		PlayerStatus.getPlayerStatus(player).updateFromPlayer(false, false);
	}
	
	public final String getDetailsOfData() {
		String rtrn = "";
		for(GMWorldData data : this.data) {
			rtrn += "=====\r\n";
			rtrn += "Data: Player \"" + data.getPlayerName() + "\" in World \"" + data.getWorldName() + "\":\r\n";
			rtrn += "Survival armor inv: " + getNumOfItemsIn(data.armorInvs.get(GameMode.SURVIVAL).getContents()) + " items\r\n";
			rtrn += "Survival inventory: " + getNumOfItemsIn(data.getSurvivalInventory().getContents()) + " items\r\n";
			rtrn += "Survival enderchest: " + getNumOfItemsIn(data.getSurvivalEnderChest().getContents()) + " items\r\n";
			rtrn += "Creative armor inv: " + getNumOfItemsIn(data.armorInvs.get(GameMode.CREATIVE).getContents()) + " items\r\n";
			rtrn += "Creative inventory: " + getNumOfItemsIn(data.getCreativeInventory().getContents()) + " items\r\n";
			rtrn += "Creative enderchest: " + getNumOfItemsIn(data.getCreativeEnderChest().getContents()) + " items\r\n";
		}
		return rtrn.isEmpty() ? "No data found!" : rtrn + "=====";
	}
	
	// ========
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		final Inventory inv = event.getSource();
		final InventoryHolder entity = inv.getHolder();
		if(entity instanceof Player) {
			final Player player = (Player) entity;
			if(!this.equals(player)) {
				return;
			}
			final PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			final BukkitRunnable task = new BukkitRunnable() {
				@Override
				public final void run() {
					status.updateSavedInventoryFromPlayer(false, true);
				}
			};
			task.runTaskLater(Main.getInstance(), 1L);
		}
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {// XXX No event handler annotation on purpose!
		final Player player = event.getPlayer();
		if(!this.equals(player)) {
			return;
		}
		Main.DEBUG("===== " + Main.pluginName + "&aonPlayerJoin() for player \"" + player.getName() + "\"'s GamemodeInventory:");
		Main.DEBUG(this.getDetailsOfData());
		
		Main.scheduler.runTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				GamemodeInventory.this.updatePlayerInventories(player, player.getWorld(), player.getGameMode(), true, true, true);
			}
		});
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {// XXX No event handler
															// annotation on
														// purpose!
		final Player player = event.getPlayer();
		if(!this.equals(player)) {
			return;
		}
		this.saveCurrentPlayerInventory(player, player.getGameMode(), player.getWorld());
		this.saveToFileAndDispose();
	}
	
	protected volatile boolean	gamemodeChanging	= false;
	protected volatile boolean	worldChanging		= false;
	
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {// XXX No event handler annotation on purpose!
		if(event.isCancelled()) {
			return;
		}
		final Player player = event.getPlayer();
		if(!this.equals(player)) {
			return;
		}
		if(this.gamemodeChanging) {
			return;
		}
		this.gamemodeChanging = true;
		GameMode mode = getCorrectGameModeToUse(player.getGameMode());
		World world = getCorrectWorldToUse(player.getWorld());
		Main.DEBUG("[GMChange] Saving player's " + mode.name() + " inv for world \"" + world.getName() + "(" + world.getUID().toString() + ")\"...");
		this.saveCurrentPlayerInventory(player, mode, world);
		Main.scheduler.runTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				World world = getCorrectWorldToUse(player.getWorld());
				GameMode gm = getCorrectGameModeToUse(player.getGameMode());
				Main.DEBUG("[GMChange] Loading player's " + gm.name() + " inv for world \"" + world.getName() + "(" + world.getUID().toString() + ")\"...");
				GamemodeInventory.this.updatePlayerInventories(player, player.getWorld(), gm, true, true, true);
				GamemodeInventory.this.gamemodeChanging = false;
			}
		});
	}
	
	public final void onPlayerChangedWorld(PlayerChangedWorldEvent event) {// XXX No event handler annotation on purpose!
		final Player player = event.getPlayer();
		if(!this.equals(player)) {
			return;
		}
		if(this.worldChanging) {
			return;
		}
		this.worldChanging = true;
		GameMode mode = getCorrectGameModeToUse(player.getGameMode());
		final World oldWorld = getCorrectWorldToUse(event.getFrom());
		Main.DEBUG("[WorldLoad] Saving player's " + mode.name() + " inv for world \"" + oldWorld.getName() + "(" + oldWorld.getUID().toString() + ")\"...");
		this.saveCurrentPlayerInventory(player, mode, oldWorld);
		Main.scheduler.runTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				World newWorld = getCorrectWorldToUse(player.getWorld());
				if(newWorld != oldWorld) {
					Main.DEBUG("[WorldLoad] Loading player's " + player.getGameMode().name() + " inv for world \"" + newWorld.getName() + "(" + newWorld.getUID().toString() + ")\"...");
					GamemodeInventory.this.updatePlayerInventories(player, newWorld, player.getGameMode(), true, true, true);
					GamemodeInventory.this.worldChanging = false;
				}
			}
		});
	}
	
}
