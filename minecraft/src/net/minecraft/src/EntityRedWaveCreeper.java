package net.minecraft.src;

public class EntityRedWaveCreeper extends EntityRedWaveMob {
	private int timeSinceIgnited;
	private int lastActiveTime;

	public EntityRedWaveCreeper(World var1) {
		super(var1);
		this.texture = "/mob/creeper.png";
		this.moveSpeed = 0.35F;
	}

	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, Byte.valueOf((byte)-1));
	}

	public void onUpdate() {
		this.lastActiveTime = this.timeSinceIgnited;
		super.onUpdate();
		if(this.playerToAttack == null && this.timeSinceIgnited > 0) {
			this.setCreeperState(-1);
			this.timeSinceIgnited = Math.max(0, this.timeSinceIgnited - 1);
		}
	}

	protected void attackEntity(Entity var1, float var2) {
		int var3 = this.getCreeperState();
		if(var3 <= 0 && var2 < 4.0F || var3 > 0 && var2 < 8.0F) {
			if(this.timeSinceIgnited == 0) {
				this.worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
			}

			this.setCreeperState(1);
			++this.timeSinceIgnited;
			if(this.timeSinceIgnited >= 20) {
				this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 5.5F);
				this.setEntityDead();
			}

			this.hasAttacked = true;
		} else {
			this.setCreeperState(-1);
			this.timeSinceIgnited = Math.max(0, this.timeSinceIgnited - 1);
		}
	}

	protected int getBreachingDelay() {
		return 16;
	}

	protected int getDropItemId() {
		return Item.gunpowder.shiftedIndex;
	}

	private int getCreeperState() {
		return this.dataWatcher.getWatchableObjectByte(16);
	}

	private void setCreeperState(int var1) {
		this.dataWatcher.updateObject(16, Byte.valueOf((byte)var1));
	}
}
