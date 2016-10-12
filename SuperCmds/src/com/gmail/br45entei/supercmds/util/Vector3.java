package com.gmail.br45entei.supercmds.util;

import org.bukkit.Location;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public final class Vector3 {
	public volatile int	x;
	public volatile int	y;
	public volatile int	z;
	
	public Vector3() {
		this(0, 0, 0);
	}
	
	public Vector3(Vector3 copy) {
		this.x = copy.x;
		this.y = copy.y;
		this.z = copy.z;
	}
	
	public Vector3(int i) {
		this(i, i, i);
	}
	
	public Vector3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3(double x, double y, double z) {
		this(new Double(x).intValue(), new Double(y).intValue(), new Double(z).intValue());
	}
	
	public final boolean isLocationSmallerOrEqualTo(Location loc) {
		if(loc != null) {
			return this.x >= loc.getBlockX() && this.y >= loc.getBlockY() && this.z >= loc.getBlockZ();
		}
		return false;
	}
	
	public final boolean isLocationGreaterOrEqualTo(Location loc) {
		if(loc != null) {
			return this.x <= loc.getBlockX() && this.y <= loc.getBlockY() && this.z <= loc.getBlockZ();
		}
		return false;
	}
	
	public final Vector3 setFromLocation(Location loc) {
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		return this;
	}
	
	public static final Vector3 getFromLocation(Location loc) {
		return new Vector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	@Override
	public final String toString() {
		return "(" + this.x + "," + this.y + "," + this.z + ")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
		result = prime * result + this.z;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(this.getClass() != obj.getClass()) return false;
		Vector3 other = (Vector3) obj;
		if(this.x != other.x) return false;
		if(this.y != other.y) return false;
		if(this.z != other.z) return false;
		return true;
	}
	
	/** @param vectorString
	 * @return A new Vector3 if the given string depicts a valid vector 3, or
	 *         null otherwise. */
	public static Vector3 getFromString(String vectorString) {
		if(vectorString == null || vectorString.isEmpty() || vectorString.length() < 5) {
			return null;
		}
		if(vectorString.startsWith("(")) {
			vectorString = vectorString.substring(1);
		}
		if(vectorString.endsWith(")")) {
			vectorString = vectorString.substring(0, vectorString.length() - 1);
		}
		if(vectorString.length() < 5) {
			return null;
		}
		if(vectorString.contains(",")) {
			String[] args = vectorString.split(",");
			if(args.length == 3) {
				if(CodeUtils.isStrAValidDouble(args[0])) {
					if(CodeUtils.isStrAValidDouble(args[1])) {
						if(CodeUtils.isStrAValidDouble(args[2])) {
							return new Vector3(CodeUtils.getDoubleFromStr(args[0], 0), CodeUtils.getDoubleFromStr(args[1], 0), CodeUtils.getDoubleFromStr(args[2], 0));
						}
					}
				}
			}
		}
		return null;
	}
	
	/*@Override
	public final boolean equals(Object vector) {
		if(vector instanceof Vector3) {
			Vector3 vec = Vector3.class.cast(vector);
			return this.x == vec.x && this.y == vec.y && this.z == vec.z;
		}
		return super.equals(vector) || vector.hashCode() == this.hashCode();
	}*/
	
	/*@Override
	public final int hashCode() {
		return new Double(((this.x + this.z) / this.y) - (this.x + this.y + this.z)).intValue();
	}*/
	
}
