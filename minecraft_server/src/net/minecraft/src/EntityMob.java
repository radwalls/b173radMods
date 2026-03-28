package net.minecraft.src;

public class EntityMob extends EntityCreature implements IMob {
	protected int attackStrength = 2;
	private boolean waveMob = false;
	private int siegeTargetX;
	private int siegeTargetY;
	private int siegeTargetZ;
	private int blockBreakProgress;

	public EntityMob(World var1) {
		super(var1);
		this.health = 20;
	}

	public void onLivingUpdate() {
		float var1 = this.getEntityBrightness(1.0F);
		if(var1 > 0.5F) {
			this.age += 2;
		}

		super.onLivingUpdate();
		if(this.waveMob && !this.worldObj.singleplayerWorld) {
			this.updateWaveSiegeBehavior();
		}
	}

	public void onUpdate() {
		super.onUpdate();
		if(!this.worldObj.singleplayerWorld && this.worldObj.difficultySetting == 0) {
			this.setEntityDead();
		}

	}

	protected Entity findPlayerToAttack() {
		double var1 = this.waveMob ? 128.0D : 16.0D;
		EntityPlayer var3 = this.worldObj.getClosestPlayerToEntity(this, var1);
		if(this.waveMob) {
			return var3;
		} else {
			return var3 != null && this.canEntityBeSeen(var3) ? var3 : null;
		}
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

	protected void func_28013_b(Entity var1, float var2) {
		if(this.waveMob) {
			this.attemptBreakNearbyPlayerBlock();
		}
	}

	protected float getTargetSearchDistance() {
		return this.waveMob ? 96.0F : super.getTargetSearchDistance();
	}

	protected float getBlockPathWeight(int var1, int var2, int var3) {
		return 0.5F - this.worldObj.getLightBrightness(var1, var2, var3);
	}

	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
		var1.setBoolean("WaveMob", this.waveMob);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		this.waveMob = var1.getBoolean("WaveMob");
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

	public boolean isWaveMob() {
		return this.waveMob;
	}

	public void setWaveMob(boolean var1) {
		this.waveMob = var1;
	}

	private void updateWaveSiegeBehavior() {
		if(this.worldObj.rand.nextInt(30) == 0) {
			ChunkPosition var1 = this.worldObj.findBestWaveTarget(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 96);
			if(var1 != null) {
				this.siegeTargetX = var1.x;
				this.siegeTargetY = var1.y;
				this.siegeTargetZ = var1.z;
				this.setPathToEntity(this.worldObj.getEntityPathToXYZ(this, this.siegeTargetX, this.siegeTargetY, this.siegeTargetZ, 96.0F));
			}
		}

		if(this.siegeTargetY > 0 && this.getDistanceSq((double)this.siegeTargetX + 0.5D, (double)this.siegeTargetY, (double)this.siegeTargetZ + 0.5D) < 9.0D) {
			this.tryBreakBlock(this.siegeTargetX, this.siegeTargetY, this.siegeTargetZ);
		}

		if(this.isCollidedHorizontally || this.worldObj.rand.nextInt(20) == 0) {
			this.attemptBreakNearbyPlayerBlock();
		}
	}

	private void attemptBreakNearbyPlayerBlock() {
		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.boundingBox.minY + 0.5D);
		int var3 = MathHelper.floor_double(this.posZ);

		for(int var4 = -1; var4 <= 1; ++var4) {
			for(int var5 = -1; var5 <= 1; ++var5) {
				for(int var6 = 0; var6 <= 1; ++var6) {
					int var7 = var1 + var4;
					int var8 = var2 + var6;
					int var9 = var3 + var5;
					if(this.worldObj.isPlayerPlacedBlock(var7, var8, var9)) {
						this.tryBreakBlock(var7, var8, var9);
						return;
					}
				}
			}
		}
	}

	private void tryBreakBlock(int var1, int var2, int var3) {
		int var4 = this.worldObj.getBlockId(var1, var2, var3);
		if(var4 != 0 && this.worldObj.isPlayerPlacedBlock(var1, var2, var3)) {
			float var5 = Block.blocksList[var4].blockHardness;
			if(var5 < 0.0F) {
				return;
			}

			int var6 = this instanceof EntityCreeper ? 25 : 120 + (int)(var5 * 45.0F);
			++this.blockBreakProgress;
			if(this.blockBreakProgress >= var6) {
				this.worldObj.setBlockWithNotify(var1, var2, var3, 0);
				this.worldObj.func_28097_e(2001, var1, var2, var3, var4);
				this.blockBreakProgress = 0;
			}
		} else {
			this.blockBreakProgress = 0;
		}
	}
}
