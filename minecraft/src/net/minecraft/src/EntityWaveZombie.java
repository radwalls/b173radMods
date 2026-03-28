package net.minecraft.src;

public class EntityWaveZombie extends EntityZombie implements IWaveMob {
	private int blockBreakProgress;
	private ChunkPosition targetStructure;

	public EntityWaveZombie(World var1) {
		super(var1);
		this.moveSpeed = 0.65F;
		this.attackStrength = 6;
	}

	protected Entity findPlayerToAttack() {
		return this.worldObj.getClosestPlayerToEntity(this, 256.0D);
	}

	protected void updatePlayerActionState() {
		if(this.ticksExisted % 40 == 0) {
			this.targetStructure = this.worldObj.findBestPlayerStructureTarget(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 128);
			if(this.targetStructure != null) {
				PathEntity var1 = this.worldObj.getEntityPathToXYZ(this, this.targetStructure.x, this.targetStructure.y, this.targetStructure.z, 64.0F);
				if(var1 != null) {
					this.setPathToEntity(var1);
				}
			}
		}

		super.updatePlayerActionState();
		this.tryBreakPlayerBlock(80);
	}

	private void tryBreakPlayerBlock(int var1) {
		int var2 = MathHelper.floor_double(this.posX);
		int var3 = MathHelper.floor_double(this.boundingBox.minY + 0.5D);
		int var4 = MathHelper.floor_double(this.posZ);

		for(int var5 = -1; var5 <= 1; ++var5) {
			for(int var6 = -1; var6 <= 1; ++var6) {
				for(int var7 = -1; var7 <= 1; ++var7) {
					int var8 = var2 + var5;
					int var9 = var3 + var6;
					int var10 = var4 + var7;
					int var11 = this.worldObj.getBlockId(var8, var9, var10);
					if(var11 != 0 && this.worldObj.isPlayerPlacedBlock(var8, var9, var10)) {
						++this.blockBreakProgress;
						if(this.blockBreakProgress >= var1) {
							this.worldObj.func_28106_e(2001, var8, var9, var10, var11);
							this.worldObj.setBlockWithNotify(var8, var9, var10, 0);
							this.blockBreakProgress = 0;
						}

						return;
					}
				}
			}
		}

		this.blockBreakProgress = 0;
	}
}
