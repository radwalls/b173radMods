package net.minecraft.src;

public abstract class EntityRedWaveMob extends EntityMob implements IRedWaveMob {
	private int breachTicks;

	public EntityRedWaveMob(World var1) {
		super(var1);
	}

	protected Entity findPlayerToAttack() {
		EntityPlayer var1 = this.worldObj.getClosestPlayerToEntity(this, 128.0D);
		return var1 != null && var1.isEntityAlive() ? var1 : null;
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();
		if(this.fire > 0) {
			this.fire = 0;
		}

		this.updateBlockBreaching();
	}

	protected void updatePlayerActionState() {
		super.updatePlayerActionState();
		if(this.ticksExisted % 30 == 0 && this.worldObj.getClosestPlayerToEntity(this, 128.0D) != null) {
			EntityPlayer var1 = this.worldObj.getClosestPlayerToEntity(this, 128.0D);
			RedWaveStructureTracker var2 = this.worldObj.getRedWaveStructureTracker();
			ChunkCoordinates var3 = var2.findBestStructureTargetNear(var1, 192);
			if(var3 != null && this.getDistanceSq((double)var3.posX, this.posY, (double)var3.posZ) > 100.0D) {
				PathEntity var4 = this.worldObj.getEntityPathToXYZ(this, var3.posX, MathHelper.floor_double(this.posY), var3.posZ, 48.0F);
				if(var4 != null) {
					this.setPathToEntity(var4);
				}
			}
		}
	}

	private void updateBlockBreaching() {
		if(this.isCollidedHorizontally && this.playerToAttack != null) {
			++this.breachTicks;
			if(this.breachTicks >= this.getBreachingDelay()) {
				this.breachTicks = 0;
				int var1 = MathHelper.floor_double(this.posX + (double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * 0.8F));
				int var2 = MathHelper.floor_double(this.boundingBox.minY + 1.0D);
				int var3 = MathHelper.floor_double(this.posZ + (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * 0.8F));
				int var4 = this.worldObj.getBlockId(var1, var2, var3);
				if(var4 > 0 && this.canBreakBlock(var4)) {
					this.worldObj.func_28101_a((EntityPlayer)null, 2001, var1, var2, var3, var4 + this.worldObj.getBlockMetadata(var1, var2, var3) * 256);
					this.worldObj.setBlockWithNotify(var1, var2, var3, 0);
				}
			}
		} else {
			this.breachTicks = 0;
		}
	}

	protected int getBreachingDelay() {
		return 45;
	}

	protected boolean canBreakBlock(int var1) {
		Block var2 = Block.blocksList[var1];
		return var2 != null && var2.blockHardness >= 0.0F && var2.blockHardness <= 2.5F && var1 != Block.bedrock.blockID && var1 != Block.obsidian.blockID;
	}

	public boolean getCanSpawnHere() {
		return super.getCanSpawnHere();
	}
}
