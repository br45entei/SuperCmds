package com.gmail.br45entei.supercmds.api.credits;

import com.gmail.br45entei.supercmds.file.PlayerStatus;
import com.gmail.br45entei.util.StringUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** @author Brian_Entei */
@SuppressWarnings({"javadoc", "unused"})
public class PurchasableItems {
	
	/** @param cmd unused */
	public static final boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {
		final Player user;
		if(sender instanceof Player) {
			user = (Player) sender;
		} else {
			user = null;
		}
		final String strArgs = StringUtil.stringArrayToString(args, ' ');
		final String userName = user != null ? (user.getDisplayName().isEmpty() ? sender.getName() : user.getDisplayName()) : sender.getName();
		final PlayerStatus status = PlayerStatus.getPlayerStatus(user);
		if(command.equals("purchase")) {
			//TODO
		}
		return false;
	}
	
}
