package com.gmail.br45entei.supercmds;

import com.gmail.br45entei.supercmds.cmds.MainCmdListener;
import com.gmail.br45entei.supercmds.util.CodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.material.Bed;
import org.bukkit.material.Cake;
import org.bukkit.material.Cauldron;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.DetectorRail;
import org.bukkit.material.Diode;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.FlowerPot;
import org.bukkit.material.Leaves;
import org.bukkit.material.Lever;
import org.bukkit.material.LongGrass;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Mushroom;
import org.bukkit.material.NetherWarts;
import org.bukkit.material.Openable;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.material.PoweredRail;
import org.bukkit.material.Rails;
import org.bukkit.material.Sandstone;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.material.TrapDoor;
import org.bukkit.material.Tree;
import org.bukkit.material.Tripwire;
import org.bukkit.material.TripwireHook;
import org.bukkit.material.Vine;
import org.bukkit.material.WoodenStep;
import org.bukkit.material.Wool;

/** @author Brian_Entei */
@SuppressWarnings({"javadoc", "unused", "deprecation"})
public class BlockAPI {
	/** This is the tag that is used to start the block listings when
	 * serializing. */
	private static final String	block_start			= "#";
	/** This is the tag that is used at the end of every serialized Block. */
	private static final String	block_separator		= ";";
	/** This is the tag that is used at the start of every block attribute(like
	 * an
	 * enchantment, block damage, block lore, the book title, the block's name,
	 * the
	 * book author, a book page, etc.) */
	private static final String	attribute_start		= ":";
	/** This is the tag that is used to separate every block attribute(like a
	 * chest's inv, a sign's text, etc.) */
	private static final String	attribute_separator	= "@";
	
	/** @param block The block to serialize
	 * @return The serialized block String, or an empty string if the block was
	 *         null or its type was somehow not a block type. */
	public static final String serializeBlock(Block block) {
		String rtrn = "";
		if(block != null && block.getType().isBlock() && block.getType() != Material.AIR) {
			String blockSpecial = serializeBlockSpecial(block);
			String location = "[x=" + block.getX() + //
					",y=" + block.getY() + //
					",z=" + block.getZ() + "]";
			String materialName = "m" + attribute_separator + block.getType().name();
			String damageValue = attribute_start + "d" + attribute_separator + block.getData();
			rtrn = location + block_start + materialName + damageValue + blockSpecial + block_separator;
			/*for(Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 0.5, 0.5, 0.5)) {
				if(!entity.isValid()) {
					continue;
				}
				Location loc = entity.getLocation();
				location = "[x=" + loc.getX() + //
				",y=" + loc.getY() + //
				",z=" + loc.getZ() + //
				",yaw=" + loc.getYaw() + //
				",pitch=" + loc.getPitch() + "]";
				String type = attribute_start + "type" + attribute_separator + entity.getType().name();
				String nameVisible = attribute_start + "nameVisible" + attribute_separator + entity.isCustomNameVisible();
				String name = entity.isCustomNameVisible() ? attribute_start + "name" + attribute_separator + convertSymbolsForSaving(entity.getCustomName()) : "";
				String entitySpecial = "";
				if(entity instanceof LivingEntity) {
					LivingEntity l = (LivingEntity) entity;
					l.getActivePotionEffects();
					l.getCanPickupItems();
					l.getEffectivePermissions();
					l.getFallDistance();
					l.getFireTicks();
					l.getMaxHealth();
					l.getHealth();
					l.getLastDamage();
					l.getMaxFireTicks();
					l.getMaximumAir();
					l.getMaximumNoDamageTicks();
					l.getNoDamageTicks();
					l.getRemainingAir();
					l.getRemoveWhenFarAway();
					l.getTicksLived();
					l.getVelocity();
					EntityEquipment equip = l.getEquipment();
					equip.getArmorContents();
					equip.getBootsDropChance();
					equip.getChestplateDropChance();
					equip.getHelmetDropChance();
					equip.getItemInHand();
					equip.getItemInHandDropChance();
					equip.getLeggingsDropChance();
					if(entity instanceof ArmorStand) {
						ArmorStand a = (ArmorStand) entity;
						a.getBodyPose();
						a.getBoots();
						a.getChestplate();
						a.getHeadPose();
						a.getHelmet();
						a.getItemInHand();
						a.getLeftArmPose();
						a.getLeftLegPose();
						a.getLeggings();
						a.getRightArmPose();
						a.getRightLegPose();
						a.hasArms();
						a.hasBasePlate();
						a.hasGravity();
						a.isMarker();
						a.isSmall();
						a.isVisible();
					}
				}
				rtrn += "\n" + location + type + nameVisible + name + entitySpecial;
			}*/
		}
		return rtrn;
	}
	
