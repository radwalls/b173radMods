package net.minecraft.src;

public abstract class EntityBird extends EntityChicken {
	protected double circleCenterX;
	protected double circleCenterY;
	protected double circleCenterZ;
	protected double targetX;
	protected double targetY;
	protected double targetZ;
	protected int circlingTicks;
	protected int perchTicks;

	public EntityBird(World var1) {
		super(var1);
		this.circleCenterX = this.posX;
		this.circleCenterY = this.posY;
		this.circleCenterZ = this.posZ;
		this.circlingTicks = this.rand.nextInt(80);
		this.perchTicks = 0;
		this.timeUntilNextEgg = Integer.MAX_VALUE;
	}

	protected void updatePlayerActionState() {
		if(this.onGround && this.perchTicks > 0) {
			this.moveStrafing = 0.0F;
			this.moveForward = 0.0F;
			this.isJumping = false;
			--this.perchTicks;
			if(this.perchTicks <= 0 || this.rand.nextInt(100) == 0) {
				this.motionY = 0.42D;
				this.isJumping = true;
			}

			return;
		}

		if(this.circlingTicks-- <= 0 || this.getDistanceSq(this.targetX, this.targetY, this.targetZ) < 3.0D) {
			this.pickFlightTarget();
		}

		double var1 = this.targetX - this.posX;
		double var3 = this.targetY - this.posY;
		double var5 = this.targetZ - this.posZ;
		double var7 = MathHelper.sqrt_double(var1 * var1 + var5 * var5);
		if(var7 < 1.0D) {
			var7 = 1.0D;
		}

		double var9 = 0.24D;
		this.motionX += (var1 / var7 * var9 - this.motionX) * 0.1D;
		this.motionZ += (var5 / var7 * var9 - this.motionZ) * 0.1D;
		this.motionY += (var3 * 0.08D - this.motionY) * 0.12D;
		this.isJumping = this.motionY > 0.05D;
		float var11 = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / (double)((float)Math.PI)) - 90.0F;
		this.rotationYaw = var11;
		this.moveForward = 0.0F;
		this.moveStrafing = 0.0F;

		if(this.onGround && this.perchTicks <= 0) {
			this.perchTicks = 20 + this.rand.nextInt(80);
		}
	}

	protected void pickFlightTarget() {
		this.circlingTicks = 30 + this.rand.nextInt(60);
		if(this.rand.nextInt(5) == 0) {
			ChunkPosition var1 = this.findTreePerch();
			if(var1 != null) {
				this.targetX = (double)var1.x + 0.5D;
				this.targetY = (double)var1.y;
				this.targetZ = (double)var1.z + 0.5D;
				return;
			}
		}

		if(this.rand.nextInt(25) == 0) {
			this.circleCenterX = this.posX + (double)(this.rand.nextInt(17) - 8);
			this.circleCenterY = this.posY + (double)(this.rand.nextInt(5) - 2);
			this.circleCenterZ = this.posZ + (double)(this.rand.nextInt(17) - 8);
		}

		float var2 = (this.rand.nextFloat() * (float)Math.PI * 2.0F);
		double var3 = 4.0D + (double)this.rand.nextInt(8);
		this.targetX = this.circleCenterX + Math.cos((double)var2) * var3;
		this.targetY = this.circleCenterY + (double)(this.rand.nextInt(5) - 2);
		this.targetZ = this.circleCenterZ + Math.sin((double)var2) * var3;
	}

	protected ChunkPosition findTreePerch() {
		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.posY);
		int var3 = MathHelper.floor_double(this.posZ);

		for(int var4 = 0; var4 < 20; ++var4) {
			int var5 = var1 + this.rand.nextInt(15) - 7;
			int var6 = var2 + this.rand.nextInt(7) - 2;
			int var7 = var3 + this.rand.nextInt(15) - 7;
			int var8 = this.worldObj.getBlockId(var5, var6 - 1, var7);
			if((var8 == Block.leaves.blockID || var8 == Block.wood.blockID) && this.worldObj.isAirBlock(var5, var6, var7)) {
				return new ChunkPosition(var5, var6, var7);
			}
		}

		return null;
	}

	protected float getBlockPathWeight(int var1, int var2, int var3) {
		return 10.0F;
	}

	public boolean getCanSpawnHere() {
		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.boundingBox.minY);
		int var3 = MathHelper.floor_double(this.posZ);
		return this.worldObj.getFullBlockLightValue(var1, var2, var3) > 7 && this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}
}
