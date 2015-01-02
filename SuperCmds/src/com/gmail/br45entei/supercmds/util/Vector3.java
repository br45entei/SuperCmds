package com.gmail.br45entei.supercmds.util;

import org.bukkit.Location;

/** @author Brian_Entei */
public final class Vector3 {
	public int	x;
	public int	y;
	public int	z;
	
	public Vector3() {
		this(0, 0, 0);
	}
	
	public Vector3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public static final Vector3 getFromLocation(Location loc) {
		return new Vector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	@Override
	public final boolean equals(Object vector) {
		if(vector instanceof Vector3) {
			Vector3 vec = Vector3.class.cast(vector);
			return this.x == vec.x && this.y == vec.y && this.z == vec.z;
		}
		return super.equals(vector) || vector.hashCode() == this.hashCode();
	}
	
	@Override
	public final String toString() {
		return "(" + this.x + "," + this.y + "," + this.z + ")";
	}
	
	@Override
	public final int hashCode() {
		return new Double(((this.x + this.z) / this.y) - (this.x + this.y + this.z)).intValue();
	}
	
}