	private static final String serializeBlockSpecial(Block block) {
		String serializedBlock = "";
		final BlockState s = block.getState();
		final MaterialData m = s.getData();
		//BlockState data:
		if(s instanceof Banner) {
			Banner banner = (Banner) s;
			serializedBlock = attribute_start + "banner" + attribute_separator + "BASECOLOR=" + banner.getBaseColor().name();
			String patterns = attribute_start + "banner" + attribute_separator;
			boolean addedAny = false;
			for(Pattern p : banner.getPatterns()) {
				patterns += "PATTERN[";
				for(Entry<String, Object> entry : p.serialize().entrySet()) {
					patterns += convertSymbolsForSaving(entry.getKey() + "|" + entry.getValue().toString() + ",");
				}
				if(patterns.endsWith(",")) {
					patterns = patterns.substring(0, patterns.length() - 1);
				}
				patterns += "]";
				addedAny = true;
			}
			if(addedAny) {
				serializedBlock += patterns;
			}
		} else if(s instanceof Beacon) {
			Beacon beacon = (Beacon) s;
			serializedBlock = attribute_start + "beacon" + attribute_separator;
			serializedBlock += "INVENTORY[" + convertSymbolsForSaving(InventoryAPI.serializeInventory(beacon.getInventory())) + "]";
		} else if(s instanceof BrewingStand) {
			BrewingStand brew = (BrewingStand) s;
			serializedBlock = attribute_start + "brewingStand" + attribute_separator;
			serializedBlock += "BREWTIME[" + brew.getBrewingTime() + "]";
			serializedBlock += "BREWINVENTORY[" + convertSymbolsForSaving(InventoryAPI.serializeInventory(brew.getInventory())) + "]";
		} else if(s instanceof Chest) {
			Chest chest = (Chest) s;
			serializedBlock = attribute_start + "chest" + attribute_separator;
			serializedBlock += "CHESTINVENTORY[" + convertSymbolsForSaving(InventoryAPI.serializeInventory(chest.getInventory())) + "]";
		} else if(s instanceof CommandBlock) {
			CommandBlock cmd = (CommandBlock) s;
			serializedBlock = attribute_start + "cmdBlock" + attribute_separator;
			serializedBlock += "NAME[" + cmd.getName() + "]";
			serializedBlock += "COMMAND[" + cmd.getCommand() + "]";
		} else if(s instanceof CreatureSpawner) {
			CreatureSpawner spawner = (CreatureSpawner) s;
			serializedBlock = attribute_start + "creaturespawner" + attribute_separator;
			EntityType type = spawner.getSpawnedType();
			int spawnDelay = spawner.getDelay();
			serializedBlock += "TYPE[" + type.name() + "]DELAY[" + spawnDelay + "]";
		} else if(s instanceof Dispenser) {
			Dispenser dispenser = (Dispenser) s;
			serializedBlock = attribute_start + "dispenser" + attribute_separator;
			serializedBlock += "DISPENSERINVENTORY[" + convertSymbolsForSaving(InventoryAPI.serializeInventory(dispenser.getInventory())) + "]";
		} else if(s instanceof Dropper) {
			Dropper dropper = (Dropper) s;
			serializedBlock = attribute_start + "dropper" + attribute_separator;
			serializedBlock += "DROPPERINVENTORY[" + convertSymbolsForSaving(InventoryAPI.serializeInventory(dropper.getInventory())) + "]";
		} else if(s instanceof Furnace) {
			Furnace furnace = (Furnace) s;
			serializedBlock = attribute_start + "furnace" + attribute_separator;
			serializedBlock += "COOKTIME[" + furnace.getCookTime() + "]";
			serializedBlock += "BURNTIME[" + furnace.getBurnTime() + "]";
			serializedBlock += "FURNACEINVENTORY[" + convertSymbolsForSaving(InventoryAPI.serializeInventory(furnace.getInventory())) + "]";
		} else if(s instanceof Hopper) {
			Hopper hopper = (Hopper) s;
			serializedBlock = attribute_start + "hopper" + attribute_separator;
			serializedBlock += "HOPPERINVENTORY[" + convertSymbolsForSaving(InventoryAPI.serializeInventory(hopper.getInventory())) + "]";
		} else if(s instanceof Jukebox) {
			Jukebox jukebox = (Jukebox) s;
			serializedBlock = attribute_start + "jukebox" + attribute_separator;
			serializedBlock += "RECORDPLAYING[" + jukebox.getPlaying().name() + "]";
		} else if(s instanceof NoteBlock) {
			NoteBlock noteblock = (NoteBlock) s;
			serializedBlock = attribute_start + "noteBlock" + attribute_separator;
			Note note = noteblock.getNote();
			int octave = note.getOctave();
			byte tone = note.getTone().getId();
			boolean sharp = note.isSharped();
			serializedBlock += "OCTAVE[" + octave + "]TONE[" + tone + "]SHARP[" + sharp + "]";
		} else if(s instanceof Sign) {
			serializedBlock = attribute_start + "sign" + attribute_separator;
			Sign sign = (Sign) s;
			for(int i = 0; i < sign.getLines().length; i++) {
				serializedBlock += "LINE" + i + "[" + convertSymbolsForSaving(sign.getLine(i)) + "]";
			}
		} else if(s instanceof Skull) {
			Skull skull = (Skull) s;
			serializedBlock = attribute_start + "skull" + attribute_separator;
			serializedBlock += "ROTATION[" + skull.getRotation().name() + "]";
			serializedBlock += "TYPE[" + skull.getSkullType() + "]";
			if(skull.getSkullType() == SkullType.PLAYER) {
				serializedBlock += "OWNER[" + skull.getOwner() + "]";
			}
		}
		return serializedBlock + serializeMaterialData(m);
	}
	
