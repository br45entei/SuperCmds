package com.gmail.br45entei.supercmds.api;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerPermissions;

/** @author Brian_Entei */
public class Permissions implements Listener {
	
	public Permissions() {
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player newPlayer = event.getPlayer();
		if(Main.handlePermissions) {
			PlayerPermissions.getPlayerPermissions(newPlayer).onPlayerJoin(event);
		}
	}
	
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
		PlayerPermissions perms = PlayerPermissions.getPlayerPermissions(uuid);
		boolean rtrn = perms.hasPermission(perm);
		if(!perms.isPlayerOnline()) {
			perms.dispose();
		}
		return rtrn;
	}
	
}
