package net.minecraft.src;

public class ItemSpyglass extends Item {
	public ItemSpyglass(int var1) {
		super(var1);
		this.setMaxStackSize(1);
		this.setFull3D();
	}

	public ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3) {
		if(!var2.multiplayerWorld) {
			var3.setSpyglassActive(!var3.isUsingSpyglass());
		}

		return var1;
	}
}
