package com.gmail.br45entei.supercmds;

import com.gmail.br45entei.supercmds.util.VersionUtil;
import com.gmail.br45entei.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** This class is a useful tool if you want a simple way to convert any
 * inventory
 * to a String, and vice versa.
 * 
 * @author <a
 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
 *         Brian_Entei
 *         </a> */
@SuppressWarnings("javadoc")
public class InventoryAPI {
	/** This is the tag that is used to start the item listings when
	 * serializing. */
	private static final String	item_start			= "#";
	/** This is the tag that is used at the end of every serialized
	 * ItemStack. */
	private static final String	item_separator		= ";";
	/** This is the tag that is used at the start of every item attribute(like
	 * an
	 * enchantment, item damage, item lore, the book title, the item's name, the
	 * book author, a book page, etc.) */
	private static final String	attribute_start		= ":";
	/** This is the tag that is used to separate every item attribute(like an
	 * enchantment, item damage, item lore, the book title, the item's name, the
	 * book author, a book page, etc.) */
	private static final String	attribute_separator	= "@";
	
	/** Converts a set of symbols that are used within the serializing functions
	 * to prevent data corruption, as well as any ChatColor characters(it
	 * changes them to their respective '&' codes).
	 * 
	 * @param str String
	 * @return The given string, with certain symbols changed to keep inventory
	 *         formatting from getting broken because of a simple character.
	 * @see InventoryAPI#convertSymbolsForLoading(String str) */
	public static String convertSymbolsForSaving(String str) {
		str = str.replaceAll(InventoryAPI.item_separator, "<item_separator>").replaceAll(InventoryAPI.item_start, "<item_start>").replaceAll(InventoryAPI.attribute_start, "<attribute_start>").replaceAll(InventoryAPI.attribute_separator, "<attribute_separator>").replaceAll("\n", "<Nline>").replaceAll("\r", "<Rline>").replaceAll("(?i)\u00A7b", "&b").replaceAll("\u00A70", "&0").replaceAll("\u00A79", "&9").replaceAll("(?i)\u00A7l", "&l").replaceAll("\u00A73", "&3").replaceAll("\u00A71", "&1").replaceAll("\u00A78", "&8").replaceAll("\u00A72", "&2").replaceAll("\u00A75", "&5").replaceAll("\u00A74", "&4").replaceAll("\u00A76", "&6").replaceAll("\u00A77", "&7").replaceAll("(?i)\u00A7a", "&a").replaceAll("(?i)\u00A7o", "&o").replaceAll("(?i)\u00A7d", "&d").replaceAll("(?i)\u00A7k", "&k").replaceAll("(?i)\u00A7c", "&c").replaceAll("(?i)\u00A7m", "&m").replaceAll("(?i)\u00A7n", "&n").replaceAll("(?i)\u00A7f", "&f").replaceAll("(?i)\u00A7e", "&e").replaceAll("(?i)\u00A7r", "&r");
		return str;
	}
	
	/** The opposite function of
	 * {@link InventoryAPI#convertSymbolsForSaving(String)}.
	 * 
	 * @param str String
	 * @return The given string, restored to its original state.
	 * @see {@link InventoryAPI#convertSymbolsForSaving(String str)} */
	public static String convertSymbolsForLoading(String str) {
		str = str.replaceAll("<item_separator>", InventoryAPI.item_separator).replaceAll("<item_start>", InventoryAPI.item_start).replaceAll("<attribute_start>", InventoryAPI.attribute_start).replaceAll("<attribute_separator>", InventoryAPI.attribute_separator).replaceAll("<Nline>", "\n").replaceAll("<Rline>", "\r").replaceAll("(?i)&b", ChatColor.AQUA + "").replaceAll("(?i)&0", ChatColor.BLACK + "").replaceAll("(?i)&9", ChatColor.BLUE + "").replaceAll("(?i)&l", ChatColor.BOLD + "").replaceAll("(?i)&3", ChatColor.DARK_AQUA + "").replaceAll("(?i)&1", ChatColor.DARK_BLUE + "").replaceAll("(?i)&8", ChatColor.DARK_GRAY + "").replaceAll("(?i)&2", ChatColor.DARK_GREEN + "").replaceAll("(?i)&5", ChatColor.DARK_PURPLE + "").replaceAll("(?i)&4", ChatColor.DARK_RED + "").replaceAll("(?i)&6", ChatColor.GOLD + "").replaceAll("(?i)&7", ChatColor.GRAY + "").replaceAll("(?i)&a", ChatColor.GREEN + "").replaceAll("(?i)&o", ChatColor.ITALIC + "").replaceAll("(?i)&d", ChatColor.LIGHT_PURPLE + "").replaceAll("(?i)&k", ChatColor.MAGIC + "").replaceAll("(?i)&c", ChatColor.RED + "").replaceAll("(?i)&m", ChatColor.STRIKETHROUGH + "").replaceAll("(?i)&n", ChatColor.UNDERLINE + "").replaceAll("(?i)&f", ChatColor.WHITE + "").replaceAll("(?i)&e", ChatColor.YELLOW + "").replaceAll("(?i)&r", ChatColor.RESET + "");
		return str;
	}
	
	public static String serializeInventory(ItemStack[] items, int invSize, String invTitle) {
		String serialization = invSize + InventoryAPI.item_separator + invTitle + InventoryAPI.item_separator;
		int i = 0;
		for(ItemStack is : items) {
			if(i >= invSize) {
				break;
			}
			serialization += InventoryAPI.serializeItemStack(is, i);
			i++;
		}
		return serialization;
	}
	
	public static String serializeItemStack(ItemStack is) {
		return serializeItemStack(is, -1);
	}
	
	public static String serializeItemStack(ItemStack is, int i) {
		String serialization = "";
		if(is != null) {
			String serializedItemStack = new String();
			String isType = String.valueOf(is.getType().name());
			serializedItemStack += "t" + InventoryAPI.attribute_separator + isType;
			if(is.getDurability() != 0) {
				String isDurability = String.valueOf(is.getDurability());
				serializedItemStack += InventoryAPI.attribute_start + "d" + InventoryAPI.attribute_separator + isDurability;
			}
			if(is.getAmount() != 1) {
				String isAmount = String.valueOf(is.getAmount());
				serializedItemStack += InventoryAPI.attribute_start + "a" + InventoryAPI.attribute_separator + isAmount;
			}
			Map<Enchantment, Integer> isEnch = is.getEnchantments();
			if(isEnch.size() > 0) {
				for(Entry<Enchantment, Integer> ench : isEnch.entrySet()) {
					serializedItemStack += InventoryAPI.attribute_start + "e" + InventoryAPI.attribute_separator + ench.getKey().getName() + InventoryAPI.attribute_separator + ench.getValue();
				}
			}
			if(is.getItemMeta().hasDisplayName()) {
				serializedItemStack += InventoryAPI.attribute_start + "n" + InventoryAPI.attribute_separator + InventoryAPI.convertSymbolsForSaving(is.getItemMeta().getDisplayName());
			}
			if(is.getItemMeta().hasLore()) {
				Iterator<String> it = is.getItemMeta().getLore().iterator();
				String lores = InventoryAPI.attribute_start + "l";
				if(it.hasNext() == false) {
					lores += InventoryAPI.attribute_separator;
				}
				while(it.hasNext()) {
					String itNext = it.next();
					lores += InventoryAPI.attribute_separator + InventoryAPI.convertSymbolsForSaving(itNext);
				}
				serializedItemStack += lores;
			}
			if(is.getType() == Material.BOOK_AND_QUILL || is.getType() == Material.WRITTEN_BOOK) {
				serializedItemStack += InventoryAPI.serializeBook(is);
			} else if(is.getType() == Material.ENCHANTED_BOOK) {
				serializedItemStack += InventoryAPI.serializeEnchantedBook(is);
			}
			if(is.getType() == Material.SKULL || is.getType() == Material.SKULL_ITEM) {
				serializedItemStack += InventoryAPI.serializeSkullData(is);
			}
			if(is.getType() == Material.FIREWORK) {
				serializedItemStack += InventoryAPI.serializeFireWork(is);
			}
			if(is.getType() == Material.BANNER) {
				serializedItemStack += InventoryAPI.serializeBanner(is);
			}
			if(is.getType() == Material.MONSTER_EGG) {
				serializedItemStack += InventoryAPI.serializeSpawnEgg(is);
			}
			serialization += (i != -1 ? i + "" : "") + InventoryAPI.item_start + serializedItemStack + InventoryAPI.item_separator;
		}
		return serialization;
	}
	
