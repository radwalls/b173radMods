package net.minecraft.src;

public class EntityRedWaveSpider extends EntityRedWaveMob {
	public EntityRedWaveSpider(World var1) {
		super(var1);
		this.texture = "/mob/spider.png";
		this.setSize(1.4F, 0.9F);
		this.moveSpeed = 0.95F;
		this.attackStrength = 3;
	}

	protected void attackEntity(Entity var1, float var2) {
		if(var2 > 2.0F && var2 < 6.0F && this.rand.nextInt(10) == 0) {
			if(this.onGround) {
				double var3 = var1.posX - this.posX;
				double var5 = var1.posZ - this.posZ;
				float var7 = MathHelper.sqrt_double(var3 * var3 + var5 * var5);
				this.motionX = var3 / (double)var7 * 0.45D * 0.9D + this.motionX * 0.2D;
				this.motionZ = var5 / (double)var7 * 0.45D * 0.9D + this.motionZ * 0.2D;
				this.motionY = 0.4D;
			}
		} else {
			super.attackEntity(var1, var2);
		}
	}
}
