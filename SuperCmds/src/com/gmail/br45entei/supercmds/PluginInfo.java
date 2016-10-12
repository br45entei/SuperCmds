package com.gmail.br45entei.supercmds;

import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

/** Interface used for plugin sorting and data
 * 
 * @author Brian_Entei */
public interface PluginInfo {
	
	/** @return This plugin's UUID */
	public UUID getPluginUUID();
	
	/** @return The Bukkit JavaPlugin */
	public JavaPlugin getPlugin();
	
	/** @return This plugin's display name */
	public String getDisplayName();
	
}
