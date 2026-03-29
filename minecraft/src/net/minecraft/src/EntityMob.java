package net.minecraft.src;

public class EntityMob extends EntityCreature implements IMob {
	protected int attackStrength = 2;
	protected boolean redWaveMob = false;
	protected boolean redWaveCreeper = false;
	private int siegeBreakTime = 0;

	public EntityMob(World var1) {
		super(var1);
		this.health = 20;
	}

	public void onLivingUpdate() {
		float var1 = this.getEntityBrightness(1.0F);
		if(var1 > 0.5F) {
			this.entityAge += 2;
		}

		if(this.redWaveMob) {
			EntityPlayer var2 = this.worldObj.getClosestPlayerToEntity(this, 96.0D);
			if(var2 != null) {
				this.playerToAttack = var2;
			}

			if(this.siegeBreakTime > 0) {
				--this.siegeBreakTime;
			}

			if(this.siegeBreakTime <= 0) {
				ChunkCoordinates var3 = RedWaveSystem.getSiegeBreakTarget(this, this.playerToAttack);
				if(var3 != null) {
					int var4 = this.worldObj.getBlockId(var3.x, var3.y, var3.z);
					if(RedWaveSystem.isBreakable(var4)) {
						this.worldObj.func_28106_e(2001, var3.x, var3.y, var3.z, var4);
						this.worldObj.setBlockWithNotify(var3.x, var3.y, var3.z, 0);
						if(this.redWaveCreeper && this.rand.nextInt(8) == 0) {
							this.worldObj.createExplosion(this, (double)var3.x + 0.5D, (double)var3.y + 0.5D, (double)var3.z + 0.5D, 1.6F);
						}

						this.siegeBreakTime = this.redWaveCreeper ? 7 : 24;
					}
				}
			}
		}

		super.onLivingUpdate();
	}

	public void onUpdate() {
		super.onUpdate();
		if(!this.worldObj.multiplayerWorld && this.worldObj.difficultySetting == 0) {
			this.setEntityDead();
		}

	}

	protected Entity findPlayerToAttack() {
		if(this.redWaveMob) {
			return this.worldObj.getClosestPlayerToEntity(this, 96.0D);
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
		var1.setBoolean("RedWaveMob", this.redWaveMob);
		var1.setBoolean("RedWaveCreeper", this.redWaveCreeper);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		this.redWaveMob = var1.getBoolean("RedWaveMob");
		this.redWaveCreeper = var1.getBoolean("RedWaveCreeper");
		if(this.redWaveCreeper) {
			this.moveSpeed = 0.95F;
		}
	}

	public boolean getCanSpawnHere() {
		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.boundingBox.minY);
		int var3 = MathHelper.floor_double(this.posZ);
		if(this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, var1, var2, var3) > this.rand.nextInt(32)) {
			return false;
		} else {
			int var4 = this.worldObj.getBlockLightValue(var1, var2, var3);
			if(this.worldObj.func_27160_B()) {
				int var5 = this.worldObj.skylightSubtracted;
				this.worldObj.skylightSubtracted = 10;
				var4 = this.worldObj.getBlockLightValue(var1, var2, var3);
				this.worldObj.skylightSubtracted = var5;
			}

			return var4 <= this.rand.nextInt(8) && super.getCanSpawnHere();
		}
	}

	public void setRedWaveMob(boolean var1) {
		this.redWaveMob = true;
		this.redWaveCreeper = var1;
		if(var1) {
			this.moveSpeed = 0.95F;
		}
	}

	public boolean isRedWaveMob() {
		return this.redWaveMob;
	}

	public boolean isRedWaveCreeper() {
		return this.redWaveCreeper;
	}
}
