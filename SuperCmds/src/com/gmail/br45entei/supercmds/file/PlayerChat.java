package com.gmail.br45entei.supercmds.file;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.PluginInfo;
import com.gmail.br45entei.supercmds.api.Permissions;
import com.gmail.br45entei.supercmds.util.CodeUtils;

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
import org.bukkit.plugin.java.JavaPlugin;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class PlayerChat extends SavablePlayerData {
	private static final ArrayList<PlayerChat> chatInstances = new ArrayList<>();
	
	public static final ArrayList<PlayerChat> getInstances() {
		return new ArrayList<>(PlayerChat.chatInstances);
	}
	
	public final boolean						isConsole;
	
	public volatile String						prefix						= "";
	public volatile String						nickname					= "";
	public volatile String						suffix						= "";
	
	public volatile UUID						lastPlayerThatIRepliedTo	= null;
	public final HashMap<UUID, ArrayList<Mail>>	mailbox						= new HashMap<>();
	public final HashMap<UUID, ArrayList<Mail>>	oldMail						= new HashMap<>();
	
	private volatile boolean					isMuted						= false;
	public volatile long						muteEndTime					= System.currentTimeMillis();
	private volatile long						timesMuted					= 0;
	
	//Don't save the following to file:
	private volatile long						lastChatTime				= System.currentTimeMillis();
	private volatile int						numOfQuickChatTimes			= 0;
	
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
	
	public final void markChatTime() {
		this.lastChatTime = System.currentTimeMillis();
	}
	
	public final long getLastChatTime() {
		return this.lastChatTime;
	}
	
	public final boolean nextChatSeemsTooQuick() {
		return (System.currentTimeMillis() - this.lastChatTime) <= 750L;
	}
	
	public final void incrementQuickChatTimes() {
		this.numOfQuickChatTimes++;
	}
	
	public final boolean seemsToBeSpamming() {
		return this.numOfQuickChatTimes > 3;
	}
	
	public final PlayerChat sendMail(UUID sender, String mail) {
		ArrayList<Mail> targetsMail = this.mailbox.get(sender);
		if(targetsMail == null) {
			targetsMail = new ArrayList<>();
			this.mailbox.put(sender, targetsMail);
		}
		targetsMail.add(new Mail(sender, mail, System.currentTimeMillis()));
		if(this.isPlayerOnline()) {
			Main.sendMessage(this.getPlayer(), Main.pluginName + "&6You have new mail!&z&aType &f/mail read&a to read it.");
		}
		Main.sendMessage(Main.getPlayer(sender), Main.pluginName + "&aYour message was sent to \"&f" + this.getDisplayName() + "&r&a\"'s inbox successfully.");
		return this;
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
	public final boolean isPlayerOnline() {
		if(this.isConsole) {
			return false;
		}
		return super.isPlayerOnline();
	}
	
	@Override
	public final String toString() {
		return "&3PlayerChat:\n" + //
				"&3Prefix: \"&f" + this.prefix + "&r&3\"\n" + //
				"&3NickName: \"&f" + this.nickname + "&r&3\"\n" + //
				"&3Suffix: \"&f" + this.suffix + "&r&3\"";
	}
	
	//=======================
	
	/** @return Whether or not this player is muted. The player may only be
	 *         muted
	 *         for a period of time, or muted indefinitely(-1). */
	public final boolean isMuted() {
		if(this.muteEndTime == -1L) {
			this.isMuted = true;
			return true;
		}
		if(this.isMuted) {
			this.isMuted = this.muteEndTime > System.currentTimeMillis();
			return this.isMuted;
		}
		return false;
	}
	
	/** @param mute Whether or not this player should be considered muted.
	 * @param millisecondsFor The length of time(in milliseconds!) that this
	 *            player should be muted for until being able to chat again. Set
	 *            to 0 for no mute and -1 for infinite mute(muted until further
	 *            notice etc)
	 * @return The return value of {@link PlayerChat#isMuted()} after the values
	 *         are set. */
	public final boolean setMuted(boolean mute, long millisecondsFor) {
		if(Permissions.hasPerm(this.uuid, "supercmds.mute.exempt")) {
			boolean wasMuted = this.isMuted();
			this.isMuted = false;
			this.muteEndTime = 0;
			return mute ? false : (wasMuted ? true : false);
		}
		if(this.isMuted() == mute) {
			return false;
		}
		if(millisecondsFor == -1L) {
			this.isMuted = true;
			this.muteEndTime = -1L;
		} else {
			this.isMuted = mute;
			this.muteEndTime = System.currentTimeMillis() + millisecondsFor;
		}
		if(mute) {
			this.timesMuted++;
		}
		return mute ? this.isMuted() : !this.isMuted();
	}
	
	@Override
	public String getSaveFolderName() {
		return "PlayerChatData";
	}
	
	@Override
	public final void onFirstLoad() {
		
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
		//==============================
		this.isMuted = mem.getBoolean("isMuted");
		this.muteEndTime = mem.getLong("muteEndTime", this.muteEndTime);
		this.timesMuted = mem.getLong("timesMuted", this.timesMuted);
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
		//=============================
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
		//=============================
		mem.set("isMuted", Boolean.valueOf(this.isMuted));
		mem.set("muteEndTime", Long.valueOf(this.muteEndTime));
		mem.set("timesMuted", Long.valueOf(this.timesMuted));
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
		Main.DEBUG("PlayerChat: onPlayerJoinEvent: " + this.name);
		Player newPlayer = event.getPlayer();
		if(SavablePlayerData.playerEquals(this.getPlayer(), newPlayer)) {
			this.name = Main.uuidMasterList.getPlayerNameFromUUID(this.uuid);
			this.loadFromFile();
			newPlayer.setDisplayName(this.getDisplayName());
			newPlayer.setPlayerListName(Main.formatColorCodes(this.getNickName()));
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
	
	public static final PlayerChat getPlayerChatWithNickName(String nickName) {
		if(nickName == null) {
			return null;
		}
		if(nickName.startsWith("~")) {
			nickName = nickName.substring(1);
		}
		if(nickName.isEmpty()) {
			return null;
		}
		nickName = Main.stripColorCodes(Main.formatColorCodes(nickName));
		for(UUID uuid : Main.uuidMasterList.getAllListedUUIDS()) {
			PlayerChat chat = PlayerChat.getPlayerChat(uuid);
			String nick = chat.getNickName();
			if(nick.startsWith("~")) {
				nick = nick.substring(1);
			}
			nick = Main.stripColorCodes(Main.formatColorCodes(nick));
			if(nick.equalsIgnoreCase(nickName)) {
				return chat;
			}
			chat.disposeIfPlayerNotOnline();
		}
		return null;
	}
	
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
		if(rtrn == null || rtrn.isEmpty()) {
			rtrn = Main.uuidMasterList.getPlayerNameFromUUID(uuid);
		}
		return rtrn;
	}
	
	public final String getNickName() {
		if(this.nickname == null || this.nickname.isEmpty()) {
			this.nickname = "&f" + this.name;
		}
		if(!Main.stripColorCodes(Main.formatColorCodes(this.nickname)).equals(this.name) && !Permissions.hasPerm(this.uuid, "supercmds.chat.notilde")) {
			return "&8~&f" + this.nickname;
		}
		return this.nickname;
	}
	
	public final String getDisplayName() {
		if(!this.getPrefix().isEmpty() && !this.getSuffix().isEmpty()) {
			return Main.formatColorCodes("&f" + this.getPrefix() + "&r&f " + (Main.displayNicknameBrackets ? "&f[" + this.getNickName() + "&r&f]" : "&f" + this.getNickName() + "&r&f") + " " + this.getSuffix() + "&r&f");
		} else if(!this.getPrefix().isEmpty()) {
			return Main.formatColorCodes("&f" + this.getPrefix() + "&r&f " + (Main.displayNicknameBrackets ? "&f[" + this.getNickName() + "&r&f]" : "&f" + this.getNickName() + "&r&f"));
		} else if(!this.getSuffix().isEmpty()) {
			return Main.formatColorCodes((Main.displayNicknameBrackets ? "&f[" + this.getNickName() + "&r&f]" : "&f" + this.getNickName() + "&r&f") + " " + this.getSuffix() + "&r&f");
		} else {
			return Main.formatColorCodes("&f" + this.getNickName() + "&r&f");
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
					perm.disposeIfPlayerNotOnline();
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
			this.getPlayer().setPlayerListName(Main.formatColorCodes(this.getNickName()));
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
	
	public static final HashMap<Integer, String> curseWordReplacements = new HashMap<>();
	
	static {
		PlayerChat.curseWordReplacements.put(new Integer(0), "");//"Ha! I just tried to cuss! Aren't I wonderful!");
		PlayerChat.curseWordReplacements.put(new Integer(1), "");//"Curse words are fun.");
		//curseWordReplacements.put(new Integer(2), "");
	}
	
	public static final String getAFunnyReplacementForACurseWord() {
		int random = Main.getRandomIntValBetween(0, PlayerChat.curseWordReplacements.size());
		return PlayerChat.curseWordReplacements.get(new Integer(random));
	}
	
	public static final boolean containsCurseWords(final String str) {
		return !str.equals(removeCurseWordsFromStr(str, ""));
	}
	
	public static final String removeCurseWordsFromStr(String str, String fallbackValue) {
		//str = Main.GrammarEnforcement(str, Main.dataFolderName);
		str = Main.replaceWord(str, "motherfucker", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "fuck", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "shit", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "cunt", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "ass", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "a\\$\\$", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "@ss", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "@\\$\\$", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "nigger", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "budder", "gold", false, Main.dataFolderName);//XD
		str = Main.replaceWord(str, "bitch", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "damn", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "dammit", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "damit", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "f\\*ck", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "fu\\*k", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "\\*uck", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "fuc\\*", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "sh\\*t", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "shi\\*", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "\\*ss", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "a\\*\\*", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "f\\*\\*\\*", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "bi\\*\\*\\*", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "b\\*\\*\\*\\*", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "whore", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "anus", PlayerChat.getAFunnyReplacementForACurseWord(), false, Main.dataFolderName);
		str = Main.replaceWord(str, "faggot", "bundle of sticks", false, Main.dataFolderName);
		str = Main.replaceWord(str, "gay", "very happy", false, Main.dataFolderName);
		str = Main.replaceWord(str, "homosexual", "different from others", false, Main.dataFolderName);
		str = Main.replaceWord(str, "nohomo", "I don't mean to offend", false, Main.dataFolderName);
		str = Main.replaceWord(str, "homo", "Homo Sapiens Sapiens", false, Main.dataFolderName);
		str = Main.replaceWord(str, "whale cum", "welcome", false, Main.dataFolderName);
		str = Main.replaceWord(str, "whalecum", "welcome", false, Main.dataFolderName);
		str = Main.replaceWord(str, "nigga", "", false, Main.dataFolderName);
		str = Main.replaceWord(str, "nigger", "", false, Main.dataFolderName);
		//str = Main.replaceWord(str, "", "", false, Main.dataFolderName);
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
	
	private static final boolean doesArrayContainMail(Mail[] array, Mail mail) {
		for(Mail m : array) {
			if(m == mail) {
				return true;
			}
		}
		return false;
	}
	
	public static final Mail[] getMailsInOrder(ArrayList<Mail> allMails) {//(oldest to newest)
		Mail[] rtrn = new Mail[allMails.size()];
		long oldestTimeStamp = Long.MAX_VALUE;
		while(Main.doesArrayContainAnyNullObjects(rtrn)) {
			Mail tmpOldest = null;
			for(Mail mail : allMails) {
				if(mail.timeStamp < oldestTimeStamp && (tmpOldest != null ? mail.timeStamp < tmpOldest.timeStamp : true) && !PlayerChat.doesArrayContainMail(rtrn, mail)) {
					tmpOldest = mail;
				}
			}
			if(tmpOldest != null) {
				int index = Main.getNextFreeIndexInArray(rtrn);
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
		
		public final String getSenderName() {
			String name = (this.sender.toString().equals(Main.consoleUUID.toString()) ? Main.consoleSayFormat.trim() : Main.uuidMasterList.getPlayerNameFromUUID(this.sender));
			if(name.isEmpty()) {
				for(JavaPlugin plugin : Main.pluginsUsingMe) {
					if(plugin instanceof PluginInfo) {
						PluginInfo info = (PluginInfo) plugin;
						if(info.getPluginUUID().toString().equals(this.sender.toString())) {
							name = info.getDisplayName();
							break;
						}
					}
				}
			}
			return name;
		}
		
		@Override
		public final String toString() {
			return "&3[" + this.getSenderName() + "]&f: " + this.msg;
		}
		
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
