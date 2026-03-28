package net.minecraft.src;

public class EntitySeagull extends EntityBird {
	public EntitySeagull(World var1) {
		super(var1);
		this.texture = "/mob/chicken.png";
		this.health = 4;
	}

	public boolean getCanSpawnHere() {
		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.boundingBox.minY);
		int var3 = MathHelper.floor_double(this.posZ);
		if(this.worldObj.getBlockId(var1, var2 - 1, var3) != Block.sand.blockID) {
			return false;
		}

		boolean var4 = false;

		for(int var5 = -6; var5 <= 6 && !var4; ++var5) {
			for(int var6 = -6; var6 <= 6; ++var6) {
				if(this.worldObj.getBlockMaterial(var1 + var5, var2 - 1, var3 + var6) == Material.water) {
					var4 = true;
					break;
				}
			}
		}

		return var4 && super.getCanSpawnHere();
	}
}
