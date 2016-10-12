package com.gmail.br45entei.supercmds.cmds;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;

@SuppressWarnings("javadoc")
public final class InventoryViewingInfo {
	private static final ArrayList<UUID>	worldUUIDsViewed	= new ArrayList<>();
	public final UUID						owner;
	
	public static final ArrayList<UUID> getAllViewedWorlds() {
		return new ArrayList<>(worldUUIDsViewed);
	}
	
	public static final void registerWorldAsHavingBeenViewed(World world) {
		if(world != null) {
			UUID uuid = world.getUID();
			if(!worldUUIDsViewed.contains(uuid)) {
				worldUUIDsViewed.add(uuid);
			}
		}
	}
	
	public final GameMode	ownerGameMode;
	public final UUID		ownerWorld;
	public final UUID		viewer;
	public final Inventory	inv;
	public final String		invType;
	
	public InventoryViewingInfo(UUID owner, GameMode ownerGameMode, UUID ownerWorld, UUID viewer, Inventory inv, String invType) {
		this.owner = owner;
		this.ownerGameMode = ownerGameMode;
		this.ownerWorld = ownerWorld;
		this.viewer = viewer;
		this.inv = inv;
		this.invType = invType;
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (this.invType == null ? 0 : this.invType.hashCode());
		result = prime * result + (this.owner == null ? 0 : this.owner.hashCode());
		result = prime * result + (this.ownerGameMode == null ? 0 : this.ownerGameMode.hashCode());
		result = prime * result + (this.ownerWorld == null ? 0 : this.ownerWorld.hashCode());
		result = prime * result + (this.viewer == null ? 0 : this.viewer.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		InventoryViewingInfo other = (InventoryViewingInfo) obj;
		if(this.invType == null) {
			if(other.invType != null) return false;
		} else if(!this.invType.equals(other.invType)) return false;
		if(this.owner == null) {
			if(other.owner != null) return false;
		} else if(!this.owner.equals(other.owner)) return false;
		if(this.ownerGameMode != other.ownerGameMode) return false;
		if(this.ownerWorld == null) {
			if(other.ownerWorld != null) return false;
		} else if(!this.ownerWorld.equals(other.ownerWorld)) return false;
		if(this.viewer == null) {
			if(other.viewer != null) return false;
		} else if(!this.viewer.equals(other.viewer)) return false;
		return true;
	}
	
	public final boolean equals(UUID owner, GameMode mode, UUID world, String invType) {
		if((owner == null) || (mode == null) || (world == null) || (invType == null) || (invType.trim().isEmpty())) {
			return false;
		}
		return (this.owner.toString().equals(owner.toString())) && (this.ownerGameMode == mode) && (this.ownerWorld.toString().equals(world.toString())) && (this.invType.equals(invType));
	}
	
}
