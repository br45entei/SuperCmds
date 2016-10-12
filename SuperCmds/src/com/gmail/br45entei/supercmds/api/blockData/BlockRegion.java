package com.gmail.br45entei.supercmds.api.blockData;

import com.gmail.br45entei.supercmds.InventoryAPI;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Vector;

/** @author Brian_Entei */
@SuppressWarnings({"javadoc", "unused"})
public class BlockRegion {//TODO TODO TODO todo toooodooooo
	
	private volatile int				xLength;
	private volatile int				yLength;
	private volatile int				zLength;
	
	private volatile BlockData[][][]	data;
	
	public BlockRegion() {
		this(new Vector(0, 0, 0), new Vector(0, 0, 0));
	}
	
	public BlockRegion(Vector start, Vector end) {
		this.setSize(start, end);
	}
	
	public final BlockRegion setSize(Vector start, Vector end) throws NullPointerException, IllegalArgumentException {
		if(start == null || end == null) {
			throw new NullPointerException("Start and end cannot be null!");
		}
		if(start.getBlockX() > end.getBlockX() || start.getBlockY() > end.getBlockY() || start.getBlockZ() > end.getBlockZ()) {
			throw new IllegalArgumentException("Start coordinates must be less than or equal to end coordinates!");
		}
		this.xLength = (end.getBlockX() - start.getBlockX()) + 1;
		this.yLength = (end.getBlockY() - start.getBlockY()) + 1;
		this.zLength = (end.getBlockZ() - start.getBlockZ()) + 1;
		this.clear();
		return this;
	}
	
	public final BlockRegion clear() {
		this.data = new BlockData[this.xLength][][];
		for(int i = 0; i < this.xLength; i++) {
			this.data[i] = new BlockData[this.yLength][];
			for(int j = 0; j < this.yLength; j++) {
				this.data[i][j] = new BlockData[this.zLength];
			}
		}
		return this;
	}
	
	public final void dispose() {
		this.data = null;
		System.gc();
	}
	
	public final BlockRegion storeArea(World world, Location start) {
		this.clear();
		for(int X = 0; X < this.xLength; X++) {
			for(int Y = 0; Y < this.yLength; Y++) {
				for(int Z = 0; Z < this.zLength; Z++) {
					Location loc = new Location(world, X, Y, Z);
					Block block = world.getBlockAt(loc);
					this.data[X][Y][Z] = getBlockDataFor(block);
				}
			}
		}
		return this;
	}
	
	public final BlockRegion retrieveArea(World world, Location start) {
		//TODO
		return this;
	}
	
	protected static final class BlockData {
		public volatile int		id;
		public volatile byte	data;
		public volatile String	serializedInv;
		
	}
	
	@SuppressWarnings("deprecation")
	private static final BlockData getBlockDataFor(Block block) {
		BlockData rtrn = new BlockData();
		if(block != null && block.getType() != Material.AIR) {
			rtrn.id = block.getTypeId();
			rtrn.data = block.getData();
			if(block.getState() instanceof InventoryHolder) {
				rtrn.serializedInv = InventoryAPI.serializeInventory(((InventoryHolder) block.getState()).getInventory());
			}
		}
		return rtrn;
	}
	
}
