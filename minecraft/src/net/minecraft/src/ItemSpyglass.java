package net.minecraft.src;

public class ItemSpyglass extends Item {
	public ItemSpyglass(int var1) {
		super(var1);
		this.maxStackSize = 1;
	}

	public ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3) {
		var3.setSpyglassUseTicks(8);
		return var1;
	}
}
