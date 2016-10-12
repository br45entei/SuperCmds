package com.gmail.br45entei.supercmds.cmds;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.util.Vector3;
import com.gmail.br45entei.util.StringUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

@SuppressWarnings("javadoc")
public final class BlockSetData {
	
	public static final ConcurrentLinkedQueue<BlockSetData> blockSetQueue = new ConcurrentLinkedQueue<>();
	
	public static final void initialize() {
		Main.scheduler.scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				BlockSetData data = blockSetQueue.peek();
				if(data != null) {
					data.execute();
				}
			}
		}, 100L, 2L);// 5 ticks = 1/4 of a second; 2 ticks = 0.1(1/10th) of a second, etc. 20 ticks = 1 second
	}
	
	private volatile boolean	isCancelled	= false;
	
	public final long			startTime	= System.currentTimeMillis();
	
	public volatile int			blocksChanged;
	private volatile String		timeElapsed	= "<Not Finished>";
	
	public final Vector3		start;
	
	public final Vector3		end;
	
	public final World			world;
	
	public final BlockSetType	type;
	
	public final Material		material;
	public final Material		replaceMaterial;
	public final byte			data;
	public final byte			replaceData;
	public final CommandSender	sender;
	protected final Vector3		nextEditLocation;
	
	public BlockSetData(BlockSetType type, int x1, int y1, int z1, int x2, int y2, int z2, World world, Material material, Material replaceMaterial, byte data, byte replaceData, CommandSender sender) {
		this(type, new Vector3(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)), new Vector3(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)), world, material, replaceMaterial, data, replaceData, sender);
	}
	
	public BlockSetData(BlockSetType type, Vector3 start, Vector3 end, World world, Material material, Material replaceMaterial, byte data, byte replaceData, CommandSender sender) {
		this.type = type;
		this.start = start;
		this.end = end;
		this.world = world;
		this.material = material;
		this.replaceMaterial = replaceMaterial;
		this.data = data;
		this.replaceData = replaceData;
		this.sender = sender;
		this.nextEditLocation = new Vector3(this.start);
	}
	
	public final BlockSetType getType() {
		return this.type;
	}
	
	public final String markFinished() {
		this.nextEditLocation.x = (this.end.x + 1);
		this.nextEditLocation.y = (this.end.y + 1);
		this.nextEditLocation.z = (this.end.z + 1);
		this.timeElapsed = StringUtil.getElapsedTime(System.currentTimeMillis() - this.startTime, true);
		return this.timeElapsed;
	}
	
	public final BlockSetData cancel() {
		this.isCancelled = true;
		return this;
	}
	
	public final boolean isCancelled() {
		return this.isCancelled;
	}
	
	public final String getTimeElapsed() {
		return this.timeElapsed.equals("<Not Finished>") ? "*" + StringUtil.getElapsedTime(System.currentTimeMillis() - this.startTime, true) : this.timeElapsed;
	}
	
	public final boolean isFinished() {
		return this.nextEditLocation.equals(new Vector3(this.end.x + 1, this.end.y + 1, this.end.z + 1));
	}
	
	public final boolean isXFinished() {
		return (this.start.x == this.end.x) || (this.nextEditLocation.x >= this.end.x + 1);
	}
	
	public final boolean isYFinished() {
		return (this.start.y == this.end.y) || (this.nextEditLocation.y >= this.end.y + 1);
	}
	
	public final boolean isZFinished() {
		return (this.start.z == this.end.z) || (this.nextEditLocation.z >= this.end.z + 1);
	}
	
	@SuppressWarnings("deprecation")
	public final void execute() {
		if(this.isCancelled()) {
			blockSetQueue.remove(this);//blockSetQueue.poll();
			Main.sendMessage(this.sender, Main.pluginName + "&eOperation cancelled. Time elapsed: &f" + this.getTimeElapsed() + ".&z&6" + this.blocksChanged + "&e blocks changed.");
			return;
		}
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime < 100L) && (!this.isFinished())) {
			if(this.isCancelled()) {
				blockSetQueue.remove(this);//blockSetQueue.poll();
				Main.sendMessage(this.sender, Main.pluginName + "&eOperation cancelled. Time elapsed: &f" + this.getTimeElapsed() + ".&z&6" + this.blocksChanged + "&e blocks changed.");
				return;
			}
			
			if(this.type == BlockSetType.SET) {
				if(this.world.getBlockAt(this.nextEditLocation.x, this.nextEditLocation.y, this.nextEditLocation.z).setTypeIdAndData(this.material.getId(), this.data, false)) {
					this.blocksChanged += 1;
				}
			} else if(this.type == BlockSetType.REPLACE) {
				Block block = this.world.getBlockAt(this.nextEditLocation.x, this.nextEditLocation.y, this.nextEditLocation.z);
				if(block.getType() == this.material && block.getData() == this.data) {
					if(block.setTypeIdAndData(this.replaceMaterial.getId(), this.replaceData, false)) {
						this.blocksChanged += 1;
					}
				}
			}
			this.nextEditLocation.z += 1;
			if(this.isZFinished()) {
				this.nextEditLocation.z = this.start.z;
				this.nextEditLocation.y += 1;
				if(this.isYFinished()) {
					this.nextEditLocation.y = this.start.y;
					this.nextEditLocation.x += 1;
					if(this.isXFinished()) {
						this.markFinished();
						break;
					}
				}
			}
		}
		if(this.isFinished()) {
			blockSetQueue.remove(this);//blockSetQueue.poll();
			Main.sendMessage(this.sender, Main.pluginName + "&aOperation complete. Time elapsed: &f" + this.getTimeElapsed() + ".&z&6" + this.blocksChanged + "&a blocks changed.");
		} else {
			Main.sendMessage(this.sender, Main.pluginName + "&6" + this.blocksChanged + "&a blocks changed. Time elapsed: &f" + this.getTimeElapsed() + ".&z&e" + this.nextEditLocation.toString() + " / " + this.end.toString());
		}
	}
	
	public static enum BlockSetType {
		SET,
		REPLACE
	}
	
}
