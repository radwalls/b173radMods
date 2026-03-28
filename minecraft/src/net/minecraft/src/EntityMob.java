package net.minecraft.src;

public class EntityMob extends EntityCreature implements IMob {
	protected int attackStrength = 2;
	private boolean isWaveMob = false;
	private int blockBreakTime = 0;
	private int targetBlockX;
	private int targetBlockY;
	private int targetBlockZ;
	private int retargetCooldown = 0;

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
		if(this.isWaveMob && !this.worldObj.multiplayerWorld) {
			this.updateWaveBlockBehavior();
		}
	}

	public void onUpdate() {
		super.onUpdate();
		if(!this.worldObj.multiplayerWorld && this.worldObj.difficultySetting == 0) {
			this.setEntityDead();
		}

	}

	protected Entity findPlayerToAttack() {
		if(this.isWaveMob) {
			return this.worldObj.getClosestPlayerToEntity(this, 128.0D);
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
		var1.setBoolean("WaveMob", this.isWaveMob);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		this.isWaveMob = var1.getBoolean("WaveMob");
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

	public boolean isWaveMob() {
		return this.isWaveMob;
	}

	public void setWaveMob(boolean var1) {
		this.isWaveMob = var1;
	}

	private void updateWaveBlockBehavior() {
		if(this.playerToAttack != null && this.playerToAttack.isEntityAlive() && this.getDistanceToEntity(this.playerToAttack) < 20.0F) {
			return;
		}

		if(this.retargetCooldown-- <= 0) {
			this.retargetCooldown = 20 + this.rand.nextInt(20);
			ChunkCoordinates var1 = WaveSurvivalManager.findPreferredBuildTarget(this.worldObj, MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 56);
			if(var1 != null) {
				this.targetBlockX = var1.posX;
				this.targetBlockY = var1.posY;
				this.targetBlockZ = var1.posZ;
				this.setPathToEntity(this.worldObj.getEntityPathToXYZ(this, this.targetBlockX, this.targetBlockY, this.targetBlockZ, 32.0F));
			}
		}

		ChunkCoordinates var2 = WaveSurvivalManager.findNearbyPlacedBlock(this.worldObj, MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 2);
		if(var2 != null) {
			int var3 = this.worldObj.getBlockId(var2.posX, var2.posY, var2.posZ);
			if(var3 > 0 && var3 != Block.bedrock.blockID && var3 != Block.obsidian.blockID) {
				int var4 = this.getWaveBlockBreakTime(var3);
				++this.blockBreakTime;
				if(this.blockBreakTime >= var4) {
					int var5 = this.worldObj.getBlockMetadata(var2.posX, var2.posY, var2.posZ);
					this.worldObj.setBlockWithNotify(var2.posX, var2.posY, var2.posZ, 0);
					this.worldObj.playAuxSFX(2001, var2.posX, var2.posY, var2.posZ, var3 + (var5 << 12));
					this.blockBreakTime = 0;
				}
			} else {
				this.blockBreakTime = 0;
			}
		} else {
			this.blockBreakTime = 0;
		}
	}

	private int getWaveBlockBreakTime(int var1) {
		if(this instanceof EntityCreeper) {
			return 20;
		}

		Block var2 = Block.blocksList[var1];
		float var3 = var2 == null ? 1.0F : var2.getBlockHardness();
		if(var3 < 0.0F) {
			return 999999;
		}

		return MathHelper.floor_float(80.0F + var3 * 60.0F);
	}
}