	private static final String serializeMaterialData(MaterialData m) {
		String serializedBlock = "";
		//MaterialData data:
		if(m instanceof Directional) {
			Directional d = (Directional) m;
			serializedBlock += attribute_start + "FACING" + attribute_separator + d.getFacing();
		}
		if(m instanceof Bed) {
			Bed bed = (Bed) m;
			serializedBlock += attribute_start + "ISHEADOFBED" + attribute_separator + bed.isHeadOfBed();
		}
		if(m instanceof Cake) {
			Cake cake = (Cake) m;
			serializedBlock += attribute_start + "CAKESLICESEATEN" + attribute_separator + cake.getSlicesEaten();
			serializedBlock += attribute_start + "CAKESLICESREMAINING" + attribute_separator + cake.getSlicesRemaining();
		}
		if(m instanceof Cauldron) {
			Cauldron cauldron = (Cauldron) m;
			byte waterLevel = cauldron.getData();
			serializedBlock += attribute_start + "CAULDRONWATERLEVEL" + attribute_separator + waterLevel;
		}
		if(m instanceof CocoaPlant) {
			CocoaPlant cocoa = (CocoaPlant) m;
			serializedBlock += attribute_start + "COCOASIZE" + attribute_separator + cocoa.getSize().name();
		}
		if(m instanceof Crops) {
			Crops crop = (Crops) m;
			serializedBlock += attribute_start + "CROPSIZE" + attribute_separator + crop.getState().name();
		}
		if(m instanceof Rails) {
			Rails rail = (Rails) m;
			serializedBlock += attribute_start + "RAILDIRECTION" + attribute_separator + rail.getDirection();
			serializedBlock += attribute_start + "RAILSLOPED" + attribute_separator + rail.isOnSlope();
			if(m instanceof DetectorRail) {
				DetectorRail r = (DetectorRail) m;
				serializedBlock += attribute_start + "RAILPRESSED" + attribute_separator + r.isPressed();//r.setPressed(boolean)
			}
		}
		if(m instanceof Diode) {
			Diode diode = (Diode) m;
			serializedBlock += attribute_start + "DIODEDELAY" + attribute_separator + diode.getDelay();
		}
		if(m instanceof Openable) {
			Openable openable = (Openable) m;
			serializedBlock += attribute_start + "ISOPEN" + attribute_separator + openable.isOpen();
		}
		if(m instanceof Door) {
			Door door = (Door) m;
			serializedBlock += attribute_start + "DOORISRIGHTHINGE" + attribute_separator + door.getHinge();
			serializedBlock += attribute_start + "DOORISTOPHALF" + attribute_separator + door.isTopHalf();
		}
		if(m instanceof FlowerPot) {
			FlowerPot flowerpot = (FlowerPot) m;
			serializedBlock += attribute_start + "FLOWERPOTCONTENTS" + attribute_separator + convertSymbolsForSaving(serializeMaterialData(flowerpot.getContents()));
		}
		if(m instanceof Leaves) {
			Leaves leaf = (Leaves) m;
			serializedBlock += attribute_start + "LEAFSPECIES" + attribute_separator + leaf.getSpecies().name();
		}
		if(m instanceof Lever) {
			Lever lever = (Lever) m;
			serializedBlock += attribute_start + "LEVERPOWERED" + attribute_separator + lever.isPowered();
		}
		if(m instanceof LongGrass) {
			LongGrass grass = (LongGrass) m;
			serializedBlock += attribute_start + "GRASSSPECIES" + attribute_separator + grass.getSpecies().name();
		}
		if(m instanceof Mushroom) {
			Mushroom marshmallow = (Mushroom) m;// XDDDD
			serializedBlock += attribute_start + "MUSHROOMISSTEM" + attribute_separator + marshmallow.isStem();
			String faces = "";
			Iterator<BlockFace> it = marshmallow.getPaintedFaces().iterator();
			while(it.hasNext()) {
				BlockFace face = it.next();
				faces += face.name() + (it.hasNext() ? "," : "");
			}
			serializedBlock += attribute_start + "MUSHROOMPAINTEDFACES" + attribute_separator + faces;
		}
		if(m instanceof NetherWarts) {
			NetherWarts warts = (NetherWarts) m;
			serializedBlock += attribute_start + "NETHERWARTSSIZE" + attribute_separator + warts.getState().name();
		}
		if(m instanceof PistonBaseMaterial) {
			PistonBaseMaterial base = (PistonBaseMaterial) m;
			serializedBlock += attribute_start + "PISTONBASEPOWERED" + attribute_separator + base.isPowered();
		}
		if(m instanceof PistonExtensionMaterial) {
			PistonExtensionMaterial ext = (PistonExtensionMaterial) m;
			serializedBlock += attribute_start + "PISTONARMSTICKY" + attribute_separator + ext.isSticky();
		}
		if(m instanceof PoweredRail) {
			PoweredRail rail = (PoweredRail) m;
			serializedBlock += attribute_start + "RAILPOWERED" + attribute_separator + rail.isPowered();
		}
		if(m instanceof Sandstone) {
			Sandstone sandstone = (Sandstone) m;
			serializedBlock += attribute_start + "SANDSTONETYPE" + attribute_separator + sandstone.getType().name();
		}
		if(m instanceof Stairs) {
			Stairs stairs = (Stairs) m;
			serializedBlock += attribute_start + "STAIRSINVERTED" + attribute_separator + stairs.isInverted();
		}
		if(m instanceof Step) {
			Step step = (Step) m;
			serializedBlock += attribute_start + "STEPINVERTED" + attribute_separator + step.isInverted();
		}
		if(m instanceof TrapDoor) {
			TrapDoor trap = (TrapDoor) m;
			serializedBlock += attribute_start + "TRAPDOORINVERTED" + attribute_separator + trap.isInverted();
		}
		if(m instanceof Tree && m.getItemType() != Material.SAPLING) {
			Tree tree = (Tree) m;
			serializedBlock += attribute_start + "TREELOGDIRECTION" + attribute_separator + tree.getDirection().name();
			serializedBlock += attribute_start + "TREELOGSPECIES" + attribute_separator + tree.getSpecies();
		}
		if(m instanceof Tripwire) {
			Tripwire wire = (Tripwire) m;
			serializedBlock += attribute_start + "TRIPWIREACTIVATED" + attribute_separator + wire.isActivated();
			serializedBlock += attribute_start + "TRIPWIREOBJECTTRIGGERING" + attribute_separator + wire.isObjectTriggering();
		}
		if(m instanceof TripwireHook) {
			TripwireHook hook = (TripwireHook) m;
			serializedBlock += attribute_start + "TRIPWIREHOOKACTIVATED" + attribute_separator + hook.isActivated();
			serializedBlock += attribute_start + "TRIPWIREHOOKCONNECTED" + attribute_separator + hook.isConnected();
		}
		if(m instanceof Vine) {
			Vine vine = (Vine) m;
			String faces = "";
			Iterator<BlockFace> it = getVineFacings(vine).iterator();
			while(it.hasNext()) {
				BlockFace face = it.next();
				faces += face.name() + (it.hasNext() ? "," : "");
			}
			serializedBlock += attribute_start + "VINEFACINGS" + attribute_separator + faces;
		}
		if(m instanceof WoodenStep) {
			WoodenStep step = (WoodenStep) m;
			serializedBlock += attribute_start + "WOODENSTEPSPECIES" + attribute_separator + step.getSpecies();
			serializedBlock += attribute_start + "WOODENSTEPINVERTED" + attribute_separator + step.isInverted();
		}
		if(m instanceof Wool) {
			Wool wool = (Wool) m;
			serializedBlock += attribute_start + "WOOLCOLOR" + attribute_separator + wool.getColor().name();
		}
		return serializedBlock;
	}
	
