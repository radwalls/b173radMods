package net.minecraft.src;

public class RedWaveSystem {
	private boolean hasTriggeredFromSleep;
	private int waveNumber;
	private long nextWaveTime = -1L;

	public void readFromWorldInfo(WorldInfo var1) {
		this.hasTriggeredFromSleep = var1.getRedWaveSleepTriggered();
		this.waveNumber = var1.getRedWaveNumber();
		this.nextWaveTime = var1.getRedWaveNextTime();
	}

	public void writeToWorldInfo(WorldInfo var1) {
		var1.setRedWaveState(this.hasTriggeredFromSleep, this.waveNumber, this.nextWaveTime);
	}

	public void onPlayerSlept(World var1) {
		if(!this.hasTriggeredFromSleep) {
			this.hasTriggeredFromSleep = true;
			this.nextWaveTime = var1.getWorldTime() + 20L;
		}
	}

	public void tick(World var1) {
		if(!this.hasTriggeredFromSleep || var1.playerEntities.isEmpty() || this.nextWaveTime < 0L) {
			return;
		}

		if(var1.getWorldTime() >= this.nextWaveTime) {
			this.startWave(var1);
			this.nextWaveTime = var1.getWorldTime() + 48000L;
		}
	}

	private void startWave(World var1) {
		++this.waveNumber;
		int var2 = 6 + this.waveNumber * 3;

		for(int var3 = 0; var3 < var2; ++var3) {
			EntityPlayer var4 = (EntityPlayer)var1.playerEntities.get(var1.rand.nextInt(var1.playerEntities.size()));
			if(var4 != null && var4.isEntityAlive()) {
				this.spawnWaveMobNearPlayer(var1, var4, var3);
			}
		}
	}

	private void spawnWaveMobNearPlayer(World var1, EntityPlayer var2, int var3) {
		double var4 = var1.rand.nextDouble() * (double)((float)Math.PI * 2.0F);
		double var6 = 192.0D + var1.rand.nextDouble() * 64.0D;
		int var8 = MathHelper.floor_double(var2.posX + Math.cos(var4) * var6);
		int var9 = MathHelper.floor_double(var2.posZ + Math.sin(var4) * var6);
		int var10 = var1.getTopSolidOrLiquidBlock(var8, var9);
		EntityRedWaveMob var11;
		int var12 = var1.rand.nextInt(10);
		if(var12 < 2) {
			var11 = new EntityRedWaveCreeper(var1);
		} else if(var12 < 5) {
			var11 = new EntityRedWaveSpider(var1);
		} else {
			var11 = new EntityRedWaveZombie(var1);
		}

		var11.setLocationAndAngles((double)var8 + 0.5D, (double)(var10 + 1), (double)var9 + 0.5D, var1.rand.nextFloat() * 360.0F, 0.0F);
		var11.setEntityToAttack(var2);
		if(var11.getCanSpawnHere()) {
			var1.entityJoinedWorld(var11);
		}
	}
}
