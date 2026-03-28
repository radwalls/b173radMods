package net.minecraft.src;

public class EntityMob extends EntityCreature implements IMob {
	protected int attackStrength = 2;
	private boolean isRedWaveMob;
	private int redWaveLevel;
	private int redWaveBreakProgress;
	private int redWaveBreakX;
	private int redWaveBreakY;
	private int redWaveBreakZ;
	private int redWaveBreakNeed;

	public EntityMob(World var1) {
		super(var1);
		this.health = 20;
	}

	public void onLivingUpdate() {
		float var1 = this.getEntityBrightness(1.0F);
		if(var1 > 0.5F) {
			this.entityAge += 2;
		}

		super.onLivingUpdate();
	}

	public void onUpdate() {
		super.onUpdate();
		if(!this.worldObj.multiplayerWorld && this.worldObj.difficultySetting == 0) {
			this.setEntityDead();
		}

		if(this.isRedWaveMob) {
			this.updateRedWaveSiege();
		}
	}

	protected Entity findPlayerToAttack() {
		if(this.isRedWaveMob) {
			EntityPlayer var2 = this.worldObj.getClosestPlayerToEntity(this, 128.0D);
			return var2 != null ? var2 : this.worldObj.getClosestPlayerToEntity(this, 256.0D);
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
		var1.setBoolean("RedWaveMob", this.isRedWaveMob);
		var1.setInteger("RedWaveLvl", this.redWaveLevel);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		if(var1.getBoolean("RedWaveMob")) {
			this.enableRedWaveMob(var1.getInteger("RedWaveLvl"));
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

	public void enableRedWaveMob(int var1) {
		this.isRedWaveMob = true;
		this.redWaveLevel = var1 < 1 ? 1 : var1;
		this.entityAge = 0;
	}

	public boolean isRedWaveMob() {
		return this.isRedWaveMob;
	}

	private void updateRedWaveSiege() {
		if(this.ticksExisted % 20 == 0) {
			EntityPlayer var1 = this.worldObj.getClosestPlayerToEntity(this, 256.0D);
			if(var1 != null) {
				this.playerToAttack = var1;
			}
		}

		if(this.playerToAttack instanceof EntityPlayer && this.ticksExisted % 10 == 0) {
			EntityPlayer var10 = (EntityPlayer)this.playerToAttack;
			ChunkPosition var2 = this.worldObj.getWaveSiegeTarget(var10, 64);
			if(var2 != null) {
				this.setPathToEntity(this.worldObj.getEntityPathToXYZ(this, var2.x, var2.y, var2.z, 24.0F));
				double var3 = this.getDistanceSq((double)var2.x + 0.5D, (double)var2.y + 0.5D, (double)var2.z + 0.5D);
				if(var3 < 6.25D) {
					this.hitStructureBlock(var2.x, var2.y, var2.z);
				}
			}
		}
	}

	private void hitStructureBlock(int var1, int var2, int var3) {
		int var4 = this.worldObj.getBlockId(var1, var2, var3);
		if(var4 == 0) {
			this.worldObj.unregisterPlacedBlock(var1, var2, var3);
			return;
		}

		if(this.redWaveBreakX != var1 || this.redWaveBreakY != var2 || this.redWaveBreakZ != var3) {
			this.redWaveBreakX = var1;
			this.redWaveBreakY = var2;
			this.redWaveBreakZ = var3;
			this.redWaveBreakProgress = 0;
			Block var5 = Block.blocksList[var4];
			float var6 = var5 != null ? var5.getHardness() : 1.0F;
			if(var6 < 0.0F) {
				this.redWaveBreakNeed = 999999;
			} else {
				int var7 = this instanceof EntityCreeper ? 7 : 2;
				this.redWaveBreakNeed = 25 + (int)(var6 * 18.0F) - this.redWaveLevel * var7;
				if(this.redWaveBreakNeed < (this instanceof EntityCreeper ? 4 : 12)) {
					this.redWaveBreakNeed = this instanceof EntityCreeper ? 4 : 12;
				}
			}
		}

		++this.redWaveBreakProgress;
		if(this.redWaveBreakProgress >= this.redWaveBreakNeed) {
			if(this.worldObj.setBlockWithNotify(var1, var2, var3, 0)) {
				this.worldObj.func_28106_e(2001, var1, var2, var3, var4);
				this.worldObj.unregisterPlacedBlock(var1, var2, var3);
			}

			this.redWaveBreakProgress = 0;
		}
	}
}
