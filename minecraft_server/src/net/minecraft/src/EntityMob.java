package net.minecraft.src;

public class EntityMob extends EntityCreature implements IMob {
	protected int attackStrength = 2;
	private int blockBreakProgress = 0;
	private ChunkPosition waveBlockTarget;
	private static final int WAVE_MOB_WATCHER_INDEX = 30;

	public EntityMob(World var1) {
		super(var1);
		this.health = 20;
	}

	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(WAVE_MOB_WATCHER_INDEX, Byte.valueOf((byte)0));
	}

	public void onLivingUpdate() {
		float var1 = this.getEntityBrightness(1.0F);
		if(var1 > 0.5F) {
			this.age += 2;
		}
		if(this.isWaveMob()) {
			this.updateWaveTargetingAndBlockBreaking();
		}

		super.onLivingUpdate();
	}

	public void onUpdate() {
		super.onUpdate();
		if(!this.worldObj.singleplayerWorld && this.worldObj.difficultySetting == 0) {
			this.setEntityDead();
		}

	}

	protected Entity findPlayerToAttack() {
		if(this.isWaveMob()) {
			return this.worldObj.getClosestPlayerToEntity(this, 256.0D);
		}

		EntityPlayer var1 = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
		return var1 != null && this.canEntityBeSeen(var1) ? var1 : null;
	}

	public boolean attackEntityFrom(Entity var1, int var2) {
		if(super.attackEntityFrom(var1, var2)) {
			if(this.riddenByEntity != var1 && this.ridingEntity != var1) {
				if(var1 != this) {
					this.playerToAttack = var1;
				}

				return true;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	protected void attackEntity(Entity var1, float var2) {
		if(this.attackTime <= 0 && var2 < 2.0F && var1.boundingBox.maxY > this.boundingBox.minY && var1.boundingBox.minY < this.boundingBox.maxY) {
			this.attackTime = 20;
			var1.attackEntityFrom(this, this.attackStrength);
		}

	}

	protected float getBlockPathWeight(int var1, int var2, int var3) {
		return 0.5F - this.worldObj.getLightBrightness(var1, var2, var3);
	}

	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
		var1.setBoolean("WaveMob", this.isWaveMob());
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		this.setWaveMob(var1.getBoolean("WaveMob"));
	}

	public boolean getCanSpawnHere() {
		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.boundingBox.minY);
		int var3 = MathHelper.floor_double(this.posZ);
		if(this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, var1, var2, var3) > this.rand.nextInt(32)) {
			return false;
		} else {
			int var4 = this.worldObj.getBlockLightValue(var1, var2, var3);
			if(this.worldObj.func_27067_u()) {
				int var5 = this.worldObj.skylightSubtracted;
				this.worldObj.skylightSubtracted = 10;
				var4 = this.worldObj.getBlockLightValue(var1, var2, var3);
				this.worldObj.skylightSubtracted = var5;
			}

			return var4 <= this.rand.nextInt(8) && super.getCanSpawnHere();
		}
	}

	public void setWaveMob(boolean var1) {
		this.dataWatcher.updateObject(WAVE_MOB_WATCHER_INDEX, Byte.valueOf((byte)(var1 ? 1 : 0)));
	}

	public boolean isWaveMob() {
		return this.dataWatcher.getWatchableObjectByte(WAVE_MOB_WATCHER_INDEX) == 1;
	}

	private void updateWaveTargetingAndBlockBreaking() {
		EntityPlayer var1 = this.worldObj.getClosestPlayerToEntity(this, 256.0D);
		if(var1 != null) {
			this.playerToAttack = var1;
		}

		World var2 = this.worldObj;
		int var3 = MathHelper.floor_double(this.posX);
		int var4 = MathHelper.floor_double(this.posY);
		int var5 = MathHelper.floor_double(this.posZ);
		if((this.waveBlockTarget == null || this.rand.nextInt(20) == 0) && var2 instanceof World) {
			this.waveBlockTarget = ((World)var2).getBestWaveBlockTarget(var3, var4, var5, 96);
		}

		if(this.waveBlockTarget != null && this.rand.nextInt(10) == 0) {
			PathEntity var6 = var2.getEntityPathToXYZ(this, this.waveBlockTarget.x, this.waveBlockTarget.y, this.waveBlockTarget.z, 64.0F);
			if(var6 != null) {
				this.setPathToEntity(var6);
			}
		}

		ChunkPosition var7 = ((World)var2).getNearestPlayerPlacedBlock(var3, var4, var5, 3);
		if(var7 != null && this.canBreakBlock(var7.x, var7.y, var7.z)) {
			++this.blockBreakProgress;
			if(this.blockBreakProgress >= this.getWaveBlockBreakInterval()) {
				int var8 = var2.getBlockId(var7.x, var7.y, var7.z);
				var2.setBlockWithNotify(var7.x, var7.y, var7.z, 0);
				var2.func_28097_e(2001, var7.x, var7.y, var7.z, var8);
				this.blockBreakProgress = 0;
			}
		} else {
			this.blockBreakProgress = 0;
		}
	}

	protected int getWaveBlockBreakInterval() {
		return 90;
	}

	private boolean canBreakBlock(int var1, int var2, int var3) {
		int var4 = this.worldObj.getBlockId(var1, var2, var3);
		return var4 != 0 && var4 != Block.bedrock.blockID;
	}
}
