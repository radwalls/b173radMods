package net.minecraft.src;

public class EntityMob extends EntityCreature implements IMob {
	protected int attackStrength = 2;
	private boolean redWaveMob;
	private int redWaveBlockBreakTime;
	private int redWaveTargetX;
	private int redWaveTargetY;
	private int redWaveTargetZ;

	public EntityMob(World var1) {
		super(var1);
		this.health = 20;
	}

	public void onLivingUpdate() {
		float var1 = this.getEntityBrightness(1.0F);
		if(var1 > 0.5F) {
			this.entityAge += 2;
		}

		if(this.redWaveMob && this.playerToAttack == null && this.ticksExisted % 10 == 0) {
			this.playerToAttack = this.findPlayerToAttack();
		}

		if(this.redWaveMob && this.playerToAttack != null) {
			this.tryBreakBlockingStructure();
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
		double var1 = this.redWaveMob ? 96.0D : 16.0D;
		EntityPlayer var3 = this.worldObj.getClosestPlayerToEntity(this, var1);
		if(var3 == null) {
			return null;
		} else {
			return this.redWaveMob || this.canEntityBeSeen(var3) ? var3 : null;
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

	protected float getBlockPathWeight(int var1, int var2, int var3) {
		return 0.5F - this.worldObj.getLightBrightness(var1, var2, var3);
	}

	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
		var1.setBoolean("RedWaveMob", this.redWaveMob);
		var1.setInteger("RedWaveTargetX", this.redWaveTargetX);
		var1.setInteger("RedWaveTargetY", this.redWaveTargetY);
		var1.setInteger("RedWaveTargetZ", this.redWaveTargetZ);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		this.redWaveMob = var1.getBoolean("RedWaveMob");
		this.redWaveTargetX = var1.getInteger("RedWaveTargetX");
		this.redWaveTargetY = var1.getInteger("RedWaveTargetY");
		this.redWaveTargetZ = var1.getInteger("RedWaveTargetZ");
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

	private void tryBreakBlockingStructure() {
		double var1 = this.playerToAttack.posX - this.posX;
		double var3 = this.playerToAttack.posY - this.posY;
		double var5 = this.playerToAttack.posZ - this.posZ;
		double var7 = Math.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
		if(var7 < 1.0D) {
			return;
		}

		int var9 = MathHelper.floor_double(this.posX + var1 / var7 * 1.4D);
		int var10 = MathHelper.floor_double(this.boundingBox.minY + 0.5D);
		int var11 = MathHelper.floor_double(this.posZ + var5 / var7 * 1.4D);
		int var12 = this.worldObj.getBlockId(var9, var10, var11);
		if(var12 > 0 && var12 != Block.bedrock.blockID) {
			++this.redWaveBlockBreakTime;
			int var13 = this.redWaveMob && this.worldObj.getRedWaveSystem().isPlayerPlacedBlock(var9, var10, var11) ? 40 : 80;
			if(this instanceof EntityCreeper) {
				var13 /= 2;
			}

			if(this.redWaveBlockBreakTime >= var13) {
				this.worldObj.setBlockWithNotify(var9, var10, var11, 0);
				this.redWaveBlockBreakTime = 0;
			}
		} else {
			this.redWaveBlockBreakTime = 0;
		}
	}

	public void markAsRedWaveMob() {
		this.redWaveMob = true;
	}

	public boolean isRedWaveMob() {
		return this.redWaveMob;
	}

	public void func_25022_c(int var1, int var2, int var3) {
		this.redWaveTargetX = var1;
		this.redWaveTargetY = var2;
		this.redWaveTargetZ = var3;
	}
}
