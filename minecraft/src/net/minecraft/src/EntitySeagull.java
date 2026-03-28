package net.minecraft.src;

public class EntitySeagull extends EntityBirdBase {
	public EntitySeagull(World var1) {
		super(var1);
		this.texture = "/mob/chicken.png";
		this.setSize(0.35F, 0.45F);
	}

	public boolean getCanSpawnHere() {
		if(!super.getCanSpawnHere()) {
			return false;
		}

		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.boundingBox.minY) - 1;
		int var3 = MathHelper.floor_double(this.posZ);
		if(this.worldObj.getBlockId(var1, var2, var3) != Block.sand.blockID) {
			return false;
		}

		for(int var4 = -4; var4 <= 4; ++var4) {
			for(int var5 = -4; var5 <= 4; ++var5) {
				Material var6 = this.worldObj.getBlockMaterial(var1 + var4, var2, var3 + var5);
				if(var6 == Material.water) {
					return true;
				}
			}
		}

		return false;
	}
}