	private static final List<BlockFace> getVineFacings(Vine vine) {
		List<BlockFace> list = new ArrayList<>();
		if(vine.isOnFace(BlockFace.WEST)) {
			list.add(BlockFace.WEST);
		}
		if(vine.isOnFace(BlockFace.NORTH)) {
			list.add(BlockFace.NORTH);
		}
		if(vine.isOnFace(BlockFace.SOUTH)) {
			list.add(BlockFace.SOUTH);
		}
		if(vine.isOnFace(BlockFace.EAST)) {
			list.add(BlockFace.EAST);
		}
		if(vine.isOnFace(BlockFace.NORTH_EAST)) {
			list.add(BlockFace.NORTH_EAST);
		}
		if(vine.isOnFace(BlockFace.NORTH_WEST)) {
			list.add(BlockFace.NORTH_WEST);
		}
		if(vine.isOnFace(BlockFace.SOUTH_EAST)) {
			list.add(BlockFace.SOUTH_EAST);
		}
		if(vine.isOnFace(BlockFace.SOUTH_WEST)) {
			list.add(BlockFace.SOUTH_WEST);
		}
		return list;
	}
	
	private static final void setVineFacings(Vine vine, List<BlockFace> facings) {
		vine.removeFromFace(BlockFace.WEST);
		vine.removeFromFace(BlockFace.NORTH);
		vine.removeFromFace(BlockFace.SOUTH);
		vine.removeFromFace(BlockFace.EAST);
		vine.removeFromFace(BlockFace.NORTH_EAST);
		vine.removeFromFace(BlockFace.NORTH_WEST);
		vine.removeFromFace(BlockFace.SOUTH_EAST);
		vine.removeFromFace(BlockFace.SOUTH_WEST);
		for(BlockFace face : facings) {
			vine.putOnFace(face);
		}
	}
	
