package com.gmail.br45entei.supercmds.cmds;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.api.Permissions;
import com.gmail.br45entei.supercmds.file.Kits;
import com.gmail.br45entei.supercmds.file.Kits.Kit;
import com.gmail.br45entei.supercmds.file.PlayerChat;
import com.gmail.br45entei.supercmds.file.PlayerChat.Mail;
import com.gmail.br45entei.supercmds.file.PlayerEcoData;
import com.gmail.br45entei.supercmds.file.PlayerPermissions;
import com.gmail.br45entei.supercmds.file.PlayerPermissions.Group;
import com.gmail.br45entei.supercmds.file.SavablePlayerData;
import com.gmail.br45entei.supercmds.file.SavablePluginData;
import com.gmail.br45entei.supercmds.file.Warps;
import com.gmail.br45entei.supercmds.file.Warps.Warp;
import com.gmail.br45entei.supercmds.util.CodeUtils;

/** @author Brian_Entei */
public final strictfp class MainCmdListener implements Listener {
	
	public static boolean	isConsoleAfk	= false;
	
	public MainCmdListener() {
		MainCmdListener.isConsoleAfk = true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerJoinEvent(PlayerJoinEvent evt) {
		final Player newPlayer = evt.getPlayer();
		Main.DEBUG(Main.pluginName + "Player \"" + newPlayer.getDisplayName() + "\" has just logged on.");
		PlayerStatus.getPlayerStatus(newPlayer).onPlayerJoin(evt);
		Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				PlayerChat chat = PlayerChat.getPlayerChat(newPlayer);
				if(chat.isPlayerOnline()) {//I've had players log in then get disconnected, so yeah XD
					ArrayList<Mail> unreadMail = chat.getAllMail();
					if(!unreadMail.isEmpty()) {
						Main.sendMessage(chat.getPlayer(), Main.pluginName + "&aYou have &f&a unread mail in your inbox!&z&aType /mail read to view them.");
					}
				}
			}
		}, 20 * 1);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerQuitEvent(PlayerQuitEvent evt) {
		final Player oldPlayer = evt.getPlayer();
		Main.DEBUG(Main.pluginName + "Player \"" + oldPlayer.getDisplayName() + "\" has just logged out.");
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onEntityTargetEvent(EntityTargetEvent event) {
		if(event.getTarget() instanceof Player) {
			PlayerStatus status = PlayerStatus.getPlayerStatus((Player) event.getTarget());
			if(status.isVanishModeOn || status.isGodModeOn) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onEntityTargetEvent(EntityTargetLivingEntityEvent event) {
		if(event.getTarget() instanceof Player) {
			PlayerStatus status = PlayerStatus.getPlayerStatus((Player) event.getTarget());
			if(status.isVanishModeOn || status.isGodModeOn) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage().replace(event.getFormat(), "");
		try {
			if(Main.startsWithIgnoreCase(msg, "what is half of ") && CodeUtils.isStrAValidDouble(msg.substring(16))) {
				double input = CodeUtils.getDoubleFromStr(msg.substring(16), 0);
				double result = input / 2;
				Main.sendMessage(player, Main.pluginName + "&aHalf of &f" + input + "&a is: &f" + result + "&a!");//Trololol XD
				event.setCancelled(true);
			}
		} catch(Throwable ignored) {
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onEntityDamageEvent(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			if(!Permissions.hasPerm(player, "supercmds.use.god") && status.isGodModeOn) {
				status.isGodModeOn = false;
				Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				event.setCancelled(false);
				return;
			}
			event.setCancelled(status.isGodModeOn);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
		Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				PlayerStatus.updatePlayerVanishStates();
				PlayerStatus.updatePlayerFlyModeStates();
				PlayerStatus.updatePlayerGodModeStates();
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		PlayerStatus status = PlayerStatus.getPlayerStatus(event.getPlayer());
		if(status.hasHome()) {
			event.setRespawnLocation(status.homeLocation);
		} else {
			event.setRespawnLocation(Main.spawnLocation);
		}
		PlayerStatus.updatePlayerVanishStates();
		PlayerStatus.updatePlayerFlyModeStates();
		PlayerStatus.updatePlayerGodModeStates();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
		if(event.isCancelled()) {
			return;
		}
		final PlayerStatus status = PlayerStatus.getPlayerStatus(event.getPlayer());
		status.isInBed = true;
		status.bedSchedulerTID = Main.scheduler.scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(!status.isInBed || !status.isPlayerOnline()) {
					int tid = status.bedSchedulerTID;
					status.bedSchedulerTID = 0;
					status.isInBed = false;
					Main.scheduler.cancelTask(tid);
					return;
				}
				if(status.getPlayer().getHealth() < status.getPlayer().getMaxHealth()) {
					status.getPlayer().setHealth(status.getPlayer().getHealth() + 1);
				}
			}
		}, 0, 20 * 5);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
		PlayerStatus.getPlayerStatus(event.getPlayer()).isInBed = false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
		final Player player = event.getPlayer();
		PlayerStatus status = PlayerStatus.getPlayerStatus(player);
		if(status.isFlyModeOn) {
			final boolean playerIsFlying = player.isFlying();
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					player.setAllowFlight(true);
					player.setFlying(playerIsFlying);
					Main.DEBUG("Set player's fly mode to: \"" + playerIsFlying + "\"(actual flying boolean: " + player.isFlying() + ")");
					PlayerStatus.updatePlayerFlyModeStates();
				}
			});
		}
	}
	
	public static final String getNoPlayerMsg(String playerName) {
		return Main.pluginName + "&ePlayer \"&f" + playerName + "&r&e\" does not exist or is not online.";
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerMoveEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		PlayerStatus status = PlayerStatus.getPlayerStatus(player);
		if(!status.isAfk) {
			return;
		}
		Location lastAfkSpot = status.lastAfkLocation;
		Location currentLocation = player.getLocation();
		double xDiff = currentLocation.getX() - lastAfkSpot.getX();
		double yDiff = currentLocation.getY() - lastAfkSpot.getY();
		double zDiff = currentLocation.getZ() - lastAfkSpot.getZ();
		double distance = Math.abs(Math.sqrt(Math.pow(xDiff, 2f) + Math.pow(yDiff, 2f) + Math.pow(zDiff, 2f)));
		if(distance >= 2) {
			status.toggleAfkState();
		} else {
			Main.DEBUG(Main.pluginName + "&aDebug: Player \"&f" + player.getDisplayName() + "&r&a\" did not move up to two blocks away from last afk spot; not changing player's afk state.");
		}
	}
	
	/** @author <a
	 *         href="http://bukkit.org/goto/post?id=1515335#post-1515335">d33k40
	 *         </a>(Gave me the basic idea of how to do it) */
	public static final HashMap<String, ItemStack[]>	onDeathItems	= new HashMap<>();
	
	public static final HashMap<String, Float>			onDeathExp		= new HashMap<>();
	public static final HashMap<String, Integer>		onDeathLevel	= new HashMap<>();
	
	/** @author <a
	 *         href="http://bukkit.org/goto/post?id=1515335#post-1515335">d33k40
	 *         </a>(Gave me the basic idea of how to do it) */
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onRespawn(PlayerRespawnEvent event) {
		if(!Permissions.hasPerm(event.getPlayer(), "supercmds.special.keepInvOnDeath")) {
			return;
		}
		String uuid = event.getPlayer().getUniqueId().toString();
		if(MainCmdListener.onDeathItems.containsKey(uuid)) {
			event.getPlayer().getInventory().clear();
			for(ItemStack stack : MainCmdListener.onDeathItems.get(uuid)) {
				if(stack != null) {
					event.getPlayer().getInventory().addItem(stack);
				}
			}
			MainCmdListener.onDeathItems.remove(uuid);
		}
		if(MainCmdListener.onDeathExp.containsKey(uuid)) {
			event.getPlayer().setExp(MainCmdListener.onDeathExp.get(uuid).floatValue());
			MainCmdListener.onDeathExp.remove(uuid);
		}
		if(MainCmdListener.onDeathLevel.containsKey(uuid)) {
			event.getPlayer().setLevel(MainCmdListener.onDeathLevel.get(uuid).intValue());
			MainCmdListener.onDeathLevel.remove(uuid);
		}
	}
	
	/** @author <a
	 *         href="http://bukkit.org/goto/post?id=1515335#post-1515335">d33k40
	 *         </a>(Gave me the basic idea of how to do it) */
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onDeath(PlayerDeathEvent event) {
		if(!Permissions.hasPerm(event.getEntity(), "supercmds.special.keepInvOnDeath")) {
			return;
		}
		ItemStack[] content = event.getEntity().getInventory().getContents();
		String uuid = event.getEntity().getUniqueId().toString();
		MainCmdListener.onDeathItems.put(uuid, content);
		MainCmdListener.onDeathExp.put(uuid, new Float(event.getEntity().getExp()));
		MainCmdListener.onDeathLevel.put(uuid, new Integer(event.getEntity().getLevel()));
		event.getEntity().getInventory().clear();
		event.getEntity().setExp(0.0F);
		event.getEntity().setLevel(0);
	}
	
	public static final boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {
		String strArgs = "";
		if(!(args.length == 0)) {
			strArgs = "";
			int x = 0;
			do {
				strArgs = strArgs.concat(args[x] + " ");
				x++;
			} while(x < args.length);
		}
		strArgs = strArgs.trim();
		Player user = null;
		if(sender instanceof Player) {
			user = ((Player) sender);
		}
		String userName = sender.getName();
		if(user != null) {
			userName = user.getDisplayName();
		}
		if(StringUtils.isEmpty(userName)) {
			userName = sender.getName();
		}
		PlayerStatus status = PlayerStatus.getPlayerStatus(user);
		if(user != null && status.isAfk && !command.equalsIgnoreCase("vanish") && !status.isVanishModeOn) {
			status.toggleAfkState();
		} else if(user == null && sender == Main.console) {
			if(MainCmdListener.isConsoleAfk) {
				MainCmdListener.isConsoleAfk = false;
				Main.broadcast("&7* " + Main.consoleSayFormat + "&f: &7is no" + (MainCmdListener.isConsoleAfk ? "w afk." : " longer afk."));
			}
		}
		if(command.equalsIgnoreCase("sudo")) {
			if(Permissions.hasPerm(sender, "supercmds.use.sudo")) {
				if(args.length >= 2) {
					Player target = Main.getPlayer(args[0]);
					if(target != null) {
						String value = Main.getElementsFromStringArrayAtIndexAsString(args, 1);
						Main.server.dispatchCommand(target, value);
						Main.sendMessage(sender, Main.pluginName + "&aMade player \"&r&f" + target.getDisplayName() + "&r&a\" perform command: \"&r&f/" + value + "&r&a\"");
					}
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {playername} {command} [args...]&e\"");
				}
			} else {
				Main.sendMessage(user, Main.pluginName + Main.noPerm);
			}
			return true;
		} else if(command.equalsIgnoreCase("heal")) {
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
					target.setHealth(((Damageable) target).getMaxHealth());
					target.setFoodLevel(100);
					target.setSaturation(100);
					Main.sendMessage(target, Main.pluginName + "&6You have been healed by &f" + userName + "&r&a.");
					Main.sendMessage(sender, Main.pluginName + "&aYou healed &f" + target.getDisplayName() + "&r&a.");
				} else {
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
				}
				return true;
			}
			if(user != null) {
				if(args.length == 0) {
					user.setHealth(((Damageable) user).getMaxHealth());
					user.setFoodLevel(100);
					user.setSaturation(100);
					Main.sendMessage(user, Main.pluginName + "&6You have been healed.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " [targetName]&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\"");
			}
			return true;
		} else if(command.equalsIgnoreCase("setmaxhealth")) {
			if(user != null) {
				if(Permissions.hasPerm(user, "supercmds.use.setmaxhealth")) {
					if(args.length == 1) {
						if(Main.checkIsNumber(args[0])) {
							double newHealth = Main.toNumber(args[0]);
							if(newHealth > 2000) {
								Main.sendMessage(user, Main.pluginName + "&eIf your maximum health goes any higher than that, your client will lag out!");
								return true;
							}
							if(newHealth <= 0) {
								Main.sendMessage(user, Main.pluginName + "&eYour maximum health can't go that low!");
								return true;
							}
							user.setMaxHealth(Main.toNumber(args[0]));
							Main.sendMessage(user, Main.pluginName + "&aYour maximum health has just been changed.");
							return true;
						}
					} else if(args.length == 2) {
						if(args[0].equalsIgnoreCase("-add") || args[0].equalsIgnoreCase("-a")) {
							if(Main.checkIsNumber(args[1])) {
								double newHealth = user.getMaxHealth() + Main.toNumber(args[1]);
								if(newHealth <= 2000) {
									user.setMaxHealth(newHealth);
									Main.sendMessage(user, Main.pluginName + "&aYour maximum health has just been changed.");
								} else {
									Main.sendMessage(user, Main.pluginName + "&eIf your maximum health goes any higher than that, your client will lag out!");
								}
								return true;
							}
						} else if(args[0].equalsIgnoreCase("-subtract") || args[0].equalsIgnoreCase("-sub") || args[0].equalsIgnoreCase("-s")) {
							if(Main.checkIsNumber(args[1])) {
								double newHealth = user.getMaxHealth() - Main.toNumber(args[1]);
								if(newHealth > 0) {
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
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " amount&e\" or \"&f/" + command + " -add amount&e\".");
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
							if(newHealth > 2000) {
								Main.sendMessage(sender, Main.pluginName + "&eClients with a health of over 2000 can lock up and/or lag out.");
								return true;
							}
							if(newHealth <= 0) {
								Main.sendMessage(sender, Main.pluginName + "&eHealth must be greater than zero.");
								return true;
							}
							target.setMaxHealth(newHealth);
							Main.sendMessage(target, Main.pluginName + "&aYour maximum health has just been changed.");
							Main.sendMessage(sender, Main.pluginName + "&aMax health changed for player \"&f" + target.getDisplayName() + "&r&a\"");
							return true;
						}
					} else {
						Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
					}
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {target} amount&e\" or \"&f/" + command + " {target} [-add|-sub] amount&e\".");
					return true;
				} else if(args.length == 3) {
					Player target = Main.getPlayer(args[0]);
					if(target != null) {
						if(args[1].equalsIgnoreCase("-add") || args[1].equalsIgnoreCase("-a")) {
							if(Main.checkIsNumber(args[2])) {
								target.setMaxHealth(target.getMaxHealth() + Main.toNumber(args[2]));
								Main.sendMessage(target, Main.pluginName + "&aYour maximum health has just been changed.");
								return true;
							}
						} else if(args[1].equalsIgnoreCase("-subtract") || args[1].equalsIgnoreCase("-sub") || args[1].equalsIgnoreCase("-s")) {
							if(Main.checkIsNumber(args[2])) {
								target.setMaxHealth(target.getMaxHealth() - Main.toNumber(args[2]));
								Main.sendMessage(target, Main.pluginName + "&aYour maximum health has just been changed.");
								return true;
							}
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag: \"&f" + args[1] + "&r&e\"");
						}
					} else {
						Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
						return true;
					}
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {target} amount&e\" or \"&f/" + command + " {target} [-add|-sub] amount&e\".");
			}
			return true;
		} else if(command.equalsIgnoreCase("thor")) {
			if(user != null) {
				if(user.isOp() || user.hasPermission("supercmds.use.thor")) {
					if(args.length == 0) {
						if(user.getItemInHand().getType() == Material.IRON_PICKAXE) {
							status.isThorModeOn = !status.isThorModeOn;
							Main.sendMessage(user, Main.pluginName + (status.isThorModeOn ? "&aThor has blessed your hammer!" : "&eThor's blessing has worn off..."));
						} else {
							Main.sendMessage(user, Main.pluginName + "&eThor is not pleased with your current equipment! He demands something conductive!");
						}
					} else if(args.length == 1) {
						if(args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("activate") || args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("true")) {
							status.isThorModeOn = true;
							Main.sendMessage(user, Main.pluginName + (status.isThorModeOn ? "&aThor has blessed your hammer!" : "&eThor's blessing has worn off..."));
						} else if(args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("deactivate") || args[0].equalsIgnoreCase("no") || args[0].equalsIgnoreCase("false")) {
							status.isThorModeOn = false;
							Main.sendMessage(user, Main.pluginName + (status.isThorModeOn ? "&aThor has blessed your hammer!" : "&eThor's blessing has worn off..."));
						} else {
							Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [on|off]&e\"");
						}
					} else {
						Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [on|off]&e\"");
					}
				} else {
					Main.sendMessage(user, Main.pluginName + Main.noPerm);
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		} else if(command.equalsIgnoreCase("tp") || command.equalsIgnoreCase("teleport")) {
			if(sender.hasPermission("supercmds.use.teleport") || sender.isOp()) {
				if(args.length == 1) {// /tp name1 (teleports YOU to name1)
					if(!Permissions.hasPerm(sender, "supercmds.use.teleport.toOthers")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					Player target = Main.getPlayer(args[0]);
					if(user != null) {
						if(target != null) {
							user.teleport(target);
						} else {
							Main.sendMessage(user, MainCmdListener.getNoPlayerMsg(args[0]));
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " [target1] [target2]&e\" or \"&f/" + command + " [target1] {x} {y} {z}&e\"");
					}
				} else if(args.length == 2) {// /tp name1 name2
					if(!Permissions.hasPerm(sender, "supercmds.use.teleport.others")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					Player target = Main.getPlayer(args[0]);
					Player destination = Main.getPlayer(args[1]);
					if(target != null) {
						if(destination != null) {
							target.teleport(destination);
							Main.sendMessage(target, Main.pluginName + "&e\"&f" + sender.getName() + "&r&e\" just teleported you to \"&f" + destination.getDisplayName() + "&r&e\".");
						} else {
							Main.sendMessage(user, MainCmdListener.getNoPlayerMsg(args[0]));
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " [target1] [target2]&e\" or \"&f/" + command + " [target1] {x} {y} {z}\"");
					}
				} else if(args.length == 3) {// /tp x y z
					if(user == null) {
						Main.sendMessage(sender, Main.pluginName + "&eCoordinate based command syntax is only usable by players.");
						Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: &f/" + command + " [target1] [target2]&e or &f/" + command + " [target1] {x} {y} {z}");
						return true;
					}
					boolean X = CodeUtils.isStrAValidDouble(args[1]);
					boolean Y = CodeUtils.isStrAValidDouble(args[2]);
					boolean Z = CodeUtils.isStrAValidDouble(args[3]);
					double x = CodeUtils.getDoubleFromStr(args[1], Double.NaN);
					double y = CodeUtils.getDoubleFromStr(args[2], Double.NaN);
					double z = CodeUtils.getDoubleFromStr(args[3], Double.NaN);
					if(X && Y && Z) {
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
				} else if(args.length == 4) {// /tp name x y z
					if(!Permissions.hasPerm(sender, "supercmds.use.teleport.others")) {
						Main.sendMessage(sender, Main.pluginName + Main.noPerm);
						return true;
					}
					Player target = Main.getPlayer(args[0]);
					if(target == null) {
						Main.sendMessage(user, MainCmdListener.getNoPlayerMsg(args[0]));
						return true;
					}
					boolean X = CodeUtils.isStrAValidDouble(args[1]);
					boolean Y = CodeUtils.isStrAValidDouble(args[2]);
					boolean Z = CodeUtils.isStrAValidDouble(args[3]);
					double x = CodeUtils.getDoubleFromStr(args[1], Double.NaN);
					double y = CodeUtils.getDoubleFromStr(args[2], Double.NaN);
					double z = CodeUtils.getDoubleFromStr(args[3], Double.NaN);
					if(X && Y && Z) {
						Location loc = target.getLocation();
						if(target.teleport(new Location((user != null ? user : target).getWorld(), x, y, z))) {
							PlayerStatus.getPlayerStatus(target).lastTeleportLoc = loc;
							Main.sendMessage(sender, Main.pluginName + "&aTeleport successful.");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eTeleport unsuccessful; is the world loaded in that area?");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eThe coordinates you entered are not valid: \"&fx: " + args[0] + ", y: " + args[1] + ", z: " + args[2] + "; " + "&r&e\".");
						Main.sendMessage(sender, "&eCheck your typing and try again.");
					}
				} else {
					if(user == null) {
						Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: &f/" + command + " [target1] [target2]&e or &f/" + command + " [target1] {x} {y} {z}");
					} else {
						Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [destination]&e\" or \"&f/" + command + " [target] [destination]&e\" or \"&f/" + command + " {x} {y} {z}&e\" or \"&f/" + command + " [target] {x} {y} {z}&e\".");
					}
				}
			} else {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			}
			return true;
		} else if(command.equalsIgnoreCase("sethome")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.sethome")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 0) {
					Location loc = user.getLocation();
					status.homeLocation = loc;
					status.hasHome = true;
					Main.sendMessage(user, Main.pluginName + "&aSet your &fhome&a to your current location.");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		} else if(command.equalsIgnoreCase("home")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.home")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 0) {
					Location loc = user.getLocation();
					if(!status.hasHome()) {
						Main.sendMessage(user, Main.pluginName + "&eYou do not have a &fhome&e yet!&z&aSet one with &f/sethome&a.");
						return true;
					}
					if(user.teleport(status.homeLocation)) {
						status.lastTeleportLoc = loc;
						Main.sendMessage(user, Main.pluginName + "&aTaking you &fhome&a.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when teleporting you to your &fhome&e; is the world loaded?");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		} else if(command.equalsIgnoreCase("back") || command.equalsIgnoreCase("return")) {
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
		} else if(command.equalsIgnoreCase("tpa")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 1) {
					Player target = Main.getPlayer(args[0]);
					if(target == null) {
						Main.sendMessage(user, MainCmdListener.getNoPlayerMsg(args[0]));
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
		} else if(command.equalsIgnoreCase("tpahere")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length == 1) {
					Player target = Main.getPlayer(args[0]);
					if(target == null) {
						Main.sendMessage(user, MainCmdListener.getNoPlayerMsg(args[0]));
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
		} else if(command.equalsIgnoreCase("tpaall")) {
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
		} else if(command.equalsIgnoreCase("tpaccept")) {
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
		} else if(command.equalsIgnoreCase("tpdeny")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.tpa")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				status.denyTeleportRequests();
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		} else if(command.equalsIgnoreCase("god") || command.equalsIgnoreCase("godmode")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.god")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null && args.length == 0) {
				status.isGodModeOn = !status.isGodModeOn;
				Main.sendMessage(user, Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				return true;
			}
			if(args.length == 1) {
				if(!Permissions.hasPerm(sender, "supercmds.use.god.others")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				Player target = Main.getPlayer(args[0]);
				if(target == null) {
					Main.sendMessage(user, MainCmdListener.getNoPlayerMsg(args[0]));
					return true;
				}
				PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
				targetStatus.isGodModeOn = !targetStatus.isGodModeOn;
				Main.sendMessage(sender, Main.pluginName + "&aSet &f" + target.getDisplayName() + "&r&a's god mode to " + (targetStatus.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
				Main.sendMessage(user, Main.pluginName + "&aYour god mode has been set to " + (targetStatus.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
			} else {
				if(user != null) {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" or \"&f/" + command + " [targetName]&e\"");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\"");
				}
			}
			return true;
		} else if(command.equalsIgnoreCase("fly")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.fly")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				if(user != null) {
					if(status.isFlyModeOn) {
						PlayerStatus.updatePlayerFlyModeStates();//lol
					}
				}
				return true;
			}
			if(user != null && args.length == 0) {
				status.isFlyModeOn = !status.isFlyModeOn;
				if(!status.isFlyModeOn) {
					if(user.getGameMode() == GameMode.SURVIVAL || user.getGameMode() == GameMode.ADVENTURE) {
						user.setFlying(false);
						user.setAllowFlight(false);
					}
				} else {
					user.setAllowFlight(true);
				}
				Main.sendMessage(user, Main.pluginName + "&aSet fly mode to " + (status.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
				PlayerStatus.updatePlayerFlyModeStates();
				return true;
			}
			if(args.length == 1) {
				if(!Permissions.hasPerm(sender, "supercmds.use.fly.others")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				Player target = Main.getPlayer(args[0]);
				if(target == null) {
					Main.sendMessage(user, MainCmdListener.getNoPlayerMsg(args[0]));
					return true;
				}
				PlayerStatus targetStatus = PlayerStatus.getPlayerStatus(target);
				targetStatus.isFlyModeOn = !targetStatus.isFlyModeOn;
				if(!targetStatus.isFlyModeOn) {
					if(target.getGameMode() == GameMode.SURVIVAL || target.getGameMode() == GameMode.ADVENTURE) {
						target.setFlying(false);
						target.setAllowFlight(false);
					}
				} else {
					target.setAllowFlight(true);
				}
				Main.sendMessage(sender, Main.pluginName + "&aSet &f" + target.getDisplayName() + "&r&a's fly mode to " + (targetStatus.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
				Main.sendMessage(target, Main.pluginName + "&aYour fly mode has been set to " + (targetStatus.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
				PlayerStatus.updatePlayerFlyModeStates();
			} else {
				if(user != null) {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" or \"&f/" + command + " [targetName]&e\"");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\"");
				}
			}
			return true;
		} else if(command.equalsIgnoreCase("setspawn")) {
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
				Main.spawnLocation = loc;
				Main.sendMessage(user, Main.pluginName + "&aSet the server &f/spawn&a to your current location.");
				Main.server.dispatchCommand(Main.console, "supercmds save");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		} else if(command.equalsIgnoreCase("spawn")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.spawn")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length != 0) {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
					return true;
				}
				if(user.teleport(Main.spawnLocation)) {
					Main.sendMessage(user, Main.pluginName + "&aTaking you to the server &f/spawn&a.");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eSomething went wrong whilst teleporting you! Is the world loaded?");
				}
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		} else if(command.equalsIgnoreCase("vanish") || command.equalsIgnoreCase("v")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.vanish")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length != 0) {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
					return true;
				}
				status.isVanishModeOn = !status.isVanishModeOn;
				Main.sendMessage(user, Main.pluginName + "&aYou are now " + (status.isVanishModeOn ? "&2completely invisible&a to other players. Server operators and players with special permissions may still be able to see you." : "&fvisible&a to other players."));
				PlayerStatus.updatePlayerVanishStates();
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		} else if(command.equalsIgnoreCase("list") || command.equalsIgnoreCase("playerlist") || command.equalsIgnoreCase("listplayers")) {
			if(args.length == 0) {
				ArrayList<Player> playersToShow;
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
					Main.sendMessage(sender, Main.pluginName + "&f" + player.getDisplayName());
				}
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
			return true;
		} else if(command.equalsIgnoreCase("gamemode") || command.equalsIgnoreCase("gm") || command.equalsIgnoreCase("gammedoe") || command.equalsIgnoreCase("gammeode")) {
			if(args.length == 1) {// Don't worry, the setGameModeForPlayer method checks for permissions etc.
				if(user != null) {
					MainCmdListener.setGameModeForPlayer(sender, user, userName, args[0]);
				} else {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
					Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {gamemode} {targetName}&e\"");
				}
			} else if(args.length == 2) {
				Player target = Main.getPlayer(args[0]);
				MainCmdListener.setGameModeForPlayer(sender, target, args[0], args[1]);
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {gamemode}" + (user == null ? " {targetName}" : " [targetName]") + "&e\"");
			}
			return true;
		} else if(command.equalsIgnoreCase("gms")) {
			if(user != null) {
				if(args.length == 0) {
					MainCmdListener.setGameModeForPlayer(user, user, userName, "survival");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		} else if(command.equalsIgnoreCase("gmc")) {
			if(user != null) {
				if(args.length == 0) {
					MainCmdListener.setGameModeForPlayer(user, user, userName, "creative");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		} else if(command.equalsIgnoreCase("gma")) {
			if(user != null) {
				if(args.length == 0) {
					MainCmdListener.setGameModeForPlayer(user, user, userName, "adventure");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		} else if(command.equalsIgnoreCase("gmspec")) {
			if(user != null) {
				if(args.length == 0) {
					MainCmdListener.setGameModeForPlayer(user, user, userName, "spectator");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			}
			return true;
		} else if(command.equalsIgnoreCase("say") || command.equalsIgnoreCase("chat")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.say") && !Permissions.hasPerm(sender, "supercmds.use.chat")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(Permissions.hasPerm(sender, "supercmds.chat.colors")) {
				if(Permissions.hasPerm(sender, "supercmds.chat.colors.magic")) {
					if(user != null) {
						user.chat(Main.formatColorCodes(strArgs));
					} else {
						Main.broadcast((sender == Main.console ? Main.consoleSayFormat : "&6[" + userName + "&r&6]&f ") + strArgs);
					}
				} else {
					if(user != null) {
						user.chat(Main.formatColorCodes(strArgs, false));
					} else {
						Main.broadcast((sender == Main.console ? Main.consoleSayFormat : "&6[" + userName + "&r&6]&f ") + strArgs);
					}
				}
			} else {
				if(user != null) {
					user.chat(strArgs);
				} else {
					Main.broadcast((sender == Main.console ? Main.consoleSayFormat : "&6[" + userName + "&r&6]&f ") + strArgs);
				}
			}
			return true;
		} else if(command.equalsIgnoreCase("me")) {
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
		} else if(command.equalsIgnoreCase("afk") || command.equalsIgnoreCase("awayfromkeyboard")) {
			if(user != null) {
				status.toggleAfkState();
			} else {
				if(sender == Main.console) {
					MainCmdListener.isConsoleAfk = !MainCmdListener.isConsoleAfk;
					Main.broadcast("&7* " + Main.consoleSayFormat + ": &7is no" + (MainCmdListener.isConsoleAfk ? "w afk." : " longer afk."));
				} else {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				}
			}
			return true;
		} else if(command.equalsIgnoreCase("perm") || command.equalsIgnoreCase("permission")) {
			if(!Main.handlePermissions) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandlePermissions&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.perm") && !Permissions.hasPerm(sender, "supercmds.use.permission")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 3) {
				UUID uuid = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				if(uuid != null) {
					String targetName = Main.uuidMasterList.getPlayerNameFromUUID(uuid);
					String flag = args[1];
					String arg3 = args[2];
					PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
					if(flag.equalsIgnoreCase("add")) {
						if(perms.setPermission(arg3, true)) {
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							perms.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when adding the permission \"&f" + arg3 + "&r&e\" to the player \"&f" + targetName + "&r&e\"!&z&aPerhaps they already have that permission?");
						perms.saveAndDisposeIfPlayerNotOnline();
						return true;
					} else if(flag.equalsIgnoreCase("remove")) {
						if(perms.setPermission(arg3, false)) {
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							perms.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when removing the permission \"&f" + arg3 + "&r&e\" from the player \"&f" + targetName + "&r&e\"!&z&aPerhaps it was never added?");
						perms.saveAndDisposeIfPlayerNotOnline();
						return true;
					} else if(flag.equalsIgnoreCase("setgroup")) {
						Group group = Group.getGroupByName(arg3);
						if(group != null) {
							if(perms.changeGroup(group)) {
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								perms.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when changing the player \"&f" + targetName + "&r&e\"'s group to \"&f" + group.displayName + "&r&e\"!");
							perms.saveAndDisposeIfPlayerNotOnline();
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + arg3 + "&r&e\" does not exist.&z&aCreate it using &f/group&a!");
						perms.disposeIfPlayerNotOnline();
						return true;
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
						perms.disposeIfPlayerNotOnline();
					}
				} else {
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {target} {add|remove|setgroup} {permission.node|groupName}&e\"");
			return true;
		} else if(command.equalsIgnoreCase("group")) {
			if(!Main.handlePermissions) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandlePermissions&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.permission")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {// /group list
				if(args[0].equalsIgnoreCase("list")) {
					Main.sendMessage(sender, Main.pluginName + "&aListing all groups:");
					for(Group group : Group.getInstances()) {
						Main.sendMessage(sender, "&3\"&f" + group.displayName + "&r&3\"(config name: &f" + group.name + "&r&3);");
					}
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + args[0] + "&r&e\".");
			} else if(args.length == 2) {// /group {groupName} {create|delete|info}
				String groupName = args[0];
				String createDeleteInfo = args[1];
				Group check = Group.getGroupByName(groupName);
				if(createDeleteInfo.equalsIgnoreCase("create")) {
					if(check != null) {
						Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + check.name + "&r&e\" already exists!");
						return true;
					}
					Group group = Group.createGroup(groupName);
					if(group != null) {
						Main.sendMessage(sender, Main.pluginName + "&aSuccessfully created the group \"&f" + group.name + "&r&a\"!&z&aThe group's display name was set to: \"&f" + group.displayName + "&r&a\".");
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when attempting to create the group \"&f" + groupName + "&r&e\".&z&ePlease contact a server administrator about this issue.(Maybe the server needs to be restarted?)");
					return true;
				} else if(createDeleteInfo.equalsIgnoreCase("delete") || createDeleteInfo.equalsIgnoreCase("del")) {
					Group group = Group.getGroupByName(groupName);
					if(group != null) {
						group.dispose();
						group = null;
						Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.&z&aPerhaps it was already deleted?");
					return true;
				} else if(createDeleteInfo.equalsIgnoreCase("info")) {
					if(check != null) {
						Main.sendMessage(sender, Main.pluginName + "&aDisplaying group information:&z&f" + check.toString());
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + createDeleteInfo + "&r&e\".");
				}
			} else if(args.length == 3) {// /group {groupName} {add|remove} {permission.node} or /group {groupName} {setRequirement} {none}
				String groupName = args[0];
				String addRemove = args[1];
				String permNode = args[2];
				Group group = Group.getGroupByName(groupName);
				if(group != null) {
					if(addRemove.equalsIgnoreCase("setrequirement")) {
						if(permNode.equalsIgnoreCase("none")) {
							group.moneyOrCredit = "NONE";
							group.costToRankup = 0.0;
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							return true;
						}
					} else if(addRemove.equalsIgnoreCase("add")) {
						if(group.setPermission(permNode, true)) {
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when adding the permission \"&f" + permNode + "&r&e\" to the group \"&f" + group.displayName + "&r&e\"!&z&aIs it already added?");
					} else if(addRemove.equalsIgnoreCase("remove")) {
						if(group.setPermission(permNode, false)) {
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when removing the permission \"&f" + permNode + "&r&e\" to the group \"&f" + group.displayName + "&r&e\"!&z&aWas it already removed?");
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + addRemove + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.");
				}
			} else if(args.length >= 4) {// /group {groupName} {set} {default|displayName|inheritance|nextgroup} {value} or /group {groupName} {setRequirement} {none|money|credit} [value]
				String groupName = args[0];
				String flag = args[1];//this might be unnecessary, but meh.
				String defaultDispInherNexGr = args[2];
				String value = Main.getElementsFromStringArrayAtIndexAsString(args, 3);
				Group group = Group.getGroupByName(groupName);
				if(group != null) {
					if(flag.equalsIgnoreCase("set")) {
						if(defaultDispInherNexGr.equalsIgnoreCase("default")) {
							Boolean val = value.equalsIgnoreCase("true") ? Boolean.TRUE : (value.equalsIgnoreCase("false") ? Boolean.FALSE : null);
							if(val != null) {
								boolean isDefault = val.booleanValue();
								if(group.setDefault(isDefault)) {
									Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when setting the group \"&f" + group.name + "&r&e\" as the default group!&z&ePerhaps there is already a default group?");
								}
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid value \"&f" + value + "&r&e\".");
						} else if(defaultDispInherNexGr.equalsIgnoreCase("displayname")) {
							if(group.setDisplayName(value)) {
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when changing the display name of the group \"&f" + group.name + "&r&e\" to \"&f" + value + "&r&e\"!&z&aPerhaps the old display name is the same as the new one?");
							}
							return true;
						} else if(defaultDispInherNexGr.equalsIgnoreCase("inheritance")) {
							Group newInheritance = Group.getGroupByName(value);
							if(newInheritance != null) {
								if(group.setInheritance(newInheritance)) {
									Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
									return true;
								}
								Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when setting the group \"&f" + group.name + "&r&e\"'s inheritance group to \"&f&r&e\"!&z&aPerhaps it was already set to that group?&z&a(Note: you can't set a group as it's own inheritance group.)");
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + value + "&r&e\" does not exist.");
						} else if(defaultDispInherNexGr.equalsIgnoreCase("nextgroup")) {
							Group nextGroup = Group.getGroupByName(value);
							if(nextGroup != null) {
								if(group.setNextGroup(nextGroup)) {
									Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
									return true;
								}
								Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when setting the group \"&f" + group.name + "&r&e\"'s next group(rankup) to \"&f&r&e\"!&z&aPerhaps it was already set to that group?&z&a(Note: you can't set a group as it's own next group.)");
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + value + "&r&e\" does not exist.");
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + defaultDispInherNexGr + "&r&e\".");
						}
					} else if(flag.equalsIgnoreCase("setrequirement")) {// /group {groupName} {setRequirement} {none|money|credit} [value]
						if(defaultDispInherNexGr.equalsIgnoreCase("money")) {
							if(!CodeUtils.isStrAValidDouble(value)) {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid amount given: \"&f" + value + "&r&e\".");
							} else {
								double val = CodeUtils.getDoubleFromStr(value, 0);
								group.moneyOrCredit = "MONEY";
								group.costToRankup = val;
								group.saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								return true;
							}
						} else if(defaultDispInherNexGr.equalsIgnoreCase("credit") || defaultDispInherNexGr.equalsIgnoreCase("credits")) {
							if(!CodeUtils.isStrAValidInt(value)) {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid &fcredit&e amount given: \"&f" + value + "&r&e\".");
							} else {
								int val = CodeUtils.getIntFromStr(value, 0);
								group.moneyOrCredit = "CREDIT";
								group.costToRankup = val;
								group.saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								return true;
							}
						} else if(defaultDispInherNexGr.equalsIgnoreCase("none")) {
							group.moneyOrCredit = "NONE";
							group.costToRankup = 0.0;
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							return true;
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + defaultDispInherNexGr + "&r&e\".");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.");
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " list&e\" or&z&e\"&f/" + command + " {groupName} {add|remove} {permission.node}&e\" or&z&e\"&f/" + command + " {groupName} {create|delete|info}&e\" or&z&e\"&f/" + command + " {groupName} {set} {default|displayName|inheritance} {value}&e\" or&z&e\"&f/" + command + " {groupName} {setRequirement} {none|money|credit} [value]&e\"");
			return true;
		} else if(command.equalsIgnoreCase("bal") || command.equalsIgnoreCase("balance") || command.equalsIgnoreCase("money") || command.equalsIgnoreCase("fe")) {
			if(!Main.handleEconomy) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleEconomy&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.money") && !Permissions.hasPerm(sender, "supercmds.use.bal") && !Permissions.hasPerm(sender, "supercmds.use.balance") && !Permissions.hasPerm(sender, "supercmds.use.fe")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 0) {
				if(user == null) {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				} else {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
					Main.sendMessage(user, Main.pluginName + "&aYou have \"&f" + Main.decimal.format(eco.balance) + "&r&a\" &f" + Main.moneyTerm + "&r&a in your pocket and &f" + eco.credits + " credits&a to your name.");
					eco.saveToFile();//Ah, to remove or not to remove, that is the question...
					return true;
				}
			} else if(args.length == 1) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target != null) {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
					Main.sendMessage(sender, Main.pluginName + "&aPlayer \"&f" + targetName + "&r&a\" has \"&f" + Main.decimal.format(eco.balance) + "&r&a\" &f" + Main.moneyTerm + "&r&a in their pocket and has &f" + eco.credits + " credits&a to their name.");
					eco.disposeIfPlayerNotOnline();
					return true;
				}
				Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
			}
			if(user == null) {
				Main.sendMessage(sender, Main.pluginName + "&eNon-player usage: \"&f/" + command + " {targetName}&e\"");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\" or \"&f/" + command + " [targetName]&e\"");
			}
			return true;
		} else if(command.equalsIgnoreCase("eco") || command.equalsIgnoreCase("econ") || command.equalsIgnoreCase("economy")) {
			if(!Main.handleEconomy) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleEconomy&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.eco") && !Permissions.hasPerm(sender, "supercmds.use.econ") && !Permissions.hasPerm(sender, "supercmds.use.economy")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("info"))) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target != null) {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
					Main.sendMessage(sender, Main.pluginName + "&aThe player &f" + targetName + "&r&a has &f" + Main.decimal.format(eco.balance) + " " + Main.moneyTerm + "&r&a and &f" + eco.credits + " credits&a to their name.");
					return true;
				}
				Main.sendMessage(sender, Main.getNoPlayerMsg(args[0]));
				return true;
			} else if(args.length == 4) {
				String flag = args[0];
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[1]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				String moneyCredit = args[2];
				String value = args[3];
				if(target != null) {
					if(!CodeUtils.isStrAValidDouble(value)) {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid amount given: \"&f" + value + "&r&e\".");
					} else {
						double amount = CodeUtils.getDoubleFromStr(value, 0);
						int intAmount = Double.valueOf(Math.round(amount)).intValue();
						PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
						if(flag.equalsIgnoreCase("give")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.balance += amount;
								Main.sendMessage(sender, Main.pluginName + "&aYou gave &f" + Main.decimal.format(amount) + " " + Main.moneyTerm + "&r&a to player \"&f" + targetName + "&r&a\". They now have &f" + Main.decimal.format(eco.balance) + " " + Main.moneyTerm + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(moneyCredit.equalsIgnoreCase("credit")) {
								eco.credits += intAmount;
								Main.sendMessage(sender, Main.pluginName + "&aYou gave &f" + intAmount + "&r&a credits to player \"&f" + targetName + "&r&a\". They now have &f" + eco.credits + "&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + moneyCredit + "&r&e\".");
							}
						} else if(flag.equalsIgnoreCase("take")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.balance -= amount;
								Main.sendMessage(sender, Main.pluginName + "&aYou took &f" + Main.decimal.format(amount) + " " + Main.moneyTerm + "&r&a from player \"&f" + targetName + "&r&a\". They now have &f" + Main.decimal.format(eco.balance) + " " + Main.moneyTerm + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(moneyCredit.equalsIgnoreCase("credit")) {
								eco.credits -= intAmount;
								Main.sendMessage(sender, Main.pluginName + "&aYou took &f" + intAmount + "&r&a credits from player \"&f" + targetName + "&r&a\". They now have &f" + eco.credits + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + moneyCredit + "&r&e\".");
							}
						} else if(flag.equalsIgnoreCase("set")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.balance = amount;
								Main.sendMessage(sender, Main.pluginName + "&aYou set \"&f" + targetName + "&r&a\"'s  &f" + Main.moneyTerm + "&r&a to &f" + Main.decimal.format(eco.balance) + " " + Main.moneyTerm + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(moneyCredit.equalsIgnoreCase("credit")) {
								eco.credits = intAmount;
								Main.sendMessage(sender, Main.pluginName + "&aYou set player \"&f" + targetName + "&r&a\"'s credit to &f" + eco.credits + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + moneyCredit + "&r&e\".");
							}
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
		} else if(command.equalsIgnoreCase("speed")) {
			//TODO Player fly/walk speed; requires permission
			return false;
		} else if(command.equalsIgnoreCase("hat")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.hat")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 0) {
				if(user != null) {
					ItemStack itemInHand = user.getInventory().getItemInHand();
					if(itemInHand != null && itemInHand.getType() != Material.AIR) {
						ItemStack itemOnHead = user.getInventory().getHelmet();
						if(itemOnHead != null && itemOnHead.getType() != Material.AIR) {
							user.getInventory().setItemInHand(itemOnHead);
						} else {
							user.getInventory().setItemInHand(null);
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
		} else if(command.equalsIgnoreCase("title")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.title")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user != null) {
				if(args.length >= 1) {
					final String arg0 = Main.getElementsFromStringArrayAtIndexAsString(args, 0);
					PlayerChat chat = PlayerChat.getPlayerChat(user);
					if(arg0.equalsIgnoreCase("clear")) {
						chat.setPrefix(null);
						Main.sendMessage(user, Main.pluginName + "&aYour title was cleared successfully.&z&aYour display name is now: &f" + user.getDisplayName());
						return true;
					}
					int arg0Length = Main.stripColorCodes(Main.formatColorCodes(arg0)).length();
					if(arg0Length <= 16) {
						if(chat.setPrefix(arg0)) {
							Main.sendMessage(user, Main.pluginName + "&aYour prefix has been set to \"&f" + chat.getPrefix() + "&r&a\"!&z&aYour display name is now: &f" + user.getDisplayName());
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when setting your title to \"&f" + arg0 + "&r&e\"!&z&aIs it the same title that you had before? If not,&z&athen please contact a server administrator about this issue.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eSorry, but chat titles cannot exceed 16 characters in length.&z&aNote: Color codes do not count towards this limit.");
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {args...|clear}&e\"");
				return true;
			}
			Main.sendMessage(sender, Main.getPlayerOnlyMsg());
			return true;
		} else if(command.equalsIgnoreCase("nick") || command.equalsIgnoreCase("nickname")) {
			if(Main.handleChat) {
				if(!Permissions.hasPerm(sender, "supercmds.use.nick") && !Permissions.hasPerm(sender, "supercmds.use.nickname")) {
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
		} else if(command.equalsIgnoreCase("realname") || command.equalsIgnoreCase("ign")) {
			if(Main.handleChat) {
				if(args.length == 1) {
					String nickName = args[0];
					String realName = null;
					for(PlayerChat chat : PlayerChat.getInstances()) {
						if(Main.stripColorCodes(Main.formatColorCodes(chat.nickname)).equalsIgnoreCase(nickName)) {
							nickName = chat.getNickName();
							realName = Main.uuidMasterList.getPlayerNameFromUUID(chat.uuid);
							break;
						}
					}
					if(realName != null) {
						Main.sendMessage(sender, Main.pluginName + "&f\"" + nickName + "&r&a\"'s real name is: &f" + realName);
						return true;
					}
					Main.sendMessage(sender, Main.pluginName + "&eThere is no one online with the nick name&z&e\"&f" + nickName + "&r&e\".");
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetNickName}&e\"");
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
			return true;
		} else if(command.equalsIgnoreCase("managechat")) {// Admin command for managing player prefixes, nicknames, and suffixes; requires permission
			if(Main.handleChat) {
				if(!Permissions.hasPerm(sender, "supercmds.use.managechat")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					Main.sendConsoleMessage(Main.pluginName + sender.getName() + "&c did not have permission to use managechat.");//debug
					return true;
				}
				if(args.length >= 2) {
					UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
					String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
					if(target == null) {
						Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
					} else {
						PlayerChat chat = PlayerChat.getPlayerChat(target);
						String flag = args[1];
						if(args.length == 2) {// /chat {targetName} {info}
							if(flag.equalsIgnoreCase("info")) {
								Main.sendMessage(sender, Main.pluginName + "&3Displaying player \"&f" + targetName + "&r&3\"'s chat information:&z&f" + chat.toString());
								chat.disposeIfPlayerNotOnline();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
						} else if(args.length == 3) {// /chat {targetName} {prefix|nickname|suffix} {args...|clear}
							String value = Main.getElementsFromStringArrayAtIndexAsString(args, 2);
							if(flag.equalsIgnoreCase("prefix")) {
								if(value.equalsIgnoreCase("clear")) {
									chat.setPrefix(null);
								} else {
									chat.setPrefix(value);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s prefix to: \"&f" + chat.prefix + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								chat.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(flag.equalsIgnoreCase("nickname")) {
								if(value.equalsIgnoreCase("clear")) {
									chat.setNickname(null);
								} else {
									chat.setNickname(value);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s nickname to: \"&f" + chat.nickname + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								chat.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(flag.equalsIgnoreCase("suffix")) {
								if(value.equalsIgnoreCase("clear")) {
									chat.setSuffix(null);
								} else {
									chat.setSuffix(value);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s suffix to: \"&f" + chat.suffix + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								chat.saveAndDisposeIfPlayerNotOnline();
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
						} else if(args.length == 4) {// /chat {targetName} {set} {color|magiccolor} {enable|disable}
							chat.disposeIfPlayerNotOnline();
							if(flag.equalsIgnoreCase("set")) {
								String colorMagic = args[2];
								String enableDisable = args[3];
								PlayerPermissions perm = PlayerPermissions.getPlayerPermissions(target);
								if(colorMagic.equalsIgnoreCase("color")) {
									if(enableDisable.equalsIgnoreCase("enable")) {
										perm.setPermission("supercmds.chat.colors", true);
										Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										perm.saveAndDisposeIfPlayerNotOnline();
										return true;
									} else if(enableDisable.equalsIgnoreCase("disable")) {
										perm.setPermission("supercmds.chat.colors", false);
										Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										perm.saveAndDisposeIfPlayerNotOnline();
										return true;
									} else {
										Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + enableDisable + "&r&e\".");
									}
								} else if(colorMagic.equalsIgnoreCase("magiccolor")) {
									if(enableDisable.equalsIgnoreCase("enable")) {
										perm.setPermission("supercmds.chat.colors.magic", true);
										Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										perm.saveAndDisposeIfPlayerNotOnline();
										return true;
									} else if(enableDisable.equalsIgnoreCase("disable")) {
										perm.setPermission("supercmds.chat.colors.magic", false);
										Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
										perm.saveAndDisposeIfPlayerNotOnline();
										return true;
									} else {
										Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + enableDisable + "&r&e\".");
									}
								} else {
									Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + colorMagic + "&r&e\".");
								}
								perm.disposeIfPlayerNotOnline();
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
							}
						}
						chat.disposeIfPlayerNotOnline();
					}
				}
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName} {info}&e\" or \"&f/" + command + " {targetName} {prefix|nickname|suffix} {args...|clear}&e\" or \"&f/" + command + " {targetName} {set} {color|magiccolor} {enable|disable}&e\"");
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
			return true;
		} else if(command.equalsIgnoreCase("invsee") || command.equalsIgnoreCase("peek")) {
			//TODO Admin command for looking at and/or editing other player's inventories; requires permission
			return false;
		} else if(command.equalsIgnoreCase("setwarp")) {// /setwarp {warpname} [requireperm|requiregroup] [perm|groupname]
			if(!Permissions.hasPerm(sender, "supercmds.use.setwarp")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null) {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			if(args.length == 3 && !Permissions.hasPerm(sender, "supercmds.use.setwarp.require")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length >= 1) {
				String warpName = args[0];
				Warp warp = Warps.getWarpByName(warpName);
				Group group = (args.length == 3 ? (args[1].equalsIgnoreCase("requiregroup") ? Group.getGroupByName(args[2]) : null) : null);
				String requiredPerm = (args.length == 3 ? (args[1].equalsIgnoreCase("requireperm") ? args[2] : null) : null);
				if(args.length == 3 && args[1].equalsIgnoreCase("requiregroup") && group == null) {
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + args[2] + "&r&e\" does not exist.");
				} else if(args.length == 3 && args[1].equalsIgnoreCase("requireperm") && (requiredPerm == null || requiredPerm.isEmpty())) {
					Main.sendMessage(sender, Main.pluginName + "&eA group cannot require a null permission; no one would ever be allowed in the group!");
				} else if(args.length == 1 || args.length == 3) {
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
		} else if(command.equalsIgnoreCase("warp")) {// /warp {warpName}
			if(!Permissions.hasPerm(sender, "supercmds.use.warp")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 2) {
				if(!Permissions.hasPerm(sender, "supercmds.use.warp.others")) {
					Main.sendMessage(sender, Main.pluginName + Main.noPerm);
					return true;
				}
				String warpName = args[0];
				Warp warp = Warps.getWarpByName(warpName);
				if(warp == null) {
					Main.sendMessage(user, Main.pluginName + "&eThe warp \"&f" + warpName + "&r&e\" does not exist.&z&aTry setting it with &f/setwarp&a!");
					return true;
				}
				Player target = Main.getPlayer(args[1]);
				if(target == null) {
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[1]));
					return true;
				}
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(target);
				if(warp.requiredGroup != null) {
					if(!perms.isAMemberOfGroup(warp.requiredGroup)) {
						Main.sendMessage(sender, Main.pluginName + "&eTarget player is not a member of that warp's required group(\"&f" + warp.requiredGroup.displayName + "&r&e\").");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
				} else if(warp.requiredPermission != null && !warp.requiredPermission.isEmpty()) {
					if(!perms.hasPermission(warp.requiredPermission)) {
						Main.sendMessage(sender, Main.pluginName + "&eTarget player does not have the permission that this warp requires" + (sender.isOp() ? "(\"&b" + warp.requiredPermission + "&r&e\")" : "") + "!");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
				}
				if(target.teleport(warp.location)) {
					Main.sendMessage(target, Main.pluginName + "&aWarping to &6/warp " + warp.name + "&r&a.");
					Main.sendMessage(sender, Main.pluginName + "&aSent player \"&f" + target.getDisplayName() + "&r&a\" to &6/warp " + warp.name + "&r&a.");
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong during the teleport! Is the world loaded in that location?");
				}
				perms.disposeIfPlayerNotOnline();
				return true;
			} else if(args.length == 1) {
				if(user == null) {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
					return true;
				}
				String warpName = args[0];
				Warp warp = Warps.getWarpByName(warpName);
				if(warp == null) {
					Main.sendMessage(user, Main.pluginName + "&eThe warp \"&f" + warpName + "&r&e\" does not exist.&z&aTry setting it with &f/setwarp&a!");
					return true;
				}
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
				if(warp.requiredGroup != null) {
					if(!perms.isAMemberOfGroup(warp.requiredGroup)) {
						Main.sendMessage(user, Main.pluginName + "&eYou are not a member of that warp's required group(\"&f" + warp.requiredGroup.displayName + "&r&e\")!");
						perms.disposeIfPlayerNotOnline();
						return true;
					}
				} else if(warp.requiredPermission != null && !warp.requiredPermission.isEmpty()) {
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
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {warpname}&e\" or \"&f/" + command + " {warpname} [target]&e\"");
			return true;
		} else if(command.equalsIgnoreCase("warps")) {
			if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("list"))) {
				Main.sendMessage(sender, Main.pluginName + "&aListing all warps...");
				int i = 0;
				for(Warp warp : Warps.getAllWarps()) {
					Main.sendMessage(sender, "&f[&3" + i + "&f]: &6/warp " + warp.name);
					i++;
				}
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " [list]&e\"");
			return true;
		} else if(command.equalsIgnoreCase("delwarp")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.delwarp")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				String warpName = args[0];
				if(warpName.equalsIgnoreCase("undo") && user != null) {
					Warp deletedWarp = status.lastDeletedWarp;
					if(deletedWarp != null) {
						Warps.warps.remove(deletedWarp);//Just in case; it happened once.
						Warps.warps.add(deletedWarp);
						status.lastDeletedWarp = null;
						Main.sendMessage(user, Main.pluginName + "&aSuccessfully restored the deleted warp &6/warp " + deletedWarp.name + "&a.");
						deletedWarp = null;
					} else {
						Main.sendMessage(user, Main.pluginName + "&eYou have not deleted any warps recently, or it has been deleted permanently since that time.");
					}
					return true;
				}
				Warp warp = Warps.getWarpByName(warpName);
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
		} else if(command.equalsIgnoreCase("rankup") || command.equalsIgnoreCase("nextgroup")) {
			if(!Main.handlePermissions) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandlePermissions&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.rankup") && !Permissions.hasPerm(sender, "supercmds.use.nextgroup")) {
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
								if(perms.group.nextGroup.costToRankup > 0) {
									PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
									if(eco.balance < perms.group.nextGroup.costToRankup) {
										Main.sendMessage(user, Main.pluginName + "&eYou do not have enough &f" + Main.moneyTerm + "&r&e to rank up to the next group!&z&aYou need &f" + Main.decimal.format(perms.group.nextGroup.costToRankup - eco.balance) + "&r&a more(the next rank costs &f" + Main.decimal.format(perms.group.nextGroup.costToRankup) + "&r&a)!");
										perms.disposeIfPlayerNotOnline();
										return true;
									}
									eco.balance -= perms.group.nextGroup.costToRankup;
									eco.saveToFile();
									Main.sendMessage(user, Main.pluginName + "&f" + Main.decimal.format(perms.group.nextGroup.costToRankup) + "&e &f" + Main.moneyTerm + "&r&e has been taken from your account.");
								}
							} else if(perms.group.nextGroup.moneyOrCredit.equals("CREDIT")) {
								final int cost = new Double(Math.round(perms.group.nextGroup.costToRankup)).intValue();
								if(cost > 0) {
									PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
									if(eco.credits < cost) {
										Main.sendMessage(user, Main.pluginName + "&eYou do not have enough &fcredit&r&e to rank up to the next group!&z&aYou need &f" + (cost - eco.credits) + "&r&a more(the next rank requires at least &f" + cost + "&r&a credits)!");
										perms.disposeIfPlayerNotOnline();
										return true;
									}
									eco.credits -= cost;
									eco.saveToFile();
									Main.sendMessage(user, Main.pluginName + "&f" + cost + " credits&r&e have been taken from your account.&z&aYou now have &f" + eco.credits + " credits&a.");
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
				if(perms.changeGroup(Group.getDefaultGroup())) {
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
		} else if(command.equalsIgnoreCase("mail")) {// /mail send {target} {args...} or /mail read or /mail {markread|clear} or /mail readold
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
				if(args.length == 0 || (args.length >= 1 && args.length <= 2 && (args[0].equalsIgnoreCase("read") || args[0].equalsIgnoreCase("readold")))) {
					if(args.length == 0 || (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("read"))) {
						if(!chat.mailbox.isEmpty()) {
							double maxLinesPerPage = 10.00D;
							final Mail[] allMail = chat.getMailInOrder();
							if(allMail.length != 0) {
								int maxPages = CodeUtils.getIntFromStr(Main.decimalRoundUp.format(allMail.length / maxLinesPerPage), 0);
								int page = 0;
								if(args.length == 2) {
									if(!CodeUtils.isStrAValidInt(args[1])) {
										Main.sendMessage(user, Main.pluginName + "&eYou have entered an invalid page number(\"&f" + args[1] + "&r&e is not a valid integer\").");
										return true;
									}
									page = CodeUtils.getIntFromStr(args[1], page) - 1;//Java starts at 0, but humans start at 1... my brain hurts.
									if(page >= maxPages) {
										Main.sendMessage(user, Main.pluginName + "&eYou only have &f" + maxPages + "&e pages of mail.");
										return true;
									} else if(page <= -1) {
										Main.sendMessage(user, Main.pluginName + "&ePage numbers must be greater than &fzero&e.");
										return true;
									}
								}
								final int p = (int) (page * maxLinesPerPage);
								Main.sendMessage(user, Main.pluginName + "&6 ===[ Unread mail: Page &f" + (page + 1) + "&6 / &f" + maxPages + "&6 ]===");
								for(int i = p; i < allMail.length; i++) {
									if(i > p + maxLinesPerPage) {//pages of 10 at a time
										break;
									}
									Main.sendMessage(user, "&f[&3" + (i + 1) + "&f]: " + (allMail[i] != null ? allMail[i].msg : "&cnull(&f?&c)&f"));
								}
								return true;
							}
							Main.sendMessage(user, Main.pluginName + "&eAn unusual circumstance has occurred that is preventing you from reading your mail.");
							Main.sendMessage(user, "&f(Reason: &b&lunable to organize mail by date&r&f)");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eYou have no unread mail!&z&aType &f/mail readold&a to see if you have any old mail.");
					} else if(args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("readold")) {
						if(!chat.oldMail.isEmpty()) {
							double maxLinesPerPage = 10.00D;
							final Mail[] allMail = chat.getOldMailInOrder();
							if(allMail.length != 0) {
								int maxPages = CodeUtils.getIntFromStr(Main.decimalRoundUp.format(allMail.length / maxLinesPerPage), 0);
								int page = 0;
								if(args.length == 2) {
									if(!CodeUtils.isStrAValidInt(args[1])) {
										Main.sendMessage(user, Main.pluginName + "&eYou have entered an invalid page number(\"&f" + args[1] + "&r&e is not a valid integer\").");
										return true;
									}
									page = CodeUtils.getIntFromStr(args[1], page) - 1;//Java starts at 0, but humans start at 1... my brain hurts.
									if(page >= maxPages) {
										Main.sendMessage(user, Main.pluginName + "&eYou only have &f" + maxPages + "&e pages of old mail.");
										return true;
									} else if(page <= -1) {
										Main.sendMessage(user, Main.pluginName + "&ePage numbers must be greater than &fzero&e.");
										return true;
									}
								}
								final int p = (int) (page * maxLinesPerPage);
								Main.sendMessage(user, Main.pluginName + "&6 ===[ Old mail: Page &f" + (page + 1) + "&6 / &f" + maxPages + "&6 ]===");
								for(int i = p; i < allMail.length; i++) {
									if(i > p + maxLinesPerPage) {//pages of 10 at a time //Yes I totally copied my own code :p XD
										break;
									}
									Main.sendMessage(user, "&f[&3" + (i + 1) + "&f]: " + (allMail[i] != null ? allMail[i].msg : "&cnull(&f?&c)&f"));
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
				} else if(args.length == 1) {
					if(args[0].equalsIgnoreCase("markread") || args[0].equalsIgnoreCase("clear")) {// /mail read/readold is above.
						final HashMap<UUID, ArrayList<Mail>> mail = chat.mailbox;
						for(Map.Entry<UUID, ArrayList<Mail>> entry : mail.entrySet()) {
							ArrayList<Mail> oldMails = chat.oldMail.get(entry.getKey());
							for(Mail m : entry.getValue()) {
								if(oldMails == null) {
									oldMails = new ArrayList<>();
								}
								if(!oldMails.contains(m)) {//potentially unnecessary if statement.
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
			} else if(sender == Main.console) {
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
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f send {target} {args...}&e\" or&z&e\"&f/" + command + "&r&f read&e\" or&z&e\"&f/" + command + "&r&f {markread|clear}&e\" or&z&e\"&f/" + command + "&r&f readold&e\"");
			return true;
		} else if(command.equalsIgnoreCase("msg")) {
			if(!Main.handleChat) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(user != null || sender == Main.console) {
				final String senderName = (user != null ? "&f[&3" + user.getName() + "&f]" : Main.consoleSayFormat.trim());
				if(args.length >= 2) {
					final String msg = Main.getElementsFromStringArrayAtIndexAsString(args, 1);
					final UUID target = Main.getPlayer(args[0]) != null ? Main.getPlayer(args[0]).getUniqueId() : (args[0].equalsIgnoreCase("console") ? Main.consoleUUID : null);
					final String targetName = target != null ? (target.toString().equals(Main.consoleUUID.toString()) ? Main.consoleSayFormat.trim() : "&f[" + Main.uuidMasterList.getPlayerNameFromUUID(target) + "]") : "";
					if(target != null) {
						if(user != null && user.getUniqueId().toString().equals(target.toString())) {
							Main.sendMessage(sender, Main.pluginName + "&aYou so silly.");
							return true;
						}
						Main.sendMessage(target.toString().equals(Main.consoleUUID.toString()) ? Main.console : Main.server.getPlayer(target), "&6{&f" + senderName + " &6--> me}&f: " + msg);
						Main.sendMessage(sender, "&6{me --> " + targetName + "&6}&f: " + msg);
						if(user != null) {
							PlayerChat chat = PlayerChat.getPlayerChat(user);
							chat.lastPlayerThatIRepliedTo = target;
							chat.saveToFile();
						}
						return true;
					}
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
				}
			} else {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&r&f {target} {args...}&e\"");
			return true;
		} else if(command.equalsIgnoreCase("r")) {
			if(!Main.handleChat) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandleChat&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}
			if(user != null || sender == Main.console) {
				final UUID me = (user != null ? user.getUniqueId() : Main.consoleUUID);
				if(args.length == 2 && args[0].equalsIgnoreCase("set")) {
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
					final String msg = Main.getElementsFromStringArrayAtIndexAsString(args, 0);
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
		} else if(command.equalsIgnoreCase("kit") || command.equalsIgnoreCase("kits")) {
			/*if(!Main.handlePermissions) {
				Main.sendMessage(sender, Main.pluginName + "&eThis plugin's '&fhandlePermissions&e' option was set to &cfalse&e in the config.yml;&z&eTherefore this command has been disabled.");
				return true;
			}*/
			if(!Permissions.hasPerm(sender, "supercmds.use.kit")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null && ((args.length == 1 && args[0].equalsIgnoreCase("list")) || command.equalsIgnoreCase("kits"))) {
				ArrayList<Kit> kits = Kits.getAllKits();
				if(kits.size() > 0) {
					Main.sendMessage(sender, Main.pluginName + "&3Listing all kits:");
					int i = 0;
					for(Kit kit : kits) {
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
			if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("list"))) {
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
				ArrayList<Kit> kitsForUser = new ArrayList<>();
				ArrayList<Kit> allKits = Kits.getAllKits();
				if(allKits.size() == 0) {
					Main.sendMessage(user, Main.pluginName + "&eThere are no kits set up at this time. Please try again later!");
					return true;
				}
				for(Kit kit : allKits) {
					boolean iCanHasKit = true;
					if(kit.requiredGroup != null) {
						if(Main.handlePermissions) {
							if(!perms.isAMemberOfGroup(kit.requiredGroup)) {
								iCanHasKit = false;
							}
						}
					}
					if(kit.requiredPermission != null && !kit.requiredPermission.isEmpty()) {
						if(!perms.hasPermission(kit.requiredPermission)) {
							iCanHasKit = false;
						}
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
				for(Kit kit : kitsForUser) {
					Main.sendMessage(user, Main.pluginName + "&f[&3" + i + "&f]: &6/kit " + kit.name);
					i++;
				}
				return true;
			} else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("claimItems")) {
					if(status.leftOverItems.isEmpty()) {
						Main.sendMessage(user, Main.pluginName + "&eYou have no unclaimed items saved!");
						return true;
					}
					ItemStack[] items = new ItemStack[status.leftOverItems.size()];
					for(int i = 0; i < items.length; i++) {
						items[i] = status.leftOverItems.get(i);
					}
					status.leftOverItems.clear();//prevents item duping
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
				Kit kit = Kits.getKitByName(args[0]);
				if(kit != null) {
					if(!status.canGetKit(kit)) {
						long futureTime = System.currentTimeMillis() + kit.obtainInterval;
						SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy @ HH:mm:ss");
						Date resultdate = new Date(futureTime);
						String time = sdf.format(resultdate);
						Main.sendMessage(user, Main.pluginName + "&eYou can't get that kit again until &f" + time + "&r&e!");
						Main.sendMessage(user, Main.pluginName + "&a(The time is currently &f" + new SimpleDateFormat("MMM dd, yyyy @ HH:mm:ss").format(new Date(System.currentTimeMillis())) + "&r&a).");
						return true;
					}
					if(Main.handlePermissions) {
						if(kit.requiredGroup != null) {
							PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
							if(!perms.isAMemberOfGroup(kit.requiredGroup)) {
								Main.sendMessage(user, Main.pluginName + "&eYou are not a member of that kit's required group(\"&f" + kit.requiredGroup.displayName + "&r&e\")!");
								return true;
							}
						}
					}
					if(kit.requiredPermission != null && !kit.requiredPermission.isEmpty()) {
						PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
						if(!perms.hasPermission(kit.requiredPermission)) {
							Main.sendMessage(user, Main.pluginName + "&eYou do not have the permission that this kit requires" + (user.isOp() ? "(\"&b" + kit.requiredPermission + "&r&e\")" : "") + "!");
							return true;
						}
					}
					if(kit.rewardMoney > 0) {
						PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
						eco.balance += kit.rewardMoney;
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
					} else {
						if(kit.rewardMoney <= 0) {
							Main.sendMessage(user, Main.pluginName + "&eThis kit doesn't appear to have any items or rewards set yet...&z&aPlease let a server staff member know so that they can fix it!");
						}
					}
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eThe kit \"&f" + args[0] + "&r&e\" does not exist.");
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " [kitName]&e\"");
			return true;
		} else if(command.equalsIgnoreCase("time")) {
			Main.sendMessage(sender, Main.pluginName + "&aThe time is currently &f" + new SimpleDateFormat("MMM dd, yyyy @ HH:mm:ss").format(new Date(System.currentTimeMillis())) + "&r&a.");
			return true;
		} else if(command.equalsIgnoreCase("smite")) {
			Main.DEBUG("&fSmite sender class: &3" + sender.getClass().getName());
			if(sender instanceof org.bukkit.craftbukkit.v1_8_R1.command.CraftBlockCommandSender && args.length != 4) {
				return true;
			}
			if(!Permissions.hasPerm(sender, "supercmds.use.smite")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1) {
				Player target = Main.getPlayer(args[0]);
				if(target == null) {
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
					return true;
				}
				target.getWorld().strikeLightning(target.getLocation());
				Main.sendMessage(target, Main.pluginName + "&eThou hast been smited!");
				Main.sendMessage(sender, Main.pluginName + "&aYou just smited \"&f" + target.getDisplayName() + "&r&a\".&z&aI hope they deserved it... XD");
				return true;
			} else if(args.length == 4) {//smite world x y z
				World world = Main.server.getWorld(args[0]);
				boolean X = CodeUtils.isStrAValidDouble(args[1]);
				boolean Y = CodeUtils.isStrAValidDouble(args[2]);
				boolean Z = CodeUtils.isStrAValidDouble(args[3]);
				double x = CodeUtils.getDoubleFromStr(args[1], Double.NaN);
				double y = CodeUtils.getDoubleFromStr(args[2], Double.NaN);
				double z = CodeUtils.getDoubleFromStr(args[3], Double.NaN);
				if(world != null) {
					if(X && Y && Z) {
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
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName}&e\" or \"&f/" + command + " {worldName} {x} {y} {z}&e\"");
			return true;
		} else if(command.equalsIgnoreCase("managekit") || command.equalsIgnoreCase("managekits")) {// /managekits {name} {create} || /managekits {name} {set} {reward|items} {amount|items.../clearitems}
			if(!Permissions.hasPerm(sender, "supercmds.use.managekit") && !Permissions.hasPerm(sender, "supercmds.use.managekits")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 2) {
				if(args[1].equalsIgnoreCase("create")) {
					Kit check = Kits.getKitByName(args[0]);
					if(check != null) {
						Main.sendMessage(sender, Main.pluginName + "&eA kit with that name already exists!");
						return true;
					}
					Kit kit = Kits.createKit(args[0]);
					Kits.getInstance().saveToFile();
					Main.sendMessage(sender, Main.pluginName + "&aSuccessfully created the kit &6/kit " + kit.name + "&r&a!");
					return true;
				}
				Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + args[1] + "&r&e\".");
			} else if(args.length >= 4) {
				Kit kit = Kits.getKitByName(args[0]);
				String flag = args[1];
				String rewardItems = args[2];
				String value = Main.getElementsFromStringArrayAtIndexAsString(args, 3);
				if(kit != null) {
					if(flag.equalsIgnoreCase("set")) {
						if(rewardItems.equalsIgnoreCase("reward") && args.length == 4) {
							if(CodeUtils.isStrAValidDouble(value)) {
								double amount = CodeUtils.getDoubleFromStr(value, 0);
								kit.rewardMoney = amount;
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set the kit &6/kit " + kit.name + "&r&a's reward money amount to &f" + Main.decimal.format(amount) + "&r&f " + Main.moneyTerm + "&r&a!");
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eInvalid amount given: \"&f" + value + "&r&e\".");
							return true;
						} else if(rewardItems.equalsIgnoreCase("items")) {//XXX Weeeee! Itemstacks from string, here I go!
							if(value.equalsIgnoreCase("clearitems")) {
								kit.items = new ItemStack[0];
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully cleared out the kit &6/kit " + kit.name + "&r&a's items.&z&aBe sure to add some back in!");
								return true;
							}
							ArrayList<ItemStack> madeItems = MainCmdListener.getItemStacksFromString(value);
							if(madeItems.isEmpty()) {
								Main.sendMessage(sender, Main.pluginName + "&eYou have entered an invalid itemstack: \"&f" + value + "&r&e\"");
								Main.sendMessage(sender, Main.pluginName + "&aItemStack example: &fminecraft:diamond_ore 64 0&a would result in 64 diamond ore blocks in one stack.&z&aThe zero is the metadata/damage value.&z&aThe \"&fminecraft:&r&a\" part is optional.&z&z&aYou can enter multiple stacks at once by separating them with a semi-colon(';').");
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
						} else if(rewardItems.equalsIgnoreCase("requiredgroup") && args.length == 4) {
							if(!Permissions.hasPerm(sender, "supercmds.use.managekit.require") && !Permissions.hasPerm(sender, "supercmds.use.managekits.require")) {
								Main.sendMessage(sender, Main.pluginName + Main.noPerm);
								return true;
							}
							if(value.equalsIgnoreCase("clear")) {
								kit.requiredGroup = null;
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully removed the required group from the kit &6/kit " + kit.name + "&r&a!");
								return true;
							}
							Group group = Group.getGroupByName(value);
							if(group != null) {
								kit.requiredGroup = group;
								Kits.getInstance().saveToFile();
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set the kit &6/kit " + kit.name + "&r&a's required group to \"&f" + kit.requiredGroup.displayName + "&r&a\"!");
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + value + "&r&e\" does not exist.");
							}
							return true;
						} else if(rewardItems.equalsIgnoreCase("requiredperm") && args.length == 4) {
							if(!Permissions.hasPerm(sender, "supercmds.use.managekit.require") && !Permissions.hasPerm(sender, "supercmds.use.managekits.require")) {
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
						} else {
							Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + rewardItems + "&r&e\".");
						}
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThe kit \"&f" + args[0] + "&r&e\" does not exist.");
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {name} {create}&e\" or \"&f/" + command + " {name} {set} {reward|items|requiredgroup|requiredperm} {amount|items.../clearitems|groupName/permNode/clear}&e\"");
			return true;
		} else if(command.equalsIgnoreCase("loadplugin")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.loadplugin")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 1 || (args.length == 2 && args[0].equalsIgnoreCase("-fullpath"))) {
				File file = (args.length == 1 ? new File(Main.dataFolder.getParentFile(), args[0]) : new File(args[1]));
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
		} else if(command.equalsIgnoreCase("enableplugin")) {
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
		} else if(command.equalsIgnoreCase("disableplugin")) {
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
		} else {
			return false;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			PlayerStatus status = PlayerStatus.getPlayerStatus(player);
			if(status.isGodModeOn && Permissions.hasPerm(player, "supercmds.use.god")) {
				int newLevel = player.getFoodLevel() + 1;
				event.setFoodLevel(newLevel);
			} else if(status.isGodModeOn) {
				PlayerStatus.updatePlayerGodModeStates();
			}
		}
	}
	
	public static final ArrayList<ItemStack> getItemStacksFromString(String str) {
		if(str == null || str.isEmpty()) {
			return null;
		}
		ArrayList<ItemStack> rtrn = new ArrayList<>();
		String[] split = str.split(";");
		for(String s : split) {
			s = s.replace("minecraft:", "").trim();//XXX If the Mojang Mod-API is *ever* finished in the future, this will need to be adjusted accordingly. Poor Bukkit is DMCA'd atm...
			String[] args = s.split(Main.getWhiteSpaceChars);
			String materialName = "air";
			int amount = 1;
			short damage = 0;
			if(args.length >= 1) {
				materialName = args[0];
			}
			if(args.length >= 2) {
				amount = CodeUtils.getIntFromStr(args[1], amount);
			}
			if(args.length >= 3) {
				damage = CodeUtils.getShortFromStr(args[2], damage);
			}
			Material type = Material.AIR;
			for(Material material : Material.values()) {
				if(material.name().toLowerCase().equals(materialName)) {
					type = material;
					break;
				}
			}
			rtrn.add(new ItemStack(type, amount, damage));
		}
		return rtrn;
	}
	
	public static final void setGameModeForPlayer(CommandSender sender, Player target, String targetName, String arg) {
		if(target == null) {
			Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(targetName));
			return;
		}
		if(!Permissions.hasPerm(sender, "supercmds.use.gamemode")) {
			Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			return;
		}
		boolean isSenderTarget = (sender != null ? sender.getName().equalsIgnoreCase(target.getName()) : false);
		if(!isSenderTarget && !Permissions.hasPerm(sender, "supercmds.use.gamemode.others")) {
			Main.sendMessage(sender, Main.pluginName + Main.noPerm);
			return;
		}
		if(arg.equalsIgnoreCase("0") || arg.equalsIgnoreCase("s") || arg.equalsIgnoreCase("survival")) {
			target.setGameMode(GameMode.SURVIVAL);
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to survival.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to survival.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to survival.");
			}
		} else if(arg.equalsIgnoreCase("1") || arg.equalsIgnoreCase("c") || arg.equalsIgnoreCase("creative")) {
			target.setGameMode(GameMode.CREATIVE);
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to creative.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to creative.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to creative.");
			}
		} else if(arg.equalsIgnoreCase("2") || arg.equalsIgnoreCase("a") || arg.equalsIgnoreCase("adventure")) {
			target.setGameMode(GameMode.ADVENTURE);
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to adventure.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to adventure.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to adventure.");
			}
		} else if(arg.equalsIgnoreCase("3") || arg.equalsIgnoreCase("spec") || arg.equalsIgnoreCase("spectator")) {
			target.setGameMode(GameMode.SPECTATOR); // <--- See?
			if(isSenderTarget) {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to spectator.");
			} else {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to spectator.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode has been changed to spectator.");
			}
			/*Main.sendMessage(sender, Main.pluginName + "&eThe spectator gamemode is supported in this version of minecraft;&z" + //
			"&ehowever, I&f(&4Brian&0_&6Entei&f)&e am unable to find any &lBukkit&r&e 1.8 \"Development Build\" .jars to use&z" + //
			"&efor compiling my plugins due to a certain bullcrap DCMA takedown.&z" + //
			"&eTherefore, switching to this gamemode is currently impossible. Sorry!&z" + //
			"&aIf you have one and can send it to me, I would be delighted! You can contact me at &4br45entei&f@gmail.com&a.");*/
		} else {
			Main.sendMessage(sender, Main.pluginName + "&eThe gamemode you entered(\"&f" + arg + "&r&e\") is not a valid gamemode&z&eor the plugin does not know what to make of it. Please try again!");
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		PlayerStatus status = PlayerStatus.getPlayerStatus(player);
		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(player.getGameMode() == GameMode.SPECTATOR) {
				return;
			}
			if(Permissions.hasPerm(player, "supercmds.use.thor")) {
				ItemStack item = player.getItemInHand();
				if(status.isThorModeOn && item.getType() == Material.IRON_PICKAXE) {
					player.getWorld().strikeLightning(player.getTargetBlock(null, 200).getLocation());
					if(player.getGameMode() != GameMode.CREATIVE) {
						item.setDurability((short) (item.getDurability() + 1));
						if(item.getDurability() >= 250) {
							item = new ItemStack(Material.AIR);
							player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1F, 1F);
						}
						player.setItemInHand(item);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onServerCommandEvent(ServerCommandEvent event) {
		if(Main.startsWithIgnoreCase(event.getCommand(), "op") || Main.startsWithIgnoreCase(event.getCommand(), "deop")) {
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					PlayerStatus.updatePlayerVanishStates();
				}
			});
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if(Main.startsWithIgnoreCase(event.getMessage(), "/op") || Main.startsWithIgnoreCase(event.getMessage(), "/deop")) {
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					PlayerStatus.updatePlayerVanishStates();
				}
			});
		}
		final String command = Main.getCommandFromMsg(event.getMessage()).substring(1);//Substring gets rid of the / that comes by default with player commands.
		final String[] args = Main.getArgumentsFromCommand(event.getMessage());
		@SuppressWarnings("unused")
		final String strArgs = Main.getStringArgumentsFromCommand(args);
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
					if(target != null && CodeUtils.isStrAValidInt(amount) && CodeUtils.isStrAValidShort(metadata)) {
						event.setCancelled(true);
						ItemStack itemToGive = new ItemStack(Material.MOB_SPAWNER, CodeUtils.getIntFromStr(amount, 1), CodeUtils.getShortFromStr(metadata, (short) 50));
						target.getInventory().addItem(itemToGive);
						Main.sendMessage(event.getPlayer(), "&ccommands.give.notFound");//XD
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onBlockRedstoneEvent(org.bukkit.event.block.BlockRedstoneEvent event) {
		if((event.getOldCurrent() == 0 && event.getNewCurrent() == 0) || event.getOldCurrent() == event.getNewCurrent()) {//The event gets spammed for some reason, so I added this check to make it run once.
			return;
		}
		if(event.getBlock().getState() instanceof CommandBlock) {
			CommandBlock block = (CommandBlock) event.getBlock().getState();
			Location loc = block.getLocation();
			Chunk chunk = Main.getChunkAtWorldCoords(block.getWorld(), loc.getBlockX(), loc.getBlockZ());
			block.getWorld().loadChunk(chunk);
			Main.DEBUG("&f[&5TEST!&f]: &5Old current: \"&f" + event.getOldCurrent() + "&r&5\";");
			Main.DEBUG("&f[&5TEST!&f]: &5New current: \"&f" + event.getNewCurrent() + "&r&5\";");
			if(event.getNewCurrent() >= 15) {
				Main.DEBUG("&f[&5TEST!&f]: &2Sure.avi.");
				final String command = Main.getCommandFromMsg(block.getCommand());
				final String[] args = Main.getArgumentsFromCommand(block.getCommand());
				if(command.equalsIgnoreCase("smite")) {
					if(args.length == 1) {
						final ArrayList<Entity> targets = new ArrayList<>();
						if(args[0].startsWith("@e") || args[0].startsWith("@p") || args[0].startsWith("@a")) {
							int minRange = 0;
							int maxRange = Integer.MAX_VALUE;
							String entityType = "ANY";
							if(args[0].charAt(2) == '[' && args[0].endsWith("]")) {
								Main.DEBUG("&f[&5TEST!&f]: &aTest 1");
								String[] cmdArgs = args[0].substring(3, args[0].length() - 1).split(",");
								for(String split : cmdArgs) {
									Main.DEBUG("&f[&5TEST!&f]: &aTest 2: Split: \"&f" + split + "&r&a\"");
									String[] arg = split.split("=");
									Main.DEBUG("&f[&5TEST!&f]: &aTest 3: arg.length: \"&f" + arg.length + "&r&a\"");
									if(arg.length == 2) {
										if(arg[0].equals("type") && !args[0].startsWith("@p")) {
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
									if(entity.getType().name().equalsIgnoreCase(entityType) || entityType.equals("ANY")) {
										Main.DEBUG("&f[&5TEST!&f]: &aTest 8: entity.getType().name(): \"&f" + entity.getType().name() + "&r&a\"");
										xDiff = entity.getLocation().getX() - loc.getX();
										yDiff = entity.getLocation().getY() - loc.getY();
										zDiff = entity.getLocation().getZ() - loc.getZ();
										double curDist = Math.abs(Math.sqrt(Math.pow(xDiff, 2f) + Math.pow(yDiff, 2f) + Math.pow(zDiff, 2f)));
										if(args[0].startsWith("@a")) {
											Main.DEBUG("&f[&5TEST!&f]: &aTest 9_0");
											if(curDist >= minRange && curDist <= maxRange) {
												targets.add(entity);
												Main.DEBUG("&f[&5TEST!&f]: &aTest 9_1: @a added: \"&f" + entity.getType().name() + "&r&a\"");
											}
										} else if(args[0].startsWith("@p")) {
											if(entity instanceof Player) {
												Main.DEBUG("&f[&5TEST!&f]: &aTest 10_0");
												if(curDist >= minRange && curDist <= maxRange) {
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
											if(curDist >= minRange && curDist <= maxRange) {
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
							if(target instanceof Player) {
								Main.sendMessage((Player) target, Main.pluginName + "&eThou hast been smited!");
							}
						}
						org.bukkit.material.Command comm = (org.bukkit.material.Command) block.getData();
						comm.setPowered(true);
						block.setData(comm);
						block.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
						return;
					}
				}
			} else {
				Main.DEBUG("&f[&5TEST!&f]: &4Nope.avi.");
				org.bukkit.material.Command comm = (org.bukkit.material.Command) block.getData();
				comm.setPowered(false);
				block.setData(comm);
				block.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
			}
		}
	}
	
	public static final class PlayerStatus extends SavablePlayerData {
		private static final ArrayList<PlayerStatus>	statuses			= new ArrayList<>();
		public boolean									isAfk				= false;
		public Location									lastAfkLocation		= null;
		public boolean									isThorModeOn		= false;
		public boolean									isGodModeOn			= false;
		public boolean									isFlyModeOn			= false;
		public boolean									isVanishModeOn		= false;
		
		public GameMode									lastGameMode		= null;
		public double									lastHealth			= 20;
		public double									lastMaxHealth		= 20;
		public float									lastFoodSaturation	= 5.0F;
		public int										lastFoodLevel		= 20;
		public int										lastXpLevel			= 0;
		public float									lastXp				= 0;
		public Location									lastTeleportLoc		= null;
		
		public boolean									hasHome				= false;
		public Location									homeLocation		= null;
		public boolean									wasFlyingLast		= false;
		
		public final HashMap<String, Long>				lastKitTimes		= new HashMap<>();
		public final ArrayList<ItemStack>				leftOverItems		= new ArrayList<>();
		
		//=============[Don't save these to file]=============
		
		public Warp										lastDeletedWarp		= null;
		public boolean									isInBed				= false;
		protected int									bedSchedulerTID		= 0;
		
		//=============[End of "Don't save these"]============
		
		@Override
		public String getSaveFolderName() {
			return "PlayerCmdData";
		}
		
		@Override
		public void loadFromConfig(ConfigurationSection mem) {
			this.isAfk = mem.getBoolean("isAfk");
			this.lastAfkLocation = SavablePlayerData.getLocationFromConfig("lastAfkLocation", mem);
			this.isThorModeOn = mem.getBoolean("isThorModeOn");
			this.isGodModeOn = mem.getBoolean("isGodModeOn");
			this.isFlyModeOn = mem.getBoolean("isFlyModeOn");
			this.isVanishModeOn = mem.getBoolean("isVanishModeOn");
			this.lastGameMode = (mem.getString("lastGameMode") != null ? (mem.getString("lastGameMode").equalsIgnoreCase("survival") ? GameMode.SURVIVAL : (mem.getString("lastGameMode").equalsIgnoreCase("creative") ? GameMode.CREATIVE : (mem.getString("lastGameMode").equalsIgnoreCase("adventure") ? GameMode.ADVENTURE : (mem.getString("lastGameMode").equalsIgnoreCase("spectator") ? GameMode.SPECTATOR : (this.isPlayerOnline() ? this.getPlayer().getGameMode() : null))))) : (this.isPlayerOnline() ? this.getPlayer().getGameMode() : null));
			this.lastHealth = mem.getDouble("lastHealth");
			this.lastMaxHealth = mem.getDouble("lastMaxHealth");
			this.lastFoodSaturation = new Double(mem.getDouble("lastFoodSaturation")).floatValue();
			this.lastFoodLevel = mem.getInt("lastFoodLevel");
			this.lastXpLevel = mem.getInt("lastXpLevel");
			this.lastXp = new Double(mem.getDouble("lastXp")).floatValue();
			this.lastTeleportLoc = SavablePlayerData.getLocationFromConfig("lastTeleportLoc", mem);
			this.hasHome = mem.getBoolean("hasHome");
			this.homeLocation = SavablePlayerData.getLocationFromConfig("homeLocation", mem);
			this.wasFlyingLast = mem.getBoolean("wasFlyingLast");
			//===
			try {
				ConfigurationSection lastKitTimes = mem.getConfigurationSection("lastKitTimes");
				if(lastKitTimes != null) {
					for(String key : lastKitTimes.getKeys(false)) {
						long value = lastKitTimes.getLong(key);
						this.lastKitTimes.put(key, new Long(value));
					}
				}
			} catch(Throwable e) {
				Main.sendConsoleMessage(Main.pluginName + "&eAn error occurred while loading lastKitTimes for player \"&f" + this.name + "&r&e\":&z&c" + Main.throwableToStr(e));
			}
			//===
			ItemStack[] leftOverItems = SavablePluginData.getItemsFromConfig("leftOverItems", mem);
			this.leftOverItems.clear();
			for(ItemStack item : leftOverItems) {
				this.leftOverItems.add(item);
			}
		}
		
		@Override
		public void saveToConfig(ConfigurationSection mem) {
			mem.set("isAfk", new Boolean(this.isAfk));
			SavablePlayerData.saveLocationToConfig("lastAfkLocation", this.lastAfkLocation, mem);
			mem.set("isThorModeOn", new Boolean(this.isThorModeOn));
			mem.set("isGodModeOn", new Boolean(this.isGodModeOn));
			mem.set("isFlyModeOn", new Boolean(this.isFlyModeOn));
			mem.set("isVanishModeOn", new Boolean(this.isVanishModeOn));
			mem.set("lastGameMode", (this.lastGameMode != null ? this.lastGameMode.name().toLowerCase() : (this.getPlayer() != null ? this.getPlayer().getGameMode() : GameMode.SURVIVAL).name().toLowerCase()));
			mem.set("lastHealth", Double.valueOf(this.lastHealth));
			mem.set("lastMaxHealth", Double.valueOf(this.lastMaxHealth));
			mem.set("lastFoodSaturation", Float.valueOf(this.lastFoodSaturation));
			mem.set("lastFoodLevel", Integer.valueOf(this.lastFoodLevel));
			mem.set("lastXpLevel", Integer.valueOf(this.lastXpLevel));
			mem.set("lastXp", Float.valueOf(this.lastXp));
			SavablePlayerData.saveLocationToConfig("lastTeleportLoc", this.lastTeleportLoc, mem);
			mem.set("hasHome", new Boolean(this.hasHome));
			SavablePlayerData.saveLocationToConfig("homeLocation", this.homeLocation, mem);
			mem.set("wasFlyingLast", new Boolean(this.wasFlyingLast));
			//===
			ConfigurationSection lastKitTimes = mem.getConfigurationSection("lastKitTimes");
			if(lastKitTimes == null) {
				lastKitTimes = mem.createSection("lastKitTimes");
			}
			for(Map.Entry<String, Long> entry : this.lastKitTimes.entrySet()) {
				lastKitTimes.set(entry.getKey(), entry.getValue());
			}
			//===
			ItemStack[] items = new ItemStack[this.leftOverItems.size()];
			for(int i = 0; i < this.leftOverItems.size(); i++) {
				items[i] = this.leftOverItems.get(i);
			}
			SavablePluginData.saveItemsToConfig("leftOverItems", items, mem);
		}
		
		public final void saveToFileAndDispose() {
			this.saveToFile();
			this.dispose();
		}
		
		public static final PlayerStatus getPlayerStatus(Player player) {
			if(player == null) {
				return null;
			}
			return PlayerStatus.getPlayerStatus(player.getUniqueId());
		}
		
		public static final PlayerStatus getPlayerStatus(UUID uuid) {
			for(PlayerStatus status : PlayerStatus.statuses) {
				if(status.uuid.equals(uuid)) {
					return status;
				}
			}
			PlayerStatus status = new PlayerStatus(uuid);
			status.loadFromFile();
			return status;
		}
		
		private PlayerStatus(UUID uuid) {
			super(uuid, Main.uuidMasterList.getPlayerNameFromUUID(uuid));
			PlayerStatus.statuses.add(this);
		}
		
		private PlayerStatus(Player player) {
			super(player);
			PlayerStatus.statuses.add(this);
		}
		
		@Override
		public void dispose() {
			super.dispose();
			PlayerStatus.statuses.remove(this);
		}
		
		//==========================================================================================
		
		private final ArrayList<UUID>	playersRequestingToTpToMe		= new ArrayList<>();
		private final ArrayList<UUID>	playersRequestingMeToTpToThem	= new ArrayList<>();
		
		@Override
		//@EventHandler(priority = EventPriority.HIGHEST)
		public final void onPlayerJoin(PlayerJoinEvent event) {
			if(!this.isPlayerOnline()) {
				return;
			}
			if(!SavablePlayerData.playerEquals(event.getPlayer(), this.getPlayer())) {
				return;
			}
			if(!this.isLoadedFromFile()) {
				this.loadFromFile();
			}
			if(this.isVanishModeOn) {
				event.setJoinMessage(null);
			}
			final PlayerStatus THIS = this;
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					if(!THIS.isPlayerOnline()) {//Believe it or not, this can happen.
						return;
					}
					THIS.getPlayer().setGameMode(THIS.lastGameMode != null ? THIS.lastGameMode : THIS.getPlayer().getGameMode());//prevents null pointer exceptions
					THIS.getPlayer().setAllowFlight(THIS.wasFlyingLast ? true : THIS.isFlyModeOn);//prevents "cannot set isFlying to true if getAllowFlight is false" error.
					THIS.getPlayer().setFlying(THIS.wasFlyingLast);
					THIS.getPlayer().setHealth((THIS.lastHealth > 0 && THIS.lastHealth <= 2000) ? THIS.lastHealth : THIS.getPlayer().getHealth());//too many hearts can freeze up the client.
					THIS.getPlayer().setMaxHealth((THIS.lastMaxHealth > 0 && THIS.lastMaxHealth <= 2000) ? THIS.lastMaxHealth : THIS.getPlayer().getMaxHealth());
					THIS.getPlayer().setSaturation((THIS.lastFoodSaturation > 0) ? THIS.lastFoodSaturation : THIS.getPlayer().getSaturation());
					THIS.getPlayer().setFoodLevel((THIS.lastFoodLevel > 0 && THIS.lastFoodLevel <= 20) ? THIS.lastFoodLevel : THIS.getPlayer().getFoodLevel());
					THIS.getPlayer().setLevel((THIS.lastXpLevel > 0) ? THIS.lastXpLevel : THIS.getPlayer().getLevel());
					THIS.getPlayer().setExp((THIS.lastXp > 0) ? THIS.lastXp : THIS.getPlayer().getExp());
					PlayerStatus.updatePlayerVanishStates();
					PlayerStatus.updatePlayerFlyModeStates();
				}
			}, (long) (20 * 0.25));//5 ticks
		}
		
		@Override
		@EventHandler(priority = EventPriority.HIGHEST)
		public final void onPlayerQuit(PlayerQuitEvent event) {
			if(!this.isPlayerOnline()) {
				return;
			}
			if(!SavablePlayerData.playerEquals(event.getPlayer(), this.getPlayer())) {
				return;
			}
			this.wasFlyingLast = this.getPlayer().isFlying();
			if(this.isVanishModeOn) {
				event.setQuitMessage(null);
			}
			this.lastGameMode = this.getPlayer().getGameMode();
			this.lastHealth = this.getPlayer().getHealth();
			this.lastMaxHealth = this.getPlayer().getMaxHealth();
			this.lastFoodSaturation = this.getPlayer().getSaturation();
			this.lastFoodLevel = this.getPlayer().getFoodLevel();
			this.lastXpLevel = this.getPlayer().getLevel();
			this.lastXp = this.getPlayer().getExp();
			this.saveToFileAndDispose();
			Main.scheduler.scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					PlayerStatus.updatePlayerVanishStates();
				}
			});
		}
		
		public final boolean canGetKit(Kit kit) {
			boolean iCanHasKitz = true;
			if(kit.obtainInterval > 0) {
				Long lastTimeIGotThisKit = this.lastKitTimes.get(kit.name);
				long NOW = System.currentTimeMillis();
				long nextTimeKitWillBeAvailable = (lastTimeIGotThisKit != null ? lastTimeIGotThisKit.longValue() + kit.obtainInterval : NOW);
				if(nextTimeKitWillBeAvailable > NOW) {
					iCanHasKitz = false;//Nu! U nu cans has kits. nope.avi. nu-uh.
				}
			}
			return iCanHasKitz;
		}
		
		public final boolean hasHome() {
			return this.hasHome ? (this.homeLocation != null) : false;
		}
		
		public final void toggleAfkState() {
			if(!this.isPlayerOnline()) {
				return;
			}
			if(!this.isVanishModeOn) {
				this.isAfk = !this.isAfk;
				if(this.isAfk) {
					this.lastAfkLocation = this.getPlayer().getLocation();
				}
				Main.broadcast("&7* &f" + this.getPlayerDisplayName() + "&r&f: &7is no" + (this.isAfk ? "w afk." : " longer afk."));
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
		
		public static final void updatePlayerVanishStates() {
			Main.DEBUG(Main.pluginName + "&3Updating player vanish states!");
			Team vanishedTeam = PlayerStatus.getVanishedTeam();
			ArrayList<Player> onlinePlayers = new ArrayList<>(Main.server.getOnlinePlayers());
			for(Player player : Main.server.getOnlinePlayers()) {
				PlayerStatus status = PlayerStatus.getPlayerStatus(player);
				if(Permissions.hasPerm(player, "supercmds.vanish.exempt")) {
					vanishedTeam.addPlayer(player);
				} else {
					vanishedTeam.removePlayer(player);
				}
				if(status.isVanishModeOn && Permissions.hasPerm(player, "supercmds.use.vanish")) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false), true);
					for(Player p : onlinePlayers) {
						if(!Permissions.hasPerm(p, "supercmds.vanish.exempt") && !p.getUniqueId().toString().equals(player.getUniqueId().toString())) {
							p.hidePlayer(player);
						} else {
							p.showPlayer(player);
						}
					}
				} else {
					player.removePotionEffect(PotionEffectType.INVISIBILITY);
					for(Player p : onlinePlayers) {
						player.showPlayer(p);
					}
				}
			}
			onlinePlayers.clear();
		}
		
		public static final void updatePlayerFlyModeStates() {
			for(PlayerStatus status : new ArrayList<>(PlayerStatus.statuses)) {
				if(status.isPlayerOnline()) {
					if(!Permissions.hasPerm(status.getPlayer(), "supercmds.use.fly") && status.isFlyModeOn) {
						status.isFlyModeOn = false;
						status.getPlayer().setFlying(false);
						status.getPlayer().setAllowFlight(false);
						Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet fly mode to " + (status.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
					} else {
						if(status.isFlyModeOn) {
							status.getPlayer().setAllowFlight(true);
						} else if(status.getPlayer().getAllowFlight()) {
							status.isFlyModeOn = true;
						} else if(!status.getPlayer().getAllowFlight()) {
							status.getPlayer().setAllowFlight(false);
						}
					}
				}
			}
		}
		
		public static final void updatePlayerGodModeStates() {
			for(PlayerStatus status : new ArrayList<>(PlayerStatus.statuses)) {
				if(status.isPlayerOnline()) {
					if(!Permissions.hasPerm(status.getPlayer(), "supercmds.use.god") && status.isGodModeOn) {
						status.isGodModeOn = false;
						Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet god mode to " + (status.isGodModeOn ? "&2true" : "&cfalse") + "&f.");
					}
				}
			}
		}
		
		public void addTeleportToMeRequest(Player requester) {
			this.playersRequestingToTpToMe.add(requester.getUniqueId());
			if(this.isPlayerOnline()) {
				Main.sendMessage(this.getPlayer(), Main.pluginName + requester.getDisplayName() + "&r&e is requesting to teleport to you.&z&eType &f/tpaccept&e to accept their request or type &f/tpdeny&e to deny their request.");
			}
		}
		
		public void addTeleportToThemRequest(Player requester) {
			this.playersRequestingMeToTpToThem.add(requester.getUniqueId());
			if(this.isPlayerOnline()) {
				Main.sendMessage(this.getPlayer(), Main.pluginName + requester.getDisplayName() + "&r&e is requesting for you to teleport to them.&z&eType &f/tpaccept&e to accept their request or type &f/tpdeny&e to deny their request.");
			}
		}
		
		public void acceptTeleportRequests() {
			if(!this.isPlayerOnline()) {
				this.denyTeleportRequests();
				return;
			}
			int i = 0;
			for(UUID uuid : this.playersRequestingMeToTpToThem) {
				Player requester = Main.server.getPlayer(uuid);
				if(requester != null) {
					this.getPlayer().teleport(requester);
					Main.sendMessage(requester, Main.pluginName + this.getPlayer().getDisplayName() + "&r&e has accepted your teleport request" + (i < (this.playersRequestingMeToTpToThem.size() - 1) ? ", but they also had other pending teleport requests" : "") + ".");
					if(requester.getUniqueId().toString().equals(this.getPlayer().getUniqueId().toString())) {
						Main.sendMessage(this.getPlayer(), Main.pluginName + "&fLol, you requested to teleport to yourself, haha.");
					}
				}
				i++;
			}
			for(UUID uuid : this.playersRequestingToTpToMe) {
				Player requester = Main.server.getPlayer(uuid);
				if(requester != null) {
					requester.teleport(this.getPlayer());
					Main.sendMessage(requester, Main.pluginName + this.getPlayer().getDisplayName() + "&r&e has accepted your teleport request.");
					if(requester.getUniqueId().toString().equals(this.getPlayer().getUniqueId().toString())) {
						Main.sendMessage(this.getPlayer(), Main.pluginName + "&fLol, you requested yourself to be teleported to yourself, haha.");
					}
				}
			}
		}
		
		public void denyTeleportRequests() {
			if(!this.isPlayerOnline()) {
				this.playersRequestingToTpToMe.clear();
				this.playersRequestingMeToTpToThem.clear();
				return;
			}
			for(UUID uuid : this.playersRequestingMeToTpToThem) {
				Player requester = Main.server.getPlayer(uuid);
				if(requester != null) {
					this.getPlayer().teleport(requester);
					Main.sendMessage(requester, Main.pluginName + this.getPlayer().getDisplayName() + "&r&e has denied your pending teleport request.");
				}
			}
			for(UUID uuid : this.playersRequestingToTpToMe) {
				Player requester = Main.server.getPlayer(uuid);
				if(requester != null) {
					this.getPlayer().teleport(requester);
					Main.sendMessage(requester, Main.pluginName + this.getPlayer().getDisplayName() + "&r&e has denied your pending teleport request.");
				}
			}
		}
		
	}
	
}