	/** @param item
	 * @return */
	public static String serializeSkullData(ItemStack item) {
		String rtrn = "";
		if(item != null) {
			if(item.getType().equals(Material.SKULL) || item.getType().equals(Material.SKULL_ITEM)) {
				SkullMeta meta = (SkullMeta) item.getItemMeta();
				if(meta.hasOwner()) {
					rtrn += InventoryAPI.attribute_start + "k" + InventoryAPI.attribute_separator + InventoryAPI.convertSymbolsForSaving(meta.getOwner());
				}
				return rtrn;
			}
		}
		return rtrn;
	}
	
	public static ItemStack deserializeSkullData(ItemStack item, String[] itemAttribute) {
		if(item != null) {
			if(item.getType().equals(Material.SKULL) || item.getType().equals(Material.SKULL_ITEM)) {
				SkullMeta meta = (SkullMeta) item.getItemMeta();
				String owner = itemAttribute[1];
				meta.setOwner(owner);
				item.setItemMeta(meta);
				return item;
			}
		}
		return item;
	}
	
	public static String serializeFireWork(ItemStack item) {
		String rtrn = "";
		if(item != null) {
			if(item.getType().equals(Material.FIREWORK)) {
				FireworkMeta meta = (FireworkMeta) item.getItemMeta();
				if(meta.hasEffects()) {
					rtrn += InventoryAPI.attribute_start + "f" + InventoryAPI.attribute_separator + meta.getPower() + InventoryAPI.attribute_separator;
					for(FireworkEffect effect : meta.getEffects()) {
						rtrn += "<EFFECT>" + effect.getType().name() + "~";
						if(effect.hasFlicker()) {
							rtrn += "flicker_true" + "~";
						} else {
							rtrn += "flicker_false" + "~";
						}
						if(effect.hasTrail()) {
							rtrn += "trail_true" + "~";
						} else {
							rtrn += "trail_false" + "~";
						}
						rtrn += "<COLORS>";
						for(org.bukkit.Color color : effect.getColors()) {
							rtrn += "<COLOR>red_" + color.getRed() + "/" + "green_" + color.getGreen() + "/" + "blue_" + color.getBlue();
						}
						rtrn += "<COLORS>";//Intentional, don't change to "FADECOLORS"!
						for(org.bukkit.Color color : effect.getFadeColors()) {
							rtrn += "<FADECOLOR>red_" + color.getRed() + "/" + "green_" + color.getGreen() + "/" + "blue_" + color.getBlue();
						}
					}
				}
				return rtrn;
			}
		}
		return rtrn;
	}
	
	public static ItemStack deserializeFirework(ItemStack item, String[] itemAttribute) {
		if(item != null) {
			if(item.getType().equals(Material.FIREWORK)) {
				int power = Integer.valueOf(itemAttribute[1]).intValue();
				String[] serializedEffects = itemAttribute[2].split("<EFFECT>");
				FireworkMeta meta = (FireworkMeta) item.getItemMeta();
				if(meta == null) {
					meta = (FireworkMeta) org.bukkit.Bukkit.getServer().getItemFactory().getItemMeta(Material.FIREWORK);
				}
				meta.setPower(power);
				for(String effect : serializedEffects) {
					if(!effect.isEmpty()) {
						String[] effectAttribute = effect.split("~");
						Main.DEBUG("&dserializedEffects[0]: &b" + serializedEffects[0]);
						Main.DEBUG("&dserializedEffects[1]: &b" + serializedEffects[1]);
						Main.DEBUG("&deffectAttribute[0]: &b" + effectAttribute[0]);
						//Main.DEBUG("&deffectAttribute[1]: &b" + effectAttribute[1]);
						
						Type effectType = Type.valueOf(effectAttribute[0]);
						boolean hasFlicker = Boolean.valueOf(effectAttribute[1].toLowerCase().replace("flicker_", "")).booleanValue();
						boolean hasTrail = Boolean.valueOf(effectAttribute[2].toLowerCase().replace("trail_", "")).booleanValue();
						String[] colors = effectAttribute[3].split("<COLORS>");
						ArrayList<Color> colorArray = new ArrayList<>();
						ArrayList<Color> fadeColorArray = new ArrayList<>();
						boolean colorRetrieved = false;
						for(String curColorArray : colors) {
							if(!curColorArray.isEmpty()) {
								String[] Colors = curColorArray.split("<COLOR>");
								String[] FadeColors = curColorArray.split("<FADECOLOR>");
								if(colorRetrieved) {
									for(String curFadeColor : FadeColors) {
										if(!curFadeColor.isEmpty()) {
											String[] values = curFadeColor.split("/");
											Main.DEBUG("&d1values[0]: &b" + values[0]);
											Main.DEBUG("&d1values[1]: &b" + values[1]);
											Main.DEBUG("&d1values[2]: &b" + values[2]);
											int red = Integer.valueOf(values[0].replace("red_", "")).intValue(),
													green = Integer.valueOf(values[1].replace("green_", "")).intValue(),
													blue = Integer.valueOf(values[2].replace("blue_", "")).intValue();
											Color MadeFadeColor = Color.fromRGB(red, green, blue);
											fadeColorArray.add(MadeFadeColor);
										}
									}
								}
								if(!colorRetrieved) {
									for(String curColor : Colors) {
										if(!curColor.isEmpty()) {
											String[] values = curColor.split("/");
											Main.DEBUG("&d0values[0]: &b" + values[0]);
											Main.DEBUG("&d0values[1]: &b" + values[1]);
											Main.DEBUG("&d0values[2]: &b" + values[2]);
											int red = Integer.valueOf(values[0].replace("red_", "")).intValue(),
													green = Integer.valueOf(values[1].replace("green_", "")).intValue(),
													blue = Integer.valueOf(values[2].replace("blue_", "")).intValue();
											Color MadeColor = Color.fromRGB(red, green, blue);
											colorArray.add(MadeColor);
											colorRetrieved = true;
										}
									}
								}
							}
						}
						Builder builder = FireworkEffect.builder().with(effectType).withColor(colorArray).withFade(fadeColorArray).trail(hasTrail).flicker(hasFlicker);
						FireworkEffect Effect = builder.build();
						meta.addEffect(Effect);
					}
				}
				item.setItemMeta(meta);
			}
		}
		return item;
	}
	
	/** Serializes the given inventory and converts and saves all data that may
	 * be associated with the items within the inventory.
	 * <p>
	 * <strong>Serialization tags(for easy readability):</strong><br>
	 * 0# = The slot number(where 0 is the first slot in the hotbar of a
	 * player's inventory)<br>
	 * t@ = Item Type(Notice this one is NOT prefixed with a colon)<br>
	 * :d@ = Item Durability<br>
	 * :a@ = Item Amount<br>
	 * :e@ = Item Enchantment Data<br>
	 * :l@ = Item Lore Data<br>
	 * :n@ = Item Name<br>
	 * :b@ = Book Data<br>
	 * :z@ = Enchanted Book Data<br>
	 * :k@ = Skull Item<br>
	 * :f@ = Firework Item Data<br>
	 * :flag@ = ItemFlag Item Data<br>
	 * You can add an additional '@' after a tag to specify additional data if
	 * it has the word 'Data' in it's name above.
	 * <p>
	 * <strong>An example of a player inventory, containing a Stone Pickaxe,
	 * with the custom Name "Hello, world!" and some enchantments and
	 * lore:</strong><br>
	 * <font face="consolas"
	 * size="2">36;container.inventory;0#t@274:d@60:e@34@3:
	 * e@35@1:e@32@2:n@&3Hello, world!:l@&4The item's Lore, Line One@&2The
	 * Item's Lore, Line Two@Etc, &3etc.;</font><br>
	 * The "36;" is the size of the inventory(in slots), and the
	 * "container.inventory;" is the title of the inventory(That is the default
	 * Minecraft title, anything else that you type here will be displayed
	 * instead of 'Inventory').<br>
	 * Deleting these two stored values will render the whole inventory
	 * un-readable, and upon being read from by
	 * {@link InventoryAPI#deserializeInventory(String)}, will be returned null.
	 * 
	 * @param inv Inventory
	 * @return The given Inventory, serialized into a single-line String.
	 *         <p>
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String, Player)}<br>
	 *      {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#deserializeBook(ItemStack, String[])} */
	public static String serializeInventory(Inventory inv) {
		return serializeInventory(InventoryData.getFromInventory(inv));
	}
	