	private static final void setDataForBlockAndUpdate(BlockState state) {
		setDataForBlockAndUpdate(state.getBlock(), state, state.getData());
	}
	
	private static final void setDataForBlockAndUpdate(Block block, BlockState state, MaterialData data) {
		block.setType(data.getItemType());
		state.update(true, false);
		state.setData(data);
		state.update(true, false);
	}
	
	/** Converts a set of symbols that are used within the serializing functions
	 * to prevent data corruption, as well as any ChatColor characters(it
	 * changes them to their respective '&' codes).
	 * 
	 * @param str String
	 * @return The given string, with certain symbols changed to keep inventory
	 *         formatting from getting broken because of a simple character.
	 * @see BlockAPI#convertSymbolsForLoading(String) */
	public static String convertSymbolsForSaving(String str) {
		str = str.replaceAll(BlockAPI.block_separator, "<block_separator>").replaceAll(BlockAPI.block_start, "<block_start>").replaceAll(BlockAPI.attribute_start, "<attribute_start>").replaceAll(BlockAPI.attribute_separator, "<attribute_separator>").replaceAll("\n", "<Nline>").replaceAll("\r", "<Rline>").replaceAll("(?i)\u00A7b", "&b").replaceAll("\u00A70", "&0").replaceAll("\u00A79", "&9").replaceAll("(?i)\u00A7l", "&l").replaceAll("\u00A73", "&3").replaceAll("\u00A71", "&1").replaceAll("\u00A78", "&8").replaceAll("\u00A72", "&2").replaceAll("\u00A75", "&5").replaceAll("\u00A74", "&4").replaceAll("\u00A76", "&6").replaceAll("\u00A77", "&7").replaceAll("(?i)\u00A7a", "&a").replaceAll("(?i)\u00A7o", "&o").replaceAll("(?i)\u00A7d", "&d").replaceAll("(?i)\u00A7k", "&k").replaceAll("(?i)\u00A7c", "&c").replaceAll("(?i)\u00A7m", "&m").replaceAll("(?i)\u00A7n", "&n").replaceAll("(?i)\u00A7f", "&f").replaceAll("(?i)\u00A7e", "&e").replaceAll("(?i)\u00A7r", "&r");
		return str;
	}
	
