package com.gmail.br45entei.supercmds.api;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerChat;
import com.gmail.br45entei.supercmds.file.PlayerStatus;

import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class Chat implements Listener {
	
	protected static final Pattern	validIpAddressRegex	= Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
	
	protected static final Pattern	validHostnameRegex	= Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
	
	public Chat() {
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player newPlayer = event.getPlayer();
		if(Main.handleChat) {
			PlayerChat chat = PlayerChat.getPlayerChat(newPlayer);
			chat.onPlayerJoin(event);
			newPlayer.setDisplayName(chat.getDisplayName());
			newPlayer.setPlayerListName(Main.formatColorCodes(chat.getNickName()));
		}
	}
	
	/** @param event Called when a player leaves a server */
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
		chatter.setDisplayName(chat.getDisplayName());
		final boolean isMuteExempt = Permissions.hasPerm(chatter, "supercmds.mute.exempt");
		if(chat.nextChatSeemsTooQuick()) {
			if(!isMuteExempt) {
				Main.sendMessage(chatter, Main.pluginName + "&eSlow down, o' keyboard warrior.&z&aDon't want to get muted eh?");
				chat.incrementQuickChatTimes();
			}
		}
		if(!isMuteExempt && chat.isMuted()) {
			event.setCancelled(true);
			if(chat.muteEndTime == -1L) {
				Main.sendMessage(chatter, Main.pluginName + "&eYou have been &cmuted&e until further notice.&z&eYou cannot chat until you are un-muted by a server administrator.");
			} else {
				Main.sendMessage(chatter, Main.pluginName + "&eYou are currently &cmuted&e.&z&eYou cannot chat until you are either un-muted or the mute expires.");
				Main.sendMessage(chatter, Main.pluginName + "&eMute time remaining: &f" + Main.getLengthOfTime(System.currentTimeMillis() - chat.muteEndTime));
			}
			return;
		}
		if(!isMuteExempt && (chat.seemsToBeSpamming() && !chat.isMuted())) {
			chat.setMuted(true, 300000L);//5 minutes
			Main.sendMessage(chatter, Main.pluginName + "&eYou appear to be spamming, and have been &cmuted&e.&z&eYou cannot chat for the next &65 &eminutes.");
		} else {
			chat.setMuted(false, 0);
		}
		PlayerStatus status = PlayerStatus.getPlayerStatus(chatter);
		if(status.isAfk) {
			status.toggleAfkState();
		}
		String msg = event.getMessage();
		if(msg.contains("http://") || msg.contains("https://") || msg.contains("ftp://")) {
			/*String[] split = msg.split(Pattern.quote(" "));
			String rMsg = "";
			int i = 0;
			for(String arg : split) {
				if(arg.startsWith("http://") || arg.startsWith("https://") || arg.startsWith("ftp://")) {
					if(!Permissions.hasPerm(chatter, "supercmds.chat.noFilter") && !Main.isPlayerAStaffMember(chatter)) {
						i++;
						continue;
					}
				}
				rMsg += arg + (i + 1 == split.length ? "" : " ");
				i++;
			}
			msg = rMsg;*/
			if(!Permissions.hasPerm(chatter, "supercmds.chat.noFilter") && !Main.isPlayerAStaffMember(chatter)) {
				event.setMessage("");
				event.setCancelled(true);
				return;
			}
		}
		if(Permissions.hasPerm(chatter, "supercmds.chat.colors")) {
			if(Permissions.hasPerm(chatter, "supercmds.chat.colors.magic")) {
				msg = Main.formatColorCodes(msg);
			} else {
				msg = Main.formatColorCodes(msg, false);
			}
		}
		/*if(chatter.getName().equals("Brian_Entei") && msg.equalsIgnoreCase("test nms block")) {
			//event.setCancelled(true);
			msg = "setBlockNatively(...) result: " + BlockNMS.setBlock(chatter.getWorld().getBlockAt(chatter.getLocation()), Material.BEDROCK, 0);
		}*/
		if(!Permissions.hasPerm(chatter, "supercmds.chat.noFilter") && !Main.isPlayerAStaffMember(chatter)) {
			if(!msg.isEmpty()) {
				event.setMessage(PlayerChat.removeCurseWordsFromStr(msg, Main.white + "I just said a message full of curse words, and it was " + Main.dred + "removed" + Main.white + "."));
			} else {
				event.setCancelled(true);
			}
		} else {
			event.setMessage(msg);
		}
		event.setFormat(PlayerChat.getChatFormat());
		chat.markChatTime();
	}
	
}
