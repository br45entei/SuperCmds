package com.gmail.br45entei.supercmds.api;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerEcoData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public final class Economy_SuperCmds extends AbstractEconomy implements Listener {
	
	private final Plugin plugin;
	
	public Economy_SuperCmds(Plugin plugin) {
		this.plugin = plugin;
		if(Main.isVaultInstalled()) {
			Bukkit.getServicesManager().register(Economy.class, this, Main.getVault(), ServicePriority.Normal);
			Main.registerEvents(this, plugin);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player newPlayer = event.getPlayer();
		if(Main.handleEconomy) {
			PlayerEcoData.getPlayerEcoData(newPlayer).onPlayerJoin(event);
		}
	}
	
	/** @param event Called when a player leaves a server */
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerQuitEvent(PlayerQuitEvent event) {
	}
	
	//===================================================================================
	
	@EventHandler(priority = EventPriority.MONITOR)
	public final void onPluginEnable(PluginEnableEvent event) {
		if(event.getPlugin().getName().equals(this.plugin.getName())) {
			Logger.getLogger("Minecraft").info("[" + this.plugin.getDescription().getName() + "][Economy] SuperCmds Economy hooked.");
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public final void onPluginDisable(PluginDisableEvent event) {
		if(event.getPlugin().getName().equals(this.plugin.getName())) {
			Logger.getLogger("Minecraft").info("[" + this.plugin.getDescription().getName() + "][Economy] SuperCmds Economy unhooked.");
		}
	}
	
	//===================================================================================
	
	@Override
	public boolean isEnabled() {
		return Main.getInstance().isEnabled();
	}
	
	@Override
	public String getName() {
		return "SuperCmds Economy";
	}
	
	@Override
	public double getBalance(String playerName) {
		double balance = 0;
		PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(Main.uuidMasterList.getUUIDFromPlayerName(playerName));
		if(eco != null) {
			balance = eco.getBalance();
		}
		return balance;
	}
	
	@Override
	public boolean createPlayerAccount(String playerName) {
		if(hasAccount(playerName)) {
			return false;
		}
		return PlayerEcoData.getPlayerEcoData(Main.uuidMasterList.getUUIDFromPlayerName(playerName)) != null;
	}
	
	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		if(amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative amonuts!");
		}
		
		double balance;
		EconomyResponse.ResponseType type;
		String errorMessage = null;
		PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(Main.uuidMasterList.getUUIDFromPlayerName(playerName));
		if(eco != null) {
			if(eco.getBalance() >= amount) {
				eco.setBalance(eco.getBalance() - amount);
			}
			balance = eco.getBalance();
			type = EconomyResponse.ResponseType.SUCCESS;
			if(eco.isPlayerOnline()) {
				Main.sendMessage(eco.getPlayer(), Main.pluginName + "&6" + amount + " &f" + Main.fixPluralWord(Long.valueOf(Math.round(amount)).intValue(), Main.moneyTerm) + "&e was just taken from your account.&z&aNew balance: &6" + balance + " &f" + Main.fixPluralWord(Long.valueOf(Math.round(balance)).intValue(), Main.moneyTerm));
			}
		} else {
			amount = 0;
			balance = 0;
			type = EconomyResponse.ResponseType.FAILURE;
			errorMessage = "Player \"" + playerName + "\" does not exist or has not logged into this server!";
		}
		return new EconomyResponse(amount, balance, type, errorMessage);
	}
	
	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		if(amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot desposit negative amounts!");
		}
		
		double balance;
		EconomyResponse.ResponseType type;
		String errorMessage = null;
		PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(Main.uuidMasterList.getUUIDFromPlayerName(playerName));
		if(eco != null) {
			eco.setBalance(eco.getBalance() + amount);
			balance = eco.getBalance();
			type = EconomyResponse.ResponseType.SUCCESS;
			if(eco.isPlayerOnline()) {
				Main.sendMessage(eco.getPlayer(), Main.pluginName + "&6" + amount + " &f" + Main.fixPluralWord(Long.valueOf(Math.round(amount)).intValue(), Main.moneyTerm) + "&2 was just added to your account.&z&aNew balance: &6" + balance + " &f" + Main.fixPluralWord(Long.valueOf(Math.round(balance)).intValue(), Main.moneyTerm));
			}
		} else {
			amount = 0;
			balance = 0;
			type = EconomyResponse.ResponseType.FAILURE;
			errorMessage = "Player \"" + playerName + "\" does not exist or has not logged into this server!";
		}
		return new EconomyResponse(amount, balance, type, errorMessage);
	}
	
	@Override
	public String format(double amount) {
		return Main.decimal.format(amount);
	}
	
	@Override
	public String currencyNameSingular() {
		return Main.fixPluralWord(1, Main.moneyTerm);
	}
	
	@Override
	public String currencyNamePlural() {
		return Main.fixPluralWord(2, Main.moneyTerm);
	}
	
	@Override
	public boolean has(String playerName, double amount) {
		PlayerEcoData eco = PlayerEcoData.getPlayerEcoData(Main.uuidMasterList.getUUIDFromPlayerName(playerName));
		if(eco == null) {
			return false;
		}
		return eco.getBalance() >= amount;
	}
	
	@Override
	public EconomyResponse createBank(String name, String player) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public EconomyResponse deleteBank(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public EconomyResponse bankHas(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public EconomyResponse bankDeposit(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public EconomyResponse isBankOwner(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public EconomyResponse bankBalance(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "SuperCmds Economy does not support bank accounts!");
	}
	
	@Override
	public List<String> getBanks() {
		return new ArrayList<>();
	}
	
	@Override
	public boolean hasBankSupport() {
		return false;
	}
	
	@Override
	public boolean hasAccount(String playerName) {
		return PlayerEcoData.doesEcoDataExistFor(Main.uuidMasterList.getUUIDFromPlayerName(playerName));
	}
	
	@Override
	public int fractionalDigits() {
		return -1;
	}
	
	@Override
	public boolean hasAccount(String playerName, String worldName) {
		return hasAccount(playerName);
	}
	
	@Override
	public double getBalance(String playerName, String world) {
		return getBalance(playerName);
	}
	
	@Override
	public boolean has(String playerName, String worldName, double amount) {
		return has(playerName, amount);
	}
	
	@Override
	public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
		return withdrawPlayer(playerName, amount);
	}
	
	@Override
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
		return depositPlayer(playerName, amount);
	}
	
	@Override
	public boolean createPlayerAccount(String playerName, String worldName) {
		return createPlayerAccount(playerName);
	}
	
}
