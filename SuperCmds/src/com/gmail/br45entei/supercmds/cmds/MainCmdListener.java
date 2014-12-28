package com.gmail.br45entei.supercmds.cmds;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.api.Permissions;
import com.gmail.br45entei.supercmds.file.PlayerChat;
import com.gmail.br45entei.supercmds.file.PlayerEcoData;
import com.gmail.br45entei.supercmds.file.PlayerPermissions;
import com.gmail.br45entei.supercmds.file.PlayerPermissions.Group;
import com.gmail.br45entei.supercmds.file.SavablePlayerData;
import com.gmail.br45entei.supercmds.file.Warps;
import com.gmail.br45entei.supercmds.file.Warps.Warp;
import com.gmail.br45entei.supercmds.util.CodeUtils;

/** @author Brian_Entei */
public final strictfp class MainCmdListener implements Listener {
	
	public static boolean	isConsoleAfk;
	
	public MainCmdListener() {
		MainCmdListener.isConsoleAfk = true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerJoinEvent(PlayerJoinEvent evt) {
		final Player newPlayer = evt.getPlayer();
		Main.DEBUG(Main.pluginName + "Player \"" + newPlayer.getDisplayName() + "\" has just logged on.");
		PlayerStatus.getPlayerStatus(newPlayer).onPlayerJoin(evt);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onPlayerQuitEvent(PlayerQuitEvent evt) {
		final Player oldPlayer = evt.getPlayer();
		Main.DEBUG(Main.pluginName + "Player \"" + oldPlayer.getDisplayName() + "\" has just logged out.");
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onEntityTargetEvent(EntityTargetEvent event) {
		if(event.getEntity() instanceof Player) {
			PlayerStatus status = PlayerStatus.getPlayerStatus((Player) event.getEntity());
			if(status.isVanishModeOn) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public static void onEntityTargetEvent(EntityTargetLivingEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			PlayerStatus status = PlayerStatus.getPlayerStatus((Player) event.getEntity());
			if(status.isVanishModeOn) {
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
			}
			event.setCancelled(status.isGodModeOn);
		}
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
					Main.sendConsoleMessage("Set player's fly mode to: \"" + playerIsFlying + "\"(actual flying boolean: " + player.isFlying() + ")");
					PlayerStatus.updatePlayerFlyModeStates();
				}
			});
		}
	}
	
	public static final String getNoPlayerMsg(String playerName) {
		return Main.pluginName + "&ePlayer \"&f" + playerName + "&r&e\" does not exist or is not online.";
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
			Main.sendConsoleMessage(Main.pluginName + "&aDebug: Player \"&f" + player.getDisplayName() + "&r&a\" did not move up to two blocks away from last afk spot; not changing player's afk state.");
		}
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
		if(user != null && status.isAfk) {
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
						String mkArgs = "";
						for(int i = 1; i < args.length; i++) {
							mkArgs += args[i] + " ";
						}
						mkArgs = mkArgs.trim();
						Main.server.dispatchCommand(target, mkArgs);
						Main.sendMessage(sender, Main.pluginName + "&aMade player \"&r&f" + target.getDisplayName() + "&r&a\" perform command: \"&r&f/" + mkArgs + "&r&a\"");
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
					double x = CodeUtils.getDoubleFromStr(args[0], Double.NaN);
					double y = CodeUtils.getDoubleFromStr(args[1], Double.NaN);
					double z = CodeUtils.getDoubleFromStr(args[2], Double.NaN);
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
					double x = CodeUtils.getDoubleFromStr(args[1], Double.NaN);
					double y = CodeUtils.getDoubleFromStr(args[2], Double.NaN);
					double z = CodeUtils.getDoubleFromStr(args[3], Double.NaN);
					if(x != Double.NaN && y != Double.NaN && z != Double.NaN) {
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
					Main.sendMessage(user, Main.pluginName + "&aSet your home to your current location.");
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
						Main.sendMessage(user, Main.pluginName + "&eYou do not have a home yet!&z&aSet one with &f/sethome&a.");
						return true;
					}
					if(user.teleport(status.homeLocation)) {
						status.lastTeleportLoc = loc;
						Main.sendMessage(user, Main.pluginName + "&2Teleport successful.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when teleporting you to your home; is the world loaded?");
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
			if(!Permissions.hasPerm(sender, "supercmds.use.permission")) {
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
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when adding the permission \"&f" + arg3 + "&r&e\" to the player \"&f" + targetName + "&r&e\"!&z&aPerhaps they already have that permission?");
						return true;
					} else if(flag.equalsIgnoreCase("remove")) {
						if(perms.setPermission(arg3, false)) {
							Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when removing the permission \"&f" + arg3 + "&r&e\" from the player \"&f" + targetName + "&r&e\"!&z&aPerhaps it was never added?");
						return true;
					} else if(flag.equalsIgnoreCase("setgroup")) {
						Group group = Group.getGroupByName(arg3);
						if(group != null) {
							if(perms.changeGroup(group)) {
								Main.sendMessage(sender, Main.pluginName + "&aThe command completed successfully.");
								return true;
							}
							Main.sendMessage(sender, Main.pluginName + "&eSomething went wrong when changing the player \"&f" + targetName + "&r&e\"'s group to \"&f" + group.displayName + "&r&e\"!");
							return true;
						}
						Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + flag + "&r&e\" does not exist.&z&aCreate it using &f/group&a!");
						return true;
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[0]));
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {target} {add|remove|setgroup} {permission.node|groupName}&e\"");
			return true;
		} else if(command.equalsIgnoreCase("group")) {
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
			} else if(args.length == 3) {// /group {groupName} {add|remove} {permission.node}
				String groupName = args[0];
				String addRemove = args[1];
				String permNode = args[2];
				Group group = Group.getGroupByName(groupName);
				if(group != null) {
					if(addRemove.equalsIgnoreCase("add")) {
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
			} else if(args.length == 4) {// /group {groupName} {set} {default|displayName|inheritance|nextgroup} {value}
				String groupName = args[0];
				String flag = args[1];//this might be unnecessary, but meh.
				String defaultDispInherNexGr = args[2];
				String value = args[3];
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
					} else {
						Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + flag + "&r&e\".");
					}
				} else {
					Main.sendMessage(sender, Main.pluginName + "&eThe group \"&f" + groupName + "&r&e\" does not exist.");
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {groupName} {add|remove} {permission.node}&e\" or&z&e\"&f/" + command + " {groupName} {create|delete|info}&e\" or&z&e\"&f/" + command + " {groupName} {set} {default|displayName|inheritance} {value}&e\"");
			return true;
		} else if(command.equalsIgnoreCase("bal") || command.equalsIgnoreCase("balance") || command.equalsIgnoreCase("money") || command.equalsIgnoreCase("fe")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.money") && !Permissions.hasPerm(sender, "supercmds.use.bal") && !Permissions.hasPerm(sender, "supercmds.use.balance") && !Permissions.hasPerm(sender, "supercmds.use.fe")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 0) {
				if(user == null) {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				} else {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
					Main.sendMessage(user, Main.pluginName + "&aYou have \"&f" + eco.balance + "&r&a\" &f" + Main.moneyTerm + "&r&a in your pocket and \"&f" + eco.credits + "&r&a\" credits to your name.");
					eco.saveToFile();//Ah, to remove or not to remove, that is the question...
					return true;
				}
			} else if(args.length == 1) {
				UUID target = Main.uuidMasterList.getUUIDFromPlayerName(args[0]);
				String targetName = Main.uuidMasterList.getPlayerNameFromUUID(target);
				if(target != null) {
					PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
					Main.sendMessage(sender, Main.pluginName + "&aPlayer \"&f" + targetName + "&r&a\" has \"&f" + eco.balance + "&r&a\" &f" + Main.moneyTerm + "&r&a in their pocket and has \"&f" + eco.credits + "&r&a\" credits to their name.");
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
			if(!Permissions.hasPerm(sender, "supercmds.use.eco") && !Permissions.hasPerm(sender, "supercmds.use.econ") && !Permissions.hasPerm(sender, "supercmds.use.economy")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
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
						double amount = CodeUtils.getDoubleFromStr(value, 0);
						int intAmount = Double.valueOf(Math.round(amount)).intValue();
						PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(target);
						if(flag.equalsIgnoreCase("give")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.balance += amount;
								Main.sendMessage(sender, Main.pluginName + "&aYou gave &f" + String.format("%.2g%n", new Double(amount)) + " " + Main.moneyTerm + "&r&a to player \"&f" + targetName + "&r&a\". They now have &f" + String.format("%.2g%n", new Double(eco.balance)) + " " + Main.moneyTerm + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(moneyCredit.equalsIgnoreCase("credit")) {
								eco.credits += intAmount;
								Main.sendMessage(sender, Main.pluginName + "&aYou gave &f" + intAmount + "&r&a credits to player \"&f" + targetName + "&r&a\". They now have &f" + eco.credits + "&r&a.");
								eco.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else {
								Main.sendMessage(sender, Main.pluginName + "&eInvalid flag \"&f" + moneyCredit + "&r&e\".");
							}
						} else if(flag.equalsIgnoreCase("take")) {
							if(moneyCredit.equalsIgnoreCase("money")) {
								eco.balance -= amount;
								Main.sendMessage(sender, Main.pluginName + "&aYou took &f" + String.format("%.2g%n", new Double(amount)) + " " + Main.moneyTerm + "&r&a from player \"&f" + targetName + "&r&a\". They now have &f" + String.format("%.2g%n", new Double(eco.balance)) + " " + Main.moneyTerm + "&r&a.");
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
								Main.sendMessage(sender, Main.pluginName + "&aYou set \"&f" + targetName + "&r&a\"'s  &f" + Main.moneyTerm + "&r&a to &f" + String.format("%.2g%n", new Double(eco.balance)) + " " + Main.moneyTerm + "&r&a.");
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
					Main.sendMessage(sender, MainCmdListener.getNoPlayerMsg(args[1]));
				}
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {give|take|set} {targetName} {money|credit} {amount} &e\"");
			return true;
		} else if(command.equalsIgnoreCase("speed")) {
			//TODO Player fly/walk speed; requires permission
			return false;
		} else if(command.equalsIgnoreCase("give")) {
			//TODO bukkit /give command; requires permission
			return false;
		} else if(command.equalsIgnoreCase("hat")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.hat")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(args.length == 0) {
				if(user != null) {
					ItemStack itemInHand = user.getInventory().getItemInHand();
					if(itemInHand == null || itemInHand.getType() == Material.AIR) {
						ItemStack itemOnHead = user.getInventory().getHelmet();
						if(itemInHand != null && itemInHand.getType() != Material.AIR) {
							user.getInventory().setItemInHand(itemOnHead);
						}
						user.getInventory().setHelmet(itemInHand);
						Main.sendMessage(sender, Main.pluginName + "&aEnjoy your new hat!");
						return true;
					}
				} else {
					Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				}
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
			return true;
		} else if(command.equalsIgnoreCase("nick") || command.equalsIgnoreCase("nickname")) {
			if(!Permissions.hasPerm(sender, "supercmds.use.nick") && !Permissions.hasPerm(sender, "supercmds.use.nickname")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(Main.handleChat) {
				if(user != null) {
					if(args.length >= 1) {
						String nickName = strArgs.trim();
						int nickLength = Main.stripColorCodes(Main.formatColorCodes(nickName)).length();
						if(nickLength <= 16) {
							PlayerChat chat = PlayerChat.getPlayerChat(user);
							chat.setNickname(nickName);
							Main.sendMessage(user, Main.pluginName + "&aYour new nickname is: \"&f" + chat.getNickName() + "&r&a\"!");
							chat = null;
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eSorry, but nicknames must be up to 16 characters in length.&z&aNote: Color codes do not count towards this limit.");
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
		} else if(command.equalsIgnoreCase("chat")) {// Admin command for managing player prefixes, nicknames, and suffixes; requires permission
			if(!Permissions.hasPerm(sender, "supercmds.use.chat")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(Main.handleChat) {
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
							String mkArgs = "";
							for(int i = 2; i < args.length; i++) {
								mkArgs += args[i] + " ";
							}
							mkArgs = mkArgs.trim();
							if(flag.equalsIgnoreCase("prefix")) {
								if(mkArgs.equalsIgnoreCase("clear")) {
									chat.setPrefix(null);
								} else {
									chat.setPrefix(mkArgs);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s prefix to: \"&f" + chat.prefix + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								chat.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(flag.equalsIgnoreCase("nickname")) {
								if(mkArgs.equalsIgnoreCase("clear")) {
									chat.setNickname(null);
								} else {
									chat.setNickname(mkArgs);
								}
								Main.sendMessage(sender, Main.pluginName + "&aSuccessfully set \"&f" + targetName + "&r&a\"'s nickname to: \"&f" + chat.nickname + "&r&a\"!&z&aTheir resulting display name is: \"&f" + chat.getDisplayName() + "&r&a\".");
								chat.saveAndDisposeIfPlayerNotOnline();
								return true;
							} else if(flag.equalsIgnoreCase("suffix")) {
								if(mkArgs.equalsIgnoreCase("clear")) {
									chat.setSuffix(null);
								} else {
									chat.setSuffix(mkArgs);
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
				Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + " {targetName} {info}&e\" or \"&f/" + command + " &e\"");
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
					}
					Main.sendMessage(user, Main.pluginName + "&6/warp " + warp.name + "&a has been set to your current location.");
					return true;
				}
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {warpname} [requireperm|requiregroup] [perm|groupname]&e\"");
			return true;
		} else if(command.equalsIgnoreCase("warp")) {// /warp {warpName}
			if(!Permissions.hasPerm(sender, "supercmds.use.setwarp")) {
				Main.sendMessage(sender, Main.pluginName + Main.noPerm);
				return true;
			}
			if(user == null) {
				Main.sendMessage(sender, Main.getPlayerOnlyMsg());
				return true;
			}
			if(args.length == 1) {
				String warpName = args[0];
				Warp warp = Warps.getWarpByName(warpName);
				if(warp == null) {
					Main.sendMessage(sender, Main.pluginName + "&eThe warp \"&f" + warpName + "&r&e\" does not exist.&z&aTry setting it with &f/setwarp&a!");
					return true;
				}
				PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(user);
				if(warp.requiredGroup != null) {
					if(!perms.isAMemberOfGroup(warp.requiredGroup)) {
						Main.sendMessage(user, Main.pluginName + "&eYou are not a member of that warp's required group(\"&f" + warp.requiredGroup.displayName + "&r&e\")!");
						perms = null;
						return true;
					}
				} else if(warp.requiredPermission != null && !warp.requiredPermission.isEmpty()) {
					if(!perms.hasPermission(warp.requiredPermission)) {
						Main.sendMessage(user, Main.pluginName + "&eYou do not have the permission that this warp requires(\"&b" + warp.requiredPermission + "&r&e\")!");
						perms = null;
						return true;
					}
				}
				if(user.teleport(warp.location)) {
					Main.sendMessage(user, Main.pluginName + "&aWarping to &6/warp " + warp.name + "&r&a.");
				} else {
					Main.sendMessage(user, Main.pluginName + "&eSomething went wrong during the teleport! Is the world loaded in that location?");
				}
				perms = null;
				return true;
			}
			Main.sendMessage(user, Main.pluginName + "&eUsage: \"&f/" + command + " {warpname}&e\"");
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
		} else if(command.equalsIgnoreCase("rankup") || command.equalsIgnoreCase("nextgroup")) {
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
						if(perms.group.nextGroup.costToRankup > 0) {
							PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(user);
							if(eco.balance < perms.group.nextGroup.costToRankup) {
								Main.sendMessage(user, Main.pluginName + "&eYou do not have enough &f" + Main.moneyTerm + "&r&e to rank up to the next group!&z&aYou need &f" + String.format("%.2g%n", new Double(perms.group.nextGroup.costToRankup - eco.balance)) + "&r&a more(the next rank costs &f" + String.format("%.2g%n", new Double(perms.group.nextGroup.costToRankup)) + "&r&a)!");
								return true;
							}
							eco.balance -= perms.group.nextGroup.costToRankup;
							eco.saveToFile();
							Main.sendMessage(user, Main.pluginName + "&f" + String.format("%.2g%n", new Double(perms.group.nextGroup.costToRankup)) + "&e &f" + Main.moneyTerm + "&r&e has been taken from your account.");
						}
						if(perms.changeGroup(perms.group.nextGroup)) {
							String msg = Main.pluginName + String.format(Main.playerPromotedMessage, user.getDisplayName(), perms.group.displayName);
							for(Player player : new ArrayList<>(Main.server.getOnlinePlayers())) {
								if(!player.getUniqueId().toString().equals(user.getUniqueId().toString())) {
									Main.sendMessage(player, msg);
								}
							}
							Main.sendConsoleMessage(msg);
							Main.sendMessage(user, Main.pluginName + "&aYou have been promoted to the \"&f" + perms.group.displayName + "&r&a\" rank!");
							return true;
						}
						Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when attempting to promote you to the next rank!&z&aPlease ask a server administrator politely for assistance.");
						return true;
					}
					Main.sendMessage(user, Main.pluginName + "&eYou are already in the highest rank possible!");
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eWhoops! It appears that you were never assigned to a group.&z&aPromoting you to the default group...");
				if(perms.changeGroup(Group.getDefaultGroup())) {
					String msg = Main.pluginName + String.format(Main.playerPromotedMessage, user.getDisplayName(), perms.group.displayName);
					for(Player player : new ArrayList<>(Main.server.getOnlinePlayers())) {
						if(!player.getUniqueId().toString().equals(user.getUniqueId().toString())) {
							Main.sendMessage(player, msg);
						}
					}
					Main.sendConsoleMessage(msg);
					Main.sendMessage(user, Main.pluginName + "&aYou have been promoted to the \"&f" + perms.group.displayName + "&r&a\" rank!");
					return true;
				}
				Main.sendMessage(user, Main.pluginName + "&eSomething went wrong when attempting to promote you to the next rank!&z&aPlease ask a server administrator politely for assistance.");
				return true;
			}
			Main.sendMessage(sender, Main.pluginName + "&eUsage: \"&f/" + command + "&e\"");
			return true;
		} else {
			return false;
		}
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
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to survival.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode was just changed to survival.");
			} else {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to survival.");
			}
		} else if(arg.equalsIgnoreCase("1") || arg.equalsIgnoreCase("c") || arg.equalsIgnoreCase("creative")) {
			target.setGameMode(GameMode.CREATIVE);
			if(isSenderTarget) {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to creative.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode was just changed to creative.");
			} else {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to creative.");
			}
		} else if(arg.equalsIgnoreCase("2") || arg.equalsIgnoreCase("a") || arg.equalsIgnoreCase("adventure")) {
			target.setGameMode(GameMode.ADVENTURE);
			if(isSenderTarget) {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to adventure.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode was just changed to adventure.");
			} else {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to adventure.");
			}
		} else if(arg.equalsIgnoreCase("3") || arg.equalsIgnoreCase("spec") || arg.equalsIgnoreCase("spectator")) {
			target.setGameMode(GameMode.SPECTATOR); // <--- See?
			if(isSenderTarget) {
				Main.sendMessage(sender, Main.pluginName + "&aChanged \"&f" + targetName + "&r&a\"'s gamemode to spectator.");
				Main.sendMessage(target, Main.pluginName + "&eYour gamemode was just changed to spectator.");
			} else {
				Main.sendMessage(target, Main.pluginName + "&aSuccessfully changed your gamemode to spectator.");
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
			if(Permissions.hasPerm(player, "supercmds.use.thor")) {
				if(status.isThorModeOn && player.getItemInHand().getType() == Material.IRON_PICKAXE) {
					player.getWorld().strikeLightning(player.getTargetBlock(null, 200).getLocation());
				}
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
			this.lastGameMode = (mem.getString("lastGameMode") != null ? (mem.getString("lastGameMode").equalsIgnoreCase("survival") ? GameMode.SURVIVAL : (mem.getString("lastGameMode").equalsIgnoreCase("creative") ? GameMode.CREATIVE : (mem.getString("lastGameMode").equalsIgnoreCase("adventure") ? GameMode.ADVENTURE : (this.isPlayerOnline() ? this.getPlayer().getGameMode() : GameMode.SURVIVAL)))) : (this.isPlayerOnline() ? this.getPlayer().getGameMode() : GameMode.SURVIVAL));
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
		@EventHandler(priority = EventPriority.HIGHEST)
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
			});
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
		
		public static void updatePlayerVanishStates() {
			//Main.sendConsoleMessage(Main.pluginName + "&3Updating player vanish states!");
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
		
		public static void updatePlayerFlyModeStates() {
			for(PlayerStatus status : new ArrayList<>(PlayerStatus.statuses)) {
				if(status.isPlayerOnline()) {
					if(!Permissions.hasPerm(status.getPlayer(), "supercmds.use.fly") && status.isFlyModeOn) {
						status.isFlyModeOn = false;
						Main.sendMessage(status.getPlayer(), Main.pluginName + "&aSet fly mode to " + (status.isFlyModeOn ? "&2true" : "&cfalse") + "&f.");
					}
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
