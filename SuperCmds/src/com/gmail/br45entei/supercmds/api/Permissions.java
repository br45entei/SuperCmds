package com.gmail.br45entei.supercmds.api;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerPermissions;
import com.gmail.br45entei.util.StringUtil;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class Permissions implements Listener {
	
	public Permissions() {
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerJoinEvent(PlayerJoinEvent event) {
		final long startTime = System.currentTimeMillis();
		System.out.println("Permissions.onPlayerJoinEvent()");
		Player newPlayer = event.getPlayer();
		if(Main.handlePermissions) {
			PlayerPermissions.getPlayerPermissions(newPlayer).onPlayerJoin(event);
		}
		System.out.println("End of Permissions.onPlayerJoinEvent(): " + StringUtil.getElapsedTime(System.currentTimeMillis() - startTime, true));
	}
	
	/** @param event Called when a player leaves a server */
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerQuitEvent(PlayerQuitEvent event) {
	}
	
	//=============================================================================
	
	public static final boolean hasPerm(CommandSender sender, String perm) {
		if(sender == null) {
			return false;
		}
		if(Main.handlePermissions) {
			if(sender.isOp()) {
				return true;
			}
			if(sender instanceof Player) {
				return Permissions.hasPerm((Player) sender, perm);
			}
		}
		return sender.hasPermission(perm);
	}
	
	public static final boolean hasPerm(Player player, String perm) {
		if(player == null) {
			return false;
		}
		if(Main.handlePermissions) {
			return Permissions.hasPerm(player.getUniqueId(), perm);
		}
		return player.hasPermission(perm);
	}
	
	public static final boolean hasPerm(UUID uuid, String perm) {
		if(uuid == null) {
			return false;
		}
		if(uuid.toString().equals(Main.consoleUUID.toString())) {
			return true;
		}
		PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
		boolean rtrn = perms.hasPermission(perm);
		perms.disposeIfPlayerNotOnline();
		return rtrn;
	}
	
}
