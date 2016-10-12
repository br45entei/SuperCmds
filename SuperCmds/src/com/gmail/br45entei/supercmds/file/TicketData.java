package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("javadoc")
public class TicketData extends SavablePluginData {
	
	private final ConcurrentHashMap<Long, String>	openTickets		= new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, String>	closedTickets	= new ConcurrentHashMap<>();
	
	private static TicketData						instance;
	
	public static final TicketData getInstance() {
		return(instance == null ? (instance = new TicketData()) : instance);
	}
	
	private TicketData() {
		super("TicketData");
	}
	
	public final ArrayList<String> getOpenTicketsInOrder() {
		TreeMap<Long, String> map = new TreeMap<>(this.openTickets);
		ArrayList<String> list = new ArrayList<>();
		for(String ticket : map.values()) {
			String[] split = ticket.split(Pattern.quote("|"));
			String uuidStr = split[0];
			ticket = StringUtil.stringArrayToString(split, '|', 1);
			if(StringUtil.isStrUUID(uuidStr)) {
				ticket = "&f" + PlayerChat.getDisplayName(UUID.fromString(uuidStr)) + "&r&f: " + ticket;
			}
			list.add(Main.formatColorCodes(ticket, false));
		}
		return list;
	}
	
	public final HashMap<Long, String> getOpenTickets() {
		return new HashMap<>(this.openTickets);
	}
	
	public final ArrayList<String> getOpenTicketStrings() {
		ArrayList<String> list = new ArrayList<>();
		for(Entry<Long, String> entry : this.openTickets.entrySet()) {
			String ticket = entry.getValue();
			ticket = ticket.contains("|") ? ticket.split(Pattern.quote("|"))[1] : ticket;
			list.add(ticket);
		}
		return list;
	}
	
	public final boolean doesOpenTicketExist(String ticket) {
		return StringUtil.containsIgnoreCase(this.getOpenTicketStrings(), ticket);
	}
	
	public final void submitTicket(Player player, String ticket) {
		this.openTickets.put(Long.valueOf(System.currentTimeMillis()), (player == null ? Main.consoleUUID : player.getUniqueId()).toString() + "|" + ticket);
	}
	
	public final ArrayList<String> getAllOpenTicketsFromPlayer(Player player) {
		return getAllOpenTicketsFrom(player != null ? player.getUniqueId() : null);
	}
	
	public final ArrayList<String> getAllOpenTicketsFrom(UUID uuid) {
		ArrayList<String> list = new ArrayList<>();
		for(Entry<Long, String> entry : this.openTickets.entrySet()) {
			String ticket = entry.getValue();
			String check = ticket.contains("|") ? ticket.split(Pattern.quote("|"))[0] : ticket;
			ticket = ticket.contains("|") ? ticket.split(Pattern.quote("|"))[1] : ticket;
			if(check != null && StringUtil.isStrUUID(check)) {
				if(check.equals(uuid.toString())) {
					list.add(ticket);
				}
			}
		}
		return list;
	}
	
	public final Collection<String> getClosedTicketsInOrder() {
		return new ArrayList<>(new TreeMap<>(this.closedTickets).values());
	}
	
	public final HashMap<Long, String> getClosedTickets() {
		return new HashMap<>(this.closedTickets);
	}
	
	public final ArrayList<String> getClosedTicketStrings() {
		ArrayList<String> list = new ArrayList<>();
		for(Entry<Long, String> entry : this.closedTickets.entrySet()) {
			String ticket = entry.getValue();
			ticket = ticket.contains("|") ? ticket.split(Pattern.quote("|"))[1] : ticket;
			list.add(ticket);
		}
		return list;
	}
	
	public final boolean doesClosedTicketExist(String ticket) {
		return StringUtil.containsIgnoreCase(this.getClosedTicketStrings(), ticket);
	}
	
	public final boolean closeTicket(String openTicket) {
		if(openTicket == null) {
			return false;
		}
		for(Entry<Long, String> entry : this.openTickets.entrySet()) {
			String ticket = entry.getValue();
			ticket = ticket.contains("|") ? ticket.split(Pattern.quote("|"))[1] : ticket;
			if(openTicket.equals(ticket)) {
				this.openTickets.remove(entry.getKey());
				this.closedTickets.put(entry.getKey(), System.currentTimeMillis() + "|" + entry.getValue().substring(0, entry.getValue().contains("|") ? entry.getValue().indexOf("|") : entry.getValue().length()) + (entry.getValue().contains("|") ? ticket : ""));
				return true;
			}
		}
		return false;
	}
	
	public final ArrayList<String> getAllClosedTicketsFromPlayer(Player player) {
		return getAllClosedTicketsFrom(player != null ? player.getUniqueId() : null);
	}
	
	public final ArrayList<String> getAllClosedTicketsFrom(UUID uuid) {
		ArrayList<String> list = new ArrayList<>();
		for(Entry<Long, String> entry : this.closedTickets.entrySet()) {
			String ticket = entry.getValue();
			String check = ticket.contains("|") ? ticket.split(Pattern.quote("|"))[0] : ticket;
			ticket = ticket.contains("|") ? ticket.split(Pattern.quote("|"))[1] : ticket;
			if(check != null && StringUtil.isStrUUID(check)) {
				if(check.equals(uuid.toString())) {
					list.add(ticket);
				}
			}
		}
		return list;
	}
	
	//================================================================================================================================
	
	/** @see com.gmail.br45entei.supercmds.file.SavablePluginData#getSaveFolderName() */
	@Override
	public String getSaveFolderName() {
		return this.name;
	}
	
	/** @see com.gmail.br45entei.supercmds.file.SavablePluginData#loadFromConfig(org.bukkit.configuration.ConfigurationSection) */
	@Override
	public void loadFromConfig(ConfigurationSection mem) {
		this.openTickets.clear();
		ConfigurationSection openTickets = mem.getConfigurationSection("openTickets");
		if(openTickets != null) {
			for(String time : mem.getKeys(false)) {
				if(StringUtil.isStrLong(time)) {
					String ticket = mem.getString(time);
					this.openTickets.put(Long.valueOf(time), ticket);
				}
			}
		}
		this.closedTickets.clear();
		ConfigurationSection closedTickets = mem.getConfigurationSection("closedTickets");
		if(closedTickets != null) {
			for(String time : mem.getKeys(false)) {
				if(StringUtil.isStrLong(time)) {
					String ticket = mem.getString(time);
					this.closedTickets.put(Long.valueOf(time), ticket);
				}
			}
		}
	}
	
	/** @see com.gmail.br45entei.supercmds.file.SavablePluginData#saveToConfig(org.bukkit.configuration.ConfigurationSection) */
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		ConfigurationSection openTickets = mem.getConfigurationSection("openTickets");
		if(openTickets == null) {
			openTickets = mem.createSection("openTickets");
		}
		for(Entry<Long, String> entry : this.openTickets.entrySet()) {
			openTickets.set(entry.getKey() + "", entry.getValue());
		}
		ConfigurationSection closedTickets = mem.getConfigurationSection("closedTickets");
		if(closedTickets == null) {
			closedTickets = mem.createSection("closedTickets");
		}
		for(Entry<Long, String> entry : this.closedTickets.entrySet()) {
			closedTickets.set(entry.getKey() + "", entry.getValue());
		}
	}
	
	/** @see com.gmail.br45entei.supercmds.file.AbstractPlayerJoinQuitClass#onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent) */
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
	}
	
	/** @see com.gmail.br45entei.supercmds.file.AbstractPlayerJoinQuitClass#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent) */
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
	}
	
}
