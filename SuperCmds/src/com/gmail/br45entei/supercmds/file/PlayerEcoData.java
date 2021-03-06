package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public final class PlayerEcoData extends SavablePlayerData {
	private static final ArrayList<PlayerEcoData> economies = new ArrayList<>();
	
	public static final ArrayList<PlayerEcoData> getInstances() {
		return new ArrayList<>(PlayerEcoData.economies);
	}
	
	public volatile boolean	wasJustEditedByPlugin	= true;
	
	private volatile double	balance					= 0;
	public volatile int		credits					= 0;
	
	private PlayerEcoData(UUID uuid) {
		super(uuid, Main.uuidMasterList.getPlayerNameFromUUID(uuid));
		PlayerEcoData.economies.add(this);
	}
	
	private PlayerEcoData(Player player) {
		super(player);
		PlayerEcoData.economies.add(this);
	}
	
	@Override
	public String getSaveFolderName() {
		return "PlayerEcoData";
	}
	
	@Override
	public final void onFirstLoad() {
		
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		this.balance = mem.getDouble("balance");
		this.credits = mem.getInt("credits");
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		mem.set("balance", new Double(this.balance));
		mem.set("credits", new Integer(this.credits));
	}
	
	public static final PlayerEcoData getPlayerEcoData(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerEcoData.getPlayerEcoData(player.getUniqueId());
	}
	
	public static final PlayerEcoData getPlayerEcoData(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(PlayerEcoData eco : PlayerEcoData.getInstances()) {
			if(eco.uuid.toString().equals(uuid.toString())) {
				return eco;
			}
		}
		PlayerEcoData eco = new PlayerEcoData(uuid);
		eco.loadFromFile();
		return eco;
	}
	
	public static final boolean doesEcoDataExistFor(UUID uuid) {
		if(uuid == null) {
			return false;
		}
		for(PlayerEcoData eco : PlayerEcoData.getInstances()) {
			if(eco.uuid.toString().equals(uuid.toString())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	//@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(PlayerJoinEvent event) {
		this.hasRunPlayerQuitEvt = false;
		if(this.hasRunPlayerJoinEvt) {
			return;
		}
		this.hasRunPlayerJoinEvt = true;
		Main.DEBUG("PlayerEcoData: onPlayerJoinEvent: " + this.name);
		if(SavablePlayerData.playerEquals(this.getPlayer(), event.getPlayer())) {
			this.loadFromFile();
		}
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerQuit(PlayerQuitEvent event) {
		this.hasRunPlayerJoinEvt = false;
		if(this.hasRunPlayerQuitEvt) {
			return;
		}
		this.hasRunPlayerQuitEvt = true;
		if(SavablePlayerData.playerEquals(this.getPlayer(), event.getPlayer())) {
			this.saveToFileAndDispose();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		PlayerEcoData.economies.remove(this);
	}
	
	//===========
	
	public final double getBalance() {
		return this.balance;
	}
	
	public final PlayerEcoData setBalance(double balance) {
		this.balance = balance;
		this.wasJustEditedByPlugin = true;
		return this;
	}
	
	//===========
	
}