	/** The opposite function of
	 * {@link BlockAPI#convertSymbolsForSaving(String)}
	 * .
	 * 
	 * @param str String
	 * @return The given string, restored to its original state.
	 * @see BlockAPI#convertSymbolsForSaving(String) */
	public static String convertSymbolsForLoading(String str) {
		str = str.replaceAll("<block_separator>", BlockAPI.block_separator).replaceAll("<block_start>", BlockAPI.block_start).replaceAll("<attribute_start>", BlockAPI.attribute_start).replaceAll("<attribute_separator>", BlockAPI.attribute_separator).replaceAll("<Nline>", "\n").replaceAll("<Rline>", "\r").replaceAll("(?i)&b", ChatColor.AQUA + "").replaceAll("(?i)&0", ChatColor.BLACK + "").replaceAll("(?i)&9", ChatColor.BLUE + "").replaceAll("(?i)&l", ChatColor.BOLD + "").replaceAll("(?i)&3", ChatColor.DARK_AQUA + "").replaceAll("(?i)&1", ChatColor.DARK_BLUE + "").replaceAll("(?i)&8", ChatColor.DARK_GRAY + "").replaceAll("(?i)&2", ChatColor.DARK_GREEN + "").replaceAll("(?i)&5", ChatColor.DARK_PURPLE + "").replaceAll("(?i)&4", ChatColor.DARK_RED + "").replaceAll("(?i)&6", ChatColor.GOLD + "").replaceAll("(?i)&7", ChatColor.GRAY + "").replaceAll("(?i)&a", ChatColor.GREEN + "").replaceAll("(?i)&o", ChatColor.ITALIC + "").replaceAll("(?i)&d", ChatColor.LIGHT_PURPLE + "").replaceAll("(?i)&k", ChatColor.MAGIC + "").replaceAll("(?i)&c", ChatColor.RED + "").replaceAll("(?i)&m", ChatColor.STRIKETHROUGH + "").replaceAll("(?i)&n", ChatColor.UNDERLINE + "").replaceAll("(?i)&f", ChatColor.WHITE + "").replaceAll("(?i)&e", ChatColor.YELLOW + "").replaceAll("(?i)&r", ChatColor.RESET + "");
		return str;
	}
	
	public static String serializeEntity(Entity entity) {
		String rtrn = "";
		entity.getCustomName();
		entity.getEffectivePermissions();
		entity.getFallDistance();
		entity.getFireTicks();
		entity.getLocation();
		entity.getMaxFireTicks();
		entity.getPassenger().getUniqueId();
		entity.getTicksLived();
		entity.getType().name();
		entity.getUniqueId();
		entity.getVelocity().toString();
		entity.getVehicle().getUniqueId();
		if(entity instanceof ItemFrame) {
			ItemFrame frame = (ItemFrame) entity;
			frame.getLocation();
			frame.getAttachedFace();
			frame.getItem();
			InventoryAPI.serializeItemStack(frame.getItem());
		}
		return rtrn;
	}
	
