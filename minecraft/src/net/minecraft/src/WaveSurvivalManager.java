package net.minecraft.src;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public final class WaveSurvivalManager {
	private static final HashMap worldStates = new HashMap();

	private static class WorldWaveState {
		public final HashSet playerPlacedBlocks = new HashSet();
		public long lastWaveDay = -1L;
		public int waveNumber = 0;
	}

	private WaveSurvivalManager() {
	}

	private static WorldWaveState getState(World var0) {
		WorldWaveState var1 = (WorldWaveState)worldStates.get(var0);
		if(var1 == null) {
			var1 = new WorldWaveState();
			worldStates.put(var0, var1);
		}

		return var1;
	}

	public static void tickWorld(World var0) {
		if(var0.multiplayerWorld || var0.playerEntities.isEmpty()) {
			return;
		}

		WorldWaveState var1 = getState(var0);
		long var2 = var0.getWorldTime();
		long var4 = var2 / 24000L;
		if(var4 > 0L && var4 % 2L == 0L && var2 % 24000L < 20L && var1.lastWaveDay != var4) {
			var1.lastWaveDay = var4;
			++var1.waveNumber;
			spawnWave(var0, var1.waveNumber);
		}
	}

	private static void spawnWave(World var0, int var1) {
		int var2 = 6 + var1 * 4;
		double var3 = (double)Math.min(96 + var1 * 4, 192);

		for(int var5 = 0; var5 < var2; ++var5) {
			EntityPlayer var6 = (EntityPlayer)var0.playerEntities.get(var0.rand.nextInt(var0.playerEntities.size()));
			double var7 = var0.rand.nextDouble() * Math.PI * 2.0D;
			int var9 = MathHelper.floor_double(var6.posX + Math.cos(var7) * var3);
			int var10 = MathHelper.floor_double(var6.posZ + Math.sin(var7) * var3);
			int var11 = var0.getHeightValue(var9, var10) + 1;
			EntityMob var12 = createWaveMob(var0, var0.rand.nextInt(4));
			if(var12 != null) {
				var12.setPosition((double)var9 + 0.5D, (double)var11, (double)var10 + 0.5D);
				if(var12.getCanSpawnHere()) {
					var12.setWaveMob(true);
					var0.spawnEntityInWorld(var12);
				}
			}
		}
	}

	private static EntityMob createWaveMob(World var0, int var1) {
		switch(var1) {
		case 0:
			return new EntityZombie(var0);
		case 1:
			return new EntitySkeleton(var0);
		case 2:
			return new EntitySpider(var0);
		default:
			return new EntityCreeper(var0);
		}
	}

	private static long toKey(int var0, int var1, int var2) {
		return (long)(var0 & 67108863) << 38 | (long)(var2 & 67108863) << 12 | (long)(var1 & 4095);
	}

	public static void markPlayerPlacedBlock(World var0, int var1, int var2, int var3) {
		getState(var0).playerPlacedBlocks.add(Long.valueOf(toKey(var1, var2, var3)));
	}

	public static void clearPlayerPlacedBlock(World var0, int var1, int var2, int var3) {
		getState(var0).playerPlacedBlocks.remove(Long.valueOf(toKey(var1, var2, var3)));
	}

	public static ChunkCoordinates findNearbyPlacedBlock(World var0, int var1, int var2, int var3, int var4) {
		WorldWaveState var5 = getState(var0);
		if(var5.playerPlacedBlocks.isEmpty()) {
			return null;
		}

		long var6 = (long)var4 * (long)var4;
		Iterator var8 = var5.playerPlacedBlocks.iterator();
		ChunkCoordinates var9 = null;
		long var10 = Long.MAX_VALUE;

		while(var8.hasNext()) {
			Long var12 = (Long)var8.next();
			long var13 = var12.longValue();
			int var15 = (int)(var13 >> 38);
			int var16 = (int)(var13 & 4095L);
			int var17 = (int)(var13 >> 12 & 67108863L);
			if(var15 >= 33554432) {
				var15 -= 67108864;
			}

			if(var17 >= 33554432) {
				var17 -= 67108864;
			}

			if(var16 >= 2048) {
				var16 -= 4096;
			}

			if(var0.getBlockId(var15, var16, var17) == 0) {
				var8.remove();
			} else {
				long var18 = (long)(var15 - var1);
				long var20 = (long)(var16 - var2);
				long var22 = (long)(var17 - var3);
				long var24 = var18 * var18 + var20 * var20 + var22 * var22;
				if(var24 <= var6 && var24 < var10) {
					var10 = var24;
					var9 = new ChunkCoordinates(var15, var16, var17);
				}
			}
		}

		return var9;
	}

	public static ChunkCoordinates findPreferredBuildTarget(World var0, int var1, int var2, int var3, int var4) {
		WorldWaveState var5 = getState(var0);
		if(var5.playerPlacedBlocks.isEmpty()) {
			return null;
		}

		long var6 = (long)var4 * (long)var4;
		ChunkCoordinates var8 = null;
		double var9 = -99999.0D;
		Iterator var11 = var5.playerPlacedBlocks.iterator();
		int var12 = 0;

		while(var11.hasNext() && var12 < 96) {
			Long var13 = (Long)var11.next();
			long var14 = var13.longValue();
			int var16 = (int)(var14 >> 38);
			int var17 = (int)(var14 & 4095L);
			int var18 = (int)(var14 >> 12 & 67108863L);
			if(var16 >= 33554432) {
				var16 -= 67108864;
			}

			if(var18 >= 33554432) {
				var18 -= 67108864;
			}

			if(var17 >= 2048) {
				var17 -= 4096;
			}

			if(var0.getBlockId(var16, var17, var18) == 0) {
				var11.remove();
			} else {
				long var19 = (long)(var16 - var1);
				long var21 = (long)(var17 - var2);
				long var23 = (long)(var18 - var3);
				long var25 = var19 * var19 + var21 * var21 + var23 * var23;
				if(var25 <= var6) {
					int var27 = countNeighbors(var5.playerPlacedBlocks, var16, var17, var18, 3);
					double var28 = (double)var27 * 8.0D - Math.sqrt((double)var25);
					if(var28 > var9) {
						var9 = var28;
						var8 = new ChunkCoordinates(var16, var17, var18);
					}
				}

				++var12;
			}
		}

		return var8;
	}

	private static int countNeighbors(HashSet var0, int var1, int var2, int var3, int var4) {
		int var5 = 0;

		for(int var6 = -var4; var6 <= var4; ++var6) {
			for(int var7 = -1; var7 <= 1; ++var7) {
				for(int var8 = -var4; var8 <= var4; ++var8) {
					if(var6 != 0 || var7 != 0 || var8 != 0) {
						long var9 = toKey(var1 + var6, var2 + var7, var3 + var8);
						if(var0.contains(Long.valueOf(var9))) {
							++var5;
						}
					}
				}
			}
		}

		return var5;
	}
}