	/** Serializes the given inventory and converts and saves all data that may
	 * be associated with the items within the inventory.
	 * <p>
	 * <strong>Serialization tags(for easy readability):</strong><br>
	 * 0# = The slot number(where 0 is the first slot in the hotbar of a
	 * player's inventory)<br>
	 * t@ = Item Type(Notice this one is NOT prefixed with a colon)<br>
	 * :d@ = Item Durability<br>
	 * :a@ = Item Amount<br>
	 * :e@ = Item Enchantment Data<br>
	 * :l@ = Item Lore Data<br>
	 * :n@ = Item Name<br>
	 * :b@ = Book Data<br>
	 * :z@ = Enchanted Book Data<br>
	 * :k@ = Skull Item<br>
	 * :f@ = Firework Item Data<br>
	 * :flag@ = ItemFlag Item Data<br>
	 * You can add an additional '@' after a tag to specify additional data if
	 * it has the word 'Data' in it's name above.
	 * <p>
	 * <strong>An example of a player inventory, containing a Stone Pickaxe,
	 * with the custom Name "Hello, world!" and some enchantments and
	 * lore:</strong><br>
	 * <font face="consolas"
	 * size="2">36;container.inventory;0#t@274:d@60:e@34@3:
	 * e@35@1:e@32@2:n@&3Hello, world!:l@&4The item's Lore, Line One@&2The
	 * Item's Lore, Line Two@Etc, &3etc.;</font><br>
	 * The "36;" is the size of the inventory(in slots), and the
	 * "container.inventory;" is the title of the inventory(That is the default
	 * Minecraft title, anything else that you type here will be displayed
	 * instead of 'Inventory').<br>
	 * Deleting these two stored values will render the whole inventory
	 * un-readable, and upon being read from by
	 * {@link InventoryAPI#deserializeInventory(String)}, will be returned null.
	 * 
	 * @param inv Inventory
	 * @return The given Inventory, serialized into a single-line String.
	 *         <p>
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String, Player)}<br>
	 *      {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#deserializeBook(ItemStack, String[])} */
	protected static String serializeInventory(InventoryData inv) {
		String serialization = inv.getSize() + InventoryAPI.item_separator + inv.getTitle() + InventoryAPI.item_separator;
		for(int i = 0; i < inv.getSize(); i++) {
			ItemStack is = inv.getItem(i);
			if(is != null) {
				String serializedItemStack = new String();
				String isType = String.valueOf(is.getType().name());
				serializedItemStack += "t" + InventoryAPI.attribute_separator + isType;
				if(is.getDurability() != 0) {
					String isDurability = String.valueOf(is.getDurability());
					serializedItemStack += InventoryAPI.attribute_start + "d" + InventoryAPI.attribute_separator + isDurability;
				}
				if(is.getAmount() != 1) {
					String isAmount = String.valueOf(is.getAmount());
					serializedItemStack += InventoryAPI.attribute_start + "a" + InventoryAPI.attribute_separator + isAmount;
				}
				Set<ItemFlag> flags = is.getItemMeta().getItemFlags();
				if(!flags.isEmpty()) {
					for(ItemFlag flag : flags) {
						serializedItemStack += InventoryAPI.attribute_start + "flag" + InventoryAPI.attribute_separator + flag.name();
					}
				}
				Map<Enchantment, Integer> isEnch = is.getEnchantments();
				if(isEnch.size() > 0) {
					for(Entry<Enchantment, Integer> ench : isEnch.entrySet()) {
						serializedItemStack += InventoryAPI.attribute_start + "e" + InventoryAPI.attribute_separator + ench.getKey().getName() + InventoryAPI.attribute_separator + ench.getValue();
					}
				}
				if(is.getItemMeta().hasDisplayName()) {
					serializedItemStack += InventoryAPI.attribute_start + "n" + InventoryAPI.attribute_separator + InventoryAPI.convertSymbolsForSaving(is.getItemMeta().getDisplayName());
				}
				if(is.getItemMeta().hasLore()) {
					Iterator<String> it = is.getItemMeta().getLore().iterator();
					String lores = InventoryAPI.attribute_start + "l";
					if(it.hasNext() == false) {
						lores += InventoryAPI.attribute_separator;
					}
					while(it.hasNext()) {
						String itNext = it.next();
						lores += InventoryAPI.attribute_separator + InventoryAPI.convertSymbolsForSaving(itNext);
					}
					serializedItemStack += lores;
				}
				if(is.getItemMeta().hasEnchants()) {
					is.getItemMeta().getEnchants();
					Map<Enchantment, Integer> isMetaEnch = is.getEnchantments();
					if(isEnch.size() > 0) {
						for(Entry<Enchantment, Integer> ench : isMetaEnch.entrySet()) {
							serializedItemStack += InventoryAPI.attribute_start + "eMeta" + InventoryAPI.attribute_separator + ench.getKey().getName() + InventoryAPI.attribute_separator + ench.getValue();
						}
					}
				}
				if(is.getType() == Material.BOOK_AND_QUILL || is.getType() == Material.WRITTEN_BOOK) {
					serializedItemStack += InventoryAPI.serializeBook(is);
				} else if(is.getType() == Material.ENCHANTED_BOOK) {
					serializedItemStack += InventoryAPI.serializeEnchantedBook(is);
				}
				if(is.getType() == Material.SKULL || is.getType() == Material.SKULL_ITEM) {
					serializedItemStack += InventoryAPI.serializeSkullData(is);
				}
				if(is.getType() == Material.FIREWORK) {
					serializedItemStack += InventoryAPI.serializeFireWork(is);
				}
				if(is.getType() == Material.BANNER) {
					serializedItemStack += InventoryAPI.serializeBanner(is);
				}
				if(is.getType() == Material.MONSTER_EGG) {
					serializedItemStack += InventoryAPI.serializeSpawnEgg(is);
				}
				serialization += i + InventoryAPI.item_start + serializedItemStack + InventoryAPI.item_separator;
			}
		}
		return serialization;
	}
	
	/** This function is the same as
	 * {@link InventoryAPI#deserializeInventory(String, Player)}, except for the
	 * Player parameter, which is null.
	 * 
	 * @param invString String
	 * @return The inventory with no holder, or null, if no string is given.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeInventory(Inventory)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String, Player)}<br>
	 *      {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#deserializeBook(ItemStack, String[])} */
	public static Inventory deserializeInventory(String invString) {
		return InventoryAPI.deserializeInventory(invString, (Player) null);
	}
	
