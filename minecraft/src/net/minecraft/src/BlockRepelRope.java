package net.minecraft.src;

import java.util.Random;

public class BlockRepelRope extends BlockLadder {
	protected BlockRepelRope(int var1, int var2) {
		super(var1, var2);
	}

	public boolean canPlaceBlockAt(World var1, int var2, int var3, int var4) {
		return super.canPlaceBlockAt(var1, var2, var3, var4) ? true : var1.getBlockId(var2, var3 + 1, var4) == this.blockID;
	}

	public void onNeighborBlockChange(World var1, int var2, int var3, int var4, int var5) {
		int var6 = var1.getBlockMetadata(var2, var3, var4);
		boolean var7 = false;
		if(var6 == 2 && var1.isBlockNormalCube(var2, var3, var4 + 1)) {
			var7 = true;
		}

		if(var6 == 3 && var1.isBlockNormalCube(var2, var3, var4 - 1)) {
			var7 = true;
		}

		if(var6 == 4 && var1.isBlockNormalCube(var2 + 1, var3, var4)) {
			var7 = true;
		}

		if(var6 == 5 && var1.isBlockNormalCube(var2 - 1, var3, var4)) {
			var7 = true;
		}

		if(!var7 && var1.getBlockId(var2, var3 + 1, var4) == this.blockID) {
			var7 = true;
		}

		if(!var7) {
			var1.setBlockWithNotify(var2, var3, var4, 0);
		}
	}

	public int quantityDropped(Random var1) {
		return 0;
	}
}
