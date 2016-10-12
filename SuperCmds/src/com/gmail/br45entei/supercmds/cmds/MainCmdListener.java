package com.gmail.br45entei.supercmds.cmds;

import com.gmail.br45entei.supercmds.BlockAPI;
import com.gmail.br45entei.supercmds.InventoryAPI;
import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.api.ItemStackFromString;
import com.gmail.br45entei.supercmds.api.Permissions;
import com.gmail.br45entei.supercmds.api.credits.ItemLibrary;
import com.gmail.br45entei.supercmds.cmds.BlockSetData.BlockSetType;
import com.gmail.br45entei.supercmds.file.Home;
import com.gmail.br45entei.supercmds.file.Kits;
import com.gmail.br45entei.supercmds.file.PlayerChat;
import com.gmail.br45entei.supercmds.file.PlayerChat.Mail;
import com.gmail.br45entei.supercmds.file.PlayerEcoData;
import com.gmail.br45entei.supercmds.file.PlayerPermissions;
import com.gmail.br45entei.supercmds.file.PlayerStatus;
import com.gmail.br45entei.supercmds.file.TicketData;
import com.gmail.br45entei.supercmds.file.Warps;
import com.gmail.br45entei.supercmds.file.inventory.GamemodeInventory;
import com.gmail.br45entei.supercmds.util.CodeUtils;
import com.gmail.br45entei.supercmds.util.Vector3;
import com.gmail.br45entei.supercmds.util.VersionUtil;
import com.gmail.br45entei.util.StringUtil;
import com.google.common.base.Joiner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

@SuppressWarnings("javadoc")
public final class MainCmdListener implements Listener {
	public static boolean								isConsoleAfk	= false;
	private static volatile MainCmdListener				instance;
	
	public static final ConcurrentLinkedDeque<String>	bannedIPs		= new ConcurrentLinkedDeque<>();//TODO
	
	public static final MainCmdListener getInstance() {
		return instance == null ? (instance = new MainCmdListener()) : instance;
	}
	
