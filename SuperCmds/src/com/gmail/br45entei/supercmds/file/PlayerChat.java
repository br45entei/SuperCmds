/**
 * 
 */
package com.gmail.br45entei.supercmds.file;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.br45entei.supercmds.Main;

/** @author Brian_Entei */
public class PlayerChat extends SavablePlayerData {
	private static final ArrayList<PlayerChat>	chatInstances	= new ArrayList<>();
	
	public static final ArrayList<PlayerChat> getInstances() {
		return new ArrayList<>(PlayerChat.chatInstances);
	}
	
	public String	prefix		= "";
	public String	nickname	= "";
	public String	suffix		= "";
	
	private PlayerChat(UUID uuid) {
		super(uuid, Main.uuidMasterList.getPlayerNameFromUUID(uuid));
		PlayerChat.chatInstances.add(this);
	}
	
	private PlayerChat(Player player) {
		super(player);
		PlayerChat.chatInstances.add(this);
	}
	
	@Override
	public String getSaveFolderName() {
		return "PlayerChatData";
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		this.prefix = mem.getString("prefix");
		this.nickname = mem.getString("nickname");
		this.suffix = mem.getString("suffix");
		if(this.nickname == null || this.nickname.isEmpty()) {
			this.nickname = this.name;
		}
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		mem.set("prefix", this.prefix);
		mem.set("nickname", this.nickname);
		mem.set("suffix", this.suffix);
	}
	
	public final void saveToFileAndDispose() {
		this.saveToFile();
		this.dispose();
	}
	
	public static final PlayerChat getPlayerChat(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerChat.getPlayerChat(player.getUniqueId());
	}
	
	public static final PlayerChat getPlayerChat(UUID uuid) {
		for(PlayerChat eco : PlayerChat.getInstances()) {
			if(eco.uuid.toString().equals(uuid.toString())) {
				return eco;
			}
		}
		PlayerChat eco = new PlayerChat(uuid);
		eco.loadFromFile();
		return eco;
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(PlayerJoinEvent event) {
		Main.sendConsoleMessage("PlayerChat: onPlayerJoinEvent: " + this.name);
		Player newPlayer = event.getPlayer();
		if(SavablePlayerData.playerEquals(this.getPlayer(), newPlayer)) {
			this.loadFromFile();
		}
		newPlayer.setDisplayName(this.getDisplayName());
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerQuit(PlayerQuitEvent event) {
		if(SavablePlayerData.playerEquals(this.getPlayer(), event.getPlayer())) {
			this.saveToFileAndDispose();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		PlayerChat.chatInstances.remove(this);
	}
	
	//===================================================================================
	
	public static final String getChatFormat(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerChat.getChatFormat(player.getUniqueId());
	}
	
	public static final String getChatFormat(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		String rtrn = chat.getChatFormat();
		if(!chat.isPlayerOnline()) {
			chat.dispose();
		}
		return rtrn;
	}
	
	public final String getNickName() {
		if(!Main.stripColorCodes(Main.formatColorCodes(this.nickname)).equals(this.name)) {
			return "~" + this.nickname;
		}
		return this.nickname;
	}
	
	public final String getDisplayName() {
		if(!this.getPrefix().isEmpty() && !this.getSuffix().isEmpty()) {
			return Main.formatColorCodes("&f[" + this.getPrefix() + "&r&f] [" + this.getNickName() + "&r&f] [" + this.getSuffix() + "&r&f]");
		} else if(!this.getPrefix().isEmpty()) {
			return Main.formatColorCodes("&f[" + this.getPrefix() + "&r&f] [" + this.getNickName() + "&r&f]");
		} else if(!this.getSuffix().isEmpty()) {
			return Main.formatColorCodes("&f[" + this.getNickName() + "&r&f] [" + this.getSuffix() + "&r&f]");
		} else {
			return Main.formatColorCodes("&f[" + this.getNickName() + "&r&f]");
		}
	}
	
	public final String getChatFormat() {
		return this.getDisplayName() + ": ";
	}
	
	public static final String getChatPrefix(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerChat.getChatPrefix(player.getUniqueId());
	}
	
	public static final String getChatPrefix(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		String rtrn = chat.getPrefix();
		if(!chat.isPlayerOnline()) {
			chat.dispose();
		}
		return rtrn;
	}
	
	public final String getPrefix() {
		if(this.prefix == null || this.prefix.isEmpty()) {
			PlayerPermissions perm = PlayerPermissions.getPlayerPermissions(this.uuid);
			if(perm.group != null) {
				return perm.group.displayName;
			}
		}
		return(this.prefix != null ? this.prefix : "");
	}
	
	public static final void setChatPrefix(Player player, String prefix) {
		if(player == null) {
			return;
		}
		PlayerChat.setChatPrefix(player.getUniqueId(), prefix);
	}
	
	public static final void setChatPrefix(UUID uuid, String prefix) {
		if(uuid == null) {
			return;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		chat.setChatPrefix(prefix);
		if(!chat.isPlayerOnline()) {
			chat.saveToFileAndDispose();
		}
	}
	
	public final boolean setChatPrefix(String prefix) {
		if(prefix == null) {
			prefix = "";
		}
		String oldPrefix = this.prefix;
		this.prefix = prefix;
		if(this.isPlayerOnline()) {
			this.getPlayer().setDisplayName(this.getDisplayName());
		}
		return !this.prefix.equals(oldPrefix);
	}
	
	public static final String getNickname(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerChat.getNickname(player.getUniqueId());
	}
	
	public static final String getNickname(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		String rtrn = chat.getNickName();
		if(!chat.isPlayerOnline()) {
			chat.dispose();
		}
		return rtrn;
	}
	
	public static final void setNickName(Player player, String nickname) {
		if(player == null) {
			return;
		}
		PlayerChat.setNickname(player.getUniqueId(), nickname);
	}
	
	public static final void setNickname(UUID uuid, String nickname) {
		if(uuid == null) {
			return;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		chat.setNickname(nickname);
		if(!chat.isPlayerOnline()) {
			chat.saveToFileAndDispose();
		}
	}
	
	public final boolean setNickname(String nickname) {
		if(nickname == null) {
			nickname = "";
		}
		String oldNick = this.nickname;
		this.nickname = nickname;
		if(this.isPlayerOnline()) {
			this.getPlayer().setDisplayName(this.getDisplayName());
		}
		return !this.nickname.equals(oldNick);
	}
	
	public static final String getChatSuffix(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerChat.getChatSuffix(player.getUniqueId());
	}
	
	public static final String getChatSuffix(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		String rtrn = chat.getSuffix();
		if(!chat.isPlayerOnline()) {
			chat.dispose();
		}
		return rtrn;
	}
	
	public final String getSuffix() {
		return(this.suffix != null ? this.suffix : "");
	}
	
	public static final void setChatSuffix(Player player, String suffix) {
		if(player == null) {
			return;
		}
		PlayerChat.setChatSuffix(player.getUniqueId(), suffix);
	}
	
	public static final void setChatSuffix(UUID uuid, String suffix) {
		if(uuid == null) {
			return;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		chat.setSuffix(suffix);
		if(!chat.isPlayerOnline()) {
			chat.saveToFileAndDispose();
		}
	}
	
	public final boolean setSuffix(String suffix) {
		if(suffix == null) {
			suffix = "";
		}
		String oldSuffix = this.suffix;
		this.suffix = suffix;
		if(this.isPlayerOnline()) {
			this.getPlayer().setDisplayName(this.getDisplayName());
		}
		return !this.suffix.equals(oldSuffix);
	}
	
}
