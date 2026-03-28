package net.minecraft.src;

public abstract class EntityBirdBase extends EntityChicken {
	private double circleCenterX;
	private double circleCenterY;
	private double circleCenterZ;
	private float circleAngle;
	private float circleRadius;
	private int circleTicks;
	private int perchTicks;

	public EntityBirdBase(World var1) {
		super(var1);
		this.timeUntilNextEgg = Integer.MAX_VALUE;
		this.circleRadius = 5.0F + this.rand.nextFloat() * 5.0F;
		this.pickNewCircleCenter();
	}

	protected void pickNewCircleCenter() {
		this.circleCenterX = this.posX + (double)(this.rand.nextInt(17) - 8);
		this.circleCenterY = this.posY + (double)(4 + this.rand.nextInt(6));
		this.circleCenterZ = this.posZ + (double)(this.rand.nextInt(17) - 8);
		this.circleRadius = 5.0F + this.rand.nextFloat() * 5.0F;
		this.circleTicks = 140 + this.rand.nextInt(120);
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();
		if(this.worldObj.multiplayerWorld) {
			return;
		}

		if(this.perchTicks > 0) {
			--this.perchTicks;
			this.motionX *= 0.2D;
			this.motionZ *= 0.2D;
			if(!this.onGround) {
				this.motionY -= 0.08D;
			}
			if(this.perchTicks <= 0) {
				this.pickNewCircleCenter();
			}
			return;
		}

		--this.circleTicks;
		if(this.circleTicks <= 0 || this.getDistanceSq(this.circleCenterX, this.circleCenterY, this.circleCenterZ) > 196.0D) {
			this.pickNewCircleCenter();
		}

		this.circleAngle += 0.08F + this.rand.nextFloat() * 0.02F;
		double var1 = this.circleCenterX + MathHelper.cos(this.circleAngle) * this.circleRadius;
		double var3 = this.circleCenterZ + MathHelper.sin(this.circleAngle) * this.circleRadius;
		double var5 = this.circleCenterY + MathHelper.sin((float)this.ticksExisted * 0.07F) * 2.0F;
		double var7 = var1 - this.posX;
		double var9 = var3 - this.posZ;
		double var11 = var5 - this.posY;
		this.motionX += var7 * 0.03D;
		this.motionZ += var9 * 0.03D;
		this.motionY += var11 * 0.02D;
		if(this.motionY > 0.35D) {
			this.motionY = 0.35D;
		} else if(this.motionY < -0.35D) {
			this.motionY = -0.35D;
		}
		this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / (double)((float)Math.PI)) - 90.0F;
		this.moveForward = 0.15F;

		if(this.rand.nextInt(140) == 0) {
			ChunkPosition var13 = this.findNearbyTreePerch();
			if(var13 != null) {
				this.setPosition((double)var13.x + 0.5D, (double)var13.y + 1.05D, (double)var13.z + 0.5D);
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
				this.perchTicks = 60 + this.rand.nextInt(120);
			}
		}
	}

	private ChunkPosition findNearbyTreePerch() {
		int var1 = MathHelper.floor_double(this.posX);
		int var2 = MathHelper.floor_double(this.posY);
		int var3 = MathHelper.floor_double(this.posZ);

		for(int var4 = 0; var4 < 18; ++var4) {
			int var5 = var1 + this.rand.nextInt(13) - 6;
			int var6 = var2 + this.rand.nextInt(7) - 2;
			int var7 = var3 + this.rand.nextInt(13) - 6;
			int var8 = this.worldObj.getBlockId(var5, var6, var7);
			if((var8 == Block.leaves.blockID || var8 == Block.wood.blockID) && !this.worldObj.isBlockNormalCube(var5, var6 + 1, var7)) {
				return new ChunkPosition(var5, var6, var7);
			}
		}

		return null;
	}

	protected int getDropItemId() {
		return Item.feather.shiftedIndex;
	}

	public int getMaxSpawnedInChunk() {
		return 3;
	}
}
