package net.minecraft.src;

public class EntityMob extends EntityCreature implements IMob {
	protected int attackStrength = 2;
	private boolean redWaveMob;
	private boolean redWaveCreeper;
	private int siegeBreakProgress;
	private int siegeBreakX;
	private int siegeBreakY;
	private int siegeBreakZ;

	public EntityMob(World var1) {
		super(var1);
		this.health = 20;
		this.siegeBreakX = Integer.MIN_VALUE;
	}

	public void onLivingUpdate() {
		float var1 = this.getEntityBrightness(1.0F);
		if(var1 > 0.5F) {
			this.entityAge += 2;
		}

		if(this.redWaveMob) {
			this.runRedWaveSiegeTick();
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
			return this.redWaveMob ? var3 : (this.canEntityBeSeen(var3) ? var3 : null);
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
			this.attackTime = this.redWaveMob ? 12 : 20;
			var1.attackEntityFrom(this, this.redWaveMob ? this.attackStrength + 1 : this.attackStrength);
		}

	}

	protected float getBlockPathWeight(int var1, int var2, int var3) {
		return this.redWaveMob ? 1.0F : 0.5F - this.worldObj.getLightBrightness(var1, var2, var3);
	}

	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
		var1.setBoolean("RedWave", this.redWaveMob);
		var1.setBoolean("RedWaveCreeper", this.redWaveCreeper);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		this.redWaveMob = var1.getBoolean("RedWave");
		this.redWaveCreeper = var1.getBoolean("RedWaveCreeper");
	}

	public boolean getCanSpawnHere() {
		if(this.redWaveMob) {
			return super.getCanSpawnHere();
		}

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

	public boolean isRedWaveMob() {
		return this.redWaveMob;
	}

	public void setRedWaveMob(boolean var1) {
		this.redWaveMob = var1;
	}

	public boolean isRedWaveCreeper() {
		return this.redWaveCreeper;
	}

	public void setRedWaveCreeper(boolean var1) {
		this.redWaveCreeper = var1;
		if(var1) {
			this.redWaveMob = true;
			this.moveSpeed *= 1.35F;
		}
	}

	private void runRedWaveSiegeTick() {
		if(this.playerToAttack == null || !this.playerToAttack.isEntityAlive()) {
			this.playerToAttack = this.findPlayerToAttack();
		}

		if(this.playerToAttack instanceof EntityPlayer && this.ticksExisted % 40 == 0) {
			EntityPlayer var1 = (EntityPlayer)this.playerToAttack;
			ChunkCoordinates var2 = this.worldObj.findDensePlayerBuiltCluster(MathHelper.floor_double(var1.posX), MathHelper.floor_double(var1.posY), MathHelper.floor_double(var1.posZ), 28, 4);
			if(var2 != null) {
				PathEntity var3 = this.worldObj.getEntityPathToXYZ(this, var2.x, var2.y, var2.z, 96.0F);
				if(var3 != null) {
					this.setPathToEntity(var3);
				}
			}
		}

		if(this.isCollidedHorizontally && this.ticksExisted % (this.redWaveCreeper ? 2 : 4) == 0) {
			this.tryBreakBlockingBlock();
		}
	}

	private void tryBreakBlockingBlock() {
		int var1 = MathHelper.floor_double(this.posX + (double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * 0.8F));
		int var2 = MathHelper.floor_double(this.boundingBox.minY + 0.5D);
		int var3 = MathHelper.floor_double(this.posZ + (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * 0.8F));
		int var4 = this.worldObj.getBlockId(var1, var2, var3);
		if(var4 == 0) {
			this.siegeBreakProgress = 0;
			this.siegeBreakX = Integer.MIN_VALUE;
			return;
		}

		Block var5 = Block.blocksList[var4];
		if(var5 == null || var5.blockHardness < 0.0F || var4 == Block.bedrock.blockID || var5.blockMaterial == Material.water || var5.blockMaterial == Material.lava) {
			return;
		}

		if(var1 != this.siegeBreakX || var2 != this.siegeBreakY || var3 != this.siegeBreakZ) {
			this.siegeBreakX = var1;
			this.siegeBreakY = var2;
			this.siegeBreakZ = var3;
			this.siegeBreakProgress = 0;
		}

		int var6 = this.worldObj.isPlayerPlacedBlock(var1, var2, var3) ? 4 : 1;
		this.siegeBreakProgress += this.redWaveCreeper ? 7 : 2;
		int var7 = 12 + (int)(var5.blockHardness * 10.0F) - var6;
		if(this.redWaveCreeper) {
			var7 -= 5;
		}

		if(var7 < 4) {
			var7 = 4;
		}

		if(this.siegeBreakProgress >= var7) {
			if(this.redWaveCreeper && this.rand.nextInt(6) == 0) {
				this.worldObj.createExplosion(this, (double)var1 + 0.5D, (double)var2 + 0.5D, (double)var3 + 0.5D, 2.2F);
			} else {
				this.worldObj.setBlockWithNotify(var1, var2, var3, 0);
				this.worldObj.playSoundEffect((double)var1 + 0.5D, (double)var2 + 0.5D, (double)var3 + 0.5D, "step.stone", 0.8F, 0.9F + this.rand.nextFloat() * 0.2F);
			}

			this.siegeBreakProgress = 0;
		}
	}
}
