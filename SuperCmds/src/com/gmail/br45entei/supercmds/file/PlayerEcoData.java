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
public final class PlayerEcoData extends SavablePlayerData {
	private static final ArrayList<PlayerEcoData>	economies	= new ArrayList<>();
	
	public static final ArrayList<PlayerEcoData> getInstances() {
		return new ArrayList<>(PlayerEcoData.economies);
	}
	
	public double	balance	= 0;
	public int		credits	= 0;
	
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
	public void loadFromConfig(ConfigurationSection mem) {
		this.balance = mem.getDouble("balance");
		this.credits = mem.getInt("credits");
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		mem.set("balance", new Double(this.balance));
		mem.set("credits", new Integer(this.credits));
	}
	
	public final void saveToFileAndDispose() {
		this.saveToFile();
		this.dispose();
	}
	
	public static final PlayerEcoData getPlayerEcoData(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerEcoData.getPlayerEcoData(player.getUniqueId());
	}
	
	public static final PlayerEcoData getPlayerEcoData(UUID uuid) {
		for(PlayerEcoData eco : PlayerEcoData.getInstances()) {
			if(eco.uuid.toString().equals(uuid.toString())) {
				return eco;
			}
		}
		PlayerEcoData eco = new PlayerEcoData(uuid);
		eco.loadFromFile();
		return eco;
	}
	
	@Override
	//@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(PlayerJoinEvent event) {
		this.hasRunPlayerQuitEvt = false;
		if(this.hasRunPlayerJoinEvt) {
			return;
		}
		this.hasRunPlayerJoinEvt = true;
		Main.sendConsoleMessage("PlayerEcoData: onPlayerJoinEvent: " + this.name);
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
	
}
