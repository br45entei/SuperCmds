package com.gmail.br45entei.supercmds.util;

import com.gmail.br45entei.supercmds.Main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class CodeUtils {
	
	public static final double getDistanceBetween(Location loc1, Location loc2) {
		return getDistanceBetween(loc1.toVector(), loc2.toVector());
	}
	
	public static final double getDistanceBetween(Vector loc1, Vector loc2) {
		return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2));
	}
	
	private static final BlockFace[]	cobbleGenerationFaces	= new BlockFace[] {		//
			BlockFace.SELF,																//
			BlockFace.UP,																//
			BlockFace.DOWN,																//
			BlockFace.NORTH,															//
			BlockFace.EAST,																//
			BlockFace.SOUTH,															//
			BlockFace.WEST};
	
	private static final Material[]		oreBlocks				= new Material[] {		//
			Material.COAL_ORE,															//
			Material.IRON_ORE,															//
			Material.REDSTONE_ORE,														//
			Material.LAPIS_ORE,															//
			Material.GOLD_ORE,															//
			Material.DIAMOND_ORE,														//
			Material.EMERALD_ORE,														//
			Material.QUARTZ_ORE};
	
	public static final ItemStack[] getMaterialsFromCobbleGenToTestWith(Material m) {
		ItemStack[] rtrn = new ItemStack[] {new ItemStack(m)};
		if(m == Material.COBBLESTONE) {
			return rtrn;
		}
		if(m == Material.COAL_ORE) {
			return new ItemStack[] {new ItemStack(Material.COAL_BLOCK), new ItemStack(Material.COAL)};
		}
		if(m == Material.IRON_ORE) {
			return new ItemStack[] {new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.IRON_INGOT)};
		}
		if(m == Material.REDSTONE_ORE) {
			return new ItemStack[] {new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.REDSTONE)};
		}
		if(m == Material.LAPIS_ORE) {
			return new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK), new ItemStack(Material.INK_SACK, (short) 1, (byte) 4)};
		}
		if(m == Material.GOLD_ORE) {
			return new ItemStack[] {new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLD_INGOT)};
		}
		if(m == Material.DIAMOND_ORE) {
			return new ItemStack[] {new ItemStack(Material.DIAMOND_BLOCK), new ItemStack(Material.DIAMOND)};
		}
		if(m == Material.EMERALD_ORE) {
			return new ItemStack[] {new ItemStack(Material.EMERALD_BLOCK), new ItemStack(Material.EMERALD)};
		}
		if(m == Material.QUARTZ_ORE) {
			return new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK), new ItemStack(Material.QUARTZ_STAIRS), new ItemStack(Material.QUARTZ)};
		}
		return rtrn;
	}
	
	public static final boolean generatesCobble(Material material, Block toBlock) {
		Material mat1 = (material == Material.WATER || material == Material.STATIONARY_WATER ? Material.LAVA : Material.WATER);
		Material mat2 = (material == Material.WATER || material == Material.STATIONARY_WATER ? Material.STATIONARY_LAVA : Material.STATIONARY_WATER);
		for(BlockFace face : cobbleGenerationFaces) {
			Block r = toBlock.getRelative(face, 1);
			if(r.getType() == mat1 || r.getType() == mat2) {
				return true;
			}
		}
		return false;
	}
	
	public static final Material getARandomOre(boolean higherOresAreRarer, boolean includeNetherOre) {
		int random = Main.getRandomIntValBetween(0, oreBlocks.length - (includeNetherOre ? 0 : 1));
		Material rtrn = oreBlocks[random];
		if(!higherOresAreRarer) {
			return rtrn;
		}
		if(rtrn == Material.COAL_ORE) {
			return rtrn;
		} else if(rtrn == Material.IRON_ORE) {
			random = Main.getRandomIntValBetween(0, 8);
			if(random >= 4) {//50% chance fail/succeed
				rtrn = Material.COAL_ORE;
			}
		} else if(rtrn == Material.REDSTONE_ORE) {
			random = Main.getRandomIntValBetween(0, 10);
			if(random >= 4) {//40% chance succeed
				rtrn = Material.IRON_ORE;
			}
		} else if(rtrn == Material.LAPIS_ORE) {
			random = Main.getRandomIntValBetween(0, 8);
			if(random >= 3) {//37.5% chance succeed
				rtrn = Material.COAL_ORE;
			}
		} else if(rtrn == Material.GOLD_ORE) {
			random = Main.getRandomIntValBetween(0, 9);
			if(random >= 3) {//33.33% chance succeed
				rtrn = Material.COBBLESTONE;
			}
		} else if(rtrn == Material.DIAMOND_ORE) {
			random = Main.getRandomIntValBetween(0, 12);
			if(random >= 3) {//25% chance succeed
				rtrn = Material.REDSTONE_ORE;
			}
		} else if(rtrn == Material.EMERALD_ORE) {
			random = Main.getRandomIntValBetween(0, 12);
			if(random >= 2) {//16.66% chance succeed
				rtrn = Material.COBBLESTONE;
			}
		} else if(rtrn == Material.QUARTZ_ORE) {
			random = Main.getRandomIntValBetween(0, 12);
			if(random >= 1) {//8.33% chance succeed
				rtrn = random >= 2 ? Material.COBBLESTONE : Material.EMERALD_ORE;
			}
		}
		return rtrn;
	}
	
	public static final boolean isStrLaxBoolean(String str) {
		return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false") || str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("no") || str.equals("1") || str.equals("0");
	}
	
	public static final Boolean getLaxBoolean(String str) {
		if(!isStrLaxBoolean(str)) {
			return null;
		}
		if(str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") || str.equals("1")) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	public static String limitStringToNumOfChars(String str, int limit) {
		return(str != null ? (str.length() >= 1 ? (str.substring(0, (str.length() >= limit ? limit : str.length()))) : "") : "");
	}
	
	public static String[] removeEmptyStrings(String[] data) {
		ArrayList<String> result = new ArrayList<>();
		for(int i = 0; i < data.length; i++) {
			if(!data[i].equals("")) {
				result.add(data[i]);
			}
		}
		String[] res = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	public static boolean isStrAValidDouble(String s) {
		boolean successful = true;
		try {
			Double.parseDouble(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidFloat(String s) {
		boolean successful = true;
		try {
			Float.parseFloat(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidLong(String s) {
		boolean successful = true;
		try {
			Long.parseLong(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidInt(String s) {
		boolean successful = true;
		try {
			Integer.parseInt(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidShort(String s) {
		boolean successful = true;
		try {
			Short.parseShort(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidByte(String s) {
		boolean successful = true;
		try {
			Byte.parseByte(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static double getDoubleFromStr(String s, double fallBackValue) {
		return CodeUtils.isStrAValidDouble(s) ? Double.parseDouble(s) : fallBackValue;
	}
	
	public static float getFloatFromStr(String s, float fallBackValue) {
		return CodeUtils.isStrAValidFloat(s) ? Float.parseFloat(s) : fallBackValue;
	}
	
	public static long getLongFromStr(String s, long fallBackValue) {
		return CodeUtils.isStrAValidLong(s) ? Long.parseLong(s) : fallBackValue;
	}
	
	public static int getIntFromStr(String s, int fallBackValue) {
		return CodeUtils.isStrAValidInt(s) ? Integer.parseInt(s) : fallBackValue;
	}
	
	public static short getShortFromStr(String s, short fallBackValue) {
		return CodeUtils.isStrAValidShort(s) ? Short.parseShort(s) : fallBackValue;
	}
	
	public static int[] toIntArray(Integer[] data) {
		int[] result = new int[data.length];
		for(int i = 0; i < data.length; i++) {
			result[i] = data[i].intValue();
		}
		return result;
	}
	
	//======================================================
	
	public static final void launchFirework(Player p, int speedMultiplier) {
		launchFirework(p, speedMultiplier, null);
	}
	
	public static final void launchFirework(Player p, int speedMultiplier, FireworkEffect effect) {
		if(p != null) {
			if(effect == null) {
				effect = FireworkEffect.builder().trail(true).withColor(Color.RED).withColor(Color.GREEN).withFade(new Color[] {Color.PURPLE, Color.ORANGE, Color.TEAL}).build();
			}
			Firework fw = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
			FireworkMeta meta = fw.getFireworkMeta();
			meta.addEffect(effect);
			fw.setVelocity(p.getLocation().getDirection().multiply(speedMultiplier));
		}
	}
	
	//======================================================
	
	/** @param getTimeOnly Whether or not time should be included but not date
	 *            as
	 *            well
	 * @param fileSystemSafe Whether or not the returned string will be used in
	 *            the making of a folder or file
	 * @return The resulting string */
	public static String getSystemTime(boolean getTimeOnly, boolean fileSystemSafe, boolean getDateOnly) {
		String timeAndDate = "";
		DateFormat dateFormat;
		if(!getTimeOnly && !getDateOnly) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "MM-dd-yyyy_HH.mm.ss" : "MM/dd/yyyy_HH:mm:ss");
		} else if(getTimeOnly) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "HH.mm.ss" : "HH:mm:ss");
		} else if(getDateOnly) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "MM-dd-yyyy" : "MM/dd/yyyy");
		} else {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "HH.mm.ss" : "HH:mm:ss");
		}
		Date date = new Date();
		timeAndDate = dateFormat.format(date);
		return timeAndDate;
	}
	
	public static boolean isJvm64bit() {
		String[] astring = new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};
		String[] astring1 = astring;
		int i = astring.length;
		for(int j = 0; j < i; ++j) {
			String s = astring1[j];
			String s1 = System.getProperty(s);
			if(s1 != null && s1.contains("64")) {
				return true;
			}
		}
		return false;
	}
	
	public static EnumOS getOSType() {
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains("win") ? EnumOS.WINDOWS : (s.contains("mac") ? EnumOS.OSX : (s.contains("solaris") ? EnumOS.SOLARIS : (s.contains("sunos") ? EnumOS.SOLARIS : (s.contains("linux") ? EnumOS.LINUX : (s.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN)))));
	}
	
	/** Causes the currently executing thread to sleep (temporarily cease
	 * execution) for the specified number of milliseconds, subject to the
	 * precision and accuracy of system timers and schedulers. The thread does
	 * not lose ownership of any monitors.
	 * 
	 * @param millis The length of time to sleep in milliseconds */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Throwable ignored) {
		}
	}
	
	/** @param runRate The rate that the current thread is running at
	 * @param sleep25 Forces this method to use 25 percent of the run rate */
	public static void threadSleep(int runRate, boolean sleep25) {
		if(sleep25) {
			CodeUtils.sleep((long) (runRate * 0.25D));
		} else {
			CodeUtils.sleep(runRate);
		}
	}
	
	public static enum EnumOS {
		LINUX,
		SOLARIS,
		WINDOWS,
		OSX,
		UNKNOWN;
	}
	
}