	/** Attempts to return the Inventory of the Player, deserialized from the
	 * String parameter. The Inventory that is returned will be owned by the
	 * Player.
	 * 
	 * @param invString String
	 * @param player Player
	 * @return The deserialized inventory with the given player parameter as the
	 *         holder, or the players' inventory, if no string is given. If
	 *         neither a string nor a player is given, then this function
	 *         returns null.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeInventory(Inventory)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#deserializeBook(ItemStack, String[])} */
	public static Inventory deserializeInventory(String invString, Player player) {
		if(invString != null) {
			if(!invString.isEmpty()) {
				String[] serializedBlocks = invString.split(InventoryAPI.item_separator);
				//String invInfo = serializedBlocks[0];
				//Inventory deserializedInventory = Bukkit.getServer().createInventory(player, invType);
				//Inventory deserializedInventory = Bukkit.getServer().createInventory(player, Integer.valueOf(invInfo));
				//InventoryData deserializedInventory = new InventoryData(player, Integer.valueOf(serializedBlocks[0]).intValue(), 64, InventoryType.PLAYER, serializedBlocks[1]);//
				Inventory deserializedInventory = Bukkit.getServer().createInventory(player, Integer.valueOf(serializedBlocks[0]).intValue(), String.valueOf(serializedBlocks[1]));
				//for(int i = 1; i < serializedBlocks.length; i++) {
				for(int i = 2; i < serializedBlocks.length; i++) {
					String[] serializedBlock = serializedBlocks[i].split(InventoryAPI.item_start);
					int stackPosition = Integer.valueOf(serializedBlock[0]).intValue();
					if(stackPosition >= deserializedInventory.getSize()) {
						continue;
					}
					ItemStack is = null;
					Boolean createdItemStack = Boolean.FALSE;
					String[] serializedItemStack = serializedBlock[1].split(InventoryAPI.attribute_start);
					for(String itemInfo : serializedItemStack) {
						String[] itemAttribute = itemInfo.split(InventoryAPI.attribute_separator);
						if(itemAttribute[0].equals("t")) {
							is = new ItemStack(Material.getMaterial(itemAttribute[1]));
							createdItemStack = Boolean.TRUE;
						}
						if(createdItemStack.booleanValue()) {
							if(is != null) {
								if(itemAttribute[0].equals("d")) {
									is.setDurability(Short.valueOf(itemAttribute[1]).shortValue());
								} else if(itemAttribute[0].equals("a")) {
									is.setAmount(Integer.valueOf(itemAttribute[1]).intValue());
								} else if(itemAttribute[0].equals("flag")) {
									ItemMeta meta;
									if(is.getItemMeta() == null) {
										meta = Bukkit.getServer().getItemFactory().getItemMeta(is.getType());
									} else {
										meta = is.getItemMeta();
									}
									ItemFlag flag = ItemFlag.valueOf(itemAttribute[1]);
									if(flag != null) {
										meta.addItemFlags(flag);
									}
									is.setItemMeta(meta);
								} else if(itemAttribute[0].equals("n")) {
									ItemMeta meta;
									if(is.getItemMeta() == null) {
										meta = Bukkit.getServer().getItemFactory().getItemMeta(is.getType());
									} else {
										meta = is.getItemMeta();
									}
									meta.setDisplayName(InventoryAPI.convertSymbolsForLoading(itemAttribute[1]));
									is.setItemMeta(meta);
								} else if(itemAttribute[0].equals("e")) {
									is.addUnsafeEnchantment(Enchantment.getByName(itemAttribute[1]), Integer.valueOf(itemAttribute[2]).intValue());
								} else if(itemAttribute[0].equals("eMeta")) {
									ItemMeta meta = null;
									if(is.getItemMeta() == null) {
										meta = Bukkit.getServer().getItemFactory().getItemMeta(is.getType());
									} else {
										meta = is.getItemMeta();
									}
									meta.addEnchant(Enchantment.getByName(itemAttribute[1]), Integer.valueOf(itemAttribute[2]).intValue(), true);
									is.setItemMeta(meta);
								} else if(itemAttribute[0].equals("z")) {
									is = InventoryAPI.deserializeEnchantedBook(is, itemAttribute);
								} else if(itemAttribute[0].equals("l")) {
									ArrayList<String> lores = new ArrayList<>();
									for(int j = 1; j < itemAttribute.length; j++) {
										lores.add(InventoryAPI.convertSymbolsForLoading(itemAttribute[j]));
									}
									ItemMeta meta = null;
									if(is.getItemMeta() == null) {
										meta = Bukkit.getServer().getItemFactory().getItemMeta(is.getType());
									} else {
										meta = is.getItemMeta();
									}
									meta.setLore(lores);
									is.setItemMeta(meta);
								} else if(itemAttribute[0].equals("b")) {
									is = InventoryAPI.deserializeBook(is, itemAttribute);
								} else if(itemAttribute[0].equals("k")) {
									is = InventoryAPI.deserializeSkullData(is, itemAttribute);
								} else if(itemAttribute[0].equals("f")) {
									is = InventoryAPI.deserializeFirework(is, itemAttribute);
								} else if(itemAttribute[0].equals("banner")) {
									is = InventoryAPI.deserializeBanner(is, itemAttribute);
								} else if(itemAttribute[0].equals("spawnType")) {
									is = InventoryAPI.deserializeSpawnEgg(is, itemAttribute);
								}
							}
						}
					}
					deserializedInventory.setItem(stackPosition, is);
				}
				return deserializedInventory;
			} else if(player != null) {
				return player.getInventory();//InventoryData.getFromInventory(player.getInventory());
				/*			}
						} else {
							return null;
						}
						return Bukkit.getServer().createInventory(null, InventoryType.PLAYER);
					}*/
			} else {
				return null;
			}
		}
		return null;
	}
	
	public static Player getPlayer(String name) {
		Player rtrn = null;
		for(Player curPlayer : org.bukkit.Bukkit.getServer().getOnlinePlayers()) {
			if(curPlayer.getName().equals(name)) {
				return curPlayer;
			}
		}
		return rtrn;
	}
	
	/** Attempts to return the Inventory of the target, deserialized from the
	 * String parameter.
	 * 
	 * @param invString String
	 * @param targetName String
	 * @return The deserialized inventory. If neither a string nor a player is
	 *         found/given, then this function returns null.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeInventory(Inventory)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#deserializeBook(ItemStack, String[])} */
	public static Inventory deserializeInventory(String invString, String targetName) {
		if(invString != null) {
			if(invString.equals("") == false) {
				String[] serializedBlocks = invString.split(InventoryAPI.item_separator);
				//String invInfo = serializedBlocks[0];
				//Inventory deserializedInventory = Bukkit.getServer().createInventory(player, invType);
				//Inventory deserializedInventory = Bukkit.getServer().createInventory(player, Integer.valueOf(invInfo));
				//InventoryData deserializedInventory = new InventoryData(InventoryAPI.getPlayer(targetName), Integer.valueOf(serializedBlocks[0]).intValue(), 64, InventoryType.PLAYER, String.valueOf(serializedBlocks[1]));
				Inventory deserializedInventory = Bukkit.getServer().createInventory(InventoryAPI.getPlayer(targetName), Integer.valueOf(serializedBlocks[0]).intValue(), String.valueOf(serializedBlocks[1]));
				//for(int i = 1; i < serializedBlocks.length; i++) {
				for(int i = 2; i < serializedBlocks.length; i++) {
					String[] serializedBlock = serializedBlocks[i].split(InventoryAPI.item_start);
					int stackPosition = Integer.valueOf(serializedBlock[0]).intValue();
					if(stackPosition >= deserializedInventory.getSize()) {
						continue;
					}
					ItemStack is = null;
					Boolean createdItemStack = Boolean.FALSE;
					String[] serializedItemStack = serializedBlock[1].split(InventoryAPI.attribute_start);
					for(String itemInfo : serializedItemStack) {
						String[] itemAttribute = itemInfo.split(InventoryAPI.attribute_separator);
						if(itemAttribute[0].equals("t")) {
							is = new ItemStack(Material.getMaterial(itemAttribute[1]));
							createdItemStack = Boolean.TRUE;
						}
						if(createdItemStack.booleanValue()) {
							if(is != null) {
								if(itemAttribute[0].equals("d")) {
									is.setDurability(Short.valueOf(itemAttribute[1]).shortValue());
								} else if(itemAttribute[0].equals("a")) {
									is.setAmount(Integer.valueOf(itemAttribute[1]).intValue());
								} else if(itemAttribute[0].equals("n")) {
									ItemMeta meta = null;
									if(is.getItemMeta() == null) {
										meta = Bukkit.getServer().getItemFactory().getItemMeta(is.getType());
									} else {
										meta = is.getItemMeta();
									}
									meta.setDisplayName(InventoryAPI.convertSymbolsForLoading(itemAttribute[1]));
									is.setItemMeta(meta);
								} else if(itemAttribute[0].equals("e")) {
									is.addUnsafeEnchantment(Enchantment.getByName(itemAttribute[1]), Integer.valueOf(itemAttribute[2]).intValue());
								} else if(itemAttribute[0].equals("eMeta")) {
									ItemMeta meta = null;
									if(is.getItemMeta() == null) {
										meta = Bukkit.getServer().getItemFactory().getItemMeta(is.getType());
									} else {
										meta = is.getItemMeta();
									}
									meta.addEnchant(Enchantment.getByName(itemAttribute[1]), Integer.valueOf(itemAttribute[2]).intValue(), true);
									is.setItemMeta(meta);
								} else if(itemAttribute[0].equals("z")) {
									is = InventoryAPI.deserializeEnchantedBook(is, itemAttribute);
								} else if(itemAttribute[0].equals("l")) {
									ArrayList<String> lores = new ArrayList<>();
									for(int j = 1; j < itemAttribute.length; j++) {
										lores.add(InventoryAPI.convertSymbolsForLoading(itemAttribute[j]));
									}
									ItemMeta meta = null;
									if(is.getItemMeta() == null) {
										meta = Bukkit.getServer().getItemFactory().getItemMeta(is.getType());
									} else {
										meta = is.getItemMeta();
									}
									meta.setLore(lores);
									is.setItemMeta(meta);
								} else if(itemAttribute[0].equals("b")) {
									is = InventoryAPI.deserializeBook(is, itemAttribute);
								} else if(itemAttribute[0].equals("k")) {
									is = InventoryAPI.deserializeSkullData(is, itemAttribute);
								} else if(itemAttribute[0].equals("f")) {
									is = InventoryAPI.deserializeFirework(is, itemAttribute);
								} else if(itemAttribute[0].equals("banner")) {
									is = InventoryAPI.deserializeBanner(is, itemAttribute);
								} else if(itemAttribute[0].equals("spawnType")) {
									is = InventoryAPI.deserializeSpawnEgg(is, itemAttribute);
								}
							}
						}
					}
					deserializedInventory.setItem(stackPosition, is);
				}
				return deserializedInventory;
			}
			return null;
		}
		return null;
	}
	
