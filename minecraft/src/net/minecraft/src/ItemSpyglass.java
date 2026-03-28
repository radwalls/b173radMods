package net.minecraft.src;

import org.lwjgl.input.Mouse;

public class ItemSpyglass extends Item {
	public ItemSpyglass(int var1) {
		super(var1);
		this.setMaxStackSize(1);
		this.setFull3D();
	}

	public ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3) {
		return var1;
	}

	public static boolean isUsingSpyglass(EntityPlayer var0) {
		if(var0 == null) {
			return false;
		} else {
			ItemStack var1 = var0.inventory.getCurrentItem();
			return var1 != null && var1.getItem() == Item.spyglass && Mouse.isButtonDown(1);
		}
	}
}
