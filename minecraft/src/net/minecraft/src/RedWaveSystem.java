package net.minecraft.src;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RedWaveSystem {
	private static final long WAVE_INTERVAL_TICKS = 48000L;
	private static final int BASE_WAVE_SIZE = 6;
	private static final int WAVE_SIZE_STEP = 3;
	private static final int MIN_BUILD_SIZE = 24;
	private boolean firstSleepTriggered;
	private long nextWaveTime = -1L;
	private int waveNumber;
	private final Set playerPlacedBlocks = new HashSet();
	private final Map chunkBuildWeight = new HashMap();

	public void readFromNBT(NBTTagCompound var1) {
		this.firstSleepTriggered = var1.getBoolean("RedWaveStarted");
		this.nextWaveTime = var1.getLong("RedWaveNextTime");
		this.waveNumber = var1.getInteger("RedWaveCount");
	}

	public void writeToNBT(NBTTagCompound var1) {
		var1.setBoolean("RedWaveStarted", this.firstSleepTriggered);
		var1.setLong("RedWaveNextTime", this.nextWaveTime);
		var1.setInteger("RedWaveCount", this.waveNumber);
	}

	public void onTick(World var1, boolean var2) {
		long var3 = var1.getWorldTime();
		if(var2 && !this.firstSleepTriggered) {
			this.firstSleepTriggered = true;
			this.startWave(var1);
			this.nextWaveTime = var3 + WAVE_INTERVAL_TICKS;
		} else if(this.firstSleepTriggered && this.nextWaveTime > 0L && var3 >= this.nextWaveTime) {
			this.startWave(var1);
			this.nextWaveTime += WAVE_INTERVAL_TICKS;
		}
	}

	private void startWave(World var1) {
		if(var1.playerEntities.isEmpty()) {
			return;
		}

		++this.waveNumber;
		EntityPlayer var2 = (EntityPlayer)var1.playerEntities.get(var1.rand.nextInt(var1.playerEntities.size()));
		ChunkPosition var3 = this.findPreferredBuildTarget(var2);
		double var4 = (double)MathHelper.floor_double(var2.posX);
		double var6 = (double)MathHelper.floor_double(var2.posZ);
		double var8 = Math.atan2((double)var3.z - var6, (double)var3.x - var4);
		double var10 = 128.0D + (double)Math.min(this.waveNumber * 4, 64);
		int var12 = BASE_WAVE_SIZE + this.waveNumber * WAVE_SIZE_STEP;

		for(int var13 = 0; var13 < var12; ++var13) {
			float var14 = ((float)var13 / (float)Math.max(var12 - 1, 1) - 0.5F) * 1.2F;
			double var15 = var8 + (double)var14 + (var1.rand.nextDouble() - 0.5D) * 0.2D;
			int var17 = MathHelper.floor_double(var4 + Math.cos(var15) * var10);
			int var18 = MathHelper.floor_double(var6 + Math.sin(var15) * var10);
			int var19 = var1.findTopSolidBlock(var17, var18);
			if(var19 <= 1) {
				var19 = MathHelper.floor_double(var2.posY);
			}

			EntityMob var20 = this.createWaveMob(var1, var13);
			if(var20 != null) {
				var20.setPosition((double)var17 + 0.5D, (double)var19 + 1.0D, (double)var18 + 0.5D);
				var20.markAsRedWaveMob();
				var20.func_25022_c(var3.x, var3.y, var3.z);
				var1.entityJoinedWorld(var20);
			}
		}
	}

	private EntityMob createWaveMob(World var1, int var2) {
		int var3 = var1.rand.nextInt(10);
		return (EntityMob)(var3 < 4 ? new EntityZombie(var1) : (var3 < 8 ? new EntitySkeleton(var1) : new EntityCreeper(var1)));
	}

	private ChunkPosition findPreferredBuildTarget(EntityPlayer var1) {
		int var2 = MathHelper.floor_double(var1.posX);
		int var3 = MathHelper.floor_double(var1.posY);
		int var4 = MathHelper.floor_double(var1.posZ);
		int var5 = this.getChunkX(var2);
		int var6 = this.getChunkZ(var4);
		int var7 = 0;
		int var8 = var5;
		int var9 = var6;

		for(int var10 = -6; var10 <= 6; ++var10) {
			for(int var11 = -6; var11 <= 6; ++var11) {
				Integer var12 = (Integer)this.chunkBuildWeight.get(Long.valueOf(this.chunkKey(var5 + var10, var6 + var11)));
				if(var12 != null && var12.intValue() > var7) {
					var7 = var12.intValue();
					var8 = var5 + var10;
					var9 = var6 + var11;
				}
			}
		}

		if(var7 >= MIN_BUILD_SIZE) {
			return new ChunkPosition((var8 << 4) + 8, var3, (var9 << 4) + 8);
		} else {
			return new ChunkPosition(var2, var3, var4);
		}
	}

	public void trackPlayerPlacement(int var1, int var2, int var3) {
		Long var4 = Long.valueOf(this.blockKey(var1, var2, var3));
		if(this.playerPlacedBlocks.add(var4)) {
			this.addChunkWeight(var1, var3, 1);
		}
	}

	public void onBlockRemoved(int var1, int var2, int var3) {
		Long var4 = Long.valueOf(this.blockKey(var1, var2, var3));
		if(this.playerPlacedBlocks.remove(var4)) {
			this.addChunkWeight(var1, var3, -1);
		}
	}

	public boolean isPlayerPlacedBlock(int var1, int var2, int var3) {
		return this.playerPlacedBlocks.contains(Long.valueOf(this.blockKey(var1, var2, var3)));
	}

	private void addChunkWeight(int var1, int var2, int var3) {
		long var4 = this.chunkKey(this.getChunkX(var1), this.getChunkZ(var2));
		Integer var6 = (Integer)this.chunkBuildWeight.get(Long.valueOf(var4));
		int var7 = (var6 == null ? 0 : var6.intValue()) + var3;
		if(var7 <= 0) {
			this.chunkBuildWeight.remove(Long.valueOf(var4));
		} else {
			this.chunkBuildWeight.put(Long.valueOf(var4), Integer.valueOf(var7));
		}
	}

	private int getChunkX(int var1) {
		return var1 >> 4;
	}

	private int getChunkZ(int var1) {
		return var1 >> 4;
	}

	private long blockKey(int var1, int var2, int var3) {
		return ((long)(var1 + 30000000) & 67108863L) << 38 | ((long)var2 & 255L) << 30 | (long)(var3 + 30000000) & 1073741823L;
	}

	private long chunkKey(int var1, int var2) {
		return (long)var1 << 32 | (long)var2 & 4294967295L;
	}
}
