package com.gmail.br45entei.supercmds.file;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** @author Brian_Entei */
public abstract class AbstractPlayerJoinQuitClass implements Listener {
	
	/** @param event Called when a player joins a server. <br>
	 * <br>
	 *            <b>Note:</b> Remember to add the
	 *            <string>@EventHandler</string> tag to the top of this method,
	 *            otherwise it will not be fired! */
	public abstract void onPlayerJoin(PlayerJoinEvent event);
	
	/** @param event Called when a player leaves a server <br>
	 * <br>
	 *            <b>Note:</b> Remember to add the
	 *            <string>@EventHandler</string> tag to the top of this method,
	 *            otherwise it will not be fired! */
	public abstract void onPlayerQuit(PlayerQuitEvent event);
	
}