	public static DeserializedBlockResult deseriaizeBlock(String blockStr, World world) {
		BlockState oldState = null;
		try {
			String[] serializedBlock = blockStr.split(BlockAPI.block_separator)[0].split(quote(BlockAPI.block_start));
			if(serializedBlock.length == 2) {
				String location = serializedBlock[0];
				Location loc = null;
				if(location.startsWith("[") && location.endsWith("]")) {
					location = location.substring(1, location.length() - 1);
					String[] coords = location.split(quote(","));
					if(coords.length == 3) {
						int x = Integer.MIN_VALUE;
						int y = Integer.MIN_VALUE;
						int z = Integer.MIN_VALUE;
						for(String coord : coords) {
							if(coord.length() > 2) {
								String value = coord.substring(2);
								if(CodeUtils.isStrAValidInt(value)) {
									int v = Integer.valueOf(value).intValue();
									if(coord.startsWith("x=")) {
										x = v;
									} else if(coord.startsWith("y=")) {
										y = v;
									} else if(coord.startsWith("z=")) {
										z = v;
									}
								}
							}
						}
						if(x != Integer.MAX_VALUE && y != Integer.MAX_VALUE && z != Integer.MAX_VALUE) {
							loc = new Location(world, x, y, z);
						}
					}
				}
				if(loc != null) {
					final Block block = world.getBlockAt(loc);
					oldState = block.getState();
					block.setType(Material.AIR);
					block.getState().update(true, false);
					String[] attributes = serializedBlock[1].split(quote(attribute_start));
					byte dataToSet = -1;
					for(String attribute : attributes) {
						String[] entry = attribute.split(quote(attribute_separator));
						if(entry.length == 2) {
							String name = entry[0];
							String value = entry[1];
							if(name.equals("m")) {
								Material m = MainCmdListener.getMaterialFromString(value);
								if(m != null) {
									block.setType(m);
									System.out.println("Type: " + block.getType().name());
								}
							} else if(name.equals("d")) {
								if(CodeUtils.isStrAValidByte(value)) {
									dataToSet = Byte.valueOf(value).byteValue();
									System.out.println("Data: " + dataToSet);
									block.setData(dataToSet);
								}
							} else if(name.equals("banner")) {
								if(value.startsWith("PATTERN[") && value.endsWith("]") && value.length() > 9) {
									final Banner banner = (Banner) block.getState();
									String[] patterns = value.split(quote("]"));
									for(String serializedPattern : patterns) {
										if(serializedPattern.trim().isEmpty()) {
											continue;
										}
										String[] patternMap = convertSymbolsForLoading(serializedPattern.substring(8)).split(quote(","));
										Map<String, Object> map = new HashMap<>();
										for(String patternEntry : patternMap) {
											String[] split = patternEntry.split(quote("|"));
											if(split.length == 2) {
												String key = split[0];
												Object v = split[1];
												map.put(key, v);
											}
										}
										Pattern pattern;
										try {
											pattern = new Pattern(map);
										} catch(Throwable ignored) {
											ignored.printStackTrace();
											continue;
										}
										System.out.println("Test: PATTERN[color|" + pattern.getColor().name() + ",pattern|" + pattern.getPattern().getIdentifier() + "]");
										banner.addPattern(pattern);
										setDataForBlockAndUpdate(banner);
									}
								} else if(value.startsWith("BASECOLOR=") && value.length() > 10) {
									DyeColor color = DyeColor.valueOf(value.substring(10));
									if(color != null) {
										final Banner banner = (Banner) block.getState();
										banner.setBaseColor(color);
										setDataForBlockAndUpdate(banner);
									}
								}
							} else if(name.equals("")) {
								//TODO
							}
						}
					}
					if(dataToSet != -1) {
						block.setData(dataToSet);
					}
					return new DeserializedBlockResult(block, null);
				}
				return new DeserializedBlockResult(null, new Throwable("Invalid location str: \"" + location + "\""));
			}
			return new DeserializedBlockResult(null, new Throwable("Invalid input. Example input: [x=0,y=66,z=0]#m@STONE:d@0;"));
		} catch(Throwable e) {
			if(oldState != null) {
				oldState.update(true, false);
			}
			return new DeserializedBlockResult(null, e);
		}
	}
	
	public static final class DeserializedBlockResult {
		
		private final Block		block;
		private final Throwable	exception;
		
		protected DeserializedBlockResult(Block block, Throwable throwable) {
			this.block = block;
			this.exception = throwable;
		}
		
		public final Block getBlock() {
			return this.block;
		}
		
		public final Throwable getFailure() {
			return this.exception;
		}
		
		public final String getFailureCause() {
			String rtrn = "UNKNOWN";
			if(this.exception != null) {
				rtrn = this.exception.getMessage() != null ? this.exception.getMessage() : this.exception.getClass().getName();
				if(this.exception.getCause() != null) {
					rtrn = this.exception.getCause().getMessage() != null ? this.exception.getCause().getMessage() : this.exception.getCause().getClass().getName();
				}
			}
			return rtrn;
		}
		
	}
	
	private static final String quote(String s) {
		return java.util.regex.Pattern.quote(s);
	}
	
}