	private MainCmdListener() {
		if(instance != null) {
			throw new Error("Wat u doing.");
		}
		Main.sendConsoleMessage(Main.pluginName + "&f========== &aN&fet&aM&finecraft&aS&ferver &aVersion: &f" + VersionUtil.getNMSVersion() + " &f==========");
		//BlockNMS.initialize();
		InventoryAPI.initialize();
		GamemodeInventory.initialize();
		BlockSetData.initialize();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerJoinEvent(PlayerJoinEvent evt) {
		final Player newPlayer = evt.getPlayer();
		Main.DEBUG(Main.pluginName + "Player \"" + newPlayer.getDisplayName() + "\" has just logged on.");
		PlayerStatus.getPlayerStatus(newPlayer).onPlayerJoin(evt);
		GamemodeInventory.getGamemodeInventoryForPlayer(newPlayer).onPlayerJoin(evt);
		Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				PlayerChat chat = PlayerChat.getPlayerChat(newPlayer);
				if(chat.isPlayerOnline()) {
					ArrayList<Mail> unreadMail = chat.getAllMail();
					if(!unreadMail.isEmpty()) {
						Main.sendMessage(chat.getPlayer(), Main.pluginName + "&aYou have &f" + unreadMail.size() + "&a unread mail in your inbox!&z&aType /mail read to view them.");
					}
				}
			}
		}, 20L);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerQuitEvent(PlayerQuitEvent evt) {
		Player oldPlayer = evt.getPlayer();
		GamemodeInventory.getGamemodeInventoryForPlayer(oldPlayer).onPlayerQuit(evt);
		Main.DEBUG(Main.pluginName + "Player \"" + oldPlayer.getDisplayName() + "\" has just logged out.");
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onEntityTargetEvent(EntityTargetEvent event) {
		if((event.getTarget() instanceof Player)) {
			PlayerStatus status = PlayerStatus.getPlayerStatus((Player) event.getTarget());
			if((status.isVanishModeOn) || (status.isGodModeOn)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onEntityTargetEvent(EntityTargetLivingEntityEvent event) {
		if((event.getTarget() instanceof Player)) {
			PlayerStatus status = PlayerStatus.getPlayerStatus((Player) event.getTarget());
			if((status.isVanishModeOn) || (status.isGodModeOn)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage().replace(event.getFormat(), "");
		try {
			if(Main.startsWithIgnoreCase(msg, "what is")) {
				if((Main.startsWithIgnoreCase(msg, "what is half of ")) && (CodeUtils.isStrAValidDouble(msg.substring(16)))) {
					double input = CodeUtils.getDoubleFromStr(msg.substring(16), 0.0D);
					double result = input / 2.0D;
					Main.sendMessage(player, Main.pluginName + "&aHalf of &f" + input + "&a is: &f" + result + "&a!");
					event.setCancelled(true);
					return;
				}
				String[] args = msg.split(Pattern.quote(" "));
				String question = Main.getElementsFromStringArrayAtIndexAsString(args, 2);
				if(args.length >= 4) {
					double result = 0.0D;
					String operator = "+";
					String invalidOperator = null;
					String invalidNumber = null;
					
					int i = 2;
					int j = 2;
					for(; i < args.length; i++) {
						if(j % 2 == 0) {
							if(Main.checkIsNumber(args[i])) {
								double number = Double.valueOf(args[i]).doubleValue();
								if(operator.equals("+")) {
									result += number;
								} else if(operator.equals("-")) {
									result -= number;
								} else if(operator.equals("*")) {
									result *= number;
								} else if(operator.equals("/")) {
									result /= number;
								} else if(operator.equals("^")) {
									result = Math.pow(result, number);
								} else if(operator.equals("%")) {
									result %= number;
								} else if(operator.equalsIgnoreCase("squared")) {
									result = Math.pow(result, 2.0D);
									j--;
								} else if(operator.equalsIgnoreCase("cubed")) {
									result = Math.pow(result, 3.0D);
									j--;
								} else {
									invalidOperator = operator;
									break;
								}
							} else {
								invalidNumber = args[i];
								break;
							}
						} else {
							operator = args[i];
							if((operator.equalsIgnoreCase("sqrt")) || (operator.equalsIgnoreCase("squareroot"))) {
								result = Math.sqrt(result);
								j--;
							}
						}
						j++;
					}
					if(invalidNumber != null) {
						if(i != 2) {
							Main.sendMessage(player, Main.pluginName + "&a\"&f" + invalidNumber + "&a\" is not a valid number.");
							event.setCancelled(true);
							return;
						}
						return;
					}
					if(invalidOperator != null) {
						Main.sendMessage(player, Main.pluginName + "&a\"&f" + invalidOperator + "&a\" is not a valid operator.");
						event.setCancelled(true);
						return;
					}
					Main.sendMessage(player, Main.pluginName + "&aThe result for \"&f" + question + "&a\" is: " + result);
					event.setCancelled(true);
					return;
				}
			}
		} catch(Throwable ignored) {
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static void onEntityDamageEvent(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			final PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			if(event.getCause() == DamageCause.LIGHTNING && Permissions.hasPerm(player, "supercmds.use.thor") && checkForMjölnir(player.getInventory())) {
				event.setCancelled(true);
				return;
			}
			if((!Permissions.hasPerm(player, "supercmds.use.god")) && (status.isGodModeOn)) {
				status.isGodModeOn = false;
				Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				event.setCancelled(false);
				return;
			}
			event.setCancelled(status.isGodModeOn);
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					status.updatePlayerHearts(false);
				}
			});
		} else if((event.getEntity() instanceof Item)) {
			Item item = (Item) event.getEntity();
			if(isItemMjölnir(item)) {
				event.setCancelled(true);
				updateMjölnir(item);
			}
		}
	}
	
	public static final void dropPlayersItemInHand(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		Item check = player.getWorld().dropItemNaturally(player.getLocation(), item);
		GamemodeInventory.updateInventoryNatively(player.getInventory());
		if(isItemMjölnir(check)) {
			updateMjölnir(check);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerBreakBlockEvent(BlockBreakEvent event) {
		final Player player = event.getPlayer();
		//final PlayerStatus status = PlayerStatus.getPlayerStatus(player);
		if(isItemMjölnir(player.getInventory().getItemInMainHand())) {
			if(Permissions.hasPerm(player, "supercmds.use.thor")) {
				Block block = event.getBlock();
				if(block.getType() == Material.OBSIDIAN || block.getType() == Material.PISTON_STICKY_BASE || block.getType() == Material.PISTON_EXTENSION || block.getType() == Material.PISTON_BASE || block.getType() == Material.PISTON_MOVING_PIECE) {
					event.setCancelled(true);
				}
			} else {
				event.setCancelled(true);
				dropPlayersItemInHand(player);
			}
		}
	}
	
	public static final boolean checkForMjölnir(Inventory inventory) {
		for(ItemStack item : inventory) {
			if(isItemMjölnir(item)) {
				return true;
			}
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		Entity attacker = event.getDamager();
		Entity victim = event.getEntity();
		if(attacker instanceof Projectile) {
			Projectile thrownObject = (Projectile) attacker;
			ProjectileSource source = thrownObject.getShooter();
			if(source instanceof Entity) {
				attacker = (Entity) source;
			}
		}
		if(attacker instanceof Player && victim instanceof LivingEntity) {
			Player player = (Player) attacker;
			LivingEntity dead = (LivingEntity) victim;
			if(isItemMjölnir(player.getInventory().getItemInMainHand()) && Permissions.hasPerm(player, "supercmds.use.thor")) {
				event.setDamage(dead.getMaxHealth());//dead.setHealth(0);
			}
		} else if(attacker instanceof LivingEntity && victim instanceof Player) {
			Player player = (Player) victim;
			LivingEntity dead = (LivingEntity) attacker;
			if(Permissions.hasPerm(player, "supercmds.use.thor")) {
				if(checkForMjölnir(player.getInventory())) {
					event.setDamage(0);
					event.setCancelled(true);
					if(dead instanceof Player) {
						Player poophead = (Player) dead;
						boolean exempt = poophead.getGameMode() == GameMode.CREATIVE || poophead.getGameMode() == GameMode.SPECTATOR || Permissions.hasPerm(poophead, "supercmds.use.thor") || PlayerStatus.getPlayerStatus(poophead).isGodModeOn;
						if(!exempt) {
							dead.setHealth(0);
						}
					} else {
						dead.setHealth(0);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerRegainHealthEvent(EntityRegainHealthEvent event) {
		if((event.getEntity() instanceof Player)) {
			final PlayerStatus status = PlayerStatus.getPlayerStatus((Player) event.getEntity());
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					if(status.isPlayerOnline()) {
						status.health = status.getPlayer().getHealth();
						status.hearts = new Long(Math.round(status.getPlayer().getMaxHealth() / 2.0D)).intValue();
					}
				}
			});
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		GamemodeInventory gmi = GamemodeInventory.getGamemodeInventoryForPlayer(player);
		gmi.onPlayerChangedWorld(event);
		Main.scheduler.runTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		PlayerStatus status = PlayerStatus.getPlayerStatus(event.getPlayer());
		if(status.hasHome()) {
			event.setRespawnLocation(status.homeLocation);
		} else {
			event.setRespawnLocation(Main.getSpawnLocation());
		}
		PlayerStatus.updatePlayerStateStates();
		/*if(!Permissions.hasPerm(event.getPlayer(), "supercmds.special.keepInvOnDeath")) {
			return;
		}
		String uuid = event.getPlayer().getUniqueId().toString();
		if(onDeathItems.containsKey(uuid)) {
			event.getPlayer().getInventory().clear();
			ItemStack[] arrayOfItemStack;
			int j = (arrayOfItemStack = onDeathItems.get(uuid)).length;
			for(int i = 0; i < j; i++) {
				ItemStack stack = arrayOfItemStack[i];
				if(stack != null) {
					event.getPlayer().getInventory().addItem(new ItemStack[] {stack});
				}
			}
			onDeathItems.remove(uuid);
		}
		if(onDeathExp.containsKey(uuid)) {
			event.getPlayer().setExp(onDeathExp.get(uuid).floatValue());
			onDeathExp.remove(uuid);
		}
		if(onDeathLevel.containsKey(uuid)) {
			event.getPlayer().setLevel(onDeathLevel.get(uuid).intValue());
			onDeathLevel.remove(uuid);
		}*/
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
		if(event.isCancelled()) {
			return;
		}
		PlayerStatus.getPlayerStatus(event.getPlayer()).onPlayerEnterBed();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
		PlayerStatus.getPlayerStatus(event.getPlayer()).onPlayerLeaveBed();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
		final Player player = event.getPlayer();
		final PlayerStatus status = PlayerStatus.getPlayerStatus(player);
		GamemodeInventory gmi = GamemodeInventory.getGamemodeInventoryForPlayer(player);
		gmi.onPlayerGameModeChange(event);
		final boolean playerIsFlying = player.isFlying();
		Runnable code = new Runnable() {
			@Override
			public void run() {
				if(status.isFlyModeOn) {
					player.setAllowFlight(true);
					player.setFlying(playerIsFlying);
					Main.DEBUG("Set player's fly mode to: \"" + playerIsFlying + "\"(actual flying boolean: " + player.isFlying() + ")");
				}
				PlayerStatus.updatePlayerStateStates();
			}
		};
		//Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), code);
		Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), code, 1L);//5L);
	}
	
	public static final String getNoPlayerMsg(String playerName) {
		return Main.pluginName + "&ePlayer \"&f" + playerName + "&r&e\" does not exist or is not online.";
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerMoveEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		PlayerStatus status = PlayerStatus.getPlayerStatus(player);
		if(!status.isAfk) {
			return;
		}
		if((player.getLocation().getBlockY() <= -63) && (Main.teleportToSpawnOnVoid)) {
			player.setVelocity(new Vector(0, 0, 0));
			player.teleport(Main.getSpawnLocation());
		}
		Location lastAfkSpot = status.lastAfkLocation;
		Location currentLocation = player.getLocation();
		double xDiff = currentLocation.getX() - lastAfkSpot.getX();
		double yDiff = currentLocation.getY() - lastAfkSpot.getY();
		double zDiff = currentLocation.getZ() - lastAfkSpot.getZ();
		double distance = Math.abs(Math.sqrt(Math.pow(xDiff, 2.0D) + Math.pow(yDiff, 2.0D) + Math.pow(zDiff, 2.0D)));
		if(distance >= 2.0D) {
			status.toggleAfkState();
		} else {
			Main.DEBUG(Main.pluginName + "&aDebug: Player \"&f" + player.getDisplayName() + "&r&a\" did not move up to two blocks away from last afk spot; not changing player's afk state.");
		}
	}
	
	@Deprecated
	public static final HashMap<String, ItemStack[]>	onDeathItems	= new HashMap<>();
	
	public static final HashMap<String, Float>			onDeathExp		= new HashMap<>();
	
	public static final HashMap<String, Integer>		onDeathLevel	= new HashMap<>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerDeathEvent(PlayerDeathEvent event) {
		if(!Permissions.hasPerm(event.getEntity(), "supercmds.special.keepInvOnDeath")) {
			return;
		}
		event.setKeepInventory(true);
		event.setDroppedExp(0);
		event.setKeepLevel(true);
		//ItemStack[] content = event.getEntity().getInventory().getContents();
		//String uuid = event.getEntity().getUniqueId().toString();
		//onDeathItems.put(uuid, content);
		//onDeathExp.put(uuid, new Float(event.getEntity().getExp()));
		//onDeathLevel.put(uuid, new Integer(event.getEntity().getLevel()));
		//event.getEntity().getInventory().clear();
		//event.getEntity().setExp(0.0F);
		//event.getEntity().setLevel(0);
	}
	
	public static final void updateTopInventoriesFromOwnerChanges(Player owner, boolean oneTickLater) {
		if(owner == null) {
			return;
		}
		updateTopInventoriesFromOwnerChanges(owner, owner.getGameMode(), owner.getWorld().getUID(), oneTickLater);
	}
	
	public static final void updateTopInventoriesFromOwnerChanges(Player owner, GameMode mode, UUID world, boolean oneTickLater) {
		if(owner == null) {
			return;
		}
		PlayerInventory sourceInv = owner.getInventory();
		Inventory sourceEnder = owner.getEnderChest();
		
		ItemStack helmet = sourceInv.getHelmet();
		ItemStack chestplate = sourceInv.getChestplate();
		ItemStack leggings = sourceInv.getLeggings();
		ItemStack boots = sourceInv.getBoots();
		ItemStack shield = sourceInv.getItemInOffHand();
		
		PlayerStatus status = PlayerStatus.getPlayerStatus(owner);
		updateTopInventoriesFromOwnerChanges(status, mode, world, GamemodeInventory.getItemMapOfInventory(sourceInv), GamemodeInventory.getItemMapOfInventory(sourceEnder), helmet, chestplate, leggings, boots, shield, oneTickLater);
	}
	
	public static final void updateTopInventoriesFromOwnerChanges(final PlayerStatus status, final GameMode gamemode, final UUID world, final Map<Integer, ItemStack> sourceInv, final Map<Integer, ItemStack> sourceEnder, final ItemStack helmet, final ItemStack chestplate, final ItemStack leggings, final ItemStack boots, final ItemStack shield, boolean oneTickLater) {
		Runnable code = new Runnable() {
			@Override
			public void run() {
				GameMode mode = GamemodeInventory.getCorrectGameModeToUse(gamemode);
				UUID w = GamemodeInventory.getCorrectWorldToUse(world);
				status.setSavedInventory("inv", mode, w, sourceInv);
				status.setSavedInventory("ender", mode, w, sourceEnder);
				
				status.setSavedHelmet(mode, w, helmet);
				status.setSavedChestplate(mode, w, chestplate);
				status.setSavedLeggings(mode, w, leggings);
				status.setSavedBoots(mode, w, boots);
				status.setSavedShieldItem(mode, w, shield);
				
				for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(status.uuid, mode, w, "inv")) {
					if(!status.uuid.toString().equals(info.owner.toString())) {
						if(info.inv != null) {
							GamemodeInventory.copyItemMapIntoInventory(sourceInv, info.inv, true);
						}
						Player viewer = Main.getPlayer(info.viewer);
						if(viewer != null) {
							viewer.updateInventory();
							GamemodeInventory.updateInventoryNatively(viewer.getOpenInventory().getTopInventory());
						}
					}
				}
				for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(status.uuid, mode, w, "ender")) {
					if(!status.uuid.toString().equals(info.owner.toString())) {
						
						if(info.inv != null) {
							GamemodeInventory.copyItemMapIntoInventory(sourceEnder, info.inv, true);
						}
						Player viewer = Main.getPlayer(info.viewer);
						if(viewer != null) {
							viewer.updateInventory();
							GamemodeInventory.updateInventoryNatively(viewer.getOpenInventory().getTopInventory());
						}
					}
				}
				for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(status.uuid, mode, w, "armor")) {
					if(!status.uuid.toString().equals(info.owner.toString())) {
						if(info.inv != null) {
							info.inv.setItem(0, helmet);
							info.inv.setItem(1, chestplate);
							info.inv.setItem(2, leggings);
							info.inv.setItem(3, boots);
							info.inv.setItem(4, shield);
							Player viewer = Main.getPlayer(info.viewer);
							if(viewer != null) {
								ArrayList<ItemStack> itemsToReturn = new ArrayList<>();
								for(int i = 5; i < info.inv.getSize(); i++) {
									ItemStack item = info.inv.getItem(i);
									if((item != null) && (item.getType() != Material.AIR)) {
										info.inv.setItem(i, null);
										itemsToReturn.add(item);
									}
								}
								Main.returnItemsToPlayer(viewer, itemsToReturn);
							}
						}
						Player viewer = Main.getPlayer(info.viewer);
						if(viewer != null) {
							viewer.updateInventory();
							GamemodeInventory.updateInventoryNatively(viewer.getOpenInventory().getTopInventory());
						}
					}
				}
			}
		};
		if(oneTickLater) {
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), code, 2L);
		} else {
			code.run();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		Item item = event.getItem();
		ItemStack stack = item.getItemStack();
		boolean isMjölnir = isItemMjölnir(stack);
		if(isMjölnir && !Permissions.hasPerm(player, "supercmds.use.thor")) {
			event.setCancelled(true);
			updateMjölnir(item);
		}
		
		if(!event.isCancelled()) {
			updateTopInventoriesFromOwnerChanges(player, true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onItemDespawnEvent(ItemDespawnEvent event) {
		Item item = event.getEntity();
		ItemStack stack = item.getItemStack();
		boolean isMjölnir = isItemMjölnir(stack);
		if(isMjölnir) {
			final Location loc = item.getLocation();//event.setCancelled(true);
			item.remove();
			updateMjölnir(loc.getWorld().dropItem(loc, stack));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		if(!event.isCancelled()) {
			updateTopInventoriesFromOwnerChanges(event.getPlayer(), true);
			Item item = event.getItemDrop();
			ItemStack stack = item.getItemStack();
			if(isItemMjölnir(stack)) {
				updateMjölnir(item);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onInventoryOpenEvent(InventoryOpenEvent event) {
		if((!event.isCancelled()) && ((event.getPlayer() instanceof Player))) {
			Player player = (Player) event.getPlayer();
			PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			updateTopInventoriesFromOwnerChanges(player, true);
			
			InventoryViewingInfo invInfo = status.getInventoryBeingViewed();
			if(invInfo != null) {
				PlayerStatus ownerStatus = PlayerStatus.getPlayerStatus(invInfo.owner);
				ownerStatus.updateSavedInventoryFromPlayer(false, false);
				updateOpenTopInventories(invInfo.owner, invInfo.ownerGameMode, invInfo.ownerWorld, invInfo.invType, ownerStatus.getSavedInventory(invInfo.invType, invInfo.ownerGameMode, invInfo.ownerWorld), false);
				status.saveToFile();
				ownerStatus.saveToFile();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onInventoryCloseEvent(InventoryCloseEvent event) {
		if((event.getPlayer() instanceof Player)) {
			Player clicker = (Player) event.getPlayer();
			PlayerStatus status = PlayerStatus.getPlayerStatus(clicker);
			InventoryViewingInfo invInfo = status.getInventoryBeingViewed();
			if(invInfo != null) {
				PlayerStatus owner = PlayerStatus.getPlayerStatus(invInfo.owner);
				updateOpenTopInventories(owner.uuid, invInfo.ownerGameMode, invInfo.ownerWorld, status.invTypeBeingViewed, GamemodeInventory.getItemMapOfInventory(clicker.getOpenInventory().getTopInventory()), true);
				owner.saveAndDisposeIfPlayerNotOnline();
			}
			status.setViewingInv(null, null, null, null);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
		if(!event.isCancelled()) {
			updateTopInventoriesFromOwnerChanges(event.getPlayer(), false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerItemBreakEvent(PlayerItemBreakEvent event) {
		updateTopInventoriesFromOwnerChanges(event.getPlayer(), true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		if(!event.isCancelled()) {
			updateTopInventoriesFromOwnerChanges(event.getPlayer(), true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
		if(!event.isCancelled()) {
			updateTopInventoriesFromOwnerChanges(event.getPlayer(), true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onServerListPingEvent(ServerListPingEvent event) {
		Iterator<Player> players;
		try {
			players = event.iterator();
		} catch(UnsupportedOperationException e) {
			return;
		}
		while(players.hasNext()) {
			Player player = players.next();
			PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			if(status.isVanishModeOn) {
				players.remove();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent event) {
		ArrayList<String> vanishedPlayerNames = new ArrayList<>();
		for(PlayerStatus status : PlayerStatus.getAllStatuses()) {
			if(status.isVanishModeOn) {
				vanishedPlayerNames.add(status.name);
			}
		}
		if(vanishedPlayerNames.size() > 0) {
			for(String string : new ArrayList<>(event.getTabCompletions())) {
				if(vanishedPlayerNames.contains(string)) {
					event.getTabCompletions().remove(string);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onVehicleEntityCollisionEvent(VehicleEntityCollisionEvent event) {
		Entity entity = event.getEntity();
		if((entity instanceof Player)) {
			Player player = (Player) entity;
			PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			if(status.isVanishModeOn) {
				event.setCancelled(true);
			}
		}
	}
	
	protected static final void updateOpenTopInventories(UUID owner, GameMode mode, UUID world, String invType, Map<Integer, ItemStack> sourceInv, boolean updateOwner) {
		for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(owner, mode, world, invType)) {
			if(info.inv != null) {
				GamemodeInventory.copyItemMapIntoInventory(sourceInv, info.inv, true);
				if(info.viewer != null) {
					Player clicker = Main.getPlayer(info.viewer);
					if(clicker != null) {
						clicker.setItemOnCursor(new ItemStack(Material.AIR));
						clicker.getOpenInventory().setCursor(null);
						clicker.updateInventory();
					}
				}
			}
		}
		if(updateOwner) {
			PlayerStatus ownerStatus = PlayerStatus.getPlayerStatus(owner);
			if(ownerStatus.isPlayerOnline()) {
				for(int i = 0; i < sourceInv.size(); i++) {
					Integer I = Integer.valueOf(i);
					if(invType.equalsIgnoreCase("inv")) {
						PlayerInventory inv = ownerStatus.getPlayer().getInventory();
						if(i < inv.getSize()) {
							inv.setItem(i, sourceInv.get(I));
						}
					} else if(invType.equalsIgnoreCase("ender")) {
						Inventory inv = ownerStatus.getPlayer().getEnderChest();
						if(i < inv.getSize()) {
							inv.setItem(i, sourceInv.get(I));
						}
					} else if(invType.equalsIgnoreCase("armor")) {
						if(i == 0) {
							ownerStatus.getPlayer().getInventory().setHelmet(sourceInv.get(I));
						} else if(i == 1) {
							ownerStatus.getPlayer().getInventory().setChestplate(sourceInv.get(I));
						} else if(i == 2) {
							ownerStatus.getPlayer().getInventory().setLeggings(sourceInv.get(I));
						} else if(i == 3) {
							ownerStatus.getPlayer().getInventory().setBoots(sourceInv.get(I));
						}
					}
				}
				ownerStatus.getPlayer().updateInventory();
				ownerStatus.updateSavedInventoryFromPlayer(false, false);
			} else if(invType.equalsIgnoreCase("inv")) {
				ownerStatus.setSavedInventory(invType, mode, world, sourceInv);
			} else if(invType.equalsIgnoreCase("armor")) {
				ownerStatus.setSavedInventory(invType, mode, world, sourceInv);
			} else if(invType.equalsIgnoreCase("ender")) {
				ownerStatus.setSavedInventory(invType, mode, world, sourceInv);
			}
			
			ownerStatus.saveAndDisposeIfPlayerNotOnline();
		}
	}
	
	private static final void noEditTopInv(Player clicker, InventoryClickEvent event, PlayerStatus owner) {
		Main.DEBUG("&5TEST: &aif !owner.canOtherPlayersEditInv");
		PlayerStatus clickerStatus = PlayerStatus.getPlayerStatus(clicker);
		event.setCancelled(true);
		clickerStatus.setViewingInv(null, null, null, null);
		clickerStatus.invTypeBeingViewed = "NONE";
		clicker.closeInventory();
		Main.sendMessage(clicker, Main.pluginName + "&eThat player does not wish to allow their inventory to be edited.");
		owner.saveToFile();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onInventoryClickEvent(InventoryClickEvent event) {
		if((!event.isCancelled()) && ((event.getWhoClicked() instanceof Player))) {
			final Player clicker = (Player) event.getWhoClicked();
			final PlayerStatus clickerStatus = PlayerStatus.getPlayerStatus(clicker);
			clickerStatus.updateFromPlayer(false, false);
			String whatInvWasClicked = Main.whatInventoryWasClicked(event);
			String topOrBottom = Main.topOrBottom(event);
			boolean clickCanCauseConflicts = (event.getClick().isShiftClick()) || (event.getClick() == ClickType.DOUBLE_CLICK) || (event.getClick() == ClickType.NUMBER_KEY);
			boolean top = (topOrBottom.equals("TOP")) || (clickCanCauseConflicts);
			if(clickerStatus.isViewingSomeonesInventory()) {
				final InventoryViewingInfo invInfo = clickerStatus.getInventoryBeingViewed();
				Main.DEBUG("&5TEST: &astatus.isViewingSomeonesInventory():");
				final PlayerStatus owner = PlayerStatus.getPlayerStatus(invInfo.owner);
				boolean isViewerOwner = clickerStatus.uuid.toString().equals(owner.uuid.toString());
				if((top) && (!isViewerOwner) && (!Permissions.hasPerm(clicker, "supercmds.use.invsee.editOthers")) && (!Permissions.hasPerm(clicker, "supercmds.use.invsee.override"))) {
					Main.DEBUG("&5TEST: &a[0]no perms");
					event.setCancelled(true);
					clickerStatus.setViewingInv(null, null, null, null);
					clicker.closeInventory();
					Main.sendMessage(clicker, Main.pluginName + "&eYou do not have permission to edit other players' inventories.");
					return;
				}
				Main.sendConsoleMessage("&5TEST: whatInvWasClicked: (" + (top ? "TOP" : "BOTTOM") + ") - " + whatInvWasClicked);
				if((top) && (invInfo.invType.equalsIgnoreCase("armor")) && (top) && (event.getRawSlot() > 3) && (clicker.getOpenInventory().getTopInventory().getSize() == 9)) {
					event.setCancelled(true);
					Main.sendMessage(clicker, Main.pluginName + "&eThat slot does not have a cooresponding armor slot.");
					if(event.getCurrentItem() != null) {
						if((event.getCursor() == null) || (event.getCursor().getType() == Material.AIR)) {
							event.getView().setCursor(event.getCurrentItem());
							event.getView().getTopInventory().setItem(event.getSlot(), event.getCursor());
						}
					} else if((event.getCursor() != null) && (event.getCursor().getType() != Material.AIR) && ((event.getCurrentItem() == null) || (event.getCurrentItem().getType() == Material.AIR))) {
						event.setCurrentItem(event.getCursor());
						event.getView().getTopInventory().setItem(event.getSlot(), event.getCurrentItem());
					}
					return;
				}
				if((top) && (!isViewerOwner) && (!owner.canOtherPlayersEditInv) && (!Permissions.hasPerm(clicker, "supercmds.use.invsee.override"))) {
					noEditTopInv(clicker, event, owner);
					return;
				}
				Inventory sourceInv = top ? event.getView().getTopInventory() : clicker.getInventory();
				final Map<Integer, ItemStack> invMap = GamemodeInventory.getItemMapOfInventory(sourceInv);
				if(invInfo.invType.equals("inv")) {
					if(top) {
						owner.setSavedInventory("inv", invInfo.ownerGameMode, invInfo.ownerWorld, invMap);
						Main.scheduler.runTask(Main.getInstance(), new Runnable() {
							@Override
							public final void run() {
								clickerStatus.updatePlayerFromSavedInventory(false);
								for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(clickerStatus.uuid, invInfo.ownerGameMode, invInfo.ownerWorld, "inv")) {
									Player viewer = Main.getPlayer(info.viewer);
									if(clickerStatus.uuid.toString().equals(info.owner.toString())) {
										if(viewer != null) {
											viewer.updateInventory();
										}
									} else {
										if(info.inv != null) {
											GamemodeInventory.copyItemMapIntoInventory(invMap, info.inv, true);
										}
										if(viewer != null) viewer.updateInventory();
									}
								}
							}
						});
						if(!isViewerOwner) {
							clickerStatus.updateSavedInventoryFromPlayer(false, false);
							Main.scheduler.runTask(Main.getInstance(), new Runnable() {
								@Override
								public final void run() {
									clickerStatus.updatePlayerFromSavedInventory(true);
									Map<Integer, ItemStack> invMap = GamemodeInventory.getItemMapOfInventory(clicker.getInventory());
									for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(clickerStatus.uuid, clicker.getGameMode(), clicker.getWorld().getUID(), "inv")) {
										if(info.inv != null) {
											GamemodeInventory.copyItemMapIntoInventory(invMap, info.inv, true);
										}
										Player viewer = Main.getPlayer(info.viewer);
										if(viewer != null) {
											viewer.updateInventory();
										}
									}
								}
							});
						}
					} else {
						clickerStatus.updateSavedInventoryFromPlayer(false, false);
						Main.scheduler.runTask(Main.getInstance(), new Runnable() {
							@Override
							public final void run() {
								Map<Integer, ItemStack> invMap = GamemodeInventory.getItemMapOfInventory(clicker.getInventory());
								for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(clickerStatus.uuid, clicker.getGameMode(), clicker.getWorld().getUID(), "inv")) {
									if(info.inv != null) {
										GamemodeInventory.copyItemMapIntoInventory(invMap, info.inv, true);
									}
									Player viewer = Main.getPlayer(info.viewer);
									if(viewer != null) {
										viewer.updateInventory();
									}
								}
							}
						});
					}
				} else if(invInfo.invType.equals("ender")) {
					if(top) {
						owner.setSavedInventory("ender", invInfo.ownerGameMode, invInfo.ownerWorld, invMap);
						Main.scheduler.runTask(Main.getInstance(), new Runnable() {
							@Override
							public final void run() {
								clickerStatus.updatePlayerFromSavedInventory(true);
								for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(clickerStatus.uuid, invInfo.ownerGameMode, invInfo.ownerWorld, "ender")) {
									if(!clickerStatus.uuid.toString().equals(info.owner.toString())) {
										if(info.inv != null) {
											GamemodeInventory.copyItemMapIntoInventory(invMap, info.inv, true);
										}
										Player viewer = Main.getPlayer(info.viewer);
										if(viewer != null) viewer.updateInventory();
									}
								}
							}
						});
					} else {
						clickerStatus.updateSavedInventoryFromPlayer(false, false);
						Main.scheduler.runTask(Main.getInstance(), new Runnable() {
							@Override
							public final void run() {
								clickerStatus.updateSavedInventoryFromPlayer(false, false);
								Map<Integer, ItemStack> invMap = GamemodeInventory.getItemMapOfInventory(clicker.getInventory());
								for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(clickerStatus.uuid, clicker.getGameMode(), clicker.getWorld().getUID(), "inv")) {
									if(!clickerStatus.uuid.toString().equals(info.owner.toString())) {
										if(info.inv != null) {
											GamemodeInventory.copyItemMapIntoInventory(invMap, info.inv, true);
										}
										Player viewer = Main.getPlayer(info.viewer);
										if(viewer != null) viewer.updateInventory();
									}
								}
							}
						});
					}
				} else if(invInfo.invType.equals("armor")) {
					if(top) {
						Main.scheduler.runTask(Main.getInstance(), new Runnable() {
							@Override
							public final void run() {
								ItemStack helmet = invInfo.inv.getItem(0);
								ItemStack chestplate = invInfo.inv.getItem(1);
								ItemStack leggings = invInfo.inv.getItem(2);
								ItemStack boots = invInfo.inv.getItem(3);
								owner.setSavedHelmet(invInfo.ownerGameMode, invInfo.ownerWorld, helmet);
								owner.setSavedChestplate(invInfo.ownerGameMode, invInfo.ownerWorld, chestplate);
								owner.setSavedLeggings(invInfo.ownerGameMode, invInfo.ownerWorld, leggings);
								owner.setSavedBoots(invInfo.ownerGameMode, invInfo.ownerWorld, boots);
								owner.updatePlayerFromSavedInventory(true);
								for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(owner.uuid, invInfo.ownerGameMode, invInfo.ownerWorld, "armor")) {
									if(!owner.uuid.toString().equals(info.owner.toString())) {
										if(info.inv != null) {
											info.inv.setItem(0, helmet);
											info.inv.setItem(1, chestplate);
											info.inv.setItem(2, leggings);
											info.inv.setItem(3, boots);
											Player viewer = Main.getPlayer(info.viewer);
											if(viewer != null) {
												ArrayList<ItemStack> itemsToReturn = new ArrayList<>();
												for(int i = 4; i < info.inv.getSize(); i++) {
													ItemStack item = info.inv.getItem(i);
													if((item != null) && (item.getType() != Material.AIR)) {
														info.inv.setItem(i, null);
														itemsToReturn.add(item);
													}
												}
												Main.returnItemsToPlayer(viewer, itemsToReturn);
											}
										}
										Player viewer = Main.getPlayer(info.viewer);
										if(viewer != null) viewer.updateInventory();
									}
								}
							}
						});
					}
					clickerStatus.updateSavedInventoryFromPlayer(false, false);
					Main.scheduler.runTask(Main.getInstance(), new Runnable() {
						@Override
						public final void run() {
							clickerStatus.updateSavedInventoryFromPlayer(false, false);
							Map<Integer, ItemStack> invMap = GamemodeInventory.getItemMapOfInventory(clicker.getInventory());
							for(InventoryViewingInfo info : PlayerStatus.getAllOpenInventoriesOfTypeForPlayer(clickerStatus.uuid, clicker.getGameMode(), clicker.getWorld().getUID(), "inv")) {
								if(info.inv != null) {
									GamemodeInventory.copyItemMapIntoInventory(invMap, info.inv, true);
								}
								Player viewer = Main.getPlayer(info.viewer);
								if(viewer != null) {
									viewer.updateInventory();
								}
							}
						}
					});
				}
			} else {
				clickerStatus.updateSavedInventoryFromPlayer(true, false);
				updateTopInventoriesFromOwnerChanges(clicker, true);
			}
		}
	}
	
	public static final void resetCommandBlock(CommandSender sender) {
		if((sender instanceof BlockCommandSender)) {
			Block block = ((BlockCommandSender) sender).getBlock();
			CommandBlock state = (CommandBlock) block.getState();
			String c = state.getCommand();
			block.setType(Material.COMMAND);
			state.setCommand(c);
			state.update(true, true);
		}
	}
	
	/** @param cmd Unused */
	public static final boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
		String strArgs = "";
		if(args.length != 0) {
			strArgs = "";
			int x = 0;
			do {
				strArgs = strArgs.concat(args[x] + " ");
				x++;
			} while(x < args.length);
		}
		strArgs = strArgs.trim();
		Player user = null;
		if((sender instanceof Player)) {
			user = (Player) sender;
		}
		String userName = sender.getName();
		if(user != null) {
			userName = user.getDisplayName();
		}
		if(StringUtils.isEmpty(userName)) {
			userName = sender.getName();
		}
		boolean senderIsCmdBlock = sender instanceof BlockCommandSender;
		final World cmdWorld;
		if(user != null) {
			cmdWorld = user.getWorld();
		} else {
			if(senderIsCmdBlock) {
				cmdWorld = ((BlockCommandSender) sender).getBlock().getWorld();
			} else
				cmdWorld = null;
		}
		if((cmdWorld != null) && ((command.equalsIgnoreCase("serializeBlock")) || (command.equalsIgnoreCase("sb")) || (command.equalsIgnoreCase("deserializeBlock")) || (command.equalsIgnoreCase("db")))) {
			if((command.equalsIgnoreCase("serializeBlock")) || (command.equalsIgnoreCase("sb"))) {
				if(user != null) {
					Set<Material> transparent = new HashSet<>();
					transparent.add(Material.AIR);
					Block targetBlock = user.getTargetBlock(transparent, 100);
					if(targetBlock != null) {
						if((args.length == 1) && ((args[0].equals("face")) || (args[0].equals("f")))) {
							try {
								List<Block> lastBlocks = user.getLastTwoTargetBlocks(transparent, 100);
								BlockFace face = lastBlocks.get(1).getFace(lastBlocks.get(0));
								Block check = targetBlock.getRelative(face, 1);
								targetBlock = (check != null) && (check.getType() != Material.AIR) ? check : targetBlock;
							} catch(Throwable localThrowable1) {
							}
						}
						user.sendMessage(BlockAPI.serializeBlock(targetBlock));
					} else {
						Main.sendMessage(user, Main.pluginName + "&ePlease target a block and try again.");
					}
				} else {
					sender.sendMessage(Main.pluginName + "&eAt the moment, this can only be used by players.");
				}
			} else {
				if(strArgs.trim().isEmpty()) {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: &f/db [x=0,y=0,z=0]#m@STONE:d@0;");
					return true;
				}
				BlockAPI.DeserializedBlockResult result = BlockAPI.deseriaizeBlock(strArgs, cmdWorld);
				if(result.getBlock() != null) {
					Main.sendMessage(sender, Main.pluginName + "&aBlock placed.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&c" + result.getFailureCause());
					if(senderIsCmdBlock) {
						resetCommandBlock(sender);
					}
				}
			}
			return true;
		}
		
		PlayerStatus status = PlayerStatus.getPlayerStatus(user);
		if((user != null) && (status.isAfk) && (!command.equalsIgnoreCase("vanish")) && (!status.isVanishModeOn)) {
			status.toggleAfkState();
		} else if((user == null) && (sender == Main.console) && (isConsoleAfk)) {
			isConsoleAfk = false;
			Main.broadcast("&7* " + Main.consoleSayFormat + "&f: &7is no" + (isConsoleAfk ? "w afk." : " longer afk."));
		}
		ItemStack mjölnir;
		ItemMeta meta;
		if(command.equalsIgnoreCase("give")) {
			if((args.length == 2) && ((args[1].equalsIgnoreCase("Mjölnir")) || (args[1].equalsIgnoreCase("Mjolnir")))) {
				Player target = Main.getPlayer(args[0]);
				if(target != null) {
					if(Permissions.hasPerm(target, "supercmds.use.thor")) {
						mjölnir = new ItemStack(Material.IRON_PICKAXE);
						meta = Main.server.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
						meta.setDisplayName("Mjölnir");
						mjölnir.setItemMeta(meta);
						updateMjölnir(mjölnir);
						if(target.getInventory().firstEmpty() != -1) {
							target.getInventory().addItem(new ItemStack[] {mjölnir});
						} else {
							target.getWorld().dropItem(target.getLocation(), mjölnir);
						}
						Main.sendMessage(sender, "&f[&6Thor&f]: &bYond person anon hast Mjölnir!");
					} else {
						Main.sendMessage(sender, "&f[&6Thor&f]: &bYond person is not worthy!");
					}
				} else {
					Main.sendMessage(sender, "&f[&6Thor&f]: &bYond playeth'r doest not existeth!");
				}
				return true;
			}
			Main.server.dispatchCommand(sender, "minecraft:" + command + " " + strArgs);
			return true;
		}
		if(command.equalsIgnoreCase("heal")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.heal")) {
				Main.sendMessage(user, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				if(!Permissions.hasPerm(sender, "supercmds.use.heal.others")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				Player target = Main.getPlayer(args[0]);
				if(target != null) {
					target.setHealth(target.getMaxHealth());
					target.setFoodLevel(100);
					target.setSaturation(100.0F);
					Main.sendMessage(target, Main.pluginName + "&6You have been healed by &f" + userName + "&r&a.");
					Main.sendMessage(sender, Main.pluginName + "&aYou healed &f" + target.getDisplayName() + "&r&a.");
				} else {
					Main.sendMessage(sender, getNoPlayerMsg(args[0]));
				}
				return true;
			}
			if(user != null) {
				if(args.length == 0) {
					user.setHealth(user.getMaxHealth());
					user.setFoodLevel(100);
					user.setSaturation(100.0F);
					Main.sendMessage(user, Main.pluginName + "&6You have been healed.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " [targetName]&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\"");
			}
			return true;
		}
		if(command.equalsIgnoreCase("last")) {
			if(Permissions.hasPerm(sender, "supercmds.use.last")) {
				if(args.length >= 1) {
					String type = args[0];
					if((type.equalsIgnoreCase("gamemode")) || (type.equalsIgnoreCase("gm")) || (type.equalsIgnoreCase("world")) || (type.equalsIgnoreCase("w")) || (type.equalsIgnoreCase("teleportlocation")) || (type.equalsIgnoreCase("teleportloc")) || (type.equalsIgnoreCase("tpl"))) {
						UUID targetUUID = args.length == 1 ? null : Main.uuidMasterList.getUUIDFromPlayerName(args[1]);
						PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(targetUUID);
						if(targetStatus != null) {
							String dispName = PlayerChat.getPlayerChat(targetUUID).getDisplayName();
							if((type.equalsIgnoreCase("gamemode")) || (type.equalsIgnoreCase("gm"))) {
								Main.sendMessage(user, Main.pluginName + "&a\"&f" + dispName + "&r&a\"'s last gamemode when logging out was: \"&2" + targetStatus.lastGameMode.name() + "&a\".");
							} else if((type.equalsIgnoreCase("world")) || (type.equalsIgnoreCase("w"))) {
								World world = Main.getWorld(targetStatus.lastWorld);
								Main.sendMessage(user, Main.pluginName + "&aThe last world \"&f" + dispName + "&r&a\" was in when logging out was: \"" + (world != null ? "&2" + world.getName() : "&4<null/unavailable>") + "&a\".");
							} else if((type.equalsIgnoreCase("teleportlocation")) || (type.equalsIgnoreCase("teleportloc")) || (type.equalsIgnoreCase("tpl"))) {
								World world = targetStatus.lastTeleportLoc.getWorld();
								Vector3 coords = Vector3.getFromLocation(targetStatus.lastTeleportLoc);
								Main.sendMessage(user, Main.pluginName + "&a\"&f" + dispName + "&r&a\"'s last teleport location was at \"&f" + coords.toString() + "&a\" in world \"" + (world != null ? "&2" + world.getName() : "&4<null/unavailable>") + "\".");
							}
							return true;
						}
						if(user != null) {
							if(args.length == 2) {
								Main.sendMessage(sender, Main.getPlayerNotOnlineMsg(args[1]));
								return true;
							}
							if((type.equalsIgnoreCase("gamemode")) || (type.equalsIgnoreCase("gm"))) {
								Main.sendMessage(user, Main.pluginName + "&aYour last gamemode when logging out was: \"&2" + status.lastGameMode.name() + "&a\".");
							} else if((type.equalsIgnoreCase("world")) || (type.equalsIgnoreCase("w"))) {
								World world = Main.getWorld(status.lastWorld);
								Main.sendMessage(user, Main.pluginName + "&aThe last world you were in when logging out was: \"" + (world != null ? "&2" + world.getName() : "&4<null/unavailable>") + "&a\".");
							} else if((type.equalsIgnoreCase("teleportlocation")) || (type.equalsIgnoreCase("teleportloc")) || (type.equalsIgnoreCase("tpl"))) {
								World world = status.lastTeleportLoc.getWorld();
								Vector3 coords = Vector3.getFromLocation(status.lastTeleportLoc);
								Main.sendMessage(user, Main.pluginName + "&aYour last teleport location was at \"&f" + coords.toString() + "&a\" in world \"" + (world != null ? "&2" + world.getName() : "&4<null/unavailable>") + "\".");
							}
							return true;
						}
					}
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f {gamemode|world|teleportloc} " + (user != null ? "[target]" : "{target}") + "&e\".");
			} else {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			}
			return true;
		}
		if(command.equalsIgnoreCase("manageshop")) {
			if(Permissions.hasPerm(sender, "supercmds.use.manageshop")) {
				if((args.length >= 5) || (args.length == 2)) {
					if(args.length == 2) {
						if(args[0].equalsIgnoreCase("remove")) {
							ItemLibrary itemToRemove = ItemLibrary.getItemFromName(args[1]);
							if(itemToRemove == null) {
								Main.sendMessage(sender, Main.pluginName + "&eThe shop item \"&f" + args[1] + "&r&e\" does not exist.");
								return true;
							}
							itemToRemove.delete();
							itemToRemove.dispose();
							itemToRemove = null;
							Main.sendMessage(sender, Main.pluginName + "&aThe shop item \"&f" + args[1] + "&r&e\" was sucessfully deleted.");
							return true;
						}
						
					} else if(args[0].equalsIgnoreCase("add")) {
						ItemLibrary itemToAdd = ItemLibrary.getItemFromName(args[1]);
						if(itemToAdd != null) {
							Main.sendMessage(sender, Main.pluginName + "&eThe shop item \"&f" + args[1] + "&r&e\" already exists!&z&aUse \"&f/" + command + "&r&f edit " + args[1] + "&r&f [buyPrice] [items...]\"&a to edit it, or type a different name and try again.");
							return true;
						}
						int buyPrice = -1;
						if(StringUtil.isStrInt(args[2])) {
							buyPrice = Integer.valueOf(args[2]).intValue();
							String itemsOrPermissions = args[3];
							if((itemsOrPermissions.equalsIgnoreCase("item")) || (itemsOrPermissions.equalsIgnoreCase("items"))) {
								String[] itemArgs = args.length == 5 ? new String[] {args[4]} : (String[]) Arrays.copyOfRange(args, 4, args.length);
								ItemStackFromString.ItemResult result = ItemStackFromString.getItemStackFromString(itemArgs);
								if(result.item != null) {
									itemToAdd = new ItemLibrary(args[1], buyPrice, new ItemStack[] {result.item});
									if(!itemToAdd.isDisposed()) {
										itemToAdd.saveToFile();
										Main.sendMessage(sender, Main.pluginName + "&aSuccessfully created shop item \"&f" + itemToAdd.name + "&r&e\" with " + itemToAdd.items.length + (itemToAdd.items.length == 1 ? " item" : "items") + "!");
									} else {
										Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when attempting to create shop item \"&f" + itemToAdd.name + "&r&e\"!&z&aPlease try again later.");
									}
								} else if((result.statusMessage != null) && (!result.statusMessage.isEmpty())) {
									Main.sendMessage(sender, Main.pluginName + result.statusMessage);
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eInvalid item arguments: \"&f" + StringUtil.stringArrayToString(itemArgs, ' ') + "&r&e\"!&z&aPlease try again later.");
								}
								
								return true;
							}
							if((itemsOrPermissions.equalsIgnoreCase("permission")) || (itemsOrPermissions.equalsIgnoreCase("permissions"))) {
								String[] permArgs = args.length == 5 ? new String[] {args[4]} : (String[]) Arrays.copyOfRange(args, 4, args.length);
								itemToAdd = new ItemLibrary(args[1], buyPrice, (ItemStack) null);
								for(int i = 0; i < permArgs.length; i++) {
									String permission = permArgs[i];
									if((permission != null) && (!permission.isEmpty()) && (!itemToAdd.permissions.contains(permission))) {
										itemToAdd.permissions.add(permission);
									}
								}
								if(!itemToAdd.isDisposed()) {
									itemToAdd.saveToFile();
									Main.sendMessage(sender, Main.pluginName + "&aSuccessfully created shop item \"&f" + itemToAdd.name + "&r&a with &6" + itemToAdd.permissions.size() + "&r&a permissions\"!");
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when attempting to create shop item \"&f" + itemToAdd.name + "&r&e\"!&z&aPlease try again later.");
								}
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid item/permission flag: \"&f" + itemsOrPermissions + "&r&e\"!");
						}
						
						Main.sendMessage(sender, Main.pluginName + "&eInvalid buy price: \"&f" + args[1] + "&r&e\"!");
					} else if(args[0].equalsIgnoreCase("edit")) {
						ItemLibrary itemToEdit = ItemLibrary.getItemFromName(args[1]);
						if(itemToEdit == null) {
							Main.sendMessage(sender, Main.pluginName + "&eThe shop item \"&f" + args[1] + "&r&e\" does not exist.");
							return true;
						}
						int buyPrice = -1;
						if(StringUtil.isStrInt(args[2])) {
							buyPrice = Integer.valueOf(args[2]).intValue();
							String itemsOrPermissions = args[3];
							if((itemsOrPermissions.equalsIgnoreCase("item")) || (itemsOrPermissions.equalsIgnoreCase("items"))) {
								if((args.length == 5) && ((args[4].equalsIgnoreCase("reset")) || (args[4].equalsIgnoreCase("clear")))) {
									itemToEdit.items = new ItemStack[0];
									if(!itemToEdit.isDisposed()) {
										itemToEdit.saveToFile();
										Main.sendMessage(sender, Main.pluginName + "&aSuccessfully reset shop item \"&f" + itemToEdit.name + "&r&e\"'s items!");
									} else {
										Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when attempting to remove shop item \"&f" + itemToEdit.name + "&r&e\"'s items!(Has it been disposed?)&z&aPlease try again later, or create a new item.");
									}
									return true;
								}
								String[] itemArgs = args.length == 5 ? new String[] {args[4]} : (String[]) Arrays.copyOfRange(args, 4, args.length);
								ItemStackFromString.ItemResult result = ItemStackFromString.getItemStackFromString(itemArgs);
								if(result.item != null) {
									itemToEdit.price = buyPrice;
									ArrayList<ItemStack> items = new ArrayList<>(Arrays.asList(itemToEdit.items));
									items.add(result.item);
									itemToEdit.items = (items.toArray(new ItemStack[items.size()]));
									if(!itemToEdit.isDisposed()) {
										itemToEdit.saveToFile();
										Main.sendMessage(sender, Main.pluginName + "&aSuccessfully created shop item \"&f" + itemToEdit.name + "&r&e\" with " + itemToEdit.items.length + (itemToEdit.items.length == 1 ? " item" : "items") + "!");
									} else {
										Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when attempting to edit shop item \"&f" + itemToEdit.name + "&r&e\"!(Has it been disposed?)&z&aPlease try again later, or create a new item.");
									}
								} else if((result.statusMessage != null) && (!result.statusMessage.isEmpty())) {
									Main.sendMessage(sender, Main.pluginName + result.statusMessage);
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eInvalid item arguments: \"&f" + StringUtil.stringArrayToString(itemArgs, ' ') + "&r&e\"!&z&aPlease try again later.");
								}
								
								return true;
							}
							if((itemsOrPermissions.equalsIgnoreCase("permission")) || (itemsOrPermissions.equalsIgnoreCase("permissions"))) {
								String[] permArgs = args.length == 5 ? new String[] {args[4]} : (String[]) Arrays.copyOfRange(args, 4, args.length);
								itemToEdit.permissions.clear();
								for(int i = 0; i < permArgs.length; i++) {
									String permission = permArgs[i];
									if((permission != null) && (!permission.isEmpty()) && (!itemToEdit.permissions.contains(permission))) {
										itemToEdit.permissions.add(permission);
									}
								}
								if(!itemToEdit.isDisposed()) {
									itemToEdit.saveToFile();
									Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set &6" + itemToEdit.permissions.size() + "&r&a permissions on shop item \"&f" + itemToEdit.name + "&r&a\"!");
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when attempting to create shop item \"&f" + itemToEdit.name + "&r&e\"!(Has it been disposed?)&z&aPlease try again later, or create a new item.");
								}
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid item/permission flag: \"&f" + itemsOrPermissions + "&r&e\"!");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eInvalid buy price: \"&f" + args[1] + "&r&e\"!");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe shop item \"&f" + args[1] + "&r&e\" does not exist.");
						return true;
					}
				}
				
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f {add|edit|remove} {shopItemName} [buyPrice] {items|permissions} [items.../reset/clear|permissions...]&e\".");
			} else {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			}
			return true;
		}
		if((command.equalsIgnoreCase("back")) || (command.equalsIgnoreCase("return"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.back")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 0) {
					Location loc = status.lastTeleportLoc;
					Location lastLoc = user.getLocation();
					if(loc == null) {
						Main.sendMessage(user, Main.pluginName + "&eYou haven't teleported anywhere using this plugin yet; setting previous teleport location to your current position.");
						status.lastTeleportLoc = lastLoc;
						return true;
					}
					if(user.teleport(loc)) {
						status.lastTeleportLoc = lastLoc;
						Main.sendMessage(user, Main.pluginName + "&2Teleporting you to your last teleport location.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when teleporting you back; is the world loaded?");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("broadcast")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.broadcast")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length >= 1) {
				Main.broadcast(Main.broadcastPrefix + "&r&f: " + strArgs);
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {args...}&e\"");
			}
			return true;
		}
		if((command.equalsIgnoreCase("delhome")) || (command.equalsIgnoreCase("deletehome"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.home")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length >= 1) {
					String homeStr = Main.stripColorCodes(Main.formatColorCodes(strArgs));
					if(status.hasHome(homeStr)) {
						Home home = status.getHome(homeStr);
						status.homes.remove(home);
						status.lastDeletedHome = home;
						Main.sendMessage(user, Main.pluginName + "&aSuccessfully deleted &6/home " + home.name + "&r&a.&z&eIf you did not mean to do this, you can restore that home with &f/delhome undo&e.&z&eNote however that if you set or delete another home without restoring this one, it will be gone permanently.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eYou do not have a home by the name of \"&f" + homeStr + "&r&e\".");
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {home name...}&e\" or&z&e\"&f/" + command + " undo&e\" to restore an accidentally deleted home.&z&aNote that you cannot delete your default &6/home&a, but you can move it with &f/sethome&a.");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("fly")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.fly")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				if((user != null) && (status.isFlyModeOn)) {
					PlayerStatus.updatePlayerStateStates();
				}
				
				return true;
			}
			if((user != null) && (args.length == 0)) {
				status.isFlyModeOn = (!status.isFlyModeOn);
				status.wasFlyModeOnBeforeSwap = status.isFlyModeOn;
				if(!status.isFlyModeOn) {
					if((user.getGameMode() != GameMode.SPECTATOR) && (user.getGameMode() != GameMode.CREATIVE)) {
						user.setFlying(false);
						user.setAllowFlight(false);
					} else {
						status.isFlyModeOn = true;
						Main.sendMessage(user, Main.pluginName + "&eGame mode " + Main.capitalizeFirstLetter(user.getGameMode().name().toLowerCase()) + " doesn't allow for turning off fly mode.");
					}
				} else {
					user.setAllowFlight(true);
				}
				Main.sendMessage(user, Main.pluginName + "&aSet fly mode to " + (status.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
				PlayerStatus.updatePlayerStateStates();
				return true;
			}
			if(args.length == 1) {
				if(!Permissions.hasPerm(sender, "supercmds.use.fly.others")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				Player target = Main.getPlayer(args[0]);
				if(target == null) {
					Main.sendMessage(user, getNoPlayerMsg(args[0]));
					return true;
				}
				PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
				targetStatus.isFlyModeOn = (!targetStatus.isFlyModeOn);
				targetStatus.wasFlyModeOnBeforeSwap = targetStatus.isFlyModeOn;
				if(!targetStatus.isFlyModeOn) {
					if((target.getGameMode() != GameMode.SPECTATOR) && (target.getGameMode() != GameMode.CREATIVE)) {
						target.setFlying(false);
						target.setAllowFlight(false);
					} else {
						status.isFlyModeOn = true;
						Main.sendMessage(user, Main.pluginName + "&eThe target's game mode(" + Main.capitalizeFirstLetter(target.getGameMode().name().toLowerCase()) + ") doesn't allow for turning off fly mode.");
					}
				} else {
					target.setAllowFlight(true);
				}
				Main.sendMessage(sender, Main.pluginName + "&aSet &f" + target.getDisplayName() + "&r&a's fly mode to " + (targetStatus.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
				if((!Permissions.hasPerm(target, "supercmds.use.fly")) && (targetStatus.isFlyModeOn)) {
					Main.sendMessage(sender, Main.pluginName + "&eNote however that since &f" + target.getDisplayName() + "&r&e does not have permission to use the /fly command, they cannot turn fly back off, so it will stay on until it is turned back off by someone.");
				}
				Main.sendMessage(target, Main.pluginName + "&aYour fly mode has been set to " + (targetStatus.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
				PlayerStatus.updatePlayerStateStates();
			} else if(user != null) {
				Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" or&z&e\"&f/" + command + " [targetName]&e\"");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\"");
			}
			
			return true;
		}
		if((command.equalsIgnoreCase("gamemode")) || (command.equalsIgnoreCase("gm")) || (command.equalsIgnoreCase("gammedoe")) || (command.equalsIgnoreCase("gammeode"))) {
			if(args.length == 1) {
				if(user != null) {
					setGameModeForPlayer(sender, user, userName, args[0]);
				} else {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {gamemode} {targetName}&e\"");
				}
			} else if(args.length == 2) {
				Player target = Main.getPlayer(args[0]);
				setGameModeForPlayer(sender, target, args[0], args[1]);
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {gamemode}" + (user == null ? " {targetName}" : " [targetName]") + "&e\"");
			}
			return true;
		}
		if(command.equalsIgnoreCase("gms")) {
			if(user != null) {
				if(args.length == 0) {
					setGameModeForPlayer(user, user, userName, "survival");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		}
		if(command.equalsIgnoreCase("gmc")) {
			if(user != null) {
				if(args.length == 0) {
					setGameModeForPlayer(user, user, userName, "creative");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		}
		if(command.equalsIgnoreCase("gma")) {
			if(user != null) {
				if(args.length == 0) {
					setGameModeForPlayer(user, user, userName, "adventure");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		}
		if(command.equalsIgnoreCase("gmspec")) {
			if(user != null) {
				if(args.length == 0) {
					setGameModeForPlayer(user, user, userName, "spectator");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		}
		if((command.equalsIgnoreCase("god")) || (command.equalsIgnoreCase("godmode"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.god")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if((user != null) && (args.length == 0)) {
				if((user.getGameMode() == GameMode.CREATIVE) || (user.getGameMode() == GameMode.SPECTATOR)) {
					Main.sendMessage(user, Main.pluginName + "&eGame mode " + Main.capitalizeFirstLetter(user.getGameMode().name().toLowerCase()) + " doesn't allow for turning off god mode.");
					if(!status.isGodModeOn) {
						status.isGodModeOn = true;
						Main.sendMessage(user, Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
					}
					return true;
				}
				status.isGodModeOn = (!status.isGodModeOn);
				status.wasGodModeOnBeforeSwap = status.isGodModeOn;
				Main.sendMessage(user, Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				if((status.isGodModeOn) && (user.getFoodLevel() == 0)) {
					user.setFoodLevel(user.getFoodLevel() + 1);
				}
				return true;
			}
			if(args.length == 1) {
				if(!Permissions.hasPerm(sender, "supercmds.use.god.others")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				Player target = Main.getPlayer(args[0]);
				if(target == null) {
					Main.sendMessage(user, getNoPlayerMsg(args[0]));
					return true;
				}
				PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
				targetStatus.isGodModeOn = (!targetStatus.isGodModeOn);
				targetStatus.wasGodModeOnBeforeSwap = targetStatus.isGodModeOn;
				if((status.isPlayerOnline()) && (status.isGodModeOn) && (status.getPlayer().getFoodLevel() == 0)) {
					status.getPlayer().setFoodLevel(status.getPlayer().getFoodLevel() + 1);
				}
				
				Main.sendMessage(sender, Main.pluginName + "&aSet &f" + target.getDisplayName() + "&r&a's god mode to " + (targetStatus.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				Main.sendMessage(user, Main.pluginName + "&aYour god mode has been set to " + (targetStatus.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
			} else if(user != null) {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" or&z&e\"&f/" + command + " [targetName]&e\"");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\"");
			}
			
			return true;
		}
		if((command.equalsIgnoreCase("home")) || (command.equalsIgnoreCase("homes"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.home")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if((user == null) && (args.length >= 1) && (command.equalsIgnoreCase("home"))) {
				if(args.length == 1) {
					Player target = Main.getPlayer(args[0]);
					if(target != null) {
						PlayerStatus s = PlayerStatus.getPlayerStatus(target);
						if(s.hasHome()) {
							Location loc = target.getLocation();
							if(target.teleport(s.homeLocation)) {
								s.lastTeleportLoc = loc;
								Main.sendMessage(target, Main.pluginName + "&aTaking you &6/home&a.");
							} else {
								Main.sendMessage(target, Main.pluginName + "&eUnable to take you &6/home&e; is the world loaded?");
							}
							return true;
						}
					}
				} else if(args.length >= 2) {
					Player target = Main.getPlayer(args[0]);
					if(target != null) {
						PlayerStatus s = PlayerStatus.getPlayerStatus(target);
						Home home = s.getHome(Main.getElementsFromStringArrayAtIndexAsString(args, 1));
						if(home != null) {
							Location loc = target.getLocation();
							if((home.location != null) && (target.teleport(home.location))) {
								s.lastTeleportLoc = loc;
								return true;
							}
						}
					}
				}
				
				return true;
			}
			if(user != null) {
				if(((command.equalsIgnoreCase("homes")) && (args.length == 0)) || ((command.equalsIgnoreCase("home")) && (args.length == 1) && (args[0].equalsIgnoreCase("list")))) {
					Main.sendMessage(user, Main.pluginName + "&aListing all of your homes...");
					if(status.hasHome()) {
						Main.sendMessage(user, "&3(Default)&f - \"&6/home&f\"");
					}
					int i = 2;
					for(Home home : status.homes) {
						Main.sendMessage(user, "&f[&3" + i + "&f]: &6/home " + home.name);
						i++;
					}
					return true;
				}
				if(command.equalsIgnoreCase("home")) {
					if(args.length == 0) {
						if(!status.hasHome()) {
							Main.sendMessage(user, Main.pluginName + "&eYou have not set your first &6/home&e yet!&z&aSet it with &f/sethome&a.");
							return true;
						}
						Location loc = user.getLocation();
						if(user.teleport(status.homeLocation)) {
							status.lastTeleportLoc = loc;
							Main.sendMessage(user, Main.pluginName + "&aTaking you &6/home&a.");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when teleporting you to your first &6/home&e; is the world loaded?");
						return true;
					}
					if(args.length >= 1) {
						String homeStr = Main.stripColorCodes(Main.formatColorCodes(strArgs));
						if(status.hasHome(homeStr)) {
							Home home = status.getHome(homeStr);
							Location loc = user.getLocation();
							if(home.location != null) {
								if(user.teleport(home.location)) {
									status.lastTeleportLoc = loc;
									Main.sendMessage(user, Main.pluginName + "&aTaking you to &6/home " + home.name + "&r&a.");
									return true;
								}
								Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when teleporting you to &6/home " + home.name + "&r&e; is the world that it is in loaded?");
								return true;
							}
							Main.sendMessage(user, Main.pluginName + "&eYour &6/home " + home.name + "&r&e's location is &cnull&e for some reason!&z&aYou will need to set it again with &f/sethome " + home.name + "&r&a.");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eYou do not have a home by the name of \"&f" + homeStr + "&r&e\".");
						if(status.canSetAnotherHome()) {
							Main.sendMessage(user, Main.pluginName + "&aSet it with &f/sethome " + homeStr + "&r&a!");
						}
						return true;
					}
				}
				Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/home&e\" or&z&e\"&f/home {home name...}&e\" or&z&e\"&f/home list&e\"");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if((command.equalsIgnoreCase("list")) || (command.equalsIgnoreCase("playerlist")) || (command.equalsIgnoreCase("listplayers"))) {
			if(args.length == 0) {
				final ArrayList<Player> playersToShow;
				if(user != null) {
					if(Permissions.hasPerm(user, "supercmds.vanish.exempt")) {
						playersToShow = new ArrayList<>(Main.server.getOnlinePlayers());
					} else {
						playersToShow = new ArrayList<>();
						for(Player player : Main.server.getOnlinePlayers()) {
							PlayerStatus curStatus = PlayerStatus.getPlayerStatus(player);
							if(!curStatus.isVanishModeOn) {
								playersToShow.add(player);
							}
						}
					}
				} else {
					playersToShow = new ArrayList<>(Main.server.getOnlinePlayers());
				}
				Main.sendMessage(sender, Main.pluginName + "&aThere are &f" + playersToShow.size() + "&a/&f" + Main.server.getMaxPlayers() + "&a players online." + (playersToShow.size() > 0 ? "&z&6Listing online players:" : ""));
				for(Player player : playersToShow) {
					Main.sendMessage(sender, "&f" + player.getDisplayName());
				}
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("purchase")) {
			if(args.length == 0) {
				Main.sendMessage(sender, Main.pluginName + "&3Listing all purchaseable shop items:");
				int i = 1;
				for(ItemLibrary item : ItemLibrary.getShopItems()) {
					Main.sendMessage(sender, "&3[" + i + "]===" + item.toString());
					i++;
				}
				if(user != null) {
					Main.sendMessage(sender, Main.pluginName + "&aTo buy a shop item, type \"&f/" + command + "&r&a\" and then the name of the shop item you wish to buy.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eNote that only players can use \"&f/" + command + "&r&e\".");
				}
				return true;
			}
			if(user != null) {
				if(args.length == 1) {
					ItemLibrary itemToPurchase = ItemLibrary.getItemFromName(args[0]);
					if(itemToPurchase == null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe shop item \"&f" + args[0] + "&r&e\" does not exist. Check your spelling and try again.");
						return true;
					}
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
					if(eco.credits >= itemToPurchase.price) {
						eco.credits -= itemToPurchase.price;
						eco.saveToFile();
						HashMap<Integer, ItemStack> leftOvers = user.getInventory().addItem(itemToPurchase.items);
						boolean droppedItemsAtFeet = false;
						if((leftOvers != null) && (leftOvers.size() > 0)) {
							for(ItemStack leftOver : leftOvers.values()) {
								user.getWorld().dropItem(user.getLocation(), leftOver);
							}
							droppedItemsAtFeet = true;
						}
						for(String permission : itemToPurchase.permissions) {
							if(!Permissions.hasPerm(user, permission)) {
								PlayerPermissions.setPermission(user, permission, true);
							}
						}
						if(droppedItemsAtFeet) {
							Main.sendMessage(sender, Main.pluginName + "&eYour inventory didn't have enough space, so some of the items were dropped at your feet.");
						}
						Main.sendMessage(sender, Main.pluginName + "&aPurchase successful! &f" + Main.capitalizeFirstLetter(Main.creditTerm) + "&r&a remaining: &6" + Main.decimal.format(eco.credits));
					} else {
						Main.sendMessage(sender, Main.pluginName + "&cInsufficient funds!&z&eThat shop item costs &6" + Main.decimal.format(itemToPurchase.price) + "&r&e; but you only have &6" + Main.decimal.format(eco.credits) + "&r&f " + Main.creditTerm + "&r&e.");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f [shopItemName]&e\".");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		}
		if(command.equalsIgnoreCase("set") || command.equalsIgnoreCase("replace")) {
			if(Permissions.hasPerm(sender, "supercmds.use." + command.toLowerCase())) {
				if((args.length == 1) && ((args[0].equalsIgnoreCase("cancel")) || (args[0].equalsIgnoreCase("c")))) {
					final BlockSetType typeToCancel = command.equalsIgnoreCase("set") ? BlockSetType.SET : BlockSetType.REPLACE;
					for(BlockSetData data : BlockSetData.blockSetQueue) {
						if(data.sender.getName().equals(sender.getName()) && data.type == typeToCancel) {
							Main.sendMessage(sender, Main.pluginName + "&aCancelling operation...");
							data.cancel();
							break;
						}
					}
					Main.sendMessage(sender, Main.pluginName + "&eNo operations found to cancel.");
					return true;
				}
				if((args.length == 1) && ((args[0].equalsIgnoreCase("cancelall")) || (args[0].equalsIgnoreCase("ca")))) {
					boolean cancelledAnOperation = false;
					final BlockSetType typeToCancel = command.equalsIgnoreCase("set") ? BlockSetType.SET : BlockSetType.REPLACE;
					for(BlockSetData data : BlockSetData.blockSetQueue) {
						if(data.sender.getName().equals(sender.getName()) && data.type == typeToCancel) {
							Main.sendMessage(sender, Main.pluginName + "&aCancelling operation...");
							data.cancel();
							cancelledAnOperation = true;
						}
					}
					if(!cancelledAnOperation) {
						Main.sendMessage(sender, Main.pluginName + "&eNo operations found to cancel.");
					}
					return true;
				}
				if(args.length >= 7) {
					int x1 = 0;
					int y1 = 0;
					int z1 = 0;
					int x2 = 0;
					int y2 = 0;
					int z2 = 0;
					if(StringUtil.isStrInt(args[0])) {
						x1 = Integer.valueOf(args[0]).intValue();
					} else if(args[0].startsWith("~") && (args[0].length() > 1 ? StringUtil.isStrInt(args[0].substring(1)) : true) && user != null) {
						x1 = user.getLocation().getBlockX() + (args[0].length() > 1 ? Integer.valueOf(args[0].substring(1)).intValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'X1' arg provided \"&f" + args[0] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrInt(args[1])) {
						y1 = Integer.valueOf(args[1]).intValue();
					} else if(args[1].startsWith("~") && (args[1].length() > 1 ? StringUtil.isStrInt(args[1].substring(1)) : true) && user != null) {
						y1 = user.getLocation().getBlockY() + (args[1].length() > 1 ? Integer.valueOf(args[1].substring(1)).intValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Y1' arg provided  \"&f" + args[1] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrInt(args[2])) {
						z1 = Integer.valueOf(args[2]).intValue();
					} else if(args[2].startsWith("~") && (args[2].length() > 1 ? StringUtil.isStrInt(args[2].substring(1)) : true) && user != null) {
						z1 = user.getLocation().getBlockZ() + (args[2].length() > 1 ? Integer.valueOf(args[2].substring(1)).intValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Z1' arg provided  \"&f" + args[2] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrInt(args[3])) {
						x2 = Integer.valueOf(args[3]).intValue();
					} else if(args[3].startsWith("~") && (args[3].length() > 1 ? StringUtil.isStrInt(args[3].substring(1)) : true) && user != null) {
						x2 = user.getLocation().getBlockX() + (args[3].length() > 1 ? Integer.valueOf(args[3].substring(1)).intValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'X2' arg provided  \"&f" + args[3] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrInt(args[4])) {
						y2 = Integer.valueOf(args[4]).intValue();
					} else if(args[4].startsWith("~") && (args[4].length() > 1 ? StringUtil.isStrInt(args[4].substring(1)) : true) && user != null) {
						y2 = user.getLocation().getBlockY() + (args[4].length() > 1 ? Integer.valueOf(args[4].substring(1)).intValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Y2' arg provided  \"&f" + args[4] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrInt(args[5])) {
						z2 = Integer.valueOf(args[5]).intValue();
					} else if(args[5].startsWith("~") && (args[5].length() > 1 ? StringUtil.isStrInt(args[5].substring(1)) : true) && user != null) {
						z2 = user.getLocation().getBlockZ() + (args[5].length() > 1 ? Integer.valueOf(args[5].substring(1)).intValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Z2' arg provided  \"&f" + args[5] + "&r&e\" is not an integer value.");
						return true;
					}
					boolean specifiedWorldInCmd = false;
					World world = Main.getWorld(args[(args.length - 1)]);
					specifiedWorldInCmd = world != null;
					ArrayList<ItemStack> stacks = getItemStacksFromString(Main.stringArrayToString(args, ' ', 6, args.length - (specifiedWorldInCmd ? 1 : 0)));
					if(stacks.size() == 0) {
						stacks = getItemStacksFromString(Main.stringArrayToString(args, ' ', 6, args.length));
					}
					if((world == null) && (user == null)) {
						Main.sendMessage(sender, Main.pluginName + "&eThe world \"&f" + args[(args.length - 1)] + "&r&e\" does not exist.");
						return true;
					}
					if((world == null) && (user != null)) {
						world = user.getWorld();
					} else if(world == null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe world \"&f" + args[(args.length - 1)] + "&r&e\" does not exist.");
						return true;
					}
					ItemStack stack = stacks.size() == 0 ? null : stacks.get(0);
					if(stack == null) {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid block data given: \"&f" + Main.stringArrayToString(args, ' ', 6, args.length - (specifiedWorldInCmd ? 1 : 0)) + "&r&e\".");
						return true;
					}
					Material material = stack.getType();
					byte data = (byte) stack.getDurability();
					if(command.equalsIgnoreCase("set")) {
						BlockSetData.blockSetQueue.add(new BlockSetData(BlockSetType.SET, x1, y1, z1, x2, y2, z2, world, material, null, data, (byte) 0, sender));
						Main.sendMessage(sender, Main.pluginName + "&aBlock operation added to queue successfully.");
						return true;
					}
					ItemStack replace = stacks.size() >= 2 ? stacks.get(1) : null;
					if(replace == null) {
						Main.sendMessage(sender, Main.pluginName + "&ePlease specify a replacement block.&z&aExample: \"&f/" + command + "&r&f {x1} {y1} {z1} {x2} {y2} {z2} minecraft:air~;minecraft:stone 1 5&a\"");
						return true;
					}
					Material replaceMaterial = replace.getType();
					byte replaceData = (byte) replace.getDurability();
					BlockSetData.blockSetQueue.add(new BlockSetData(BlockSetType.REPLACE, x1, y1, z1, x2, y2, z2, world, material, replaceMaterial, data, replaceData, sender));
					Main.sendMessage(sender, Main.pluginName + "&aBlock operation added to queue successfully.");
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f {x1} {y1} {z1} {x2} {y2} {z2} {block data...} " + (user != null ? "[world]" : "{world}") + "&e\"&z&eor '&6/set cancel&e' to cancel an ongoing operation.");
			} else {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			}
			return true;
		}
		if(command.equalsIgnoreCase("sudo")) {
			if(Permissions.hasPerm(sender, "supercmds.use.sudo")) {
				if(args.length >= 2) {
					Player target = Main.getPlayer(args[0]);
					if(target != null) {
						String value = Main.getElementsFromStringArrayAtIndexAsString(args, 1);
						Main.server.dispatchCommand(target, value);
						Main.sendMessage(sender, Main.pluginName + "&aMade player \"&r&f" + target.getDisplayName() + "&r&a\" perform command: \"&r&f/" + value + "&r&a\"");
					} else {
						Main.sendMessage(sender, Main.getPlayerNotOnlineMsg(args[0]));
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {playername} {command} [args...]&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			}
			return true;
		}
		if(command.equalsIgnoreCase("setspawn")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.setspawn")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length != 0) {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
					return true;
				}
				Location loc = user.getLocation();
				World world = loc.getWorld();
				world.setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
				Main.setSpawnLocation(loc);
				Main.sendMessage(user, Main.pluginName + "&aSet the server &f/spawn&a to your current location.");
				Main.server.dispatchCommand(Main.console, "supercmds save");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("spawn")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.spawn")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length != 0) {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
					return true;
				}
				Main.getInstance();
				Location spawnLocation = Main.getSpawnLocation();
				if(spawnLocation == null) {
					Main.sendMessage(user, Main.pluginName + "&eThe server spawn has not yet been set(or it was lost due to an improper /reload).");
					return true;
				}
				Location lastLoc = user.getLocation();
				if(user.teleport(spawnLocation)) {
					Main.sendMessage(user, Main.pluginName + "&aTaking you to the server &f/spawn&a.");
					status.lastTeleportLoc = lastLoc;
				} else {
					Main.sendMessage(user, Main.pluginName + "&eSomething went wrong whilst teleporting you! Is the world loaded?");
				}
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("sethome")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.sethome")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 0) {
					Location loc = user.getLocation();
					status.homeLocation = loc;
					status.hasDefaultHome = true;
					Main.sendMessage(user, Main.pluginName + "&aSet your first &6/home&a to your current location.");
					return true;
				}
				if(args.length >= 1) {
					Home home = status.getHome(strArgs);
					if(home != null) {
						home.location = user.getLocation();
						Main.sendMessage(user, Main.pluginName + "&aSet &6/home " + home.name + "&a to your current location.");
						status.lastDeletedHome = null;
						return true;
					}
					if(status.canSetAnotherHome()) {
						String homeStr = Main.stripColorCodes(Main.formatColorCodes(strArgs));
						if((homeStr.equalsIgnoreCase("list")) || (homeStr.equalsIgnoreCase("undo"))) {
							Main.sendMessage(user, Main.pluginName + "&eYou can't set a home with that name because then you would be unable to use the &f" + (homeStr.equalsIgnoreCase("list") ? "/home" : "/delhome") + "&e command properly.");
							return true;
						}
						home = new Home(homeStr, user.getLocation());
						status.homes.add(home);
						Main.sendMessage(user, Main.pluginName + "&aCreated and set &6/home " + home.name + "&a to your current location.");
						status.lastDeletedHome = null;
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eYou do not have permission to have that many homes.");
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" or&z&e\"&f/" + command + " {home name...}&e\"");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("setmaxhealth")) {
			if(user != null) {
				if(Permissions.hasPerm(user, "supercmds.use.setmaxhealth")) {
					if(args.length == 1) {
						if(Main.checkIsNumber(args[0])) {
							double newHealth = Main.toNumber(args[0]);
							if(newHealth > 2000.0D) {
								Main.sendMessage(user, Main.pluginName + "&eIf your maximum health goes any higher than that, your client will lag out!");
								return true;
							}
							if(newHealth <= 0.0D) {
								Main.sendMessage(user, Main.pluginName + "&eYour maximum health can't go that low!");
								return true;
							}
							user.setMaxHealth(Main.toNumber(args[0]));
							status.updatePlayerHearts(false);
							Main.sendMessage(user, Main.pluginName + "&aYour maximum health has just been changed.");
							return true;
						}
					} else if(args.length == 2) {
						if((args[0].equalsIgnoreCase("-add")) || (args[0].equalsIgnoreCase("-a"))) {
							if(Main.checkIsNumber(args[1])) {
								double newHealth = user.getMaxHealth() + Main.toNumber(args[1]);
								if(newHealth <= 2000.0D) {
									user.setMaxHealth(newHealth);
									Main.sendMessage(user, Main.pluginName + "&aYour maximum health has just been changed.");
								} else {
									Main.sendMessage(user, Main.pluginName + "&eIf your maximum health goes any higher than that, your client will lag out!");
								}
								return true;
							}
						} else if((args[0].equalsIgnoreCase("-subtract")) || (args[0].equalsIgnoreCase("-sub")) || (args[0].equalsIgnoreCase("-s"))) {
							if(Main.checkIsNumber(args[1])) {
								double newHealth = user.getMaxHealth() - Main.toNumber(args[1]);
								if(newHealth > 0.0D) {
									user.setMaxHealth(newHealth);
									Main.sendMessage(user, Main.pluginName + "&aYour maximum health has just been changed.");
								} else {
									Main.sendMessage(user, Main.pluginName + "&eYour maximum health can't go that low!");
								}
								return true;
							}
						} else {
							Main.sendMessage(user, Main.pluginName + "&eInvalid flag: \"&f" + args[0] + "&r&e\"");
						}
					}
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " amount&e\" or&z&e\"&f/" + command + " -add amount&e\".");
				} else {
					Main.sendMessage(user, Main.pluginName + Main.noPerm);
				}
			} else {
				if(args.length == 2) {
					if(!Permissions.hasPerm(sender, "supercmds.use.setmaxhealth.others")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					Player target = Main.getPlayer(args[0]);
					if(target != null) {
						if(Main.checkIsNumber(args[1])) {
							double newHealth = Main.toNumber(args[1]);
							if(newHealth > 2000.0D) {
								Main.sendMessage(sender, Main.pluginName + "&eClients with a health of over 2000 can lock up and/or lag out.");
								return true;
							}
							if(newHealth <= 0.0D) {
								Main.sendMessage(sender, Main.pluginName + "&eHealth must be greater than zero.");
								return true;
							}
							target.setMaxHealth(newHealth);
							Main.sendMessage(target, Main.pluginName + "&aYour maximum health has just been changed.");
							Main.sendMessage(sender, Main.pluginName + "&aMax health changed for player \"&f" + target.getDisplayName() + "&r&a\"");
							return true;
						}
					} else {
						Main.sendMessage(sender, getNoPlayerMsg(args[0]));
					}
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {target} amount&e\" or&z&e\"&f/" + command + " {target} [-add|-sub] amount&e\".");
					return true;
				}
				if(args.length == 3) {
					Player target = Main.getPlayer(args[0]);
					if(target != null) {
						if((args[1].equalsIgnoreCase("-add")) || (args[1].equalsIgnoreCase("-a"))) {
							if(Main.checkIsNumber(args[2])) {
								target.setMaxHealth(target.getMaxHealth() + Main.toNumber(args[2]));
								Main.sendMessage(target, Main.pluginName + "&aYour maximum health has just been changed.");
								return true;
							}
						} else if((args[1].equalsIgnoreCase("-subtract")) || (args[1].equalsIgnoreCase("-sub")) || (args[1].equalsIgnoreCase("-s"))) {
							if(Main.checkIsNumber(args[2])) {
								target.setMaxHealth(target.getMaxHealth() - Main.toNumber(args[2]));
								Main.sendMessage(target, Main.pluginName + "&aYour maximum health has just been changed.");
								return true;
							}
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag: \"&f" + args[1] + "&r&e\"");
						}
					} else {
						Main.sendMessage(sender, getNoPlayerMsg(args[0]));
						return true;
					}
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {target} amount&e\" or&z&e\"&f/" + command + " {target} [-add|-sub] amount&e\".");
			}
			return true;
		}
		if(command.equalsIgnoreCase("thor")) {
			if(user != null) {
				if(Permissions.hasPerm(user, "supercmds.use.thor")) {
					if(args.length == 0) {
						if(user.getInventory().getItemInMainHand().getType() == Material.IRON_PICKAXE) {
							status.isThorModeOn = (!status.isThorModeOn);
							Main.sendMessage(user, Main.pluginName + (status.isThorModeOn ? "&aThor has blessed your hammer!" : "&eThor's blessing has worn off..."));
						} else {
							Main.sendMessage(user, Main.pluginName + "&eThor is not pleased with your current equipment! He demands something conductive!");
						}
					} else if(args.length == 1) {
						if((args[0].equalsIgnoreCase("on")) || (args[0].equalsIgnoreCase("activate")) || (args[0].equalsIgnoreCase("yes")) || (args[0].equalsIgnoreCase("true"))) {
							status.isThorModeOn = true;
							Main.sendMessage(user, Main.pluginName + (status.isThorModeOn ? "&aThor has blessed your hammer!" : "&eThor's blessing has worn off..."));
						} else if((args[0].equalsIgnoreCase("off")) || (args[0].equalsIgnoreCase("deactivate")) || (args[0].equalsIgnoreCase("no")) || (args[0].equalsIgnoreCase("false"))) {
							status.isThorModeOn = false;
							Main.sendMessage(user, Main.pluginName + (status.isThorModeOn ? "&aThor has blessed your hammer!" : "&eThor's blessing has worn off..."));
						} else {
							Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [on|off]&e\"");
						}
					} else {
						Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [on|off]&e\"");
					}
				} else {
					Main.sendMessage(user, Main.pluginName + "&cThou art not worthy.");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		}
		if(command.equalsIgnoreCase("ticket") || command.equalsIgnoreCase("issue")) {// XXX /ticket create {report.....} or /ticket view [#] or /ticket list {player} | /ticket close {#|player}
			if(!Permissions.hasPerm(sender, "supercmds.use.ticket")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length >= 1) {
				final TicketData tickets = TicketData.getInstance();
				if(args[0].equalsIgnoreCase("create")) {
					if(!Permissions.hasPerm(sender, "supercmds.use.ticket.create")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					if(args.length >= 2) {
						String report = StringUtil.stringArrayToString(args, ' ', 1);
						if(tickets.doesOpenTicketExist(report) || tickets.doesClosedTicketExist(report)) {
							Main.sendMessage(sender, Main.pluginName + "&eYou have already submitted that report!");
							return true;
						}
						if(PlayerChat.containsCurseWords(report)) {
							Main.sendMessage(sender, Main.pluginName + "&4Please don't use profanity in your report.");
							return true;
						}
						tickets.submitTicket(user, report);
						Main.sendMessage(sender, Main.pluginName + "&aThank you for your report.");
					} else {
						Main.sendMessage(sender, Main.pluginName + "&cPlease enter your report after the \"create\" argument.");
					}
				} else if(args[0].equalsIgnoreCase("view")) {
					if(!Permissions.hasPerm(sender, "supercmds.use.ticket.manage")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					if(args.length == 2) {
						if(StringUtil.isStrInt(args[1])) {
							int index = Integer.valueOf(args[1]).intValue();
							if(index < 1) {
								Main.sendMessage(sender, Main.pluginName + "&e\"&f" + args[1] + "&r&e\" is too low; integer must be above 0.");
								return true;
							}
							int i = index - 1;
							ArrayList<String> list = tickets.getOpenTicketsInOrder();
							if(list.size() <= i) {
								Main.sendMessage(sender, Main.pluginName + "&e\"&f" + args[1] + "&r&e\" is too large; there " + Main.fixPluralWord(list.size(), "are") + " only \"&b" + list.size() + "&e\" open " + Main.fixPluralWord(list.size(), "reports") + " at this time.");
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&aListing &2open&a report &b#" + i + "&a: ==========&z" + list.get(i));
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&c\"&f" + args[1] + "&r&e\" is not a valid integer.");
					}
					Main.sendMessage(sender, Main.pluginName + "&cPlease specify the report # to look up.");
				} else if(args[0].equalsIgnoreCase("list")) {
					if(!Permissions.hasPerm(sender, "supercmds.use.ticket.manage")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					//TODO
				} else if(args[0].equalsIgnoreCase("close")) {
					if(!Permissions.hasPerm(sender, "supercmds.use.ticket.manage")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					//TODO
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f create {report.....}&e\" or \"&f/" + command + "&r&f view [#]&e\" or \"&f/" + command + "&r&f list {player}&e\" or \"&f/" + command + "&r&f close {#|player}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("tp") || command.equalsIgnoreCase("teleport") || command.equalsIgnoreCase("tphere") || command.equalsIgnoreCase("tpall")) {
			if(Permissions.hasPerm(sender, "supercmds.use.teleport")) {
				if((command.equalsIgnoreCase("tphere")) && (user == null)) {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
					return true;
				}
				if(command.equalsIgnoreCase("tpall")) {
					if(user == null) {
						Main.sendMessage(sender, Main.getPlayerOnlyMsg());
						return true;
					}
					if(!Permissions.hasPerm(sender, "supercmds.use.teleport.all")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					if(args.length == 0) {
						for(Player player : Main.server.getOnlinePlayers()) {
							if(!player.getUniqueId().toString().equals(user.getUniqueId().toString())) {
								player.teleport(user);
								Main.sendMessage(player, Main.pluginName + "&aYou have just been teleported to \"&f" + userName + "&r&a\".");
							}
						}
						Main.sendMessage(user, Main.pluginName + "&aTeleported all online players to your position.");
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&e\"");
					return true;
				}
				if((args.length == 1) && (!command.equalsIgnoreCase("tphere"))) {
					if(!Permissions.hasPerm(sender, "supercmds.use.teleport.toOthers")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					Player target = Main.getPlayer(args[0]);
					if(user != null) {
						if(target != null) {
							if(user.teleport(target)) {
								Main.sendMessage(user, Main.pluginName + "&aSuccessfully teleported you to \"&f" + target.getDisplayName() + "&r&a\".");
							} else {
								Main.sendMessage(user, Main.pluginName + "&eSomething went wrong during the teleport!&z&aMaybe you should try again?");
							}
						} else {
							Main.sendMessage(user, getNoPlayerMsg(args[0]));
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " [target1] [target2]&e\" or&z&e\"&f/" + command + " [target1] {x} {y} {z}&e\"");
					}
				} else if((args.length == 2) || ((command.equalsIgnoreCase("tphere")) && (args.length == 1))) {
					if(!Permissions.hasPerm(sender, "supercmds.use.teleport.others")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					final Player target;
					final Player destination;
					if(args.length == 2) {
						target = Main.getPlayer(args[0]);
						destination = Main.getPlayer(args[1]);
					} else {
						target = Main.getPlayer(args[0]);
						destination = user;
					}
					if(target != null) {
						if(destination != null) {
							if(target.teleport(destination)) {
								Main.sendMessage(target, Main.pluginName + "&e\"&f" + sender.getName() + "&r&e\" just teleported you to \"&f" + destination.getDisplayName() + "&r&e\".");
								if(args.length == 2) {
									Main.sendMessage(sender, Main.pluginName + "&aSuccessfully teleported \"&f" + target.getDisplayName() + "&r&a\" to \"&f" + destination.getDisplayName() + "&r&a\".");
								} else {
									Main.sendMessage(user, Main.pluginName + "&aSuccessfully teleported \"&f" + target.getDisplayName() + "&r&a\" to you.");
								}
							} else if(args.length == 2) {
								Main.sendMessage(sender, Main.pluginName + "&eUnable to teleport \"&f" + target.getDisplayName() + "&r&e\" to \"&f" + destination.getDisplayName() + "&r&e\";&z&aMaybe you should try again?");
							} else {
								Main.sendMessage(user, Main.pluginName + "&eUnable to teleport \"&f" + target.getDisplayName() + "&r&a\" to you;&z&aMaybe you should try again?");
							}
						} else {
							Main.sendMessage(user, getNoPlayerMsg(args[1]));
						}
					} else if(user != null) {
						if(args.length == 2) {
							Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [target1] [target2]&e\" or&z&e\"&f/" + command + " [target1] {x} {y} {z}\"");
						} else {
							Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {target}&e\"");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " [target1] [target2]&e\" or&z&e\"&f/" + command + " [target1] {x} {y} {z}\"");
					}
				} else if(args.length == 3) {
					if(user == null) {
						Main.sendMessage(sender, Main.pluginName + "&eCoordinate based command syntax is only usable by players.");
						Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: &f/" + command + " [target1] [target2]&e or &f/" + command + " [target1] {x} {y} {z}");
						return true;
					}
					double x = Double.NaN;
					double y = Double.NaN;
					double z = Double.NaN;
					if(StringUtil.isStrDouble(args[0])) {
						x = Double.valueOf(args[0]).doubleValue();
					} else if(args[0].startsWith("~") && (args[0].length() > 1 ? StringUtil.isStrDouble(args[0].substring(1)) : true)) {
						x = user.getLocation().getX() + (args[0].length() > 1 ? Double.valueOf(args[0].substring(1)).doubleValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'X1' arg provided \"&f" + args[0] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrDouble(args[1])) {
						y = Double.valueOf(args[1]).doubleValue();
					} else if(args[1].startsWith("~") && (args[1].length() > 1 ? StringUtil.isStrDouble(args[1].substring(1)) : true)) {
						y = user.getLocation().getY() + (args[1].length() > 1 ? Double.valueOf(args[1].substring(1)).doubleValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Y1' arg provided  \"&f" + args[1] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrDouble(args[2])) {
						z = Double.valueOf(args[2]).doubleValue();
					} else if(args[2].startsWith("~") && (args[2].length() > 1 ? StringUtil.isStrDouble(args[2].substring(1)) : true)) {
						z = user.getLocation().getZ() + (args[2].length() > 1 ? Double.valueOf(args[2].substring(1)).doubleValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Z1' arg provided  \"&f" + args[2] + "&r&e\" is not an integer value.");
						return true;
					}
					if(x != Double.NaN && y != Double.NaN && z != Double.NaN) {
						Location loc = user.getLocation();
						if(user.teleport(new Location(user.getWorld(), x, y, z))) {
							PlayerStatus.getPlayerStatus(user).lastTeleportLoc = loc;
							Main.sendMessage(user, Main.pluginName + "&aTeleport successful.");
						} else {
							Main.sendMessage(user, Main.pluginName + "&eTeleport unsuccessful; is the world loaded in that area?");
						}
					} else {
						Main.sendMessage(user, Main.pluginName + "&eThe coordinates you entered are not valid(or the plugin failed to descipher them): \"&fx: " + args[0] + ", y: " + args[1] + ", z: " + args[2] + "; " + "&r&e\".");
						Main.sendMessage(user, "&eCheck your typing and try again.");
					}
				} else if(args.length == 4) {
					if(!Permissions.hasPerm(sender, "supercmds.use.teleport.others")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					Player target = Main.getPlayer(args[0]);
					if(target == null) {
						Main.sendMessage(user, getNoPlayerMsg(args[0]));
						return true;
					}
					double x = Double.NaN;
					double y = Double.NaN;
					double z = Double.NaN;
					if(StringUtil.isStrDouble(args[1])) {
						x = Double.valueOf(args[1]).doubleValue();
					} else if(args[1].startsWith("~") && (args[1].length() > 1 ? StringUtil.isStrDouble(args[1].substring(1)) : true) && user != null) {
						x = user.getLocation().getX() + (args[1].length() > 1 ? Double.valueOf(args[1].substring(1)).doubleValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'X1' arg provided \"&f" + args[1] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrDouble(args[2])) {
						y = Double.valueOf(args[2]).doubleValue();
					} else if(args[2].startsWith("~") && (args[2].length() > 1 ? StringUtil.isStrDouble(args[2].substring(1)) : true) && user != null) {
						y = user.getLocation().getY() + (args[2].length() > 1 ? Double.valueOf(args[2].substring(1)).doubleValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Y1' arg provided  \"&f" + args[2] + "&r&e\" is not an integer value.");
						return true;
					}
					if(StringUtil.isStrDouble(args[3])) {
						z = Double.valueOf(args[3]).doubleValue();
					} else if(args[3].startsWith("~") && (args[3].length() > 1 ? StringUtil.isStrDouble(args[3].substring(1)) : true) && user != null) {
						z = user.getLocation().getZ() + (args[3].length() > 1 ? Double.valueOf(args[3].substring(1)).doubleValue() : 0);
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe 'Z1' arg provided  \"&f" + args[3] + "&r&e\" is not an integer value.");
						return true;
					}
					if(x != Double.NaN && y != Double.NaN && z != Double.NaN) {
						Location loc = target.getLocation();
						if(target.teleport(new Location((user != null ? user : target).getWorld(), x, y, z))) {
							PlayerStatus.getPlayerStatus(target).lastTeleportLoc = loc;
							Main.sendMessage(sender, Main.pluginName + "&aTeleport successful.");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eTeleport unsuccessful; is the world loaded in that area?");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe coordinates you entered are not valid: \"&fx: " + args[1] + ", y: " + args[2] + ", z: " + args[3] + "; " + "&r&e\".");
						Main.sendMessage(sender, "&eCheck your typing and try again.");
					}
				} else if(user == null) {
					Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: &f/" + command + " [target1] [target2]&e or &f/" + command + " [target1] {x} {y} {z}");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [destination]&e\" or&z&e\"&f/" + command + " [target] [destination]&e\" or&z&e\"&f/" + command + " {x} {y} {z}&e\" or&z&e\"&f/" + command + " [target] {x} {y} {z}&e\".");
				}
			} else {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			}
			return true;
		}
		if(command.equalsIgnoreCase("tpa")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 1) {
					Player target = Main.getPlayer(args[0]);
					if(target == null) {
						Main.sendMessage(user, getNoPlayerMsg(args[0]));
						return true;
					}
					PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
					targetStatus.addTeleportToMeRequest(user);
					Main.sendMessage(user, Main.pluginName + "&aTeleport request sent.");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {playerName}&e\"");
				}
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("tpahere")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 1) {
					Player target = Main.getPlayer(args[0]);
					if(target == null) {
						Main.sendMessage(user, getNoPlayerMsg(args[0]));
						return true;
					}
					PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
					targetStatus.addTeleportToThemRequest(user);
					Main.sendMessage(user, Main.pluginName + "&aTeleport request sent.");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {playerName}&e\"");
				}
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("tpaall")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				for(Player player : Main.server.getOnlinePlayers()) {
					user.performCommand("tpahere " + player.getName());
				}
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("tpaccept")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				status.acceptTeleportRequests();
				Main.sendMessage(user, Main.pluginName + "&aYou accepted incoming teleport requests.");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("tpdeny")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				int numOfDeniedRequests = status.denyTeleportRequests();
				Main.sendMessage(sender, Main.pluginName + "&eYou denied &f" + numOfDeniedRequests + "&r&e pending teleport " + Main.fixPluralWord(numOfDeniedRequests, "requests") + ".");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if((command.equalsIgnoreCase("say")) || (command.equalsIgnoreCase("chat"))) {
			if((!Permissions.hasPerm(sender, "supercmds.use.say")) && (!Permissions.hasPerm(sender, "supercmds.use.chat"))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(Permissions.hasPerm(sender, "supercmds.chat.colors")) {
				if(Permissions.hasPerm(sender, "supercmds.chat.colors.magic")) {
					if(user != null) {
						user.chat(Main.formatColorCodes(strArgs));
					} else {
						Main.broadcast((sender == Main.console ? Main.consoleSayFormat : new StringBuilder("&6[").append(userName).append("&r&6]&f ").toString()) + strArgs);
					}
				} else if(user != null) {
					user.chat(Main.formatColorCodes(strArgs, false));
				} else {
					Main.broadcast((sender == Main.console ? Main.consoleSayFormat : new StringBuilder("&6[").append(userName).append("&r&6]&f ").toString()) + strArgs);
				}
				
			} else if(user != null) {
				user.chat(strArgs);
			} else {
				Main.broadcast((sender == Main.console ? Main.consoleSayFormat : new StringBuilder("&6[").append(userName).append("&r&6]&f ").toString()) + strArgs);
			}
			
			return true;
		}
		if(command.equalsIgnoreCase("me")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.me")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 0) {
				Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {args...}&e\"");
				return true;
			}
			Main.broadcast("&7* &f" + userName + "&r&f: &5" + strArgs);
			return true;
		}
		if((command.equalsIgnoreCase("afk")) || (command.equalsIgnoreCase("awayfromkeyboard"))) {
			if(user != null) {
				status.toggleAfkState();
			} else if(sender == Main.console) {
				isConsoleAfk = !isConsoleAfk;
				Main.broadcast("&7* " + Main.consoleSayFormat + ": &7is no" + (isConsoleAfk ? "w afk." : " longer afk."));
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			
			return true;
		}
		if((command.equalsIgnoreCase("perm")) || (command.equalsIgnoreCase("permission"))) {
			if(!Main.handlePermissions) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandlePermissions&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if((!Permissions.hasPerm(sender, "supercmds.use.perm")) && (!Permissions.hasPerm(sender, "supercmds.use.permission"))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 2) {
				UUID uuid = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				if(uuid != null) {
					String targetName = Main.uuidMasterList.getPlayerNameFromUUID(uuid);
					String flag = args[1];
					PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
					if(flag.equalsIgnoreCase("info")) {
						Main.sendMessage(sender, Main.pluginName + "&3Showing permission information for player \"&f" + targetName + "&r&3\":");
						Main.sendMessage(sender, perms.toString());
						perms.saveAndDisposeIfPlayerNotOnline();
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					perms.disposeIfPlayerNotOnline();
				} else {
					Main.sendMessage(sender, getNoPlayerMsg(args[0]));
					return true;
				}
			} else if(args.length == 3) {
				UUID uuid = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				if(uuid != null) {
					String targetName = Main.uuidMasterList.getPlayerNameFromUUID(uuid);
					String flag = args[1];
					String perm = args[2];
					PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
					if(flag.equalsIgnoreCase("add")) {
						if(perms.setPermission(perm, true)) {
							if(perms.saveAndDisposeIfPlayerNotOnline()) {
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
							}
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when adding the permission \"&f" + perm + "&r&e\" to the player \"&f" + targetName + "&r&e\"!&z&aPerhaps they already have that permission?");
						perms.saveAndDisposeIfPlayerNotOnline();
						return true;
					}
					if(flag.equalsIgnoreCase("remove")) {
						if(perms.setPermission(perm, false)) {
							if(perms.saveAndDisposeIfPlayerNotOnline()) {
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
							}
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when removing the permission \"&f" + perm + "&r&e\" from the player \"&f" + targetName + "&r&e\"!&z&aPerhaps it was never added?");
						perms.saveAndDisposeIfPlayerNotOnline();
						return true;
					}
					if(flag.equalsIgnoreCase("setgroup")) {
						PlayerPermissions.Group group = PlayerPermissions.Group.getGroupByName(perm);
						if(group != null) {
							if(perms.changeGroup(group)) {
								if(perms.saveAndDisposeIfPlayerNotOnline()) {
									Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
								}
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when changing the player \"&f" + targetName + "&r&e\"'s group to \"&f" + group.displayName + "&r&e\"!");
							perms.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + perm + "&r&e\" does not exist.&z&aCreate it using &f/group&a!");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					perms.disposeIfPlayerNotOnline();
				} else {
					Main.sendMessage(sender, Main.getNoPlayerMsg(args[0]));
					return true;
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {target} info&e\" or&z&e\"&f/" + command + " {target} {add|remove|setgroup} {permission.node|groupName}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("group")) {
			if(!Main.handlePermissions) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandlePermissions&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.group")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("list")) {
					Main.sendMessage(sender, Main.pluginName + "&aListing all groups:");
					for(PlayerPermissions.Group group : PlayerPermissions.Group.getInstances()) {
						Main.sendMessage(sender, "&3\"&f" + group.displayName + "&r&3\"(config name: &f" + group.name + "&r&3);");
					}
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + args[0] + "&r&e\".");
			} else if(args.length == 2) {
				String groupName = args[0];
				String createDeleteInfo = args[1];
				PlayerPermissions.Group check = PlayerPermissions.Group.getGroupByName(groupName);
				if(createDeleteInfo.equalsIgnoreCase("create")) {
					if(check != null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + check.name + "&r&e\" already exists!");
						return true;
					}
					PlayerPermissions.Group group = PlayerPermissions.Group.createGroup(groupName);
					if(group != null) {
						Main.sendMessage(sender, Main.pluginName + "&aSuccessfully created the group \"&f" + group.name + "&r&a\"!&z&aThe group's display name was set to: \"&f" + group.displayName + "&r&a\".");
						PlayerPermissions.Group.saveToStaticFile();
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when attempting to create the group \"&f" + groupName + "&r&e\".&z&ePlease contact a server administrator about this issue.(Maybe the server needs to be restarted?)");
					return true;
				}
				if((createDeleteInfo.equalsIgnoreCase("delete")) || (createDeleteInfo.equalsIgnoreCase("del"))) {
					PlayerPermissions.Group group = PlayerPermissions.Group.getGroupByName(groupName);
					if(group != null) {
						group.dispose();
						group = null;
						PlayerPermissions.Group.saveToStaticFile();
						Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.&z&aPerhaps it was already deleted?");
					return true;
				}
				if(createDeleteInfo.equalsIgnoreCase("info")) {
					if(check != null) {
						Main.sendMessage(sender, Main.pluginName + "&aDisplaying group information:&z&f" + check.toString());
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + createDeleteInfo + "&r&e\".");
				}
			} else if(args.length == 3) {
				String groupName = args[0];
				String addRemove = args[1];
				String permNode = args[2];
				PlayerPermissions.Group group = PlayerPermissions.Group.getGroupByName(groupName);
				if(group != null) {
					if(addRemove.equalsIgnoreCase("setrequirement")) {
						if(permNode.equalsIgnoreCase("none")) {
							group.moneyOrCredit = "NONE";
							group.costToRankup = 0.0D;
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							PlayerPermissions.Group.saveToStaticFile();
							return true;
						}
					} else if(addRemove.equalsIgnoreCase("add")) {
						if(group.setPermission(permNode, true)) {
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							PlayerPermissions.Group.saveToStaticFile();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when adding the permission \"&f" + permNode + "&r&e\" to the group \"&f" + group.displayName + "&r&e\"!&z&aIs it already added?");
					} else if(addRemove.equalsIgnoreCase("remove")) {
						if(group.setPermission(permNode, false)) {
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							PlayerPermissions.Group.saveToStaticFile();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when removing the permission \"&f" + permNode + "&r&e\" from the group \"&f" + group.displayName + "&r&e\"!&z&aWas it already removed?");
					} else if(addRemove.equalsIgnoreCase("inheritance")) {
						if(permNode.equals("info")) {
							Main.sendMessage(sender, Main.pluginName + "&aListing group inheritance info for group \"&f" + group.displayName + "&r&a\":");
							ArrayList<String> groupNames = group.getInheritedGroupNames();
							int i = 0;
							for(String name : groupNames) {
								Main.sendMessage(sender, "&3[&f" + i + "&3]: &f" + name);
								i++;
							}
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + permNode + "&r&e\".");
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + addRemove + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.");
				}
			} else if(args.length >= 4) {
				String groupName = args[0];
				String flag = args[1];
				String defaultDispInherNexGr = args[2];
				String value = Main.getElementsFromStringArrayAtIndexAsString(args, 3);
				PlayerPermissions.Group group = PlayerPermissions.Group.getGroupByName(groupName);
				if(group != null) {
					if(flag.equalsIgnoreCase("set")) {
						if(defaultDispInherNexGr.equalsIgnoreCase("default")) {
							Boolean val = value.equalsIgnoreCase("false") ? Boolean.FALSE : value.equalsIgnoreCase("true") ? Boolean.TRUE : null;
							if(val != null) {
								boolean isDefault = val.booleanValue();
								if(group.setDefault(isDefault)) {
									Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
									PlayerPermissions.Group.saveToStaticFile();
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when setting the group \"&f" + group.name + "&r&e\" as the default group!&z&ePerhaps there is already a default group?");
								}
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid value \"&f" + value + "&r&e\".");
						} else {
							if(defaultDispInherNexGr.equalsIgnoreCase("displayname")) {
								if(group.setDisplayName(value)) {
									Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
									PlayerPermissions.Group.saveToStaticFile();
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when changing the display name of the group \"&f" + group.name + "&r&e\" to \"&f" + value + "&r&e\"!&z&aPerhaps the old display name is the same as the new one?");
								}
								return true;
							}
							if(defaultDispInherNexGr.equalsIgnoreCase("nextgroup")) {
								PlayerPermissions.Group nextGroup = PlayerPermissions.Group.getGroupByName(value);
								if(nextGroup != null) {
									if(group.setNextGroup(nextGroup)) {
										Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										PlayerPermissions.Group.saveToStaticFile();
										return true;
									}
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when setting the group \"&f" + group.name + "&r&e\"'s next group(rankup) to \"&f" + nextGroup.name + "&r&e\"!&z&aPerhaps it was already set to that group?&z&a(Note: you can't set a group as it's own next group.)");
									return true;
								}
								Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + value + "&r&e\" does not exist.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + defaultDispInherNexGr + "&r&e\".");
							}
						}
					} else if((flag.equalsIgnoreCase("add")) || (flag.equalsIgnoreCase("remove"))) {
						if(flag.equalsIgnoreCase("add")) {
							if(defaultDispInherNexGr.equalsIgnoreCase("inheritance")) {
								PlayerPermissions.Group newInheritance = PlayerPermissions.Group.getGroupByName(value);
								if(newInheritance != null) {
									if(group.addInheritance(newInheritance)) {
										Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										PlayerPermissions.Group.saveToStaticFile();
										return true;
									}
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when adding the group to \"&f" + newInheritance.name + "&r&e\" to the group \"&f" + group.name + "&r&e\"'s inheritances!&z&aPerhaps it already contained that group?&z&a(Note: you can't set a group as it's own inheritance group.)");
									return true;
								}
								Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + value + "&r&e\" does not exist.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + defaultDispInherNexGr + "&r&e\".");
							}
						} else if(flag.equalsIgnoreCase("remove")) {
							if(defaultDispInherNexGr.equalsIgnoreCase("inheritance")) {
								PlayerPermissions.Group newInheritance = PlayerPermissions.Group.getGroupByName(value);
								if(newInheritance != null) {
									if(group.removeInheritance(newInheritance)) {
										Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										PlayerPermissions.Group.saveToStaticFile();
										return true;
									}
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when removing the group to \"&f" + newInheritance.name + "&r&e\" from the group \"&f" + group.name + "&r&e\"'s inheritances!&z&aPerhaps it was already removed?");
									return true;
								}
								Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + value + "&r&e\" does not exist.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + defaultDispInherNexGr + "&r&e\".");
							}
						}
					} else if(flag.equalsIgnoreCase("setrequirement")) {
						if(defaultDispInherNexGr.equalsIgnoreCase("money")) {
							if(!CodeUtils.isStrAValidDouble(value)) {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid amount given: \"&f" + value + "&r&e\".");
							} else {
								double val = CodeUtils.getDoubleFromStr(value, 0.0D);
								group.moneyOrCredit = "MONEY";
								group.costToRankup = val;
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								PlayerPermissions.Group.saveToStaticFile();
								return true;
							}
						} else if((defaultDispInherNexGr.equalsIgnoreCase("credit")) || (defaultDispInherNexGr.equalsIgnoreCase("credits"))) {
							if(!CodeUtils.isStrAValidInt(value)) {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid &fcredit&e amount given: \"&f" + value + "&r&e\".");
							} else {
								int val = CodeUtils.getIntFromStr(value, 0);
								group.moneyOrCredit = "CREDIT";
								group.costToRankup = val;
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								PlayerPermissions.Group.saveToStaticFile();
								return true;
							}
						} else {
							if(defaultDispInherNexGr.equalsIgnoreCase("none")) {
								group.moneyOrCredit = "NONE";
								group.costToRankup = 0.0D;
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								PlayerPermissions.Group.saveToStaticFile();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + defaultDispInherNexGr + "&r&e\".");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.");
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " list&e\" or&z&e\"&f/" + command + " {groupName} {add|remove} {permission.node}&e\" or&z&e\"&f/" + command + " {groupName} {create|delete|info}&e\" or&z&e\"&f/" + command + " {groupName} inheritance info&e\" or&z&e\"&f/" + command + " {groupName} set {default|displayName|nextGroup} {value}&e\" or&z&e\"&f/" + command + " {groupName} {add|remove} inheritance {value}&e\" or&z&e\"&f/" + command + " {groupName} {setRequirement} {none|money|credit} [value]&e\"");
			return true;
		}
		if((command.equalsIgnoreCase("bal")) || (command.equalsIgnoreCase("balance")) || (command.equalsIgnoreCase("money")) || (command.equalsIgnoreCase("fe")) || (command.equalsIgnoreCase("credit")) || (command.equalsIgnoreCase("credits"))) {
			if(!Main.handleEconomy) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleEconomy&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if((!Permissions.hasPerm(sender, "supercmds.use.money")) && (!Permissions.hasPerm(sender, "supercmds.use.bal")) && (!Permissions.hasPerm(sender, "supercmds.use.balance")) && (!Permissions.hasPerm(sender, "supercmds.use.fe")) && (!Permissions.hasPerm(sender, "supercmds.use.credits")) && (!Permissions.hasPerm(sender, "supercmds.use.credit"))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 0) {
				if(user == null) {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				} else {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
					Main.sendMessage(user, Main.pluginName + "&aYou have \"&f" + Main.decimal.format(eco.getBalance()) + "&r&a\" &f" + Main.moneyTerm + "&r&a in your pocket and &f" + eco.credits + " " + Main.creditTerm + "&a to your name.");
					eco.saveToFile();
					return true;
				}
			} else if(args.length == 1) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target != null) {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
					Main.sendMessage(sender, Main.pluginName + "&aPlayer \"&f" + targetName + "&r&a\" has \"&f" + Main.decimal.format(eco.getBalance()) + "&r&a\" &f" + Main.moneyTerm + "&r&a in their pocket and has &f" + eco.credits + " " + Main.creditTerm + "&a to their name.");
					eco.disposeIfPlayerNotOnline();
					return true;
				}
				Main.sendMessage(sender, getNoPlayerMsg(args[0]));
			}
			if(user == null) {
				Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " {targetName}&e\"");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" or&z&e\"&f/" + command + " [targetName]&e\"");
			}
			return true;
		}
		if((command.equalsIgnoreCase("eco")) || (command.equalsIgnoreCase("econ")) || (command.equalsIgnoreCase("economy"))) {
			if(!Main.handleEconomy) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleEconomy&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if((!Permissions.hasPerm(sender, "supercmds.use.eco")) && (!Permissions.hasPerm(sender, "supercmds.use.econ")) && (!Permissions.hasPerm(sender, "supercmds.use.economy"))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if((args.length == 1) || ((args.length == 2) && (args[1].equalsIgnoreCase("info")))) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target != null) {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
					Main.sendMessage(sender, Main.pluginName + "&aThe player &f" + targetName + "&r&a has &f" + Main.decimal.format(eco.getBalance()) + " " + Main.moneyTerm + "&r&a and &f" + eco.credits + " " + Main.creditTerm + "&a to their name.");
					return true;
				}
				Main.sendMessage(sender, Main.getNoPlayerMsg(args[0]));
				return true;
			}
			if(args.length == 4) {
				String flag = args[0];
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[1]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				String moneyCredit = args[2];
				String value = args[3];
				if(target != null) {
					if(!CodeUtils.isStrAValidDouble(value)) {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid amount given: \"&f" + value + "&r&e\".");
					} else {
						double amount = CodeUtils.getDoubleFromStr(value, 0.0D);
						int intAmount = Double.valueOf(Math.round(amount)).intValue();
						PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
						if(flag.equalsIgnoreCase("give")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.setBalance(eco.getBalance() + amount);
								Main.sendMessage(sender, Main.pluginName + "&aYou gave &f" + Main.decimal.format(amount) + " " + Main.moneyTerm + "&r&a to player \"&f" + targetName + "&r&a\". They now have &f" + Main.decimal.format(eco.getBalance()) + " " + Main.moneyTerm + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							if(moneyCredit.equalsIgnoreCase("credit")) {
								eco.credits += intAmount;
								Main.sendMessage(sender, Main.pluginName + "&aYou gave &f" + intAmount + "&r&a " + Main.creditTerm + " to player \"&f" + targetName + "&r&a\". They now have &f" + eco.credits + "&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + moneyCredit + "&r&e\".");
						} else if(flag.equalsIgnoreCase("take")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.setBalance(eco.getBalance() - amount);
								Main.sendMessage(sender, Main.pluginName + "&aYou took &f" + Main.decimal.format(amount) + " " + Main.moneyTerm + "&r&a from player \"&f" + targetName + "&r&a\". They now have &f" + Main.decimal.format(eco.getBalance()) + " " + Main.moneyTerm + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							if(moneyCredit.equalsIgnoreCase("credit")) {
								eco.credits -= intAmount;
								Main.sendMessage(sender, Main.pluginName + "&aYou took &f" + intAmount + "&r&a " + Main.creditTerm + " from player \"&f" + targetName + "&r&a\". They now have &f" + eco.credits + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + moneyCredit + "&r&e\".");
						} else if(flag.equalsIgnoreCase("set")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.setBalance(amount);
								Main.sendMessage(sender, Main.pluginName + "&aYou set \"&f" + targetName + "&r&a\"'s  &f" + Main.moneyTerm + "&r&a to &f" + Main.decimal.format(eco.getBalance()) + " " + Main.moneyTerm + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							if(moneyCredit.equalsIgnoreCase("credit")) {
								eco.credits = intAmount;
								Main.sendMessage(sender, Main.pluginName + "&aYou set player \"&f" + targetName + "&r&a\"'s " + Main.creditTerm + " to &f" + eco.credits + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + moneyCredit + "&r&e\".");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
						}
						eco.disposeIfPlayerNotOnline();
					}
				} else {
					Main.sendMessage(sender, Main.getNoPlayerMsg(args[1]));
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {give|take|set} {targetName} {money|credit} {amount} &e\" or&z&e\"&f/" + command + " {targetName} [info]&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("speed")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.speed")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				if(user == null) {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
					Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " {amount|reset} {targetName} {fly|walk}&e\"");
					return true;
				}
				if(!CodeUtils.isStrAValidFloat(args[0])) {
					if(args[0].equalsIgnoreCase("reset")) {
						if(user.isFlying()) {
							user.setFlySpeed(0.1F);
							status.lastFlySpeed = 0.1F;
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully set your fly speed to &f" + Main.decimal.format(user.getFlySpeed()) + "&a!");
						} else {
							user.setWalkSpeed(0.2F);
							status.lastWalkSpeed = 0.2F;
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully set your walk/run speed to &f" + Main.decimal.format(user.getWalkSpeed()) + "&a!");
						}
						status.saveToFile();
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eInvalid speed given: \"&f" + args[0] + "&r&e\".");
				} else {
					float newSpeed = CodeUtils.getFloatFromStr(args[0], 0.0F);
					if((newSpeed > 1.0F) || (newSpeed < -1.0F)) {
						Main.sendMessage(user, Main.pluginName + "&eSpeed values must be between -1 and 1.&z&aDefault walk speed is 0.2; default fly speed is 0.1.");
					} else {
						if(user.isFlying()) {
							user.setFlySpeed(newSpeed);
							status.lastFlySpeed = newSpeed;
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully set your fly speed to &f" + Main.decimal.format(user.getFlySpeed()) + "&a!");
						} else {
							user.setWalkSpeed(newSpeed);
							status.lastWalkSpeed = newSpeed;
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully set your walk/run speed to &f" + Main.decimal.format(user.getWalkSpeed()) + "&a!");
						}
						status.saveToFile();
						return true;
					}
				}
			} else if(args.length == 3) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[1]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target == null) {
					Main.sendMessage(sender, Main.getNoPlayerMsg(args[1]));
					return true;
				}
				if(!CodeUtils.isStrAValidFloat(args[0])) {
					if(args[0].equalsIgnoreCase("reset")) {
						String flyOrWalk = args[2];
						if(flyOrWalk.equalsIgnoreCase("fly")) {
							PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
							targetStatus.lastFlySpeed = 0.1F;
							PlayerStatus.updatePlayerFlyModeStates();
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully reset \"&f" + targetName + "&r&a\"'s fly speed back to &f" + Main.decimal.format(targetStatus.lastFlySpeed) + "&a!");
							targetStatus.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						if(flyOrWalk.equalsIgnoreCase("walk")) {
							PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
							targetStatus.lastWalkSpeed = 0.2F;
							PlayerStatus.updatePlayerFlyModeStates();
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully reset \"&f" + targetName + "&r&a\"'s walk/run speed back to &f" + Main.decimal.format(targetStatus.lastWalkSpeed) + "&a!");
							targetStatus.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flyOrWalk + "&r&e\".");
						
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eInvalid speed given: \"&f" + args[0] + "&r&e\".");
				} else {
					float newSpeed = CodeUtils.getFloatFromStr(args[0], 0.0F);
					if((newSpeed > 1.0F) || (newSpeed < -1.0F)) {
						Main.sendMessage(user, Main.pluginName + "&eSpeed values must be between -1 and 1.&z&aDefault walk speed is 0.2; default fly speed is 0.1.");
					} else {
						String flyOrWalk = args[2];
						if(flyOrWalk.equalsIgnoreCase("fly")) {
							PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
							targetStatus.lastFlySpeed = newSpeed;
							PlayerStatus.updatePlayerFlyModeStates();
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s fly speed to &f" + Main.decimal.format(targetStatus.lastFlySpeed) + "&a!");
							targetStatus.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						if(flyOrWalk.equalsIgnoreCase("walk")) {
							PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
							targetStatus.lastWalkSpeed = newSpeed;
							PlayerStatus.updatePlayerFlyModeStates();
							Main.sendMessage(user, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s walk/run speed to &f" + Main.decimal.format(targetStatus.lastWalkSpeed) + "&a!");
							targetStatus.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flyOrWalk + "&r&e\".");
					}
				}
			}
			
			if(user == null) {
				Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " {amount|reset} {targetName} {fly|walk}&e\"");
			} else {
				Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {amount|reset} [targetName] [fly|walk]&e\"");
			}
			return true;
		}
		if(command.equalsIgnoreCase("hat")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.hat")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 0) {
				if(user != null) {
					ItemStack itemInHand = user.getInventory().getItemInMainHand();
					if((itemInHand != null) && (itemInHand.getType() != Material.AIR)) {
						ItemStack itemOnHead = user.getInventory().getHelmet();
						if((itemOnHead != null) && (itemOnHead.getType() != Material.AIR)) {
							user.getInventory().setItemInMainHand(itemOnHead);
						} else {
							user.getInventory().setItemInMainHand(null);
						}
						user.getInventory().setHelmet(itemInHand);
						Main.sendMessage(sender, Main.pluginName + "&aEnjoy your new hat!");
						return true;
					}
				} else {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
					return true;
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" while holding an item.");
			return true;
		}
		if(command.equalsIgnoreCase("title")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.title")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(Permissions.hasPerm(sender, "supercmds.use.title.any")) {
					if((args.length >= 2) && (args[0].equalsIgnoreCase("custom"))) {
						String arg1 = Main.getElementsFromStringArrayAtIndexAsString(args, 1);
						PlayerChat chat = PlayerChat.getPlayerChat(user);
						if(arg1.equalsIgnoreCase("clear")) {
							chat.setPrefix(null);
							chat.saveToFile();
							Main.sendMessage(user, Main.pluginName + "&aYour title was cleared successfully.&z&aYour display name is now: &f" + user.getDisplayName());
							return true;
						}
						int arg0Length = Main.stripColorCodes(Main.formatColorCodes(arg1)).length();
						if(arg0Length <= 16) {
							if(chat.setPrefix(arg1)) {
								Main.sendMessage(user, Main.pluginName + "&aYour prefix has been set to \"&f" + chat.getPrefix() + "&r&a\"!&z&aYour display name is now: &f" + user.getDisplayName());
								return true;
							}
							Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when setting your title to \"&f" + arg1 + "&r&e\"!&z&aIs it the same title that you had before? If not,&z&athen please contact a server administrator about this issue.");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eSorry, but chat titles cannot exceed 16 characters in length.&z&aNote: Color codes do not count towards this limit.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {args...|clear} or /custom {args...|clear}&e\"");
				}
				
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if((command.equalsIgnoreCase("nick")) || (command.equalsIgnoreCase("nickname"))) {
			if(Main.handleChat) {
				if((!Permissions.hasPerm(sender, "supercmds.use.nick")) && (!Permissions.hasPerm(sender, "supercmds.use.nickname"))) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				if(user != null) {
					if(args.length >= 1) {
						String nickName = strArgs.trim();
						int nickLength = Main.stripColorCodes(Main.formatColorCodes(nickName)).length();
						if(nickLength <= 16) {
							PlayerChat chat = PlayerChat.getPlayerChat(user);
							chat.setNickname(nickName);
							Main.sendMessage(user, Main.pluginName + "&aYour new nickname is: \"&f" + chat.getNickName() + "&r&a\"!");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eSorry, but nicknames cannot exceed 16 characters in length.&z&aNote: Color codes do not count towards this limit.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {args...}&e\"");
				} else {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				}
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
			return true;
		}
		if((command.equalsIgnoreCase("realname")) || (command.equalsIgnoreCase("ign"))) {
			if(args.length >= 1) {
				String nickName = Main.getElementsFromStringArrayAtIndexAsString(args, 0);
				PlayerChat chat = PlayerChat.getPlayerChatWithNickName(args[0]);
				if(chat != null) {
					chat.updateName();
					Main.sendMessage(sender, Main.pluginName + "&a\"&f" + chat.getNickName() + "&r&a\"'s real name is: &f" + chat.name);
					chat.saveAndDisposeIfPlayerNotOnline();
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThere aren't any players on this server who have the nick name \"&f" + nickName + "&r&a\".");
				}
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {nickName...}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("managechat")) {
			if(Main.handleChat) {
				if(!Permissions.hasPerm(sender, "supercmds.use.managechat")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					Main.sendConsoleMessage(Main.pluginName + sender.getName() + "&c did not have permission to use managechat.");
					return true;
				}
				if(args.length >= 2) {
					UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
					String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
					if(target == null) {
						Main.sendMessage(sender, getNoPlayerMsg(args[0]));
					} else {
						PlayerChat chat = PlayerChat.getPlayerChat(target);
						String flag = args[1];
						if(args.length == 2) {
							if(flag.equalsIgnoreCase("info")) {
								Main.sendMessage(sender, Main.pluginName + "&3Displaying player \"&f" + targetName + "&r&3\"'s chat information:&z&f" + chat.toString());
								chat.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
						} else if(args.length >= 3) {
							String value = Main.getElementsFromStringArrayAtIndexAsString(args, 2);
							if(flag.equalsIgnoreCase("prefix")) {
								if(!Permissions.hasPerm(sender, "supercmds.use.managechat.prefix")) {
									Main.sendMessage(sender, Main.pluginName + Main.noPerm);
									chat.saveAndDisposeIfPlayerNotOnline();
									return true;
								}
								if(value.equalsIgnoreCase("clear")) {
									chat.setPrefix(null);
								} else {
									chat.setPrefix(value);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s prefix to: \"&f" + chat.prefix + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								if(!chat.saveAndDisposeIfPlayerNotOnline()) {
									Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
								}
								return true;
							}
							if(flag.equalsIgnoreCase("nickname")) {
								if(!Permissions.hasPerm(sender, "supercmds.use.managechat.nickname")) {
									Main.sendMessage(sender, Main.pluginName + Main.noPerm);
									chat.saveAndDisposeIfPlayerNotOnline();
									return true;
								}
								if(value.equalsIgnoreCase("clear")) {
									chat.setNickname(null);
								} else {
									chat.setNickname(value);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s nickname to: \"&f" + chat.nickname + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								if(!chat.saveAndDisposeIfPlayerNotOnline()) {
									Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
								}
								return true;
							}
							if(flag.equalsIgnoreCase("suffix")) {
								if(!Permissions.hasPerm(sender, "supercmds.use.managechat.suffix")) {
									Main.sendMessage(sender, Main.pluginName + Main.noPerm);
									chat.saveAndDisposeIfPlayerNotOnline();
									return true;
								}
								if(value.equalsIgnoreCase("clear")) {
									chat.setSuffix(null);
								} else {
									chat.setSuffix(value);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s suffix to: \"&f" + chat.suffix + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								if(!chat.saveAndDisposeIfPlayerNotOnline()) {
									Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
								}
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
						} else if(args.length == 4) {
							chat.saveAndDisposeIfPlayerNotOnline();
							if(flag.equalsIgnoreCase("set")) {
								String colorMagic = args[2];
								String enableDisable = args[3];
								PlayerPermissions perm = PlayerPermissions.getPlayerPermissions(target);
								if(colorMagic.equalsIgnoreCase("color")) {
									if(!Permissions.hasPerm(sender, "supercmds.use.managechat.set.color")) {
										Main.sendMessage(sender, Main.pluginName + Main.noPerm);
										perm.saveAndDisposeIfPlayerNotOnline();
										return true;
									}
									if(enableDisable.equalsIgnoreCase("enable")) {
										perm.setPermission("supercmds.chat.colors", true);
										if(perm.saveAndDisposeIfPlayerNotOnline()) {
											Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										} else {
											Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
										}
										return true;
									}
									if(enableDisable.equalsIgnoreCase("disable")) {
										perm.setPermission("supercmds.chat.colors", false);
										if(perm.saveAndDisposeIfPlayerNotOnline()) {
											Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										} else {
											Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
										}
										return true;
									}
									Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + enableDisable + "&r&e\".");
								} else if(colorMagic.equalsIgnoreCase("magiccolor")) {
									if(!Permissions.hasPerm(sender, "supercmds.use.managechat.set.magiccolor")) {
										Main.sendMessage(sender, Main.pluginName + Main.noPerm);
										perm.saveAndDisposeIfPlayerNotOnline();
										return true;
									}
									if(enableDisable.equalsIgnoreCase("enable")) {
										perm.setPermission("supercmds.chat.colors.magic", true);
										if(perm.saveAndDisposeIfPlayerNotOnline()) {
											Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										} else {
											Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
										}
										return true;
									}
									if(enableDisable.equalsIgnoreCase("disable")) {
										perm.setPermission("supercmds.chat.colors.magic", false);
										if(perm.saveAndDisposeIfPlayerNotOnline()) {
											Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										} else {
											Main.sendMessage(sender, Main.pluginName + "&eThe command completed successfully, but data did not get saved to file.");
										}
										return true;
									}
									Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + enableDisable + "&r&e\".");
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + colorMagic + "&r&e\".");
								}
								perm.saveAndDisposeIfPlayerNotOnline();
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
							}
						}
						chat.saveAndDisposeIfPlayerNotOnline();
					}
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName} {info}&e\" or&z&e\"&f/" + command + " {targetName} {prefix|nickname|suffix} {args...|clear}&e\" or&z&e\"&f/" + command + " {targetName} {set} {color|magiccolor} {enable|disable}&e\"");
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
			return true;
		}
		if((command.equalsIgnoreCase("invsee")) || (command.equalsIgnoreCase("peek"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.invsee")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null) {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eThis command is being worked on due to technical difficulties and potential loss of inventory data. Sorry!");
			return true;
		}
		
		if(command.equalsIgnoreCase("setwarp")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.setwarp")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null) {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			if((args.length == 3) && (!Permissions.hasPerm(sender, "supercmds.use.setwarp.require"))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length >= 1) {
				String warpName = args[0];
				Warps.Warp warp = Warps.getWarpByName(warpName);
				PlayerPermissions.Group group = args.length == 3 ? (args[1].equalsIgnoreCase("requiregroup") ? PlayerPermissions.Group.getGroupByName(args[2]) : null) : null;
				String requiredPerm = args.length == 3 ? (args[1].equalsIgnoreCase("requireperm") ? args[2] : null) : null;
				if((args.length == 3) && (args[1].equalsIgnoreCase("requiregroup")) && (group == null)) {
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + args[2] + "&r&e\" does not exist.");
				} else if((args.length == 3) && (args[1].equalsIgnoreCase("requireperm")) && ((requiredPerm == null) || (requiredPerm.isEmpty()))) {
					Main.sendMessage(sender, Main.pluginName + "&eA group cannot require a null permission; no one would ever be allowed in the group!");
				} else if((args.length == 1) || (args.length == 3)) {
					if(warp == null) {
						warp = Warps.createWarp(warpName, user.getLocation(), group, requiredPerm);
					} else {
						warp.location = user.getLocation();
						warp.requiredGroup = group;
						warp.requiredPermission = requiredPerm;
						Warps.getInstance().saveToFile();
					}
					Main.sendMessage(user, Main.pluginName + "&6/warp " + warp.name + "&a has been set to your current location.");
					return true;
				}
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {warpname} [requireperm|requiregroup] [perm|groupname]&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("warp")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.warp")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 2) {
				if(!Permissions.hasPerm(sender, "supercmds.use.warp.others")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				if(sender.getClass().getSimpleName().equals("CraftCommandBlock")) {
					return true;
				}
				String warpName = args[0];
				Warps.Warp warp = Warps.getWarpByName(warpName);
				if(warp == null) {
					Main.sendMessage(sender, Main.pluginName + "&eThe warp \"&f" + warpName + "&r&e\" does not exist.&z&aTry setting it with &f/setwarp&a!");
					return true;
				}
				if(warp.location == null) {
					Main.sendMessage(sender, Main.pluginName + "&eThe warp \"&6/warp " + warp.name + "&r&e\" is currently pointing to an invalid location!&z&aAre all the worlds loaded like they should be?");
					return true;
				}
				Player target = Main.getPlayer(args[1]);
				if(target == null) {
					Main.sendMessage(sender, getNoPlayerMsg(args[1]));
					return true;
				}
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(target);
				if(warp.requiredGroup != null) {
					if(!perms.isAMemberOfGroup(warp.requiredGroup)) {
						Main.sendMessage(sender, Main.pluginName + "&eTarget player is not a member of that warp's required group(\"&f" + warp.requiredGroup.displayName + "&r&e\").");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
				} else if((warp.requiredPermission != null) && (!warp.requiredPermission.isEmpty()) && (!perms.hasPermission(warp.requiredPermission))) {
					Main.sendMessage(sender, Main.pluginName + "&eTarget player does not have the permission that this warp requires" + (sender.isOp() ? "(\"&b" + warp.requiredPermission + "&r&e\")" : "") + "!");
					perms.disposeIfPlayerNotOnline();
					return true;
				}
				
				if(target.teleport(warp.location)) {
					Main.sendMessage(target, Main.pluginName + "&aWarping to &6/warp " + warp.name + "&r&a.");
					Main.sendMessage(sender, Main.pluginName + "&aSent player \"&f" + target.getDisplayName() + "&r&a\" to &6/warp " + warp.name + "&r&a.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong during the teleport! Is the world loaded in that location?");
				}
				perms.disposeIfPlayerNotOnline();
				return true;
			}
			if(args.length == 1) {
				if(user == null) {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
					return true;
				}
				String warpName = args[0];
				Warps.Warp warp = Warps.getWarpByName(warpName);
				if(warp == null) {
					Main.sendMessage(user, Main.pluginName + "&eThe warp \"&f" + warpName + "&r&e\" does not exist.&z&aTry setting it with &f/setwarp&a!");
					return true;
				}
				if(warp.location == null) {
					Main.sendMessage(user, Main.pluginName + "&eThe warp \"&6/warp " + warp.name + "&r&e\" is currently pointing to an invalid location!&z&aAre all the worlds loaded like they should be?");
					return true;
				}
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
				if(warp.requiredGroup != null) {
					if(!perms.isAMemberOfGroup(warp.requiredGroup)) {
						Main.sendMessage(user, Main.pluginName + "&eYou are not a member of that warp's required group(\"&f" + warp.requiredGroup.displayName + "&r&e\")!");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
				} else if((warp.requiredPermission != null) && (!warp.requiredPermission.isEmpty())) {
					if(!perms.hasPermission(warp.requiredPermission)) {
						Main.sendMessage(user, Main.pluginName + "&eYou do not have the permission that this warp requires" + (sender.isOp() ? "(\"&b" + warp.requiredPermission + "&r&e\")" : "") + "!");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
					Main.DEBUG("&5TEST! Player \"&f" + user.getDisplayName() + "&r&5\" has warp permission \"&f" + warp.requiredPermission + "&r&5\"!");
				}
				if(user.teleport(warp.location)) {
					Main.sendMessage(user, Main.pluginName + "&aWarping to &6/warp " + warp.name + "&r&a.");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eSomething went wrong during the teleport! Is the world loaded in that location?");
				}
				perms.disposeIfPlayerNotOnline();
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {warpname}&e\" or&z&e\"&f/" + command + " {warpname} [target]&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("warps")) {
			if((args.length == 0) || ((args.length == 1) && (args[0].equalsIgnoreCase("list")))) {
				Main.sendMessage(sender, Main.pluginName + "&aListing all warps...");
				int i = 0;
				for(Warps.Warp warp : Warps.getAllWarps()) {
					Main.sendMessage(sender, "&f[&3" + i + "&f]: &6/warp " + warp.name);
					i++;
				}
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " [list]&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("delwarp")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.delwarp")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				String warpName = args[0];
				if((warpName.equalsIgnoreCase("undo")) && (user != null)) {
					Warps.Warp deletedWarp = status.lastDeletedWarp;
					if(deletedWarp != null) {
						Warps.registerWarp(deletedWarp);
						status.lastDeletedWarp = null;
						Main.sendMessage(user, Main.pluginName + "&aSuccessfully restored the deleted warp &6/warp " + deletedWarp.name + "&a.");
						deletedWarp = null;
					} else {
						Main.sendMessage(user, Main.pluginName + "&eYou have not deleted any warps recently, or it has been deleted permanently since that time.");
					}
					return true;
				}
				Warps.Warp warp = Warps.getWarpByName(warpName);
				if(warp == null) {
					Main.sendMessage(sender, Main.pluginName + "&eThe warp \"&f" + warpName + "&r&e\" does not exist.");
					return true;
				}
				warp.dispose();
				Main.sendMessage(sender, Main.pluginName + "&aWarp &6/warp " + warp.name + "&r&a has been deleted." + (user != null ? "&z&aType &f/delwarp undo&a to restore it before you log out or delete another warp, otherwise it will be gone for good." : ""));
				if(user != null) {
					status.lastDeletedWarp = warp;
				}
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {warpname}&e\"");
			return true;
		}
		if((command.equalsIgnoreCase("rankup")) || (command.equalsIgnoreCase("nextgroup"))) {
			if(!Main.handlePermissions) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandlePermissions&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if((!Permissions.hasPerm(sender, "supercmds.use.rankup")) && (!Permissions.hasPerm(sender, "supercmds.use.nextgroup"))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null) {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			if(args.length == 0) {
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
				if(perms.group != null) {
					if(perms.group.canRankup()) {
						if(!perms.group.nextGroup.moneyOrCredit.equals("NONE")) {
							if(perms.group.nextGroup.moneyOrCredit.equals("MONEY")) {
								if(perms.group.nextGroup.costToRankup > 0.0D) {
									PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
									if(eco.getBalance() < perms.group.nextGroup.costToRankup) {
										Main.sendMessage(user, Main.pluginName + "&eYou do not have enough &f" + Main.moneyTerm + "&r&e to rank up to the next group!&z&aYou need &f" + Main.decimal.format(perms.group.nextGroup.costToRankup - eco.getBalance()) + "&r&a more(the next rank costs &f" + Main.decimal.format(perms.group.nextGroup.costToRankup) + "&r&a)!");
										perms.disposeIfPlayerNotOnline();
										return true;
									}
									eco.setBalance(eco.getBalance() - perms.group.nextGroup.costToRankup);
									eco.saveToFile();
									Main.sendMessage(user, Main.pluginName + "&f" + Main.decimal.format(perms.group.nextGroup.costToRankup) + "&e &f" + Main.moneyTerm + "&r&e has been taken from your account.");
								}
							} else if(perms.group.nextGroup.moneyOrCredit.equals("CREDIT")) {
								int cost = new Double(Math.round(perms.group.nextGroup.costToRankup)).intValue();
								if(cost > 0) {
									PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
									if(eco.credits < cost) {
										Main.sendMessage(user, Main.pluginName + "&eYou do not have enough &fcredit&r&e to rank up to the next group!&z&aYou need &f" + (cost - eco.credits) + "&r&a more(the next rank requires at least &f" + cost + "&r&a " + Main.creditTerm + ")!");
										perms.disposeIfPlayerNotOnline();
										return true;
									}
									eco.credits -= cost;
									eco.saveToFile();
									Main.sendMessage(user, Main.pluginName + "&f" + cost + " " + Main.creditTerm + "&r&e have been taken from your account.&z&aYou now have &f" + eco.credits + " " + Main.creditTerm + "&a.");
								}
							}
						}
						if(perms.changeGroup(perms.group.nextGroup)) {
							String msg = Main.pluginName + Main.playerPromotedMessage.replace("PLAYERNAME", user.getDisplayName()).replace("GROUPNAME", perms.group.displayName);
							for(Player player : new ArrayList<>(Main.server.getOnlinePlayers())) {
								if(!player.getUniqueId().toString().equals(user.getUniqueId().toString())) {
									Main.sendMessage(player, msg);
								}
							}
							Main.sendConsoleMessage(msg);
							Main.sendMessage(user, Main.pluginName + "&aYou have been promoted to the \"&f" + perms.group.displayName + "&r&a\" rank!");
							perms.disposeIfPlayerNotOnline();
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when attempting to promote you to the next rank!&z&aPlease ask a server administrator politely for assistance.");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eYou are already in the highest rank possible!");
					perms.disposeIfPlayerNotOnline();
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eWhoops! It appears that you were never assigned to a group.&z&aPromoting you to the default group...");
				if(perms.changeGroup(PlayerPermissions.Group.getDefaultGroup())) {
					String msg = Main.pluginName + Main.playerPromotedMessage.replace("PLAYERNAME", user.getDisplayName()).replace("GROUPNAME", perms.group.displayName);
					for(Player player : new ArrayList<>(Main.server.getOnlinePlayers())) {
						if(!player.getUniqueId().toString().equals(user.getUniqueId().toString())) {
							Main.sendMessage(player, msg);
						}
					}
					Main.sendConsoleMessage(msg);
					Main.sendMessage(user, Main.pluginName + "&aYou have been promoted to the \"&f" + perms.group.displayName + "&r&a\" rank!");
					perms.disposeIfPlayerNotOnline();
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when attempting to promote you to the next rank!&z&aPlease ask a server administrator politely for assistance.");
				perms.disposeIfPlayerNotOnline();
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("mail")) {
			if(!Main.handleChat) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.mail")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				PlayerChat chat = PlayerChat.getPlayerChat(user);
				if((args.length == 0) || ((args.length >= 1) && (args.length <= 2) && ((args[0].equalsIgnoreCase("read")) || (args[0].equalsIgnoreCase("readold"))))) {
					if((args.length == 0) || ((args.length >= 1) && (args.length <= 2) && (args[0].equalsIgnoreCase("read")))) {
						if(!chat.mailbox.isEmpty()) {
							int maxIntLinesPerPage = 10;
							double maxLinesPerPage = 10.0D;
							Mail[] allMail = chat.getMailInOrder();
							if(allMail.length != 0) {
								int maxPages = CodeUtils.getIntFromStr(Main.decimalRoundUp.format(allMail.length / maxLinesPerPage), 0);
								int page = 0;
								if(args.length == 2) {
									if(!CodeUtils.isStrAValidInt(args[1])) {
										Main.sendMessage(user, Main.pluginName + "&eYou have entered an invalid page number(\"&f" + args[1] + "&r&e is not a valid integer\").");
										return true;
									}
									page = CodeUtils.getIntFromStr(args[1], page) - 1;
									if(page >= maxPages) {
										Main.sendMessage(user, Main.pluginName + "&eYou only have &f" + maxPages + "&e page" + (maxPages == 1 ? "" : "s") + " of mail.");
										return true;
									}
									if(page <= -1) {
										Main.sendMessage(user, Main.pluginName + "&ePage numbers must be greater than &fzero&e.");
										return true;
									}
								}
								int p = (int) (page * maxLinesPerPage);
								Main.sendMessage(user, Main.pluginName + "&6 ===[ Unread mail: Page &f" + (page + 1) + "&6 / &f" + maxPages + "&6 ]===");
								for(int i = p; i < allMail.length; i++) {
									if(i >= maxIntLinesPerPage * (page + 1)) {
										break;
									}
									Main.sendMessage(user, "&f[&3" + (i + 1) + "&f]: " + (allMail[i] != null ? allMail[i].toString() : "&cnull(&f?&c)&f"));
								}
								return true;
							}
							Main.sendMessage(user, Main.pluginName + "&eAn unusual circumstance has occurred that is preventing you from reading your mail.");
							Main.sendMessage(user, "&f(Reason: &b&lunable to organize mail by date&r&f)");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eYou have no unread mail!&z&aType &f/mail readold&a to see if you have any old mail.");
					} else if((args.length >= 1) && (args.length <= 2) && (args[0].equalsIgnoreCase("readold"))) {
						if(!chat.oldMail.isEmpty()) {
							double maxLinesPerPage = 10.0D;
							int maxIntLinesPerPage = 10;
							Mail[] allMail = chat.getOldMailInOrder();
							if(allMail.length != 0) {
								int maxPages = CodeUtils.getIntFromStr(Main.decimalRoundUp.format(allMail.length / maxLinesPerPage), 0);
								int page = 0;
								if(args.length == 2) {
									if(!CodeUtils.isStrAValidInt(args[1])) {
										Main.sendMessage(user, Main.pluginName + "&eYou have entered an invalid page number(\"&f" + args[1] + "&r&e is not a valid integer\").");
										return true;
									}
									page = CodeUtils.getIntFromStr(args[1], page) - 1;
									if(page >= maxPages) {
										Main.sendMessage(user, Main.pluginName + "&eYou only have &f" + maxPages + "&e page" + (maxPages == 1 ? "" : "s") + " of old mail.");
										return true;
									}
									if(page <= -1) {
										Main.sendMessage(user, Main.pluginName + "&ePage numbers must be greater than &fzero&e.");
										return true;
									}
								}
								int p = (int) (page * maxLinesPerPage);
								Main.sendMessage(user, Main.pluginName + "&6 ===[ Old mail: Page &f" + (page + 1) + "&6 / &f" + maxPages + "&6 ]===");
								for(int i = p; i < allMail.length; i++) {
									if(i >= maxIntLinesPerPage * (page + 1)) {
										break;
									}
									Main.sendMessage(user, "&f[&3" + (i + 1) + "&f]: " + (allMail[i] != null ? allMail[i].toString() : "&cnull(&f?&c)&f"));
								}
								return true;
							}
							Main.sendMessage(user, Main.pluginName + "&eAn unusual circumstance has occurred that is preventing you from reading your old mail.");
							Main.sendMessage(user, "&f(Reason: &b&lunable to organize old mail by date&r&f)");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eYou have no old mail!&z&aType &f/mail read&a to see if you have any new mail.");
					}
					return true;
				}
				if(args.length == 1) {
					if((args[0].equalsIgnoreCase("markread")) || (args[0].equalsIgnoreCase("clear"))) {
						HashMap<UUID, ArrayList<Mail>> mail = chat.mailbox;
						for(Map.Entry<UUID, ArrayList<Mail>> entry : mail.entrySet()) {
							ArrayList<Mail> oldMails = chat.oldMail.get(entry.getKey());
							for(Mail m : entry.getValue()) {
								if(oldMails == null) {
									oldMails = new ArrayList<>();
								}
								if(!oldMails.contains(m)) {
									oldMails.add(m);
								}
							}
							chat.oldMail.put(entry.getKey(), oldMails);
						}
						chat.mailbox.clear();
						chat.saveToFile();
						Main.sendMessage(user, Main.pluginName + "&aThe mail in your mailbox was marked as read and moved to your old mailbox for storage.&z&aTo read old mail, type &f/mail readold&a.");
						return true;
					}
				} else if(args.length >= 3) {
					String msg = Main.getElementsFromStringArrayAtIndexAsString(args, 2);
					if(args[0].equalsIgnoreCase("send")) {
						UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[1]);
						if(target == null) {
							Main.sendMessage(sender, Main.getNoPlayerMsg(args[1]));
							return true;
						}
						PlayerChat targetChat = PlayerChat.getPlayerChat(target);
						ArrayList<Mail> targetsMail = targetChat.mailbox.get(user.getUniqueId());
						if(targetsMail == null) {
							targetsMail = new ArrayList<>();
							targetChat.mailbox.put(user.getUniqueId(), targetsMail);
						}
						targetsMail.add(new Mail(user.getUniqueId(), msg, System.currentTimeMillis()));
						if(targetChat.isPlayerOnline()) {
							Main.sendMessage(targetChat.getPlayer(), Main.pluginName + "&6You have new mail!&z&aType &f/mail read&a to read it.");
						}
						targetChat.saveAndDisposeIfPlayerNotOnline();
						Main.sendMessage(user, Main.pluginName + "&aYour message was sent to \"&f" + targetChat.getDisplayName() + "&r&a\"'s inbox successfully.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eInvalid flag \"&f" + args[0] + "&r&e\".");
				}
			} else {
				if(sender == Main.console) {
					if(args.length >= 3) {
						String msg = Main.getElementsFromStringArrayAtIndexAsString(args, 2);
						if(args[0].equalsIgnoreCase("send")) {
							UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[1]);
							if(target == null) {
								Main.sendMessage(sender, Main.getNoPlayerMsg(args[1]));
								return true;
							}
							PlayerChat targetChat = PlayerChat.getPlayerChat(target);
							ArrayList<Mail> targetsMail = targetChat.mailbox.get(Main.consoleUUID);
							if(targetsMail == null) {
								targetsMail = new ArrayList<>();
								targetChat.mailbox.put(Main.consoleUUID, targetsMail);
							}
							targetsMail.add(new Mail(Main.consoleUUID, msg, System.currentTimeMillis()));
							if(targetChat.isPlayerOnline()) {
								Main.sendMessage(targetChat.getPlayer(), Main.pluginName + "&6You have new mail!&z&aType &f/mail read&a to read it.");
							}
							targetChat.saveAndDisposeIfPlayerNotOnline();
							Main.sendMessage(sender, Main.pluginName + "&aYour message was sent to \"&f" + targetChat.getDisplayName() + "&r&a\"'s inbox successfully.");
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + args[0] + "&r&e\".");
					}
					Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + "&r&f send {target} {args...}&e\"");
					return true;
				}
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f send {target} {args...}&e\" or&z&e\"&f/" + command + "&r&f read&e\" or&z&e\"&f/" + command + "&r&f {markread|clear}&e\" or&z&e\"&f/" + command + "&r&f readold&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("msg")) {
			if(!Main.handleChat) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if((user != null) || (sender == Main.console)) {
				String senderName = user != null ? "&f[&3" + user.getName() + "&f]" : Main.consoleSayFormat.trim();
				if(args.length >= 2) {
					String msg = Main.getElementsFromStringArrayAtIndexAsString(args, 1);
					UUID target = args[0].equalsIgnoreCase("console") ? Main.consoleUUID : Main.getPlayer(args[0]) != null ? Main.getPlayer(args[0]).getUniqueId() : null;
					String targetName = target != null ? (target.toString().equals(Main.consoleUUID.toString()) ? Main.consoleSayFormat.trim() : "&f[" + Main.uuidMasterList.getPlayerNameFromUUID(target) + "]") : "";
					if(target != null) {
						if((user != null) && (user.getUniqueId().toString().equals(target.toString()))) {
							Main.sendMessage(sender, Main.pluginName + "&aYou so silly.");
							return true;
						}
						Main.sendMessage(target.toString().equals(Main.consoleUUID.toString()) ? Main.console : Main.server.getPlayer(target), "&6{&f" + senderName + " &6--> me}&f: " + msg);
						Main.sendMessage(sender, "&6{me --> " + targetName + "&6}&f: " + msg);
						for(Player player : Main.server.getOnlinePlayers()) {
							PlayerStatus s = PlayerStatus.getPlayerStatus(player);
							if((Permissions.hasPerm(player, "supercmds.chat.socialspy")) && (s.isSocialSpyModeOn)) {
								Main.sendMessage(player, "&6{" + userName + " --> " + targetName + "&6}&f: " + msg);
							}
						}
						if(user != null) {
							PlayerChat chat = PlayerChat.getPlayerChat(user);
							chat.lastPlayerThatIRepliedTo = target;
							chat.saveToFile();
						}
						return true;
					}
					Main.sendMessage(sender, getNoPlayerMsg(args[0]));
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f {target} {args...}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("r")) {
			if(!Main.handleChat) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if((user != null) || (sender == Main.console)) {
				UUID me = user != null ? user.getUniqueId() : Main.consoleUUID;
				if((args.length == 2) && (args[0].equalsIgnoreCase("set"))) {
					UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[1]);
					String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
					if(args[1].equalsIgnoreCase("console")) {
						target = Main.consoleUUID;
						targetName = Main.consoleSayFormat.trim();
					}
					if(target != null) {
						PlayerChat chat = PlayerChat.getPlayerChat(me);
						chat.lastPlayerThatIRepliedTo = target;
						Main.sendMessage(sender, Main.pluginName + "&aYou will now automatically reply to \"&f" + targetName + "&a\" when using &f/" + command + "&r&a.");
						return true;
					}
					Main.sendMessage(sender, Main.getNoPlayerMsg(args[1]));
					return true;
				}
				if(args.length >= 1) {
					String msg = Main.getElementsFromStringArrayAtIndexAsString(args, 0);
					PlayerChat chat = PlayerChat.getPlayerChat(me);
					UUID t = chat.lastPlayerThatIRepliedTo;
					if(t == null) {
						Main.sendMessage(sender, Main.pluginName + "&eThere is no one to whom you can reply.&z&aStrike up a friendly conversation with &f/msg&a!");
						return true;
					}
					CommandSender target = t.toString().equals(Main.consoleUUID.toString()) ? Main.console : Main.server.getPlayer(t);
					if(target == null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe player you were chatting with is not online.");
						return true;
					}
					String targetName = t.toString().equals(Main.consoleUUID.toString()) ? "console" : target.getName();
					Main.server.dispatchCommand(sender, "msg " + targetName + " " + msg);
					return true;
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f {msg...}&e\" or&z&e\"&f/" + command + "&r&f set {target|clear}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("repair") || command.equalsIgnoreCase("fix")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.repair")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null) {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			ItemStack item = user.getInventory().getItemInMainHand();
			if(isItemMjölnir(item)) {
				Main.sendMessage(sender, "&f[&6Thor&f]: &bYond item can not beest did repair.");
				return true;
			}
			Material material = item.getType();
			final short maxDurability = material.getMaxDurability();
			short durability = item.getDurability();
			final short compareDurability = durability;
			//Main.sendMessage(sender, "Durability: " + durability + "; max: " + maxDurability);
			final String newItem = Main.pluginName + "&aYour '" + Main.getItemStackName(item) + "&r&a' is already good as new!";
			if(material == Material.SHEARS) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / 2);
				durability = findAndRemoveItemsForRepair(durability, increment, Material.IRON_INGOT, user);
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.FLINT_AND_STEEL) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / 2);
				durability = findAndRemoveItemsForRepair(durability, increment, Material.FLINT, user);
				item.setDurability(durability);
				if(durability > 0) {
					durability = findAndRemoveItemsForRepair(durability, increment, Material.IRON_INGOT, user);
					item.setDurability(durability);
				}
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.WOOD_SWORD || material == Material.WOOD_HOE || material == Material.WOOD_PICKAXE || material == Material.WOOD_SPADE || material == Material.WOOD_AXE) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.WOOD_SWORD || material == Material.WOOD_HOE ? 2 : (material == Material.WOOD_SPADE ? 1 : 3)));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.WOOD, user);
				item.setDurability(durability);
				if(durability > 0) {
					increment /= 4;//= (short) (increment / 4);
					durability = findAndRemoveItemsForRepair(durability, increment, Material.STICK, user);
					item.setDurability(durability);
				}
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.STONE_SWORD || material == Material.STONE_HOE || material == Material.STONE_PICKAXE || material == Material.STONE_SPADE || material == Material.STONE_AXE) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.STONE_SWORD || material == Material.STONE_HOE ? 2 : (material == Material.STONE_SPADE ? 1 : 3)));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.COBBLESTONE, user);
				item.setDurability(durability);
				if(durability > 0) {
					durability = findAndRemoveItemsForRepair(durability, increment, Material.STONE, user);
					item.setDurability(durability);
				}
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.IRON_SWORD || material == Material.IRON_HOE || material == Material.IRON_PICKAXE || material == Material.IRON_SPADE || material == Material.IRON_AXE) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.IRON_SWORD || material == Material.IRON_HOE ? 2 : (material == Material.IRON_SPADE ? 1 : 3)));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.IRON_INGOT, user);
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.GOLD_SWORD || material == Material.GOLD_HOE || material == Material.GOLD_PICKAXE || material == Material.GOLD_SPADE || material == Material.GOLD_AXE) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.GOLD_SWORD || material == Material.GOLD_HOE ? 2 : (material == Material.GOLD_SPADE ? 1 : 3)));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.GOLD_INGOT, user);
				item.setDurability(durability);
				if(durability > 0) {
					increment /= 9;//= (short) (increment / 9);
					durability = findAndRemoveItemsForRepair(durability, increment, Material.GOLD_NUGGET, user);
					item.setDurability(durability);
				}
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.DIAMOND_SWORD || material == Material.DIAMOND_HOE || material == Material.DIAMOND_PICKAXE || material == Material.DIAMOND_SPADE || material == Material.DIAMOND_AXE) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.DIAMOND_SWORD || material == Material.DIAMOND_HOE ? 2 : (material == Material.DIAMOND_SPADE ? 1 : 3)));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.DIAMOND, user);
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.BOW) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / 3);
				durability = findAndRemoveItemsForRepair(durability, increment, Material.STICK, user);
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.LEATHER_BOOTS || material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_CHESTPLATE || material == Material.LEATHER_HELMET) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.LEATHER_CHESTPLATE ? 9 : (material == Material.LEATHER_LEGGINGS ? 7 : (material == Material.LEATHER_HELMET ? 5 : 4))));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.LEATHER, user);
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.IRON_BOOTS || material == Material.IRON_LEGGINGS || material == Material.IRON_CHESTPLATE || material == Material.IRON_HELMET) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.IRON_CHESTPLATE ? 9 : (material == Material.IRON_LEGGINGS ? 7 : (material == Material.IRON_HELMET ? 5 : 4))));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.IRON_INGOT, user);
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.GOLD_BOOTS || material == Material.GOLD_LEGGINGS || material == Material.GOLD_CHESTPLATE || material == Material.GOLD_HELMET) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.GOLD_CHESTPLATE ? 9 : (material == Material.GOLD_LEGGINGS ? 7 : (material == Material.GOLD_HELMET ? 5 : 4))));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.GOLD_INGOT, user);
				item.setDurability(durability);
				if(durability > 0) {
					increment /= 9;//= (short) (increment / 9);
					durability = findAndRemoveItemsForRepair(durability, increment, Material.GOLD_NUGGET, user);
					item.setDurability(durability);
				}
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.DIAMOND_BOOTS || material == Material.DIAMOND_LEGGINGS || material == Material.DIAMOND_CHESTPLATE || material == Material.DIAMOND_HELMET) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / (material == Material.DIAMOND_CHESTPLATE ? 9 : (material == Material.DIAMOND_LEGGINGS ? 7 : (material == Material.DIAMOND_HELMET ? 5 : 4))));
				durability = findAndRemoveItemsForRepair(durability, increment, Material.DIAMOND, user);
				item.setDurability(durability);
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else if(material == Material.SHIELD) {
				if(durability == 0) {
					Main.sendMessage(sender, newItem);
					return true;
				}
				short increment = (short) (maxDurability / 4);
				durability = findAndRemoveItemsForRepair(durability, increment, Material.IRON_INGOT, user);
				item.setDurability(durability);
				if(durability > 0) {
					increment = (short) (maxDurability / 6);
					durability = findAndRemoveItemsForRepair(durability, increment, Material.WOOD, user);
					item.setDurability(durability);
					if(durability > 0) {
						increment /= 4;//= (short) (increment / 4);
						durability = findAndRemoveItemsForRepair(durability, increment, Material.STICK, user);
						item.setDurability(durability);
					}
				}
				GamemodeInventory.updateInventoryNatively(user.getInventory());
				status.updateSavedInventoryFromPlayer(true, true);
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eYour '" + Main.getItemStackName(item) + "&r&e' doesn't appear to be a repairable item.");
				return true;
			}
			if(durability != compareDurability) {
				Main.sendMessage(sender, Main.pluginName + "&aYour '" + Main.getItemStackName(item) + "&r&a' was patched up" + (durability == maxDurability ? " all the way" : "") + "!");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eNone of the items in your inventory matched the material type of your '" + Main.getItemStackName(item) + "&r&e'!");
			}
			return true;
		}
		if(command.equalsIgnoreCase("kit") || (command.equalsIgnoreCase("kits"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.kit")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null && ((args.length == 1 && args[0].equalsIgnoreCase("list")) || command.equalsIgnoreCase("kits"))) {
				ArrayList<Kits.Kit> kits = Kits.getAllKits();
				if(kits.size() > 0) {
					Main.sendMessage(sender, Main.pluginName + "&3Listing all kits:");
					int i = 0;
					for(Kits.Kit kit : kits) {
						Main.sendMessage(sender, "&f[&3" + i + "&f]: " + kit.toString());
						i++;
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThere are no kits created at this time.&z&aCreate some with &f/managekits {name} create&a!");
				}
				return true;
			}
			if(user == null) {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			if((args.length == 0) || ((args.length == 1) && (args[0].equalsIgnoreCase("list")))) {
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
				ArrayList<Kits.Kit> kitsForUser = new ArrayList<>();
				ArrayList<Kits.Kit> allKits = Kits.getAllKits();
				if(allKits.size() == 0) {
					Main.sendMessage(user, Main.pluginName + "&eThere are no kits set up at this time. Please try again later!");
					return true;
				}
				boolean iCanHasKit;
				for(Kits.Kit kit : allKits) {
					iCanHasKit = true;
					if((kit.requiredGroup != null) && (Main.handlePermissions) && (!perms.isAMemberOfGroup(kit.requiredGroup))) {
						iCanHasKit = false;
					}
					
					if((kit.requiredPermission != null) && (!kit.requiredPermission.isEmpty()) && (!perms.hasPermission(kit.requiredPermission))) {
						iCanHasKit = false;
					}
					
					if(iCanHasKit) {
						kitsForUser.add(kit);
					}
				}
				if(kitsForUser.size() == 0) {
					Main.sendMessage(user, Main.pluginName + "&eThere are no kits available for you at this time. Please try again later or try ranking up!");
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&aYou have access to the following kits at this time:");
				int i = 0;
				for(Kits.Kit kit : kitsForUser) {
					Main.sendMessage(user, Main.pluginName + "&f[&3" + i + "&f]: &6/kit " + kit.name);
					i++;
				}
				return true;
			}
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("claimItems")) {
					if(status.leftOverItems.isEmpty()) {
						Main.sendMessage(user, Main.pluginName + "&eYou have no unclaimed items saved!");
						return true;
					}
					ItemStack[] items = new ItemStack[status.leftOverItems.size()];
					for(int i = 0; i < items.length; i++) {
						items[i] = (status.leftOverItems.get(i));
					}
					status.leftOverItems.clear();
					HashMap<Integer, ItemStack> leftovers = user.getInventory().addItem(items);
					if(leftovers.size() > 0) {
						for(Map.Entry<Integer, ItemStack> entry : leftovers.entrySet()) {
							status.leftOverItems.add(entry.getValue());
						}
						Main.sendMessage(user, Main.pluginName + "&aYour inventory ran out of room while the plugin was giving you leftover items, so the items were re-saved for you.");
						Main.sendMessage(user, "&aType &f/kit claimitems&a again to claim them when you have more room.");
					}
					return true;
				}
				Kits.Kit kit = Kits.getKitByName(args[0]);
				if(kit != null) {
					String time;
					if(!status.canGetKit(kit)) {
						long futureTime = status.lastKitTimes.get(kit.name).longValue() + kit.obtainInterval * 1000L;
						time = Main.getTimeAndDate(futureTime);
						Main.sendMessage(user, Main.pluginName + "&eYou can't get that kit again until &f" + time + "&r&e!");
						Main.sendMessage(user, Main.pluginName + "&a(The time is currently &f" + Main.getTimeAndDate(System.currentTimeMillis()) + "&r&a).");
						return true;
					}
					if((Main.handlePermissions) && (kit.requiredGroup != null)) {
						PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
						if(!perms.isAMemberOfGroup(kit.requiredGroup)) {
							Main.sendMessage(user, Main.pluginName + "&eYou are not a member of that kit's required group(\"&f" + kit.requiredGroup.displayName + "&r&e\")!");
							return true;
						}
					}
					
					if((kit.requiredPermission != null) && (!kit.requiredPermission.isEmpty())) {
						PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
						if(!perms.hasPermission(kit.requiredPermission)) {
							Main.sendMessage(user, Main.pluginName + "&eYou do not have the permission that this kit requires" + (user.isOp() ? "(\"&b" + kit.requiredPermission + "&r&e\")" : "") + "!");
							return true;
						}
					}
					status.lastKitTimes.put(kit.name, new Long(System.currentTimeMillis()));
					if(kit.rewardMoney > 0.0D) {
						PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
						eco.setBalance(eco.getBalance() + kit.rewardMoney);
						Main.sendMessage(user, Main.pluginName + "&f" + Main.decimal.format(kit.rewardMoney) + "&r&a has been added to your account.");
					}
					if(kit.items.length >= 1) {
						HashMap<Integer, ItemStack> leftovers = user.getInventory().addItem(kit.items);
						if(leftovers.size() > 0) {
							for(Map.Entry<Integer, ItemStack> entry : leftovers.entrySet()) {
								status.leftOverItems.add(entry.getValue());
							}
							Main.sendMessage(user, Main.pluginName + "&aYour inventory ran out of room while the kit was giving you items, so the items were saved for you.");
							Main.sendMessage(user, "&aType &f/kit claimitems&a to claim them when you have more room.");
						}
					} else if(kit.rewardMoney <= 0.0D) {
						Main.sendMessage(user, Main.pluginName + "&eThis kit doesn't appear to have any items or rewards set yet...&z&aPlease let a server staff member know so that they can fix it!");
					}
					
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eThe kit \"&f" + args[0] + "&r&e\" does not exist.");
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [kitName]&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("realtime") || command.equalsIgnoreCase("rt")) {
			Main.sendMessage(sender, Main.pluginName + "&aThe time is currently &f" + Main.getTimeAndDate(System.currentTimeMillis()) + "&r&a.");
			return true;
		}
		if(command.equalsIgnoreCase("vanish") || command.equalsIgnoreCase("v")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.vanish")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length != 0) {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
					return true;
				}
				status.isVanishModeOn = (!status.isVanishModeOn);
				status.lastVanishTime = (status.isVanishModeOn ? System.currentTimeMillis() : -1L);
				Main.sendMessage(user, Main.pluginName + "&aYou are now " + (status.isVanishModeOn ? "&2completely invisible&a to other players. Server operators and players with special permissions may still be able to see you." : "&fvisible&a to other players."));
				PlayerStatus.updatePlayerStateStates();
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("smite")) {
			Main.DEBUG("&fSmite sender class: &3" + sender.getClass().getName());
			if((sender.getClass().getSimpleName().equals("CraftCommandBlock")) && (args.length != 4)) {
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.smite")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				Player target = Main.getPlayer(args[0]);
				if(target == null) {
					Main.sendMessage(sender, getNoPlayerMsg(args[0]));
					return true;
				}
				target.getWorld().strikeLightning(target.getLocation());
				Main.sendMessage(target, Main.pluginName + "&eThou hast been smited!");
				Main.sendMessage(sender, Main.pluginName + "&aYou just smited \"&f" + target.getDisplayName() + "&r&a\".&z&aI hope they deserved it... XD");
				return true;
			}
			if(args.length == 4) {
				World world = Main.server.getWorld(args[0]);
				boolean X = CodeUtils.isStrAValidDouble(args[1]);
				boolean Y = CodeUtils.isStrAValidDouble(args[2]);
				boolean Z = CodeUtils.isStrAValidDouble(args[3]);
				double x = CodeUtils.getDoubleFromStr(args[1], Double.NaN);
				double y = CodeUtils.getDoubleFromStr(args[2], Double.NaN);
				double z = CodeUtils.getDoubleFromStr(args[3], Double.NaN);
				if(world != null) {
					if((X) && (Y) && (Z)) {
						Location loc = new Location(world, x, y, z);
						world.strikeLightning(loc);
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eThe coordinates you entered are not valid(or the plugin failed to descipher them): \"&fx: " + args[0] + ", y: " + args[1] + ", z: " + args[2] + "; " + "&r&e\".");
					Main.sendMessage(sender, "&eCheck your typing and try again.");
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eThe world \"&f" + args[0] + "&r&e\" does not exist.");
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\" or&z&e\"&f/" + command + " {worldName} {x} {y} {z}&e\"");
			return true;
		}
		if((command.equalsIgnoreCase("managekit")) || (command.equalsIgnoreCase("managekits"))) {
			if((!Permissions.hasPerm(sender, "supercmds.use.managekit")) && (!Permissions.hasPerm(sender, "supercmds.use.managekits"))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("list")) {
					ArrayList<Kits.Kit> kits = Kits.getAllKits();
					if(kits.size() != 0) {
						Main.sendMessage(sender, Main.pluginName + "&aListing all kits:");
						int i = 0;
						for(Kits.Kit kit : kits) {
							Main.sendMessage(sender, Main.pluginName + "&f[" + i + "]: " + kit.toString());
							i++;
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&aThere are no kits created at this time.&z&aCreate some with &6/managekits&a!");
					}
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + args[1] + "&r&e\".");
			} else if(args.length == 2) {
				if(args[1].equalsIgnoreCase("create")) {
					Kits.Kit check = Kits.getKitByName(args[0]);
					if(check != null) {
						Main.sendMessage(sender, Main.pluginName + "&eA kit with that name already exists!");
						return true;
					}
					Kits.Kit kit = Kits.createKit(args[0]);
					Kits.getInstance().saveToFile();
					Main.sendMessage(sender, Main.pluginName + "&aSuccessfully created the kit &6/kit " + kit.name + "&r&a!");
					return true;
				}
				if(args[1].equalsIgnoreCase("delete")) {
					Kits.Kit kit = Kits.getKitByName(args[0]);
					if(kit == null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe kit \"&f" + args[0] + "&r&e\" does not exist.&z&aPerhaps it was already deleted?");
						return true;
					}
					kit.dispose();
					Kits.getInstance().saveToFile();
					if(user != null) {
						status.lastDeletedKit = kit;
					}
					Main.sendMessage(sender, Main.pluginName + "&aSuccessfully deleted the kit &6/kit " + kit.name + "&r&a." + (user == null ? "" : new StringBuilder("&z&a(If you did not mean to delete this kit, you can type &f/").append(command).append("&r&f ").append(kit.name).append("&r&f undodelete&a\", but if you log out before doing so, the kit will be &cun-retrievable&a.)").toString()));
					return true;
				}
				if(args[1].equalsIgnoreCase("undodelete")) {
					if(user == null) {
						Main.sendMessage(sender, Main.getPlayerOnlyMsg());
						return true;
					}
					Kits.Kit deletedKit = status.lastDeletedKit;
					if(deletedKit == null) {
						Main.sendMessage(sender, Main.pluginName + "&eYou have not deleted any kits during this login session.");
						return true;
					}
					Kits.Kit check = Kits.getKitByName(deletedKit.name);
					if(check != null) {
						Main.sendMessage(user, Main.pluginName + "&eA kit with the same name as the one you deleted already exists!&z&aDid someone re-create it?");
						return true;
					}
					Kits.registerKit(deletedKit);
					Kits.getInstance().saveToFile();
					Main.sendMessage(user, Main.pluginName + "&aSuccessfully restored the kit &6/kit " + deletedKit.name + "&r&a.&z&aBe careful!");
					return true;
				}
				if(args[1].equalsIgnoreCase("info")) {
					Kits.Kit kit = Kits.getKitByName(args[0]);
					if(kit == null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe kit \"&f" + args[0] + "&r&e\" does not exist.");
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&3Listing information about kit &6/kit " + kit.name + "&3:");
					Main.sendMessage(sender, kit.toString());
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + args[1] + "&r&e\".");
			} else if(args.length >= 4) {
				Kits.Kit kit = Kits.getKitByName(args[0]);
				String flag = args[1];
				String rewardItems = args[2];
				String value = Main.getElementsFromStringArrayAtIndexAsString(args, 3);
				if(kit != null) {
					if(flag.equalsIgnoreCase("set")) {
						if((rewardItems.equalsIgnoreCase("reward")) && (args.length == 4)) {
							if(CodeUtils.isStrAValidDouble(value)) {
								double amount = CodeUtils.getDoubleFromStr(value, 0.0D);
								kit.rewardMoney = amount;
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set the kit &6/kit " + kit.name + "&r&a's reward money amount to &f" + Main.decimal.format(amount) + "&r&f " + Main.moneyTerm + "&r&a!");
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid amount given: \"&f" + value + "&r&e\".");
							return true;
						}
						if(rewardItems.equalsIgnoreCase("items")) {
							if(value.equalsIgnoreCase("clearitems")) {
								kit.items = new ItemStack[0];
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully cleared out the kit &6/kit " + kit.name + "&r&a's items.&z&aBe sure to add some back in!");
								return true;
							}
							ArrayList<ItemStack> madeItems = getItemStacksFromString(value);
							if(madeItems == null) {
								Main.sendMessage(sender, Main.pluginName + "&eYou have entered an invalid itemstack data tag: \"&f" + Main.stringArrayToString(args, ' ', 3) + "&r&e\".");
								return true;
							}
							if(madeItems.isEmpty()) {
								Main.sendMessage(sender, Main.pluginName + "&eYou have entered an invalid itemstack: \"&f" + value + "&r&e\"");
								Main.sendMessage(sender, Main.pluginName + "&aItemStack example: &fminecraft:diamond_ore 64 0&a would result in 64 diamond ore blocks in one stack.&z&aThe zero is the metadata/damage value.&z&aThe \"&fminecraft:&r&a\" part is optional.&z&z&aYou can enter multiple stacks at once by separating them with a tilde('~') followed by a semi-colon(';'). (Separator: '~;')");
								return true;
							}
							try {
								ArrayList<ItemStack> existingItems = new ArrayList<>(Arrays.asList(kit.items));
								existingItems.addAll(madeItems);
								ItemStack[] items = new ItemStack[existingItems.size()];
								int i = 0;
								for(ItemStack it : existingItems) {
									items[i] = it;
									i++;
								}
								kit.items = items;
								Kits.getInstance().saveToFile();
								if(madeItems.size() > 0) {
									Main.sendMessage(sender, Main.pluginName + "&aSuccessfully added the itemstack" + (madeItems.size() == 1 ? "" : "s") + " to the kit &6/kit " + kit.name + "&r&a!");
								}
							} catch(Throwable e) {
								Main.sendMessage(sender, Main.pluginName + "&eAn error occurred when trying to add the itemstack" + (madeItems.size() == 1 ? "" : "s") + " you provided to the kit!&z&aPlease let a server administrator know so that they can go see the stack trace in the log.&z&aThe error was: \"&c" + e.getMessage() + "&a\".");
								Main.sendConsoleMessage(Main.pluginName + "&eAn error occurred when CommandSender \"&f" + userName + "&r&e\" tried to add itemstack" + (madeItems.size() == 1 ? "" : "s") + "&z&eto the kit \"&6/kit " + kit.name + "&e\":&z&c" + Main.throwableToStr(e) + "&z&eThe sender's input string was: \"&f" + value + "&r&e\"...");
							}
							return true;
						}
						if((rewardItems.equalsIgnoreCase("requiredgroup")) && (args.length == 4)) {
							if((!Permissions.hasPerm(sender, "supercmds.use.managekit.require")) && (!Permissions.hasPerm(sender, "supercmds.use.managekits.require"))) {
								Main.sendMessage(sender, Main.pluginName + Main.noPerm);
								return true;
							}
							if(value.equalsIgnoreCase("clear")) {
								kit.requiredGroup = null;
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully removed the required group from the kit &6/kit " + kit.name + "&r&a!");
								return true;
							}
							PlayerPermissions.Group group = PlayerPermissions.Group.getGroupByName(value);
							if(group != null) {
								kit.requiredGroup = group;
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set the kit &6/kit " + kit.name + "&r&a's required group to \"&f" + kit.requiredGroup.displayName + "&r&a\"!");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + value + "&r&e\" does not exist.");
							}
							return true;
						}
						if((rewardItems.equalsIgnoreCase("requiredperm")) && (args.length == 4)) {
							if((!Permissions.hasPerm(sender, "supercmds.use.managekit.require")) && (!Permissions.hasPerm(sender, "supercmds.use.managekits.require"))) {
								Main.sendMessage(sender, Main.pluginName + Main.noPerm);
								return true;
							}
							if(value.equalsIgnoreCase("clear")) {
								kit.requiredPermission = null;
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully removed the required permission from the kit &6/kit " + kit.name + "&r&a!");
								return true;
							}
							kit.requiredPermission = value;
							Kits.getInstance().saveToFile();
							Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set the kit &6/kit " + kit.name + "&r&a's required permission to \"&f" + kit.requiredPermission + "&r&a\"!");
							return true;
						}
						if((rewardItems.equalsIgnoreCase("waittime")) && (args.length == 4)) {
							if((!Permissions.hasPerm(sender, "supercmds.use.managekit.require")) && (!Permissions.hasPerm(sender, "supercmds.use.managekits.require"))) {
								Main.sendMessage(sender, Main.pluginName + Main.noPerm);
								return true;
							}
							if(!CodeUtils.isStrAValidLong(value)) {
								Main.sendMessage(sender, Main.pluginName + "&eYou have entered an invalid amount(\"&f" + value + "&r&e\" is not a valid long value).");
								return true;
							}
							long waitTimeInSeconds = CodeUtils.getLongFromStr(value, 0L);
							if(waitTimeInSeconds < 0L) {
								Main.sendMessage(sender, Main.pluginName + "&eKit wait times cannot be negative.");
								return true;
							}
							kit.obtainInterval = waitTimeInSeconds;
							Kits.getInstance().saveToFile();
							Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set the kit &6/kit " + kit.name + "&r&a's wait time to &f" + kit.obtainInterval + "&r&a seconds!");
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + rewardItems + "&r&e\".");
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThe kit \"&f" + args[0] + "&r&e\" does not exist.");
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {name} {create|delete|undodelete|info}&e\" or&z&e\"&f/" + command + " {name} {set} {reward|items|requiredgroup|requiredperm|waittime} {amount|items.../clearitems|groupName/permNode/clear}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("loadplugin")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.loadplugin")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if((args.length == 1) || ((args.length == 2) && (args[0].equalsIgnoreCase("-fullpath")))) {
				File file = args.length == 1 ? new File(Main.dataFolder.getParentFile(), args[0]) : new File(args[1]);
				if(!file.exists()) {
					Main.sendMessage(sender, Main.pluginName + "&eThe file \"&f" + file.getAbsolutePath() + "&r&e\" does not exist.");
					return true;
				}
				try {
					Plugin plugin = Main.server.getPluginManager().loadPlugin(file);
					if(plugin != null) {
						Main.sendMessage(sender, Main.pluginName + "&aThe plugin \"&f" + plugin.getName() + "&r&a\" was loaded successfully!");
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe file you provided is not a valid plugin(or Bukkit failed to load it).");
					}
					return true;
				} catch(Throwable e) {
					Main.sendMessage(sender, Main.pluginName + "&eAn error occurred while loading the plugin:&z&c" + Main.throwableToStr(e));
					return true;
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {pluginFile.jar}&e\" or&z&e\"&f/" + command + " {-fullpath} {full/path/to/pluginFile.jar}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("enableplugin")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.enableplugin")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				Plugin plugin = null;
				for(Plugin p : Main.server.getPluginManager().getPlugins()) {
					if(p.getName().equalsIgnoreCase(args[0])) {
						plugin = p;
						break;
					}
				}
				if(plugin == null) {
					Main.sendMessage(sender, Main.pluginName + "&eThere are no plugins loaded with the name \"&f" + args[0] + "&r&e\".");
					return true;
				}
				if(plugin.isEnabled()) {
					Main.sendMessage(sender, Main.pluginName + "&eThe plugin \"&f" + plugin.getName() + "&r&e\" is already enabled.");
					return true;
				}
				Main.server.getPluginManager().enablePlugin(plugin);
				Main.sendMessage(sender, Main.pluginName + "&aThe plugin was told to enable successfully.&z&aIt may take a while to boot/load completely.");
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {pluginName}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("disableplugin")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.disableplugin")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				Plugin plugin = null;
				for(Plugin p : Main.server.getPluginManager().getPlugins()) {
					if(p.getName().equalsIgnoreCase(args[0])) {
						plugin = p;
						break;
					}
				}
				if(plugin == null) {
					Main.sendMessage(sender, Main.pluginName + "&eThere are no plugins loaded with the name \"&f" + args[0] + "&r&e\".");
					return true;
				}
				if(!plugin.isEnabled()) {
					Main.sendMessage(sender, Main.pluginName + "&eThe plugin \"&f" + plugin.getName() + "&r&e\" is already disabled.");
					return true;
				}
				Main.server.getPluginManager().disablePlugin(plugin);
				Main.sendMessage(sender, Main.pluginName + "&aThe plugin was told to disable successfully.&z&aIf it is still enabled then it may still be shutting down etc.");
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {pluginName}&e\"");
			return true;
		}
		if((command.equalsIgnoreCase("mute")) || (command.equalsIgnoreCase("unmute"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.mute")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			boolean mute = command.equalsIgnoreCase("mute");
			if(args.length == 1) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target != null) {
					PlayerChat chat = PlayerChat.getPlayerChat(target);
					if((mute) && (chat.isMuted())) {
						Main.sendMessage(sender, Main.pluginName + "&eThat player is already muted.&z&eYou must &f/unmute&e them first before making changes.");
					} else if((!mute) && (!chat.isMuted())) {
						Main.sendMessage(sender, Main.pluginName + "&eThat player is not muted.");
					} else if(mute) {
						if(chat.setMuted(true, -1L)) {
							if(chat.isPlayerOnline()) {
								Main.sendMessage(chat.getPlayer(), Main.pluginName + "&eYou have just been &4muted&e by &f" + userName + "&r&e.&z&eIf you think that this was a mistake, &f/msg&e them or a server administrator about the issue.&z&eOtherwise, please treat the chat with &crespect&e.&z&eDon't curse, spam, hate on others, etc.");
							}
							Main.sendMessage(sender, Main.pluginName + "&aYou have just muted &f" + targetName + "&r&a.&z&aHope they deserved it!");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eUnable to mute player &f" + targetName + "&r&e.&z&aPerhaps they were already muted(or have the &fsupercmds.mute.exempt&a permission)?");
						}
					} else if(chat.setMuted(false, 0L)) {
						if(chat.isPlayerOnline()) {
							Main.sendMessage(chat.getPlayer(), Main.pluginName + "&aYou have just been &2unmuted&e by &f" + userName + "&r&a.&z&ePlease remember that using the chat is a privelage.");
						}
						Main.sendMessage(sender, Main.pluginName + "&aYou have just unmuted &f" + targetName + "&r&a.");
					} else {
						Main.sendMessage(sender, Main.pluginName + "&cUnable to un-mute player &f" + targetName + "&r&c.&z&eAre they already un-muted?");
					}
					
					chat.saveAndDisposeIfPlayerNotOnline();
					return true;
				}
				Main.sendMessage(sender, Main.getNoPlayerMsg(args[0]));
				return true;
			}
			if(args.length == 2) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target != null) {
					Long millis = getMilliSecondsFromString(args[1], true);
					if(millis == null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe number you entered(\"&f" + args[1] + "&r&e\") is not in the correct format.&z&aPlease enter numbers in the following format:&z&f42 3s 1m 2h 6d &a(where no letter or an s depicts a second, &fm&a for minutes, &fh&a for hours, and &fd&a for days.)");
						return true;
					}
					if(millis.longValue() <= 0L) {
						Main.sendMessage(sender, Main.pluginName + "&eMute timeout values must be greater than zero.");
						return true;
					}
					PlayerChat chat = PlayerChat.getPlayerChat(target);
					if((mute) && (chat.isMuted())) {
						Main.sendMessage(sender, Main.pluginName + "&eThat player is already muted.&z&eYou must &f/unmute&e them first before making changes.");
					} else if((!mute) && (!chat.isMuted())) {
						Main.sendMessage(sender, Main.pluginName + "&eThat player is not muted.");
					} else if(mute) {
						if(chat.setMuted(true, millis.longValue())) {
							if(chat.isPlayerOnline()) {
								Main.sendMessage(chat.getPlayer(), Main.pluginName + "&eYou have just been &4muted&e by &f" + userName + "&r&e.&z&eIf you think that this was a mistake, &f/msg&e them or a server administrator about the issue.&z&eOtherwise, please treat the chat with &crespect&e.&z&eDon't curse, spam, hate on others, etc.");
							}
							Main.sendMessage(sender, Main.pluginName + "&aYou have just muted &f" + targetName + "&r&a.&z&aHope they deserved it!&z&aP.S. You could just type \"&f/" + command + " " + args[0] + "&r&a\" instead of providing the time argument when unmuting a player, the time argument is not used in this case.");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eUnable to mute player &f" + targetName + "&r&e.&z&aPerhaps they were already muted(or have the &fsupercmds.mute.exempt&a permission)?");
						}
					} else if(chat.setMuted(false, 0L)) {
						if(chat.isPlayerOnline()) {
							Main.sendMessage(chat.getPlayer(), Main.pluginName + "&aYou have just been &2unmuted&e by &f" + userName + "&r&a.&z&ePlease remember that using the chat is a privelage.");
						}
						Main.sendMessage(sender, Main.pluginName + "&aYou have just unmuted &f" + targetName + "&r&a");
					} else {
						Main.sendMessage(sender, Main.pluginName + "&cUnable to un-mute player &f" + targetName + "&r&c.&z&eAre they already un-muted?");
					}
					
					chat.saveAndDisposeIfPlayerNotOnline();
					return true;
				}
				Main.sendMessage(sender, Main.getNoPlayerMsg(args[0]));
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName/targetUUID} [muteTime]&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("seen")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.seen")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				if(target != null) {
					final long now = System.currentTimeMillis();
					PlayerStatus s = PlayerStatus.getPlayerStatus(target);
					if((!s.isPlayerOnline()) || (s.isVanishModeOn)) {
						Long lastTimeSeen = s.isVanishModeOn ? Long.valueOf(s.lastVanishTime) : s.getLastLogoutTime();
						if(lastTimeSeen != null) {
							Main.sendMessage(sender, Main.pluginName + "&aThe player \"&f" + s.getPlayerNickName() + "&r&a\" was last seen \"&f" + Main.getLengthOfTime(now - lastTimeSeen.longValue()) + "&r&a\" ago.");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eThe player \"&f" + s.getPlayerNickName() + "&r&e\" has not yet been on this server(or at least not since this plugin was installed).");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&aPlayer &f" + s.getPlayerNickName() + "&r&a has been online for &f" + Main.getLengthOfTime(now - s.loginTime) + "&a.");
					}
				}
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName/targetUUID}&e\"");
			return true;
		}
		if((command.equalsIgnoreCase("staffchat")) || (command.equalsIgnoreCase("sc"))) {
			if((!Permissions.hasPerm(sender, "supercmds.use.staffchat")) && ((user == null) || (!Main.isPlayerAStaffMember(user)))) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			String displayName = PlayerChat.getDisplayName(user);
			for(Player player : Main.server.getOnlinePlayers()) {
				if((Permissions.hasPerm(player, "supercmds.use.staffchat")) || (Main.isPlayerAStaffMember(player))) {
					Main.sendMessage(player, displayName + ": " + strArgs);
				}
			}
			return true;
		}
		if(command.equalsIgnoreCase("rules")) {
			final boolean hasESP = Main.isESPPresent();
			ArrayList<String> rules = new ArrayList<>();
			rules.add("No spamming or advertising");
			rules.add("No cursing or hate speech");
			rules.add("No mods or hacks, please");
			rules.add("No scamming");
			rules.add("Be respectful and polite");
			rules.add("Treat others as you would have them treat you");
			if(hasESP) {
				rules.add("No island farming(creating a new island, saving");
				rules.add("-the resources from it, rinse, repeat)");
				rules.add("-This will likely get you banned for up to 6 months.");
			}
			rules.add("Use common sense, and have fun!");
			Main.sendMessage(sender, Main.pluginName + "&aRules:");
			int i = 1;
			for(String rule : rules) {
				if(rule.startsWith("-")) {
					Main.sendMessage(sender, "&a     " + rule.substring(1));
				} else {
					Main.sendMessage(sender, "&f[&3" + i + "&f]: &a" + rule);
					i++;
				}
			}
			if(status != null) {
				status.hasReadRules = true;
			}
			Main.sendMessage(sender, Main.pluginName + (hasESP ? "&aType &f/acceptrules&a if you accept the rules.&z" : "") + "&aBreaking the rules repeatedly may result in anything&z&afrom(but is not limited to) a temp ban" + (hasESP ? ", having your island reset" : "") + ",&z&aa permaban, privilages being revoked, etc.");
			return true;
		}
		if(command.equalsIgnoreCase("ping")) {
			Main.sendMessage(sender, "&6Pong!");
			return true;
		}
		if(command.equalsIgnoreCase("socialspy")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.socialspy")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 0) {
					status.isSocialSpyModeOn = (!status.isSocialSpyModeOn);
					Main.sendMessage(user, Main.pluginName + "&aSet SocialSpy mode " + (status.isSocialSpyModeOn ? "&2on" : "&3off") + "&a.");
					status.saveToFile();
					return true;
				}
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("on")) {
						status.isSocialSpyModeOn = true;
						Main.sendMessage(user, Main.pluginName + "&aSet SocialSpy mode " + (status.isSocialSpyModeOn ? "&2on" : "&3off") + "&a.");
						status.saveToFile();
						return true;
					}
					if(args[0].equalsIgnoreCase("off")) {
						status.isSocialSpyModeOn = false;
						Main.sendMessage(user, Main.pluginName + "&aSet SocialSpy mode " + (status.isSocialSpyModeOn ? "&2on" : "&3off") + "&a.");
						status.saveToFile();
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + args[0] + "&r&e\".");
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " [on|off]&e\"");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		}
		if(command.equalsIgnoreCase("ipban") || command.equalsIgnoreCase("banip")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.ipban") && !Permissions.hasPerm(sender, "supercmds.use.banip")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {playername}&e\"");
			return true;
		}
		if(command.equalsIgnoreCase("z")) {
			if(Permissions.hasPerm(sender, "supercmds.use.z")) {
				Main.broadcast(strArgs);
				return true;
			}
		}
		return false;
	}
	
	private static final short findAndRemoveItemsForRepair(short durability, short increment, Material material, Player user) {
		final PlayerInventory inv = user.getInventory();
		for(int i = 0; i < inv.getSize(); i++) {
			final ItemStack check = inv.getItem(i);
			if(durability < 0) {
				durability = 0;
				break;
			}
			if(check != null && check.getType() == material) {
				if(check.hasItemMeta()) {
					//if(check.getItemMeta().hasEnchants() || check.getItemMeta().hasDisplayName() || check.getItemMeta().hasLore()) {
					continue;
					//}
				}
				if(check.getAmount() > 1) {
					while(check.getAmount() > 1) {
						check.setAmount(check.getAmount() - 1);
						durability -= increment;
						if(durability < 0) {
							durability = 0;
							break;
						}
					}
					if(durability == 0) {
						break;
					}
				} else if(check.getAmount() == 1) {
					inv.setItem(i, new ItemStack(Material.AIR));
					durability -= increment;
					if(durability < 0) {
						durability = 0;
						break;
					}
				}
			}
		}
		return durability;
	}
	
	public static final Long getMilliSecondsFromString(String str, boolean multiplyBy1000IfNoTimeLetter) {
		if(str == null) {
			return null;
		}
		if(CodeUtils.isStrAValidLong(str)) {
			if(multiplyBy1000IfNoTimeLetter) {
				return new Long(CodeUtils.getLongFromStr(str, -1L) * 1000L);
			}
			return new Long(CodeUtils.getLongFromStr(str, -1L));
		}
		if(str.length() > 1) {
			String num = str.substring(0, str.length() - 1);
			if(!CodeUtils.isStrAValidLong(num)) {
				return null;
			}
			String timeType = str.substring(str.length() - 1);
			if(timeType.equalsIgnoreCase("s")) return new Long(CodeUtils.getLongFromStr(num, -1L) * 1000L);
			if(timeType.equalsIgnoreCase("m")) return new Long(CodeUtils.getLongFromStr(num, -1L) * 1000L * 60L);
			if(timeType.equalsIgnoreCase("h")) return new Long(CodeUtils.getLongFromStr(num, -1L) * 1000L * 60L * 60L);
			if(timeType.equalsIgnoreCase("d")) {
				return new Long(CodeUtils.getLongFromStr(num, -1L) * 1000L * 60L * 60L * 24L);
			}
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static final void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
		if((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			if((status.isGodModeOn) && (Permissions.hasPerm(player, "supercmds.use.god"))) {
				int newLevel = player.getFoodLevel() + 1;
				event.setFoodLevel(newLevel);
			} else if(status.isGodModeOn) {
				PlayerStatus.updatePlayerStateStates();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static final Material getMaterialFromString(String str) {
		Material type = Material.matchMaterial(str);
		type = type == null ? Bukkit.getUnsafe().getMaterialFromInternalName(str) : type;
		if(type == null) {
			if(str.trim().equalsIgnoreCase("crafting_table")) {
				type = Material.WORKBENCH;
			} else if(str.trim().equalsIgnoreCase("wheat_block")) {
				type = Material.CROPS;
			} else if((str.trim().equalsIgnoreCase("redstone_dust")) || (str.trim().equalsIgnoreCase("redstone_powder"))) {
				type = Material.REDSTONE_WIRE;
			} else if(str.trim().equalsIgnoreCase("sign")) {
				type = Material.WALL_SIGN;
			} else if(str.trim().equalsIgnoreCase("banner")) {
				type = Material.WALL_BANNER;
			} else if(str.trim().equalsIgnoreCase("mycelium")) {
				type = Material.MYCEL;
			} else if(str.trim().equalsIgnoreCase("glass_pane")) {
				type = Material.THIN_GLASS;
			}
		}
		return type;
	}
	
	@SuppressWarnings("deprecation")
	public static final ArrayList<ItemStack> getItemStacksFromString(String str) {
		if((str == null) || (str.isEmpty())) {
			return null;
		}
		ArrayList<ItemStack> rtrn = new ArrayList<>();
		String[] split = str.split(Pattern.quote("~;"));
		String[] arrayOfString1;
		int j = (arrayOfString1 = split).length;
		for(int i = 0; i < j; i++) {
			String s = arrayOfString1[i];
			s = Main.formatColorCodes(s.replace("minecraft:", "").trim(), true);
			String[] args = s.split("\\s+");
			int amount = 1;
			short data = 0;
			if(args.length != 0) {
				
				Material type = Material.matchMaterial(args[0]);
				type = type == null ? Bukkit.getUnsafe().getMaterialFromInternalName(args[0]) : type;
				if(type == null) {
					if(s.trim().equalsIgnoreCase("crafting_table")) {
						type = Material.WORKBENCH;
					} else if(s.trim().equalsIgnoreCase("wheat_block")) {
						type = Material.CROPS;
					} else if((s.trim().equalsIgnoreCase("redstone_dust")) || (s.trim().equalsIgnoreCase("redstone_powder"))) {
						type = Material.REDSTONE_WIRE;
					} else if(s.trim().equalsIgnoreCase("sign")) {
						type = Material.WALL_SIGN;
					} else if(s.trim().equalsIgnoreCase("banner")) {
						type = Material.WALL_BANNER;
					} else if(s.trim().equalsIgnoreCase("mycelium")) {
						type = Material.MYCEL;
					} else if(s.trim().equalsIgnoreCase("glass_pane")) {
						type = Material.THIN_GLASS;
					}
				}
				if(args.length >= 2) {
					amount = CodeUtils.getIntFromStr(args[1], amount);
					if(args.length >= 3) {
						data = CodeUtils.getShortFromStr(args[2], data);
					}
				}
				if(type != null) {
					try {
						ItemStack item = args.length >= 4 ? Bukkit.getUnsafe().modifyItemStack(new ItemStack(type, amount, data), Joiner.on(' ').join(Arrays.asList(args).subList(3, args.length))) : new ItemStack(type, amount, data);
						if((item.getType() != Material.AIR) || ((item.getType() == Material.AIR) && (s.trim().equalsIgnoreCase("air")))) {
							rtrn.add(item);
						}
					} catch(Throwable ignored) {
						return null;
					}
				}
			}
		}
		return rtrn;
	}
	
	public static final void setGameModeForPlayer(CommandSender sender, Player target, String targetName, String arg) {
		if(target == null) {
			Main.sendMessage(sender, getNoPlayerMsg(targetName));
			return;
		}
		if(!Permissions.hasPerm(sender, "supercmds.use.gamemode")) {
			Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			return;
		}
		boolean isSenderTarget = sender != null ? sender.getName().equalsIgnoreCase(target.getName()) : false;
		if((!isSenderTarget) && (!Permissions.hasPerm(sender, "supercmds.use.gamemode.others"))) {
			Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			return;
		}
		if((arg.equalsIgnoreCase("0")) || (arg.equalsIgnoreCase("s")) || (arg.equalsIgnoreCase("survival"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.gamemode.survival")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return;
			}
			target.setGameMode(GameMode.SURVIVAL);
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to survival.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to survival.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to survival.");
			}
		} else if((arg.equalsIgnoreCase("1")) || (arg.equalsIgnoreCase("c")) || (arg.equalsIgnoreCase("creative"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.gamemode.creative")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return;
			}
			target.setGameMode(GameMode.CREATIVE);
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to creative.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to creative.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to creative.");
			}
		} else if((arg.equalsIgnoreCase("2")) || (arg.equalsIgnoreCase("a")) || (arg.equalsIgnoreCase("adventure"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.gamemode.adventure")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return;
			}
			target.setGameMode(GameMode.ADVENTURE);
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to adventure.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to adventure.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to adventure.");
			}
		} else if((arg.equalsIgnoreCase("3")) || (arg.equalsIgnoreCase("spec")) || (arg.equalsIgnoreCase("spectator"))) {
			if(!Permissions.hasPerm(sender, "supercmds.use.gamemode.spectator")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return;
			}
			target.setGameMode(GameMode.SPECTATOR);
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to spectator.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to spectator.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to spectator.");
				
			}
		} else {
			Main.sendMessage(sender, Main.pluginName + "&eThe gamemode you entered(\"&f" + arg + "&r&e\") is not a valid gamemode&z&eor the plugin does not know what to make of it. Please try again!");
		}
	}
	
	public static final boolean isItemMjölnir(Item item) {
		return item != null ? isItemMjölnir(item.getItemStack()) : false;
	}
	
	public static final boolean isItemMjölnir(ItemStack item) {
		return (item != null) && (item.getType() == Material.IRON_PICKAXE) && (item.hasItemMeta()) && (item.getItemMeta().hasDisplayName()) && (Main.stripColorCodes(Main.formatColorCodes(item.getItemMeta().getDisplayName())).equals("Mjölnir"));
	}
	
	private static final Item updateMjölnir(Item item) {
		ItemStack stack = item.getItemStack();
		if(isItemMjölnir(stack)) {
			item.setItemStack(updateMjölnir(stack));
			item.setCustomName(Main.formatColorCodes("&r&bMjölnir"));
			item.setCustomNameVisible(true);
			item.setFireTicks(630720000);
			item.setOp(true);
		}
		return item;
	}
	
	private static final ItemStack updateMjölnir(ItemStack item) {
		if(isItemMjölnir(item)) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(Main.formatColorCodes("&r&bMjölnir"));
			item.setItemMeta(meta);
			if(item.getDurability() != 0) {
				item.setDurability((short) 0);
			}
			item.addUnsafeEnchantment(Enchantment.DURABILITY, 999);
			item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 999);
			meta.addEnchant(Enchantment.DURABILITY, 999, true);
			meta.addEnchant(Enchantment.DIG_SPEED, 999, true);
			meta.addItemFlags(new ItemFlag[] {ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS});
			item.setItemMeta(meta);
		}
		return item;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerInteractEvent(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		PlayerStatus status = PlayerStatus.getPlayerStatus(player);
		if((event.getAction() == Action.LEFT_CLICK_AIR) || (event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if((player.getGameMode() == GameMode.SPECTATOR) && (!Main.isPlayerAStaffMember(player))) {
				return;
			}
			if(Permissions.hasPerm(player, "supercmds.use.thor")) {
				ItemStack item = player.getInventory().getItemInMainHand();
				boolean isMjölnir = isItemMjölnir(item);
				if(isMjölnir) {
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(Main.formatColorCodes("&r&bMjölnir"));
					item.setItemMeta(meta);
					if(item.getDurability() != 0) {
						item.setDurability((short) 0);
					}
					item.addUnsafeEnchantment(Enchantment.DURABILITY, 999);
					item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 999);
					meta.addEnchant(Enchantment.DURABILITY, 999, true);
					meta.addEnchant(Enchantment.DIG_SPEED, 999, true);
					meta.addItemFlags(new ItemFlag[] {ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS});
					item.setItemMeta(meta);
					player.getInventory().setItemInMainHand(item);//player.setItemInHand(item);
				}
				if((status.isThorModeOn) && (item.getType() == Material.IRON_PICKAXE)) {
					final HashSet<Material> transparentBlocks = new HashSet<>();
					transparentBlocks.add(Material.AIR);
					try {
						player.getWorld().strikeLightning(player.getTargetBlock(transparentBlocks, 200).getLocation());
					} catch(IllegalStateException ignored) {
						if((ignored.getMessage() != null) && (ignored.getMessage().equals("Start block missed in BlockIterator"))) {
							Main.sendMessage(player, "&f[&6Thor&f]: &bHo! Doth thee wanteth to sloweth t down?!");
						}
					}
					
					if(isMjölnir) {
						for(int i = 2; i < 24; i += 2) {
							Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
								@Override
								public final void run() {
									player.getWorld().strikeLightning(player.getTargetBlock(transparentBlocks, 200).getLocation());
								}
							}, i);
						}
					} else if(GamemodeInventory.getCorrectGameModeToUse(player.getGameMode()) != GameMode.CREATIVE) {
						item.setDurability((short) (item.getDurability() + 1));
						if(item.getDurability() >= 250) {
							item = new ItemStack(Material.AIR);
							player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
						}
						player.getInventory().setItemInMainHand(item);//player.setItemInHand(item);
					}
				}
			}
		}
		
		if(!event.isCancelled()) {
			updateTopInventoriesFromOwnerChanges(event.getPlayer(), true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static void onServerCommandEvent(ServerCommandEvent event) {
		if(Main.startsWithIgnoreCase(event.getCommand(), "reload")) {
			event.setCommand("exit nowait");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if((Main.startsWithIgnoreCase(event.getMessage(), "/op")) || (Main.startsWithIgnoreCase(event.getMessage(), "/deop"))) {
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
				}
			});
		}
		
		String command = Main.getCommandFromMsg(event.getMessage()).substring(1);
		String[] args = Main.getArgumentsFromCommand(event.getMessage());
		
		//String strArgs = Main.getStringArgumentsFromCommand(args);
		if(command.equalsIgnoreCase("give")) {
			if(!Permissions.hasPerm(event.getPlayer(), "minecraft.command.give")) {
				return;
			}
			if(args.length == 4) {
				String targetName = args[0];
				String itemId = args[1];
				String amount = args[2];
				String metadata = args[3];
				if(itemId.equals("minecraft:mob_spawner")) {
					Player target = Main.getPlayer(targetName);
					if((target != null) && (CodeUtils.isStrAValidInt(amount)) && (CodeUtils.isStrAValidShort(metadata))) {
						event.setCancelled(true);
						ItemStack itemToGive = new ItemStack(Material.MOB_SPAWNER, CodeUtils.getIntFromStr(amount, 1), CodeUtils.getShortFromStr(metadata, (short) 50));
						target.getInventory().addItem(new ItemStack[] {itemToGive});
						Main.sendMessage(event.getPlayer(), "&ccommands.give.notFound");
					}
				}
			}
		} else if((command.equalsIgnoreCase("reload")) && ((Permissions.hasPerm(event.getPlayer(), "minecraft.command.reload")) || (Permissions.hasPerm(event.getPlayer(), "supercmds.use.exit")))) {
			event.setCancelled(true);
			Main.server.dispatchCommand(event.getPlayer(), "exit");
			return;
		}
	}
	
	public static final ArrayList<Entity[]> parseCommandBlockArgs(CommandBlock block) {
		ArrayList<Entity[]> rtrn = new ArrayList<>();
		Location loc = block.getLocation();
		String[] args = Main.getArgumentsFromCommand(block.getCommand());
		rtrn.ensureCapacity(args.length);
		for(int i = 0; i < args.length; i++) {
			rtrn.set(i, new Entity[65535]);
			if((args[i].startsWith("@e")) || (args[i].startsWith("@p")) || (args[i].startsWith("@a"))) {
				int minRange = 0;
				int maxRange = 65535;
				String entityType = "ANY";
				if((args[i].charAt(2) == '[') && (args[i].endsWith("]"))) {
					Main.DEBUG("&f[&5TEST!&f]: &aTest 1");
					String[] cmdArgs = args[i].substring(3, args[i].length() - 1).split(",");
					String[] arrayOfString1 = cmdArgs;
					for(String split : arrayOfString1) {
						Main.DEBUG("&f[&5TEST!&f]: &aTest 2: Split: \"&f" + split + "&r&a\"");
						String[] arg = split.split("=");
						Main.DEBUG("&f[&5TEST!&f]: &aTest 3: arg.length: \"&f" + arg.length + "&r&a\"");
						if(arg.length == 2) {
							if((arg[0].equals("type")) && (!args[i].startsWith("@p"))) {
								entityType = arg[1];
								Main.DEBUG("&f[&5TEST!&f]: &aTest 4: entityType: \"&f" + entityType + "&r&a\"");
							}
							if(arg[0].equals("rm")) {
								minRange = CodeUtils.getIntFromStr(arg[1], minRange);
								Main.DEBUG("&f[&5TEST!&f]: &aTest 5: minRange: \"&f" + minRange + "&r&a\"");
							}
							if(arg[0].equals("r")) {
								maxRange = CodeUtils.getIntFromStr(arg[1], maxRange);
								Main.DEBUG("&f[&5TEST!&f]: &aTest 6: maxRange: \"&f" + maxRange + "&r&a\"");
							}
						}
					}
				}
				try {
					Main.DEBUG("&f[&5TEST!&f]: &aEntity Name: \"&f" + entityType + "&r&a\"!");
					World world = loc.getWorld();
					double xDiff = loc.getX();
					double yDiff = loc.getY();
					double zDiff = loc.getZ();
					double distance = maxRange;
					for(Entity entity : world.getEntities()) {
						Main.DEBUG("&f[&5TEST!&f]: &aTest 7: entityType: \"&f" + entityType + "&r&a\"");
						if((entity.getType().name().equalsIgnoreCase(entityType)) || (entityType.equals("ANY"))) {
							Main.DEBUG("&f[&5TEST!&f]: &aTest 8: entity.getType().name(): \"&f" + entity.getType().name() + "&r&a\"");
							xDiff = entity.getLocation().getX() - loc.getX();
							yDiff = entity.getLocation().getY() - loc.getY();
							zDiff = entity.getLocation().getZ() - loc.getZ();
							double curDist = Math.abs(Math.sqrt(Math.pow(xDiff, 2.0D) + Math.pow(yDiff, 2.0D) + Math.pow(zDiff, 2.0D)));
							if(args[i].startsWith("@a")) {
								Main.DEBUG("&f[&5TEST!&f]: &aTest 9_0");
								if((curDist >= minRange) && (curDist <= maxRange)) {
									Entity[] array = rtrn.get(i);
									if(array == null) {
										array = new Entity[65535];
									}
									int index = Main.getNextFreeIndexInArray(array);
									if(index != -1) {
										array[index] = entity;
										rtrn.set(i, array);
										Main.DEBUG("&f[&5TEST!&f]: &aTest 9_1: @a added: \"&f" + entity.getType().name() + "&r&a\"");
									}
								}
							} else if(args[i].startsWith("@p")) {
								if((entity instanceof Player)) {
									Main.DEBUG("&f[&5TEST!&f]: &aTest 10_0");
									if((curDist >= minRange) && (curDist <= maxRange)) {
										Main.DEBUG("&f[&5TEST!&f]: &aTest 10_1");
										if(curDist <= distance) {
											distance = curDist;
											Entity[] array = rtrn.get(i);
											if(array == null) {
												array = new Entity[65535];
											}
											array[0] = entity;
											rtrn.set(i, array);
											Main.DEBUG("&f[&5TEST!&f]: &aTest 10_2: @p added: \"&f" + entity.getType().name() + "&r&a\"");
										}
									}
								}
							} else if(args[i].startsWith("@e")) {
								Main.DEBUG("&f[&5TEST!&f]: &aTest 11_0");
								if((curDist >= minRange) && (curDist <= maxRange)) {
									Main.DEBUG("&f[&5TEST!&f]: &aTest 11_1");
									if(curDist <= distance) {
										Main.DEBUG("&f[&5TEST!&f]: &aTest 11_2");
										distance = curDist;
										Entity[] array = rtrn.get(i);
										if(array == null) {
											array = new Entity[65535];
										}
										array[0] = entity;
										rtrn.set(i, array);
										Main.DEBUG("&f[&5TEST!&f]: &aTest 11_3: @e set: \"&f" + entity.getType().name() + "&r&a\"");
									}
								}
							}
						} else {
							Main.DEBUG("&f[&5TEST!&f]: &cTest 8_1: entity.getType().name(): \"&f" + entity.getType().name() + "&r&c\"");
						}
					}
				} catch(Throwable ignored) {
					Main.sendConsoleMessage("&f[&5TEST!&f]: &45:&z&c" + Main.throwableToStr(ignored));
				}
			}
		}
		return rtrn;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onBlockRedstoneEvent(BlockRedstoneEvent event) {
		if(((event.getOldCurrent() == 0) && (event.getNewCurrent() == 0)) || (event.getOldCurrent() == event.getNewCurrent())) {
			return;
		}
		if((event.getBlock().getState() instanceof CommandBlock)) {
			CommandBlock block = (CommandBlock) event.getBlock().getState();
			Location loc = block.getLocation();
			Chunk chunk = Main.getChunkAtWorldCoords(block.getWorld(), loc.getBlockX(), loc.getBlockZ());
			block.getWorld().loadChunk(chunk);
			Main.DEBUG("&f[&5TEST!&f]: &5Old current: \"&f" + event.getOldCurrent() + "&r&5\";");
			Main.DEBUG("&f[&5TEST!&f]: &5New current: \"&f" + event.getNewCurrent() + "&r&5\";");
			if(event.getNewCurrent() >= 15) {
				Main.DEBUG("&f[&5TEST!&f]: &2Sure.avi.");
				String command = Main.getCommandFromMsg(block.getCommand());
				String[] args = Main.getArgumentsFromCommand(block.getCommand());
				if(command.equalsIgnoreCase("smite")) {
					if(args.length == 1) {
						ArrayList<Entity> targets = new ArrayList<>();
						int maxRange;
						if((args[0].startsWith("@e")) || (args[0].startsWith("@p")) || (args[0].startsWith("@a"))) {
							int minRange = 0;
							maxRange = Integer.MAX_VALUE;
							String entityType = "ANY";
							if((args[0].charAt(2) == '[') && (args[0].endsWith("]"))) {
								Main.DEBUG("&f[&5TEST!&f]: &aTest 1");
								String[] cmdArgs = args[0].substring(3, args[0].length() - 1).split(",");
								String[] arrayOfString1;
								int j = (arrayOfString1 = cmdArgs).length;
								for(int i = 0; i < j; i++) {
									String split = arrayOfString1[i];
									Main.DEBUG("&f[&5TEST!&f]: &aTest 2: Split: \"&f" + split + "&r&a\"");
									String[] arg = split.split("=");
									Main.DEBUG("&f[&5TEST!&f]: &aTest 3: arg.length: \"&f" + arg.length + "&r&a\"");
									if(arg.length == 2) {
										if((arg[0].equals("type")) && (!args[0].startsWith("@p"))) {
											entityType = arg[1];
											Main.DEBUG("&f[&5TEST!&f]: &aTest 4: entityType: \"&f" + entityType + "&r&a\"");
										}
										if(arg[0].equals("rm")) {
											minRange = CodeUtils.getIntFromStr(arg[1], minRange);
											Main.DEBUG("&f[&5TEST!&f]: &aTest 5: minRange: \"&f" + minRange + "&r&a\"");
										}
										if(arg[0].equals("r")) {
											maxRange = CodeUtils.getIntFromStr(arg[1], maxRange);
											Main.DEBUG("&f[&5TEST!&f]: &aTest 6: maxRange: \"&f" + maxRange + "&r&a\"");
										}
									}
								}
							}
							try {
								Main.DEBUG("&f[&5TEST!&f]: &aEntity Name: \"&f" + entityType + "&r&a\"!");
								World world = loc.getWorld();
								double xDiff = loc.getX();
								double yDiff = loc.getY();
								double zDiff = loc.getZ();
								double distance = maxRange;
								for(Entity entity : world.getEntities()) {
									Main.DEBUG("&f[&5TEST!&f]: &aTest 7: entityType: \"&f" + entityType + "&r&a\"");
									if((entity.getType().name().equalsIgnoreCase(entityType)) || (entityType.equals("ANY"))) {
										Main.DEBUG("&f[&5TEST!&f]: &aTest 8: entity.getType().name(): \"&f" + entity.getType().name() + "&r&a\"");
										xDiff = entity.getLocation().getX() - loc.getX();
										yDiff = entity.getLocation().getY() - loc.getY();
										zDiff = entity.getLocation().getZ() - loc.getZ();
										double curDist = Math.abs(Math.sqrt(Math.pow(xDiff, 2.0D) + Math.pow(yDiff, 2.0D) + Math.pow(zDiff, 2.0D)));
										if(args[0].startsWith("@a")) {
											Main.DEBUG("&f[&5TEST!&f]: &aTest 9_0");
											if((curDist >= minRange) && (curDist <= maxRange)) {
												targets.add(entity);
												Main.DEBUG("&f[&5TEST!&f]: &aTest 9_1: @a added: \"&f" + entity.getType().name() + "&r&a\"");
											}
										} else if(args[0].startsWith("@p")) {
											if((entity instanceof Player)) {
												Main.DEBUG("&f[&5TEST!&f]: &aTest 10_0");
												if((curDist >= minRange) && (curDist <= maxRange)) {
													Main.DEBUG("&f[&5TEST!&f]: &aTest 10_1");
													if(curDist <= distance) {
														distance = curDist;
														targets.clear();
														targets.add(entity);
														Main.DEBUG("&f[&5TEST!&f]: &aTest 10_2: @p set: \"&f" + entity.getType().name() + "&r&a\"");
													}
												}
											}
										} else if(args[0].startsWith("@e")) {
											Main.DEBUG("&f[&5TEST!&f]: &aTest 11_0");
											if((curDist >= minRange) && (curDist <= maxRange)) {
												Main.DEBUG("&f[&5TEST!&f]: &aTest 11_1");
												if(curDist <= distance) {
													Main.DEBUG("&f[&5TEST!&f]: &aTest 11_2");
													distance = curDist;
													targets.clear();
													targets.add(entity);
													Main.DEBUG("&f[&5TEST!&f]: &aTest 11_3: @e set: \"&f" + entity.getType().name() + "&r&a\"");
												}
											}
										}
									} else {
										Main.DEBUG("&f[&5TEST!&f]: &cTest 8_1: entity.getType().name(): \"&f" + entity.getType().name() + "&r&c\"");
									}
								}
							} catch(Throwable ignored) {
								Main.DEBUG("&f[&5TEST!&f]: &45:&z&c" + Main.throwableToStr(ignored));
							}
						}
						if(targets.isEmpty()) {
							return;
						}
						for(Entity target : targets) {
							target.getWorld().strikeLightning(target.getLocation());
							if((target instanceof Player)) {
								Main.sendMessage((Player) target, Main.pluginName + "&eThou hast been smited!");
							}
						}
						org.bukkit.material.Command comm = (org.bukkit.material.Command) block.getData();
						comm.setPowered(true);
						block.setData(comm);
						block.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
					}
				} else if((command.equalsIgnoreCase("warp")) && (args.length == 2)) {
					String warpName = args[0];
					Warps.Warp warp = Warps.getWarpByName(warpName);
					if(warp == null) {
						return;
					}
					if(warp.location == null) {
						return;
					}
					Player target = Main.getPlayer(args[1]);
					if(target == null) {
						return;
					}
					PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(target);
					if(warp.requiredGroup != null) {
						if(!perms.isAMemberOfGroup(warp.requiredGroup)) {
							perms.disposeIfPlayerNotOnline();
						}
					} else if((warp.requiredPermission != null) && (!warp.requiredPermission.isEmpty()) && (!perms.hasPermission(warp.requiredPermission))) {
						perms.disposeIfPlayerNotOnline();
						return;
					}
					
					if(target.teleport(warp.location)) {
						Main.sendMessage(target, Main.pluginName + "&aWarping to &6/warp " + warp.name + "&r&a.");
					}
					perms.disposeIfPlayerNotOnline();
				}
				org.bukkit.material.Command comm = (org.bukkit.material.Command) block.getData();
				comm.setPowered(true);
				block.setData(comm);
				block.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
			} else {
				Main.DEBUG("&f[&5TEST!&f]: &4Nope.avi.");
				org.bukkit.material.Command comm = (org.bukkit.material.Command) block.getData();
				comm.setPowered(false);
				block.setData(comm);
				block.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
			}
		}
	}
	
}
