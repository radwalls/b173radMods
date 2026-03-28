package net.minecraft.src;

public abstract class EntityBird extends EntityChicken {
	private double circleCenterX;
	private double circleCenterY;
	private double circleCenterZ;
	private float circlingAngle;
	private float circlingRadius;
	private float circlingSpeed;
	private int flightCooldown;
	private int perchTime;

	public EntityBird(World var1) {
		super(var1);
		this.timeUntilNextEgg = Integer.MAX_VALUE;
		this.circlingAngle = this.rand.nextFloat() * (float)Math.PI * 2.0F;
		this.circlingRadius = 2.5F + this.rand.nextFloat() * 3.0F;
		this.circlingSpeed = 0.09F + this.rand.nextFloat() * 0.05F;
		this.setCircleCenter(this.posX, this.posY + 4.0D, this.posZ);
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();
		if(this.worldObj.multiplayerWorld) {
			return;
		}

		if(this.perchTime > 0) {
			--this.perchTime;
			this.motionX *= 0.4D;
			this.motionZ *= 0.4D;
			if(this.perchTime == 0) {
				this.takeOff();
			}

			return;
		}

		if(this.onGround) {
			if(this.flightCooldown > 0) {
				--this.flightCooldown;
			}

			if(this.flightCooldown <= 0) {
				this.takeOff();
			}

			return;
		}

		this.flyInCircle();
		if(this.rand.nextInt(180) == 0) {
			this.tryToLandOnTree();
		}
	}

	private void flyInCircle() {
		if(this.rand.nextInt(240) == 0 || this.getDistanceSq(this.circleCenterX, this.circleCenterY, this.circleCenterZ) > 196.0D) {
			this.setCircleCenter(this.posX, this.posY + 4.0D + (double)this.rand.nextInt(4), this.posZ);
			this.circlingRadius = 2.5F + this.rand.nextFloat() * 3.0F;
		}

		this.circlingAngle += this.circlingSpeed;
		double var1 = this.circleCenterX + MathHelper.cos(this.circlingAngle) * this.circlingRadius;
		double var3 = this.circleCenterZ + MathHelper.sin(this.circlingAngle) * this.circlingRadius;
		double var5 = this.circleCenterY + MathHelper.sin(this.circlingAngle * 2.0F) * 1.2D;
		double var7 = var1 - this.posX;
		double var9 = var5 - this.posY;
		double var11 = var3 - this.posZ;
		this.motionX += var7 * 0.02D;
		this.motionY += var9 * 0.015D;
		this.motionZ += var11 * 0.02D;
		this.motionX *= 0.91D;
		this.motionY *= 0.86D;
		this.motionZ *= 0.91D;
		if(this.motionY < -0.2D) {
			this.motionY = -0.2D;
		}

		this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / (double)((float)Math.PI)) - 90.0F;
	}

	private void tryToLandOnTree() {
		for(int var1 = 0; var1 < 8; ++var1) {
			int var2 = MathHelper.floor_double(this.posX + (double)this.rand.nextInt(13) - 6.0D);
			int var3 = MathHelper.floor_double(this.posY + (double)this.rand.nextInt(6) - 2.0D);
			int var4 = MathHelper.floor_double(this.posZ + (double)this.rand.nextInt(13) - 6.0D);
			int var5 = this.worldObj.getBlockId(var2, var3, var4);
			if((var5 == Block.wood.blockID || var5 == Block.leaves.blockID) && !this.worldObj.isBlockNormalCube(var2, var3 + 1, var4)) {
				this.setPosition((double)var2 + 0.5D, (double)var3 + 1.0D, (double)var4 + 0.5D);
				this.motionX = this.motionY = this.motionZ = 0.0D;
				this.perchTime = 40 + this.rand.nextInt(80);
				this.flightCooldown = 40;
				return;
			}
		}
	}

	private void takeOff() {
		this.perchTime = 0;
		this.flightCooldown = 60 + this.rand.nextInt(80);
		this.motionY = 0.45D;
		this.setCircleCenter(this.posX, this.posY + 4.0D + (double)this.rand.nextInt(4), this.posZ);
	}

	private void setCircleCenter(double var1, double var3, double var5) {
		this.circleCenterX = var1;
		this.circleCenterY = var3;
		this.circleCenterZ = var5;
	}

	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
		var1.setDouble("BirdCenterX", this.circleCenterX);
		var1.setDouble("BirdCenterY", this.circleCenterY);
		var1.setDouble("BirdCenterZ", this.circleCenterZ);
		var1.setInteger("BirdCooldown", this.flightCooldown);
		var1.setInteger("BirdPerch", this.perchTime);
		var1.setFloat("BirdAngle", this.circlingAngle);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		this.circleCenterX = var1.getDouble("BirdCenterX");
		this.circleCenterY = var1.getDouble("BirdCenterY");
		this.circleCenterZ = var1.getDouble("BirdCenterZ");
		this.flightCooldown = var1.getInteger("BirdCooldown");
		this.perchTime = var1.getInteger("BirdPerch");
		this.circlingAngle = var1.getFloat("BirdAngle");
	}

	protected int getDropItemId() {
		return Item.feather.shiftedIndex;
	}

	public int getMaxSpawnedInChunk() {
		return 4;
	}
}
