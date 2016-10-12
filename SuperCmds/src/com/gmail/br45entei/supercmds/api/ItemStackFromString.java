package com.gmail.br45entei.supercmds.api;

import com.gmail.br45entei.util.StringUtil;
import com.google.common.base.Joiner;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class ItemStackFromString {
	
	@SuppressWarnings("deprecation")
	public static final ItemResult getItemStackFromString(final String[] args) {
		Material material = Material.matchMaterial(args[0]);
		
		if(material == null) {
			material = Bukkit.getUnsafe().getMaterialFromInternalName(args[0]);
		}
		
		if(material != null) {
			int amount = 1;
			short data = 0;
			
			if(args.length >= 2) {
				amount = 1;
				
				if(StringUtil.isStrInt(args[1])) {
					amount = Integer.valueOf(args[1]).intValue();
				} else {
					return new ItemResult("&eNot a valid integer: \"&f" + args[1] + "&r&e\".");
				}
				
				if(amount < 1) {
					amount = 1;
				} else if(amount > material.getMaxStackSize()) {
					amount = material.getMaxStackSize();
				}
				if(args.length >= 4) {
					try {
						data = Short.parseShort(args[2]);
					} catch(NumberFormatException ex) {
					}
				}
			}
			
			ItemStack stack = new ItemStack(material, amount, data);
			
			if(args.length >= 4) {
				try {
					stack = Bukkit.getUnsafe().modifyItemStack(stack, Joiner.on(' ').join(Arrays.asList(args).subList(3, args.length)));
				} catch(Throwable t) {
					return new ItemResult("&eNot a valid tag: \"&f" + StringUtil.stringArrayToString(args, ' ', 3) + "&r&e\".");
				}
			}
			return new ItemResult(null, stack);
		}
		return new ItemResult("&eThere's no item called \"&f" + args[0] + "&r&e\".");
	}
	
	public static final class ItemResult {
		
		public final String		statusMessage;
		public final ItemStack	item;
		
		public ItemResult(String errorMessage) {
			this.statusMessage = errorMessage;
			this.item = null;
		}
		
		public ItemResult(String statusMessage, ItemStack item) {
			this.statusMessage = statusMessage;
			this.item = item;
		}
		
	}
	
}
