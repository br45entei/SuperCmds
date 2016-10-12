package com.gmail.br45entei.supercmds.file;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

@SuppressWarnings("javadoc")
public final class Home {
	public Location	location;
	public String	name;
	
	public Home(String name, Location location) {
		this.name = name;
		this.location = location;
	}
	
	public static final Home getFromConfig(String name, ConfigurationSection homesMem) {
		if(homesMem == null) {
			return null;
		}
		ConfigurationSection home = homesMem.getConfigurationSection(name);
		if(home != null) {
			Location location = SavablePlayerData.getLocationFromConfig("location", home);
			return new Home(name, location);
		}
		return null;
	}
	
	public final boolean saveToConfig(ConfigurationSection homesMem) {
		if(homesMem == null) {
			return false;
		}
		ConfigurationSection home = homesMem.getConfigurationSection(this.name);
		if(home == null) {
			home = homesMem.createSection(this.name);
		}
		SavablePlayerData.saveLocationToConfig("location", this.location, home, true);
		return true;
	}
	
}
