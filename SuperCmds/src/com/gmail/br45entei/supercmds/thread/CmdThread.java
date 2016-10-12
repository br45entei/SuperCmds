package com.gmail.br45entei.supercmds.thread;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.file.PlayerPermissions.Group;
import com.gmail.br45entei.supercmds.file.SavablePlayerData;
import com.gmail.br45entei.supercmds.file.SavablePluginData;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class CmdThread extends EngineThread {
	private static CmdThread instance;
	
	public static final CmdThread getInstance() {
		if(CmdThread.instance == null) {
			CmdThread.instance = new CmdThread();
			CmdThread.instance.start();
		} else if(!CmdThread.instance.isRunning()) {
			try {
				CmdThread.instance.start();
			} catch(IllegalThreadStateException ignored) {
				CmdThread.instance.stopThread();
				CmdThread.instance = null;
				return CmdThread.getInstance();
			}
		}
		return CmdThread.instance;
	}
	
	/** Default Constructor */
	public CmdThread() {
		super(Main.getInstance(), "SuperCmdsThread", 60);
	}
	
	public static boolean	savePlayerData		= false;
	public static boolean	savePluginData		= false;
	public static boolean	reloadPlayerData	= false;
	public static boolean	reloadPluginData	= false;
	
	@Override
	public final void execute() {
		if(CmdThread.savePlayerData) {
			CmdThread.savePlayerData = false;
			for(SavablePlayerData data : SavablePlayerData.getAllInstances()) {
				if(data.saveAndLoadWithSuperCmds) {
					data.saveToFile();
				}
			}
		} else if(CmdThread.reloadPlayerData) {
			CmdThread.reloadPlayerData = false;
			for(SavablePlayerData data : SavablePlayerData.getAllInstances()) {
				if(data.saveAndLoadWithSuperCmds) {
					data.loadFromFile();
				}
			}
		}
		if(CmdThread.savePluginData) {
			CmdThread.savePluginData = false;
			for(SavablePluginData data : SavablePluginData.getAllInstances()) {
				if(data.saveAndLoadWithSuperCmds) {
					data.saveToFile();
				}
			}
			try {
				Group.saveToStaticFile();
			} catch(Throwable e) {
				Main.sendConsoleMessage(Main.pluginName + "&cUnable to load from groups.yml file:&z&4" + Main.throwableToStr(e));
			}
		} else if(CmdThread.reloadPluginData) {
			CmdThread.reloadPluginData = false;
			for(SavablePluginData data : SavablePluginData.getAllInstances()) {
				if(data.saveAndLoadWithSuperCmds) {
					data.loadFromFile();
				}
			}
			try {
				Group.reloadFromFile();
			} catch(Throwable e) {
				Main.sendConsoleMessage(Main.pluginName + "&cUnable to load from groups.yml file:&z&4" + Main.throwableToStr(e));
			}
		}
	}
	
	@Override
	public final void cleanup() {
	}
	
}
