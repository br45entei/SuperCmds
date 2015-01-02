package com.gmail.br45entei.supercmds.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.util.CodeUtils;

/** @author Brian_Entei */
public class PlayerChat extends SavablePlayerData {
	private static final ArrayList<PlayerChat>	chatInstances	= new ArrayList<>();
	
	public static final ArrayList<PlayerChat> getInstances() {
		return new ArrayList<>(PlayerChat.chatInstances);
	}
	
	public final boolean						isConsole;
	
	public String								prefix						= "";
	public String								nickname					= "";
	public String								suffix						= "";
	
	public UUID									lastPlayerThatIRepliedTo	= null;
	public final HashMap<UUID, ArrayList<Mail>>	mailbox						= new HashMap<>();
	public final HashMap<UUID, ArrayList<Mail>>	oldMail						= new HashMap<>();
	
	private PlayerChat(UUID uuid) {
		super(uuid, Main.uuidMasterList.getPlayerNameFromUUID(uuid));
		this.isConsole = uuid.toString().equals(Main.consoleUUID.toString());
		if(this.isConsole) {
			this.prefix = "";
			this.nickname = Main.consoleSayFormat.trim();
			this.suffix = "";
		}
		PlayerChat.chatInstances.add(this);
	}
	
	private PlayerChat(Player player) {
		super(player);
		this.isConsole = false;
		PlayerChat.chatInstances.add(this);
	}
	
	//=======================
	
	@Override
	public final Player getPlayer() {
		if(this.isConsole) {
			return null;
		}
		return super.getPlayer();
	}
	
	@Override
	public boolean isPlayerOnline() {
		if(this.isConsole) {
			return false;
		}
		return super.isPlayerOnline();
	}
	
	//=======================
	
	@Override
	public final String toString() {
		return "&3PlayerChat:\n" + //
		"&3Prefix: \"&f" + this.prefix + "&r&3\"\n" + //
		"&3NickName: \"&f" + this.nickname + "&r&3\"\n" + //
		"&3Suffix: \"&f" + this.suffix + "&r&3\"";
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
		try {
			this.lastPlayerThatIRepliedTo = (mem.getString("lastPlayerThatIRepliedTo") != null ? UUID.fromString(mem.getString("lastPlayerThatIRepliedTo")) : null);
		} catch(IllegalArgumentException ignored) {
		}
		//=============================================================================================
		ConfigurationSection mailbox = mem.getConfigurationSection("mailbox");
		if(mailbox != null) {
			for(String key : mailbox.getKeys(false)) {
				Mail m = Mail.getFromConfig(mailbox, key);
				if(m != null) {
					ArrayList<Mail> mails = this.mailbox.get(m.sender);
					if(mails == null) {
						mails = new ArrayList<>();
					}
					mails.add(m);
					this.mailbox.put(m.sender, mails);
				}
			}
		}
		ConfigurationSection oldMail = mem.getConfigurationSection("oldMail");
		if(oldMail != null) {
			for(String key : oldMail.getKeys(false)) {
				Mail m = Mail.getFromConfig(oldMail, key);
				if(m != null) {
					ArrayList<Mail> mails = this.oldMail.get(m.sender);
					if(mails == null) {
						mails = new ArrayList<>();
					}
					mails.add(m);
					this.oldMail.put(m.sender, mails);
				}
			}
		}
	}
	
	@Override
	public void saveToConfig(ConfigurationSection mem) {
		if(this.isConsole) {
			mem.set("isConsole", Boolean.TRUE);
		}
		mem.set("prefix", this.prefix);
		mem.set("nickname", this.nickname);
		mem.set("suffix", this.suffix);
		mem.set("lastPlayerThatIRepliedTo", this.lastPlayerThatIRepliedTo != null ? this.lastPlayerThatIRepliedTo.toString() : "");
		ConfigurationSection mailbox = mem.getConfigurationSection("mailbox");
		if(mailbox == null) {
			mailbox = mem.createSection("mailbox");
		}
		for(Map.Entry<UUID, ArrayList<Mail>> entry : this.mailbox.entrySet()) {
			ArrayList<Mail> mails = entry.getValue();
			for(Mail mail : mails) {
				mail.saveToConfig(mailbox);
			}
		}
		ConfigurationSection oldMail = mem.getConfigurationSection("oldMail");
		if(oldMail == null) {
			oldMail = mem.createSection("oldMail");
		}
		for(Map.Entry<UUID, ArrayList<Mail>> entry : this.oldMail.entrySet()) {
			ArrayList<Mail> mails = entry.getValue();
			for(Mail mail : mails) {
				mail.saveToConfig(oldMail);
			}
		}
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
		for(PlayerChat chat : PlayerChat.getInstances()) {
			if(chat.uuid.toString().equals(uuid.toString())) {
				return chat;
			}
		}
		PlayerChat chat = new PlayerChat(uuid);
		chat.loadFromFile();
		return chat;
	}
	
