package com.gmail.br45entei.supercmds.api;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerEcoData;

/** @author Brian_Entei */
public final class Economy implements Listener {
	
	public Economy() {
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player newPlayer = event.getPlayer();
		if(Main.handleEconomy) {
			PlayerEcoData.getPlayerEcoData(newPlayer).onPlayerJoin(event);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerQuitEvent(PlayerQuitEvent event) {
	}
	
	//===================================================================================
	
	//TODO
	
}
