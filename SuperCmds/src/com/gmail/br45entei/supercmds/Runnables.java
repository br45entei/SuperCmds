package com.gmail.br45entei.supercmds;

import com.gmail.br45entei.supercmds.cmds.MainCmdListener.PlayerStatus;

/** @author Brian_Entei */
public class Runnables {
	
	public static final Runnable	updateVanishStatesTask;
	
	static {
		updateVanishStatesTask = new Runnable() {
			@Override
			public void run() {
				if(Main.server.getOnlinePlayers().size() > 0) {
					PlayerStatus.updatePlayerVanishStates();
				}
			}
		};
	}
	
}