	@Override
	//@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(PlayerJoinEvent event) {
		this.hasRunPlayerQuitEvt = false;
		if(this.hasRunPlayerJoinEvt) {
			return;
		}
		this.hasRunPlayerJoinEvt = true;
		Main.sendConsoleMessage("PlayerChat: onPlayerJoinEvent: " + this.name);
		Player newPlayer = event.getPlayer();
		if(SavablePlayerData.playerEquals(this.getPlayer(), newPlayer)) {
			this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
			this.loadFromFile();
			newPlayer.setDisplayName(this.getDisplayName());
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
		PlayerChat.chatInstances.remove(this);
	}
	
	//===================================================================================
	
	public static final String getDisplayName(Player player) {
		if(player == null) {
			return null;
		}
		return PlayerChat.getDisplayName(player.getUniqueId());
	}
	
	public static final String getDisplayName(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		PlayerChat chat = PlayerChat.getPlayerChat(uuid);
		String rtrn = chat.getDisplayName();
		if(!chat.isPlayerOnline()) {
			chat.dispose();
		}
		return rtrn;
	}
	
	public final String getNickName() {
		if(this.nickname == null || this.nickname.isEmpty()) {
			this.nickname = "&f" + this.name;
		}
		if(!Main.stripColorCodes(Main.formatColorCodes(this.nickname)).equals(this.name)) {
			return "~" + this.nickname;
		}
		return this.nickname;
	}
	
	public final String getDisplayName() {
		if(!this.getPrefix().isEmpty() && !this.getSuffix().isEmpty()) {
			return Main.formatColorCodes("&f[" + this.getPrefix() + "&r&f] " + (Main.displayNicknameBrackets ? "&f[" + this.getNickName() + "&r&f]" : "&f" + this.getNickName() + "&r&f") + " [" + this.getSuffix() + "&r&f]");
		} else if(!this.getPrefix().isEmpty()) {
			return Main.formatColorCodes("&f[" + this.getPrefix() + "&r&f] " + (Main.displayNicknameBrackets ? "&f[" + this.getNickName() + "&r&f]" : "&f" + this.getNickName() + "&r&f"));
		} else if(!this.getSuffix().isEmpty()) {
			return Main.formatColorCodes((Main.displayNicknameBrackets ? "&f[" + this.getNickName() + "&r&f]" : "&f" + this.getNickName() + "&r&f") + " [" + this.getSuffix() + "&r&f]");
		} else {
			return Main.formatColorCodes("&f[" + this.getNickName() + "&r&f]");
		}
	}
	
