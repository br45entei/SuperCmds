package com.gmail.br45entei.supercmds.api;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerChat;

/** @author Brian_Entei */
public class Chat implements Listener {
	
	public Chat() {
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player newPlayer = event.getPlayer();
		if(Main.handleChat) {
			PlayerChat.getPlayerChat(newPlayer).onPlayerJoin(event);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerQuitEvent(PlayerQuitEvent event) {
	}
	
	//=============================================================================
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		if(!Main.handleChat) {
			return;
		}
		Player chatter = event.getPlayer();
		PlayerChat chat = PlayerChat.getPlayerChat(chatter);
		String msg = event.getMessage();
		if(Permissions.hasPerm(chatter, "supercmds.chat.colors")) {
			if(Permissions.hasPerm(chatter, "supercmds.chat.colors.magic")) {
				msg = Main.formatColorCodes(msg);
			} else {
				msg = Main.formatColorCodes(msg, false);
			}
		}
		event.setFormat(chat.getChatFormat() + msg);
	}
	
}