	/** With this function allows you to easily choose what Inventory is to be
	 * used from the given Player in the function
	 * {@link InventoryAPI#serializeInventory(Inventory)}, and have it returned
	 * all in the same function. <br>
	 * With the parameter invToConvert, you can specify either "inventory",
	 * "armor", or "enderchest", and the function will return the serialized
	 * inventory of the type that you specified.
	 * 
	 * @param player Player
	 * @param invToConvert String
	 * @return The serialized inventory as a single-line String. If the Player
	 *         parameter is null, then a blank String is returned. If the String
	 *         parameter is null, or isn't one of the options listed, the
	 *         player's default inventory is returned, serialized as a
	 *         single-line String. If both the Player and the String parameters
	 *         are null, a blank String is returned.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeInventory(Inventory)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String, Player)}<br>
	 *      {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#deserializeBook(ItemStack, String[])} */
	public static String serializeInventory(Player player, String invToConvert) {
		if(player != null) {
			Inventory invInventory = Bukkit.getServer().createInventory(player, InventoryType.PLAYER);
			if(invToConvert != null) {
				if(invToConvert.equalsIgnoreCase("inventory")) {
					invInventory = player.getInventory();
				} else if(invToConvert.equalsIgnoreCase("armor")) {
					Inventory newInv = Bukkit.getServer().createInventory(player, 9);
					//newInv.setContents(player.getInventory().getArmorContents());
					int num = 0;
					for(ItemStack curItem : player.getInventory().getArmorContents()) {
						newInv.setItem(num, curItem);
						num++;
					}
					invInventory = newInv;
				} else if(invToConvert.equalsIgnoreCase("enderchest")) {
					invInventory = player.getEnderChest();
				} else {
					invInventory = player.getInventory();
				}
			} else {
				invInventory = player.getInventory();
			}
			return InventoryAPI.serializeInventory(InventoryData.getFromInventory(invInventory));
		}
		return "";
	}
	
	/** Attempts to return the given Player's default Inventory, serialized as a
	 * single-line String. Uses
	 * {@link InventoryAPI#serializeInventory(Inventory)} for serialization.
	 * 
	 * @param player Player
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @return The player's default Inventory, serialized as a single-line
	 *         String. If the player is null, a blank string is returned.
	 * @see {@link InventoryAPI#serializeInventory(Inventory)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String, Player)}<br>
	 *      {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#deserializeBook(ItemStack, String[])} */
	public static String serializeInventory(Player player) {
		return InventoryAPI.serializeInventory(player, null);
	}
	
	/** Attempts to serialize the given ItemStack and return it as a single-line
	 * String. If the ItemStack provided is not a Book and Quill or a Written
	 * Book, this function will return a blank string.
	 * 
	 * @param item ItemStack
	 * @return The given ItemStack in a serialized single-line String if it is a
	 *         Book and Quill or a Written Book. Otherwise, a blank string is
	 *         returned.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#deserializeBook(ItemStack, String[])}<br>
	 *      {@link InventoryAPI#serializeInventory(Inventory)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String, Player)} */
	public static String serializeBook(ItemStack item) {
		String rtrn = ":b@";
		BookMeta meta;
		if(item.getType() == Material.BOOK_AND_QUILL || item.getType() == Material.WRITTEN_BOOK) {
			Main.DEBUG("&aserializeBook(ItemStack item(&f" + item.getType().name() + "&a))");
			meta = (BookMeta) item.getItemMeta();
			String author = (meta.hasAuthor() ? meta.getAuthor() : null);
			String title = (meta.hasTitle() ? meta.getTitle() : null);
			if(author != null) {
				rtrn += InventoryAPI.convertSymbolsForSaving(author);
			} else {
				rtrn += "<NO-AUTHOR>";
			}
			if(title != null) {
				rtrn += "@" + InventoryAPI.convertSymbolsForSaving(meta.getTitle());
			} else {
				rtrn += "@<NO-TITLE>";
			}
			if(meta.hasPages()) {
				List<String> pages = meta.getPages();
				for(String curPage : pages) {
					rtrn += "@" + InventoryAPI.convertSymbolsForSaving(curPage);//The start of the pages will be itemAttribute[3] and up.
				}
			}
		}
		return(rtrn.equals(":b@") ? "" : rtrn);
	}
	