	public static final String getChatFormat() {
		return "%s: %s";
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
			if(perm != null) {
				if(perm.group != null) {
					String displayName = perm.group.displayName;
					perm.saveAndDisposeIfPlayerNotOnline();
					return displayName;
				}
			}
		}
		return(this.prefix != null ? this.prefix : (this.prefix = ""));
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
		chat.setPrefix(prefix);
		if(!chat.isPlayerOnline()) {
			chat.saveToFileAndDispose();
		}
	}
	
	public final boolean setPrefix(String prefix) {
		if(prefix == null) {
			prefix = "";
		}
		String oldPrefix = this.prefix;
		this.prefix = (prefix.isEmpty() ? "" : PlayerChat.removeCurseWordsFromStr(prefix, oldPrefix));
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
			nickname = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
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
		return(this.suffix != null ? this.suffix : (this.suffix = ""));
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
		this.suffix = (suffix.isEmpty() ? "" : PlayerChat.removeCurseWordsFromStr(suffix, oldSuffix));
		if(this.isPlayerOnline()) {
			this.getPlayer().setDisplayName(this.getDisplayName());
		}
		return !this.suffix.equals(oldSuffix);
	}
	
	//==============================================================================================
	
	public static final HashMap<Integer, String>	curseWordReplacements	= new HashMap<>();
	
	static {
		PlayerChat.curseWordReplacements.put(new Integer(0), "Ha! I just tried to cuss! Aren't I wonderful!");
		PlayerChat.curseWordReplacements.put(new Integer(1), "Weee! I just love getting dirty!");
		//curseWordReplacements.put(new Integer(2), "");
	}
	
	public static final String getAFunnyReplacementForACurseWord() {
		int random = Main.getRandomIntValBetween(0, PlayerChat.curseWordReplacements.size() - 1);
		return PlayerChat.curseWordReplacements.get(new Integer(random));
	}
	
	public static final String removeCurseWordsFromStr(String str, String fallbackValue) {
		str = str.replaceAll("(?i)motherfucker", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)fuck", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)shit", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)cunt", "");
		str = str.replaceAll("(?i)ass", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)a\\$\\$", "");
		str = str.replaceAll("(?i)@ss", "");
		str = str.replaceAll("@\\$\\$", "");
		str = str.replaceAll("(?i)nigger", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)budder", "gold");//XD
		str = str.replaceAll("(?i)bitch", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)damn", "");
		str = str.replaceAll("(?i)dammit", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)damit", "");
		str = str.replaceAll("(?i)f\\*ck", "");
		str = str.replaceAll("(?i)fu\\*k", "");
		str = str.replaceAll("(?i)\\*uck", "");
		str = str.replaceAll("(?i)fuc\\*", "");
		str = str.replaceAll("(?i)sh\\*t", "");
		str = str.replaceAll("(?i)shi\\*", "");
		str = str.replaceAll("(?i)\\*ss", "");
		str = str.replaceAll("(?i)a\\*\\*", "");
		str = str.replaceAll("(?i)f\\*\\*\\*", "");
		str = str.replaceAll("(?i)bi\\*\\*\\*", "");
		str = str.replaceAll("(?i)b\\*\\*\\*\\*", "");
		str = str.replaceAll("(?i)whore", "");
		str = str.replaceAll("(?i)anus", PlayerChat.getAFunnyReplacementForACurseWord());
		str = str.replaceAll("(?i)faggot", "bundle of sticks");
		str = str.replaceAll("(?i)gay", "very happy");
		str = str.replaceAll("(?i)homosexual", "different from others");
		str = str.replaceAll("(?i)nohomo", "I don't mean to offend");
		str = str.replaceAll("(?i)homo", "Homo Sapiens Sapiens");
		str = str.replaceAll("(?i)whale cum", "welcome");
		str = str.replaceAll("(?i)whalecum", "welcome");
		//str = str.replaceAll("(?i)", "");
		if(str.isEmpty()) {
			return fallbackValue;
		}
		return str;
	}
	
	//==============================================================================================
	
	public final ArrayList<Mail> getAllMail() {
		ArrayList<Mail> rtrn = new ArrayList<>();
		for(Map.Entry<UUID, ArrayList<Mail>> entry : this.mailbox.entrySet()) {
			for(Mail mail : entry.getValue()) {
				if(!rtrn.contains(mail)) {
					rtrn.add(mail);
				}
			}
		}
		return rtrn;
	}
	
	public final ArrayList<Mail> getAllOldMail() {
		ArrayList<Mail> rtrn = new ArrayList<>();
		for(Map.Entry<UUID, ArrayList<Mail>> entry : this.oldMail.entrySet()) {
			for(Mail mail : entry.getValue()) {
				if(!rtrn.contains(mail)) {
					rtrn.add(mail);
				}
			}
		}
		return rtrn;
	}
	
	private static final boolean doesArrayContainAnyNullObjects(Object[] array) {
		for(int i = 0; i < array.length; i++) {
			if(array[i] == null) {
				return true;
			}
		}
		return false;
	}
	
	private static final boolean doesArrayContainMail(Mail[] array, Mail mail) {
		for(Mail m : array) {
			if(m == mail) {
				return true;
			}
		}
		return false;
	}
	
	private static final int getNextFreeIndexInArray(Object[] array) {
		if(array == null || !PlayerChat.doesArrayContainAnyNullObjects(array)) {
			return -1;
		}
		for(int i = 0; i < array.length; i++) {
			if(array[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	public static final Mail[] getMailsInOrder(ArrayList<Mail> allMails) {//(oldest to newest)
		Mail[] rtrn = new Mail[allMails.size()];
		long oldestTimeStamp = Long.MAX_VALUE;
		while(PlayerChat.doesArrayContainAnyNullObjects(rtrn)) {
			Mail tmpOldest = null;
			for(Mail mail : allMails) {
				if(mail.timeStamp < oldestTimeStamp && (tmpOldest != null ? mail.timeStamp < tmpOldest.timeStamp : true) && !PlayerChat.doesArrayContainMail(rtrn, mail)) {
					tmpOldest = mail;
				}
			}
			if(tmpOldest != null) {
				int index = PlayerChat.getNextFreeIndexInArray(rtrn);
				if(index != -1) {
					rtrn[index] = tmpOldest;
				}
			}
		}
		return rtrn;
	}
	
	public final Mail[] getMailInOrder() {
		return PlayerChat.getMailsInOrder(this.getAllMail());
	}
	
	public final Mail[] getOldMailInOrder() {
		return PlayerChat.getMailsInOrder(this.getAllOldMail());
	}
	
	public static final class Mail {
		public final UUID	sender;
		public final String	msg;
		public final long	timeStamp;
		
		public Mail(UUID sender, String msg, long timeStamp) {
			this.sender = sender;
			this.msg = msg;
			this.timeStamp = timeStamp;
		}
		
		@Override
		public final int hashCode() {//Generated using eclipse
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.msg == null) ? 0 : this.msg.hashCode());
			result = prime * result + ((this.sender == null) ? 0 : this.sender.hashCode());
			result = prime * result + (int) (this.timeStamp ^ (this.timeStamp >>> 32));
			return result;
		}
		
		@Override
		public final boolean equals(Object obj) {//Generated using eclipse
			if(this == obj) return true;
			if(obj == null) return false;
			if(this.getClass() != obj.getClass()) return false;
			Mail other = (Mail) obj;
			if(this.msg == null) {
				if(other.msg != null) return false;
			} else if(!this.msg.equals(other.msg)) return false;
			if(this.sender == null) {
				if(other.sender != null) return false;
			} else if(!this.sender.equals(other.sender)) return false;
			if(this.timeStamp != other.timeStamp) return false;
			return true;
		}
		
		/* @Override
		public final boolean equals(Object obj) {//My version
			if(obj instanceof Mail) {
				Mail mail = Mail.class.cast(obj);
				return(mail.msg.equals(this.msg) && mail.timeStamp == this.timeStamp && mail.sender.toString().equals(this.sender.toString()));
			}
			return super.equals(obj);
		} */
		
		public static final Mail getFromConfig(ConfigurationSection mailbox, String key) {
			if(CodeUtils.isStrAValidLong(key)) {
				long timeStamp = CodeUtils.getLongFromStr(key, -1L);
				if(timeStamp != -1L) {
					ConfigurationSection mail = mailbox.getConfigurationSection(key);
					if(mail != null) {
						String Sender = mail.getString("sender");
						if(Main.isStringUUID(Sender)) {
							UUID sender = UUID.fromString(Sender);
							String msg = mail.getString("msg");
							return new Mail(sender, msg, timeStamp);
						}
					}
				}
			}
			return null;
		}
		
		public final void saveToConfig(ConfigurationSection mailbox) {
			String key = this.timeStamp + "";
			ConfigurationSection mail = mailbox.getConfigurationSection(key);
			if(mail == null) {
				mail = mailbox.createSection(key);
			}
			mail.set("sender", this.sender.toString());
			mail.set("msg", this.msg);
		}
		
	}
	
}
