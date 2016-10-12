/**
 * 
 */
package com.gmail.br45entei.supercmds.api;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.util.VersionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@SuppressWarnings("javadoc")
public class BlockNMS implements Listener {
	
	// The original classes in reflected variable form, named as they would normally be.
	private static Class<?>				CraftWorld, Block;						//World;
	
	// The methods in the classes (CraftWorld#getHandle, World#setTypeIdAndData)
	private static Method				getHandle, getBlockAt, setTypeIdAndData;
	
	private static volatile BlockNMS	instance;
	
	private static final BlockNMS getInstance() {
		return(instance == null ? (instance = new BlockNMS()) : instance);
	}
	
	public static final void initialize() {
		if(instance != null) {
			throw new Error("Wat u doing.");
		}
		try {
			// Setup the reflection as the plugin enables, this will save time later.
			setupReflection();
			Main.registerEvents(getInstance(), Main.getInstance());
		} catch(NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			HandlerList.unregisterAll(getInstance());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static final void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getPlayer().getName().equals("Brian_Entei")) {
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				Block b = e.getClickedBlock();
				int typeId = 89;
				int data = 0;
				
				try {
					// Simply pass the Bukkit Block, a type ID and data to the method.
					setBlockNatively(b, typeId, data);
				} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	// Stores the reflected classes & methods in variable for access later
	//@SuppressWarnings("deprecation")
	private static final void setupReflection() throws NoSuchMethodException, SecurityException {
		CraftWorld = VersionUtil.getNMSClass("org.bukkit.craftbukkit.%s.CraftWorld");
		Block = VersionUtil.getNMSClass("net.minecraft.server.%s.Block");//World = VersionUtil.getNMSClass("net.minecraft.server.%s.World");
		//org.bukkit.craftbukkit.v1_10_R1.CraftWorld w = (org.bukkit.craftbukkit.v1_10_R1.CraftWorld) new Object();
		//w.getBlockAt(0, 0, 0).setTypeIdAndData(1, (byte) 1, false);
		getHandle = CraftWorld.getDeclaredMethod("getHandle", new Class<?>[] {});
		getBlockAt = CraftWorld.getDeclaredMethod("getBlockAt", int.class, int.class, int.class);
		//net.minecraft.server.v1_10_R1.Block b = (net.minecraft.server.v1_10_R1.Block) new Object();
		setTypeIdAndData = Block.getDeclaredMethod("setTypeIdAndData", int.class, byte.class, boolean.class);//World.getDeclaredMethod("setTypeIdAndData", int.class, int.class, int.class, int.class, int.class, int.class);
	}
	
	public static final boolean setBlock(org.bukkit.block.Block block, int materialId, int data) {
		try {
			return setBlockNatively(block, materialId, data);
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static final boolean setBlock(org.bukkit.block.Block block, Material material, int data) {
		try {
			return setBlockNatively(block, material.getId(), data);
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Mimics the original World#setTypeIdAndData method in the NMS class.
	private static final boolean setBlockNatively(org.bukkit.block.Block b, int typeId, int data) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object craftWorld = getHandle.invoke(CraftWorld.cast(b.getWorld()), new Object[] {});
		Location loc = b.getLocation();
		Object block = getBlockAt.invoke(craftWorld, Integer.valueOf(loc.getBlockX()), Integer.valueOf(loc.getBlockY()), Integer.valueOf(loc.getBlockZ()));
		Object result = setTypeIdAndData.invoke(block, Integer.valueOf(typeId), Byte.valueOf((byte) data), Boolean.FALSE);
		return result == Boolean.TRUE;
	}
	
}