	public static String serializeEnchantedBook(ItemStack item) {
		String rtrn = "";
		if(item.getType() == Material.ENCHANTED_BOOK) {
			org.bukkit.inventory.meta.EnchantmentStorageMeta EnchantMeta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) item.getItemMeta();
			if(EnchantMeta != null) {
				Map<Enchantment, Integer> isEnch = EnchantMeta.getStoredEnchants();
				if(isEnch.size() > 0) {
					for(Entry<Enchantment, Integer> ench : isEnch.entrySet()) {
						rtrn += InventoryAPI.attribute_start + "z" + InventoryAPI.attribute_separator + ench.getKey().getName() + InventoryAPI.attribute_separator + ench.getValue();
					}
				}
			}
		}
		return rtrn;
	}
	
	public static ItemStack deserializeEnchantedBook(ItemStack item, String[] itemAttribute) {
		if(item == null && itemAttribute == null) {
			return new ItemStack(Material.ENCHANTED_BOOK);
		}
		if(item == null) {
			item = new ItemStack(Material.ENCHANTED_BOOK);
		}
		if(itemAttribute == null) {
			return item;
		}
		if(item.getType() == Material.ENCHANTED_BOOK) {
			org.bukkit.inventory.meta.EnchantmentStorageMeta EnchantMeta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) (item.hasItemMeta() ? item.getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(item.getType()));
			if(itemAttribute[1] != null && itemAttribute[2] != null) {
				EnchantMeta.addStoredEnchant(Enchantment.getByName(itemAttribute[1]), Integer.valueOf(itemAttribute[2]).intValue(), true);
			}
			item.setItemMeta(EnchantMeta);
		}
		return item;
	}
	
	/** Attempts to deserialize the BookMeta data from the String[] parameter
	 * and
	 * add it to the given ItemStack, which is then returned. If no ItemStack is
	 * given, a Written Book is generated in its place, and the BookMeta
	 * assigned. If no String[] parameter is given, then this function will
	 * return the given ItemStack, or a blank Book and Quill if no ItemStack was
	 * given either.
	 * 
	 * @param item ItemStack
	 * @param itemAttribute String[]
	 * @return The deserialized Book, with all of its previous BookMeta intact.
	 *         If the String[] parameter is null, then this will return the
	 *         given ItemStack.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeBook(ItemStack)}<br>
	 *      {@link InventoryAPI#serializeInventory(Inventory)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player, String)}<br>
	 *      {@link InventoryAPI#serializeInventory(Player)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String)}<br>
	 *      {@link InventoryAPI#deserializeInventory(String, Player)} */
	public static ItemStack deserializeBook(ItemStack item, String[] itemAttribute) {
		if(item == null && itemAttribute == null) {
			return new ItemStack(Material.BOOK_AND_QUILL);
		}
		if(item == null) {
			item = new ItemStack(Material.WRITTEN_BOOK);
		}
		if(itemAttribute == null) {
			return item;
		}
		if(item.getType() == Material.BOOK_AND_QUILL || item.getType() == Material.WRITTEN_BOOK) {
			BookMeta meta = (BookMeta) (item.hasItemMeta() ? item.getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(item.getType()));
			String author = itemAttribute[1];
			String title = itemAttribute[2];
			if(author != null) {
				meta.setAuthor((author.equals("<NO-AUTHOR>") == false ? InventoryAPI.convertSymbolsForLoading(author) : null));
			} else {
				item.setItemMeta(meta);
				return item;
			}
			if(title != null) {
				meta.setTitle((title.equals("<NO-TITLE>") == false ? InventoryAPI.convertSymbolsForLoading(title) : null));
			} else {
				item.setItemMeta(meta);
				return item;
			}
			for(int i = 3; i < itemAttribute.length;) {
				if(itemAttribute[i] != null) {
					meta.addPage(InventoryAPI.convertSymbolsForLoading(itemAttribute[i]));
				}
				i++;
			}
			item.setItemMeta(meta);
		}
		return item;
	}
	
	public static String serializeBanner(ItemStack item) {
		String rtrn = attribute_start + "banner" + attribute_separator;
		BannerMeta banner;
		if(item.getType() == Material.STANDING_BANNER || item.getType() == Material.WALL_BANNER) {
			banner = (BannerMeta) item.getItemMeta();
			DyeColor baseColor = banner.getBaseColor();
			List<Pattern> patterns = banner.getPatterns();
			rtrn += "BASECOLOR=" + baseColor.name();
			String serializedPatterns = attribute_start + "banner" + attribute_separator;
			boolean addedAny = false;
			for(Pattern p : patterns) {
				serializedPatterns += "PATTERN[";
				for(Entry<String, Object> entry : p.serialize().entrySet()) {
					serializedPatterns += convertSymbolsForSaving(entry.getKey() + "|" + entry.getValue().toString() + ",");
				}
				if(serializedPatterns.endsWith(",")) {
					serializedPatterns = serializedPatterns.substring(0, serializedPatterns.length() - 1);
				}
				serializedPatterns += "]";
				addedAny = true;
			}
			if(addedAny) {
				rtrn += serializedPatterns;
			}
		}
		return(rtrn.equals(attribute_start + "banner" + attribute_separator) ? "" : rtrn);
	}
	
	public static ItemStack deserializeBanner(ItemStack item, String[] itemAttribute) {
		if(item.getType() == Material.STANDING_BANNER || item.getType() == Material.WALL_BANNER && itemAttribute[0].equals("banner")) {
			final BannerMeta banner = (BannerMeta) item.getItemMeta();
			final String value = itemAttribute[1];
			if(value.startsWith("PATTERN[") && value.endsWith("]") && value.length() > 9) {
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
				}
			} else if(value.startsWith("BASECOLOR=") && value.length() > 10) {
				DyeColor color = DyeColor.valueOf(value.substring(10));
				if(color != null) {
					banner.setBaseColor(color);
				}
			}
			item.setItemMeta(banner);
		}
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static final String serializeSpawnEgg(ItemStack item) {
		if(item.getType() == Material.MONSTER_EGG) {
			EntityType type = getSpawnEggType(item);
			if(type == null) {
				type = EntityType.PIG;
			}
			return InventoryAPI.attribute_start + "spawnType" + InventoryAPI.attribute_separator + type.getName();
		}
		return "";
	}
	
	public static ItemStack deserializeSpawnEgg(ItemStack item, String[] itemAttribute) {
		/*boolean _1 = item == null;
		boolean _2 = _1 || item.getType() != Material.MONSTER_EGG;
		boolean _3 = itemAttribute == null;
		boolean _4 = _3 || itemAttribute.length != 2;
		boolean _5 = _3 || _4 || !itemAttribute[0].equalsIgnoreCase("spawnType");
		System.out.println("if(item == null || item.getType() != Material.MONSTER_EGG || itemAttribute == null || itemAttribute.length != 2 || !itemAttribute[0].equalsIgnoreCase(\"spawnType\"))");
		System.out.println("if(" + _1 + " || " + _2 + " || " + _3 + " || " + _4 + " || " + _5 + ")");
		*/
		if(item == null || item.getType() != Material.MONSTER_EGG || itemAttribute == null || itemAttribute.length != 2 || !itemAttribute[0].equalsIgnoreCase("spawnType")) {
			return item;
		}
		@SuppressWarnings("deprecation")
		EntityType type = EntityType.fromName(itemAttribute[1]);
		//ItemStack check = setSpawnEggType(item, type);
		//EntityType checkType = getSpawnEggType(check);
		//System.out.println("================ Original type: \"" + (type == null ? "null" : type.getName()) + "\"; New type: \"" + (checkType == null ? "null" : checkType.getName()) + "\"; =================");
		return setSpawnEggType(item, type);
	}
	
	/** Example usage: inv = setTitle("Hello, world!", inv);
	 * 
	 * @param str String
	 * @param inv Inventory
	 * @return The inv parameter with the new title.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a> */
	public static Inventory setTitle(String str, Inventory inv) {
		//MainInvClass.sendConsoleMessage("&eDebug: The inventory's size is: \"&f" + inv.getSize() + "&e\"!");
		Inventory newInv = Bukkit.getServer().createInventory(inv.getHolder(), inv.getSize(), str);
		newInv.setContents(inv.getContents());
		newInv.setMaxStackSize(inv.getMaxStackSize());//In case some of you out there need this to do this.
		return newInv;
	}
	
	/** Attempts to return the given experience and level serialized, as a
	 * single-line String.
	 * 
	 * @param level Integer
	 * @param exp float
	 * @return The serialized result, or a blank string if the given parameters
	 *         somehow had no value.
	 * @see {@link InventoryAPI#serializeExperience(Player)}<br>
	 *      {@link InventoryAPI#deserializeExperience(String)}<br>
	 *      {@link InventoryAPI#deserializeExp(String)}<br>
	 *      {@link InventoryAPI#deserializeLevel(String)} */
	public static String serializeExperience(int level, float exp) {
		String rtrn = "x@";
		rtrn += level + "@" + exp;
		return(rtrn.equals("x@") ? "" : (rtrn.equals("x@@") ? "" : rtrn));
	}
	
	/** Attempts to serialize the level and experience of the given Player as a
	 * single-line String.<br>
	 * This function uses {@link InventoryAPI#serializeExperience(int, float)}
	 * for deserialization.
	 * 
	 * @param player Player
	 * @return The serialized form of the given Player's experience and level.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#serializeExperience(int, float)}<br>
	 *      {@link InventoryAPI#deserializeExperience(String)}<br>
	 *      {@link InventoryAPI#deserializeExp(String)}<br>
	 *      {@link InventoryAPI#deserializeLevel(String)} */
	public static String serializeExperience(Player player) {
		return InventoryAPI.serializeExperience(player.getLevel(), player.getExp());
	}
	
	/** Attempts to return a String[] array containing the Integer level and the
	 * float experience, deserialized from the String parameter.
	 * 
	 * @param serializedExp String
	 * @return A String[] array containing the Integer level and the float
	 *         experience, deserialized from the String parameter. May return
	 *         values that are not an Integer or a float in the String[] array,
	 *         so it is recommended to either check the result of this function
	 *         or use one(or both) of the following functions instead:<br>
	 *         {@link InventoryAPI#deserializeExp(String)}<br>
	 *         {@link InventoryAPI#deserializeLevel(String)}
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#deserializeExp(String)}<br>
	 *      {@link InventoryAPI#deserializeLevel(String)}<br>
	 *      {@link InventoryAPI#serializeExperience(int, float)}<br>
	 *      {@link InventoryAPI#serializeExperience(Player)} */
	public static String[] deserializeExperience(String serializedExp) {
		return serializedExp.replace("x@", "").split("@");
	}
	
	/** Attempts to return the int value of the deserialized level.<br>
	 * This function uses {@link InventoryAPI#deserializeExperience(String)} for
	 * deserialization.
	 * 
	 * @param serializedExp String
	 * @return The int value of the deserialized level, or 0 if the
	 *         deserialization did not return a valid Integer.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#deserializeExperience(String)}
	 *      {@link InventoryAPI#deserializeExp(String)}
	 *      {@link InventoryAPI#serializeExperience(int, float)}
	 *      {@link InventoryAPI#serializeExperience(Player)} */
	public static int deserializeLevel(String serializedExp) {
		int rtrn = 0;
		int num = 0;
		for(String curNum : InventoryAPI.deserializeExperience(serializedExp)) {
			num++;
			boolean isInt = true;
			if(num == 1) {
				try {
					Integer.parseInt(curNum);
				} catch(NumberFormatException e) {
					isInt = false;
				}
				if(isInt) {
					rtrn = Integer.parseInt(curNum);
				}
			}
		}
		return rtrn;
	}
	
	/** Attempts to return the float value of the deserialized experience.<br>
	 * This function uses {@link InventoryAPI#deserializeExperience(String)} for
	 * deserialization.
	 * 
	 * @param serializedExp String
	 * @return The float value of the deserialized experience, or 0 if the
	 *         deserialization did not return a valid float.
	 * @author <a
	 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">
	 *         Brian_Entei</a>
	 * @see {@link InventoryAPI#deserializeExperience(String)}<br>
	 *      {@link InventoryAPI#deserializeLevel(String)}<br>
	 *      {@link InventoryAPI#serializeExperience(int, float)}<br>
	 *      {@link InventoryAPI#serializeExperience(Player)} */
	public static float deserializeExp(String serializedExp) {
		float rtrn = 0;
		int num = 0;
		for(String curNum : InventoryAPI.deserializeExperience(serializedExp)) {
			num++;
			boolean isFloat = true;
			if(num == 2) {
				try {
					Float.parseFloat(curNum);
				} catch(NumberFormatException e) {
					isFloat = false;
				}
				if(isFloat) {
					rtrn = Float.parseFloat(curNum);
				}
			}
		}
		return rtrn;
	}
	
	public static double[] deserializeHealth(String HealthString, Player player) {
		double[] rtrn = {20, 20};
		//This function makes it so that it can only return the health for one player, not any random player.
		
		String[] deserializedHealth = HealthString.replace(InventoryAPI.convertSymbolsForSaving(player.getName()), "").replace("#", "").replace(";", "").split("e@");
		
		rtrn[0] = Double.valueOf(deserializedHealth[0]).doubleValue();
		rtrn[1] = Double.valueOf(deserializedHealth[1]).doubleValue();
		
		Main.DEBUG("&6rtrn[0] = " + rtrn[0]);
		Main.DEBUG("&6rtrn[1] = " + rtrn[1]);
		
		return rtrn;
	}
	
	public static String serializeHealth(Player player) {
		return InventoryAPI.convertSymbolsForSaving(player.getName()) + "#" + player.getHealth() + "e@" + player.getMaxHealth() + ";";
	}
	
	public static String[] deserializeHunger(String HungerString, Player player) {
		return HungerString.replace(player.getName() + "#f", "").replace(";", "").split("@e");
	}
	
	public static String serializeHunger(Player player) {
		return player.getName() + "#f" + player.getFoodLevel() + "@e" + player.getExhaustion() + ";";
	}
	
	public static java.util.Collection<PotionEffect> deserializePotionEffects(String potionEffectString, Player player) {
		return InventoryAPI.deserializePotionEffects(potionEffectString, player.getName());
	}
	
	public static java.util.Collection<PotionEffect> deserializePotionEffects(String potionEffectString, String playerName) {
		java.util.Collection<PotionEffect> rtrn = new ArrayList<>();
		potionEffectString = potionEffectString.replace(InventoryAPI.convertSymbolsForSaving(playerName) + ";", "").replace(";", "");
		for(String serializedEffect : potionEffectString.split("e@")) {
			String effectName = serializedEffect.split(":d@")[0];
			String effectDuration = "";
			String effectAmplifier = "";
			for(String serializedEffectSplit : serializedEffect.split(":d@")) {
				if(serializedEffectSplit.equals(effectName) == false) {
					String[] getDurAndAmp = serializedEffectSplit.split(":a@");
					for(int i = 0; i < getDurAndAmp.length; i++) {
						//System.out.println("\"i\": " + i);
						//System.out.println("\"getDurAndAmp.length\": " + getDurAndAmp.length);
						
						//System.out.println("\"getDurAndAmp[0]\": " + getDurAndAmp[0]);
						//System.out.println("\"getDurAndAmp[1]\": " + getDurAndAmp[1]);
						if(i == 0) {
							if(getDurAndAmp[i].equals("") == false) {
								effectDuration = getDurAndAmp[i];
							}
						}
						if(i == 1) {
							if(getDurAndAmp[i].equals("") == false) {
								effectAmplifier = getDurAndAmp[i];
							}
						}
					}
				}
			}
			if(effectName.equals("") == false && effectDuration.equals("") == false && effectAmplifier.equals("") == false) {
				rtrn.add(new PotionEffect(PotionEffectType.getByName(effectName), Integer.valueOf(effectDuration).intValue(), Integer.valueOf(effectAmplifier).intValue()));
			}
		}
		return rtrn;
	}
	
	/** @param player
	 * @return */
	public static String serializePotionEffects(Player player) {
		java.util.Collection<PotionEffect> effects = player.getActivePotionEffects();
		String rtrn = InventoryAPI.convertSymbolsForSaving(player.getName()) + ";";
		for(PotionEffect effect : effects) {
			String effectName = effect.getType().getName();
			int duration = effect.getDuration();
			int amplify = effect.getAmplifier();
			rtrn += "e@" + effectName + ":d@" + duration + ":a@" + amplify;
		}
		rtrn += ";";
		return rtrn;
	}
	
	public static String serializePotionEffects(ArrayList<PotionEffect> effects, String playerName) {
		String rtrn = InventoryAPI.convertSymbolsForSaving(playerName) + ";";
		for(PotionEffect effect : effects) {
			String effectName = effect.getType().getName();
			int duration = effect.getDuration();
			int amplify = effect.getAmplifier();
			rtrn += "e@" + effectName + ":d@" + duration + ":a@" + amplify;
		}
		rtrn += ";";
		return rtrn;
	}
	
	private static final String quote(String s) {
		return java.util.regex.Pattern.quote(s);
	}
	
	public static final class InventoryData {
		
		private final ItemStack[]	items;
		private volatile int		maxStackSize;
		private final String		name;
		private final String		title;
		
		/** @param size The size of this inventory(set to 0 to use the type's
		 *            size)
		 * @param maxStackSize The max stack size(set to 0 to use the default)
		 * @param type The InventoryType(can be null if the size and
		 *            maxStackSize are set) */
		public InventoryData(int size, int maxStackSize, InventoryType type) {
			this(null, size, maxStackSize, type, null);
		}
		
		/** @param holder InventoryHolder, or null
		 * @param size The size of this inventory(set to 0 to use the type's
		 *            size)
		 * @param maxStackSize The max stack size(set to 0 to use the default)
		 * @param type The InventoryType(can be null if the size and
		 *            maxStackSize are set)
		 * @param title The Inventory's title(can be null) */
		public InventoryData(InventoryHolder holder, int size, int maxStackSize, InventoryType type, String title) {
			this.items = new ItemStack[size == 0 ? type.getDefaultSize() : size];
			this.maxStackSize = maxStackSize == 0 ? 64 : maxStackSize;
			this.name = type == null ? "container.inventory" : type.getDefaultTitle();
			this.title = title == null ? this.name : title;
		}
		
		/** @param inventory The Inventory to copy
		 * @return The resulting InventoryData */
		public static final InventoryData getFromInventory(Inventory inventory) {
			InventoryData inv = new InventoryData(inventory.getHolder(), (inventory instanceof PlayerInventory ? 36 : inventory.getSize()), inventory.getMaxStackSize(), inventory.getType(), inventory.getTitle());
			inv.setContents(inventory.getContents());
			return inv;
		}
		
		/** @return The size of this inventory. */
		public int getSize() {
			return this.items.length;
		}
		
		public final ItemStack[] getContents() {
			return StringUtil.resizeArray(ItemStack.class, this.items, 0, this.items.length);
		}
		
		public final InventoryData setContents(ItemStack[] contents) {
			for(int i = 0; i < contents.length && i < this.items.length; i++) {
				this.items[i] = contents[i];
			}
			return this;
		}
		
		public final int firstEmpty() {
			for(int i = 0; i < this.items.length; i++) {
				if(this.items[i] == null) {
					return i;
				}
			}
			return -1;
		}
		
		public final ItemStack getItem(int index) {
			return this.items[index];
		}
		
		/** @return The Inventory Name */
		public String getName() {
			return this.name;
		}
		
		/** @return The Inventory Title */
		public String getTitle() {
			return this.title;
		}
		
		/** @param index The slot to set
		 * @param item The itemstack to put
		 * @return The itemstack that was in the slot before being replaced */
		public final ItemStack setItem(int index, ItemStack item) {
			ItemStack rtrn = this.items[index];
			this.items[index] = item;
			return rtrn;
		}
		
		/** @return The maxStackSize */
		public int getMaxStackSize() {
			return this.maxStackSize;
		}
		
		/** @param maxStackSize The maxStackSize to set
		 * @return This InventoryData */
		public InventoryData setMaxStackSize(int maxStackSize) {
			this.maxStackSize = maxStackSize;
			return this;
		}
		
		/** Clears this InventoryData */
		public final void clear() {
			for(int i = 0; i < this.getSize(); i++) {
				this.items[i] = null;
			}
		}
		
		//=========
		
		public static final void test(Inventory inv) {
			InventoryData test = new InventoryData(null, 36, 64, InventoryType.PLAYER, "Brian_Entei's Creative Inventory");
			test.setItem(0, new ItemStack(Material.GLOWSTONE, 46));
			test.setItem(1, new ItemStack(Material.LAVA_BUCKET));
			test.setItem(27, new ItemStack(Material.SAPLING, 8));
			System.out.println(serializeInventory(test) + "\r\n");
			System.out.println(serializeInventory(inv));
		}
		
	}
	
	//==================
	
	private static Class<?>	CraftItemStack;	//org.bukkit.craftbukkit.%s.inventory.CraftItemStack
	private static Method	asNMSCopy;		//ItemStack
	private static Method	asBukkitCopy;	//Versioned ItemStack
	private static Class<?>	ItemStack1;		//net.minecraft.server.%s.ItemStack
	private static Method	getTag;			//Returns NBTTagCompound
	private static Method	setTag;			//NBTTagCompound
	private static Class<?>	NBTTagCompound;	//net.minecraft.server.%s.NBTTagCompound
	private static Method	setString;		//String, String
	private static Class<?>	NBTBase;		//net.minecraft.server.%s.NBTBase
	private static Method	set;			//String, NBTTagCompound
	private static Method	get;			//String
	private static Method	getString;		//String
	
	public static final void initialize() {
		try {
			setupReflection();
		} catch(NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	// Stores the reflected classes & methods in variable for access later
	private static final void setupReflection() throws NoSuchMethodException, SecurityException {
		CraftItemStack = VersionUtil.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
		asNMSCopy = CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
		ItemStack1 = VersionUtil.getNMSClass("net.minecraft.server.%s.ItemStack");
		asBukkitCopy = CraftItemStack.getDeclaredMethod("asBukkitCopy", ItemStack1);
		getTag = ItemStack1.getDeclaredMethod("getTag");
		NBTTagCompound = VersionUtil.getNMSClass("net.minecraft.server.%s.NBTTagCompound");
		setTag = ItemStack1.getDeclaredMethod("setTag", NBTTagCompound);
		setString = NBTTagCompound.getDeclaredMethod("setString", String.class, String.class);
		NBTBase = VersionUtil.getNMSClass("net.minecraft.server.%s.NBTBase");
		set = NBTTagCompound.getDeclaredMethod("set", String.class, NBTBase);
		get = NBTTagCompound.getDeclaredMethod("get", String.class);
		getString = NBTTagCompound.getDeclaredMethod("getString", String.class);
	}
	
	@SuppressWarnings("deprecation")
	private static final ItemStack getSpawnEggUsingReflection(EntityType type, int amount) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
		//For 1.8 and below:
		item.setDurability(type.getTypeId());
		//For 1.9 and above:
		Object stack = asNMSCopy.invoke(CraftItemStack, item);//net.minecraft.server.%s.ItemStack stack = org.bukkit.craftbukkit.%s.inventory.CraftItemStack.asNMSCopy(item);
		Object tagCompound = getTag.invoke(stack);//net.minecraft.server.%s.NBTTagCompound tagCompound = stack.getTag();
		if(tagCompound == null) {
			tagCompound = NBTTagCompound.newInstance();//tagCompound = new net.minecraft.server.%s.NBTTagCompound();
		}
		Object id = NBTTagCompound.newInstance();//net.minecraft.server.%s.NBTTagCompound id = new net.minecraft.server.%s.NBTTagCompound();
		setString.invoke(id, "id", type.getName());//id.setString("id", type.getName());
		set.invoke(tagCompound, "EntityTag", id);//tagCompound.set("EntityTag", id);
		setTag.invoke(stack, tagCompound);//stack.setTag(tagCompound);
		return (ItemStack) asBukkitCopy.invoke(CraftItemStack, stack);//return org.bukkit.craftbukkit.%s.inventory.CraftItemStack.asBukkitCopy(stack);
	}
	
	@SuppressWarnings("deprecation")
	private static final ItemStack setSpawnEggTypeUsingReflection(ItemStack item, EntityType type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		//For 1.8 and below:
		item.setDurability(type.getTypeId());
		//For 1.9 and above:
		Object stack = asNMSCopy.invoke(CraftItemStack, item);//net.minecraft.server.%s.ItemStack stack = org.bukkit.craftbukkit.%s.inventory.CraftItemStack.asNMSCopy(item);
		Object tagCompound = getTag.invoke(stack);//net.minecraft.server.%s.NBTTagCompound tagCompound = stack.getTag();
		if(tagCompound == null) {
			tagCompound = NBTTagCompound.newInstance();//tagCompound = new net.minecraft.server.%s.NBTTagCompound();
		}
		Object id = NBTTagCompound.newInstance();//net.minecraft.server.%s.NBTTagCompound id = new net.minecraft.server.%s.NBTTagCompound();
		setString.invoke(id, "id", type.getName());//id.setString("id", type.getName());
		set.invoke(tagCompound, "EntityTag", id);//tagCompound.set("EntityTag", id);
		setTag.invoke(stack, tagCompound);//stack.setTag(tagCompound);
		return (ItemStack) asBukkitCopy.invoke(CraftItemStack, stack);//return item;
	}
	
	public static final ItemStack setSpawnEggType(ItemStack item, EntityType type) {
		try {
			return setSpawnEggTypeUsingReflection(item, type);
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | ClassCastException e) {
			e.printStackTrace();
			return item;
		}
	}
	
	@SuppressWarnings("deprecation")
	private static final EntityType getSpawnEggTypeUsingReflection(ItemStack item) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//For 1.8 and below:
		EntityType rtrn = null;
		if(item.getType() == Material.MONSTER_EGG) {
			SpawnEgg egg = (SpawnEgg) item.getData();
			rtrn = egg.getSpawnedType();
		}
		if(rtrn != null) {
			return rtrn;
		}
		//For 1.9 and above:
		Object stack = asNMSCopy.invoke(CraftItemStack, item);//net.minecraft.server.%s.ItemStack stack = org.bukkit.craftbukkit.%s.inventory.CraftItemStack.asNMSCopy(item);
		Object tagCompound = getTag.invoke(stack);//net.minecraft.server.%s.NBTTagCompound tagCompound = stack.getTag();
		if(tagCompound == null) {
			return rtrn;
		}
		//
		Object idTag = get.invoke(tagCompound, "EntityTag");
		Object typeStr = getString.invoke(idTag, "id");
		if(typeStr instanceof String) {
			String type = (String) typeStr;
			//System.out.println("===================== Type: \"" + type + "\" =======================");
			rtrn = EntityType.fromName(type);
		}
		return rtrn;
	}
	
	public static final EntityType getSpawnEggType(ItemStack item) {
		try {
			return getSpawnEggTypeUsingReflection(item);
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static final ItemStack getSpawnEgg(EntityType type, int amount) {
		try {
			return getSpawnEggUsingReflection(type, amount);
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | ClassCastException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*@SuppressWarnings("deprecation")
	public static final ItemStack getSpawnEgg(EntityType type, int amount) {
		ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
		net.minecraft.server.v1_10_R1.ItemStack stack = org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack.asNMSCopy(item);
		net.minecraft.server.v1_10_R1.NBTTagCompound tagCompound = stack.getTag();
		if(tagCompound == null) {
			tagCompound = new net.minecraft.server.v1_10_R1.NBTTagCompound();
		}
		net.minecraft.server.v1_10_R1.NBTTagCompound id = new net.minecraft.server.v1_10_R1.NBTTagCompound();
		id.setString("id", type.getName());
		tagCompound.set("EntityTag", id);
		stack.setTag(tagCompound);
		return org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack.asBukkitCopy(stack);//return item;
	}*/
	
}
