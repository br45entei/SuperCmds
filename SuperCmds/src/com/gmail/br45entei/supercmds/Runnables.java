package com.gmail.br45entei.supercmds;

import com.gmail.br45entei.supercmds.file.PlayerStatus;

import org.bukkit.scheduler.BukkitRunnable;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class Runnables {
	
	public static final BukkitRunnable updatePlayerStatesTask;
	
	static {
		updatePlayerStatesTask = new BukkitRunnable() {
			@Override
			public void run() {
				if(Main.server.getOnlinePlayers().size() > 0) {
					PlayerStatus.updatePlayerStateStates();
				}
			}
		};
	}
	
}
