package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RedWaveSystem {
	private static final int[] MOB_TYPES = new int[]{0, 1, 2, 3};
	private static final Map worldStates = new HashMap();

	private static class SpawnResult {
		public int totalSpawned = 0;
		public final boolean[] usedDirections = new boolean[4];
	}

	private static class RedWaveWorldState {
		public int waveCount = 0;
		public long lastWaveTime = -48000L;
		public boolean firstWaveTriggered = false;
		public final HashSet playerBuiltBlocks = new HashSet();
	}

	private static RedWaveWorldState getState(World var0) {
		RedWaveWorldState var1 = (RedWaveWorldState)worldStates.get(var0);
		if(var1 == null) {
			var1 = new RedWaveWorldState();
			worldStates.put(var0, var1);
		}

		return var1;
	}

	public static void onPlayerWake(EntityPlayer var0) {
		if(var0 == null || var0.worldObj == null || var0.worldObj.multiplayerWorld) {
			return;
		}

		RedWaveWorldState var1 = getState(var0.worldObj);
		if(!var1.firstWaveTriggered) {
			startWave(var0.worldObj, var0, true);
			var1.firstWaveTriggered = true;
		}
	}

	public static void onWorldTick(World var0) {
		if(var0 == null || var0.multiplayerWorld) {
			return;
		}

		if(var0.playerEntities.isEmpty()) {
			return;
		}

		RedWaveWorldState var1 = getState(var0);
		if(!var1.firstWaveTriggered) {
			return;
		}

		long var2 = var0.worldInfo.getWorldTime();
		if(var2 - var1.lastWaveTime >= 48000L) {
			EntityPlayer var4 = (EntityPlayer)var0.playerEntities.get(0);
			startWave(var0, var4, false);
		}
	}

	private static void startWave(World var0, EntityPlayer var1, boolean var2) {
		if(var1 == null) {
			return;
		}

		RedWaveWorldState var3 = getState(var0);
		++var3.waveCount;
		int var4 = var2 ? 80 + var0.rand.nextInt(41) : 14 + var3.waveCount * 8;
		SpawnResult var5 = spawnWaveMobs(var0, var1, var4);
		var3.lastWaveTime = var0.worldInfo.getWorldTime();
		String var6 = getDirectionMessage(var5.usedDirections[0], var5.usedDirections[1], var5.usedDirections[2], var5.usedDirections[3]);
		var1.addChatMessage("HEROBRINE: RED WAVE HAS STARTED. SURVIVE THE RED WAVES...");
		var1.addChatMessage("HEROBRINE: Spawned " + var5.totalSpawned + " mobs from the edges: " + var6);
		var0.playSoundAtEntity(var1, "ambient.cave.cave", 1.0F, 0.5F);
	}

	private static SpawnResult spawnWaveMobs(World var0, EntityPlayer var1, int var2) {
		SpawnResult var3 = new SpawnResult();
		Random var4 = var0.rand;
		int[] var5 = new int[]{0, 1, 2, 3};

		for(int var6 = 0; var6 < var2; ++var6) {
			int var7 = var5[var6 % var5.length];
			EntityMob var8 = createWaveMob(var0, var4);
			if(var8 != null && setupSpawnForDirection(var8, var1, var7, var4)) {
				if(var8 instanceof EntityCreature) {
					((EntityCreature)var8).setTarget(var1);
				}

				var0.entityJoinedWorld(var8);
				++var3.totalSpawned;
				var3.usedDirections[var7] = true;
			}
		}

		return var3;
	}

	private static EntityMob createWaveMob(World var0, Random var1) {
		int var2 = MOB_TYPES[var1.nextInt(MOB_TYPES.length)];
		EntityMob var3;
		if(var2 == 0) {
			var3 = new EntityZombie(var0);
		} else if(var2 == 1) {
			var3 = new EntitySkeleton(var0);
		} else if(var2 == 2) {
			var3 = new EntitySpider(var0);
		} else {
			var3 = new EntityCreeper(var0);
		}

		var3.setRedWaveMob(var3 instanceof EntityCreeper);
		return var3;
	}

	private static boolean setupSpawnForDirection(EntityMob var0, EntityPlayer var1, int var2, Random var3) {
		int var4 = MathHelper.floor_double(var1.posX);
		int var5 = MathHelper.floor_double(var1.posZ);
		int var6 = 80 + var3.nextInt(71);
		int var7 = var3.nextInt(41) - 20;
		int var8 = var4;
		int var9 = var5;
		if(var2 == 0) {
			var9 = var5 - var6;
			var8 = var4 + var7;
		} else if(var2 == 1) {
			var9 = var5 + var6;
			var8 = var4 + var7;
		} else if(var2 == 2) {
			var8 = var4 + var6;
			var9 = var5 + var7;
		} else {
			var8 = var4 - var6;
			var9 = var5 + var7;
		}

		if(!var0.worldObj.blockExists(var8, 64, var9)) {
			return false;
		}

		int var10 = var0.worldObj.getHeightValue(var8, var9);
		if(var10 < 1) {
			var10 = 1;
		}

		var0.setLocationAndAngles((double)var8 + 0.5D, (double)var10 + 1.0D, (double)var9 + 0.5D, var3.nextFloat() * 360.0F, 0.0F);
		return true;
	}

	private static String getDirectionMessage(boolean var0, boolean var1, boolean var2, boolean var3) {
		List var4 = new ArrayList();
		if(var0) {
			var4.add("NORTH");
		}

		if(var1) {
			var4.add("SOUTH");
		}

		if(var2) {
			var4.add("EAST");
		}

		if(var3) {
			var4.add("WEST");
		}

		StringBuffer var5 = new StringBuffer();
		for(int var6 = 0; var6 < var4.size(); ++var6) {
			if(var6 > 0) {
				var5.append(", ");
			}

			var5.append((String)var4.get(var6));
		}

		return var5.toString();
	}

	public static void markPlayerBlock(World var0, int var1, int var2, int var3) {
		if(var0 == null || var0.multiplayerWorld) {
			return;
		}

		RedWaveWorldState var4 = getState(var0);
		var4.playerBuiltBlocks.add(Long.valueOf(toBlockKey(var1, var2, var3)));
	}

	public static ChunkCoordinates getDensePlayerBuildTarget(World var0, Entity var1, Entity var2) {
		if(var0 == null || var1 == null) {
			return null;
		}

		RedWaveWorldState var3 = getState(var0);
		if(var3.playerBuiltBlocks.isEmpty()) {
			return null;
		}

		Iterator var4 = var3.playerBuiltBlocks.iterator();
		int var5 = -1;
		ChunkCoordinates var6 = null;
		int var7 = 0;

		while(var4.hasNext() && var7 < 96) {
			Long var8 = (Long)var4.next();
			int[] var9 = fromBlockKey(var8.longValue());
			int var10 = var9[0];
			int var11 = var9[1];
			int var12 = var9[2];
			if(var0.getBlockId(var10, var11, var12) == 0) {
				var4.remove();
			} else {
				double var13 = var1.getDistanceSq((double)var10, (double)var11, (double)var12);
				if(var13 <= 4096.0D) {
					if(var2 != null && var2.getDistanceSq((double)var10, (double)var11, (double)var12) > 9216.0D) {
						continue;
					}

					int var15 = countPlayerBlocksInRadius(var3, var0, var10, var11, var12, 4);
					if(var15 > var5 && var15 >= 8) {
						var5 = var15;
						var6 = new ChunkCoordinates(var10, var11, var12);
					}
				}
			}

			++var7;
		}

		return var6;
	}

	private static int countPlayerBlocksInRadius(RedWaveWorldState var0, World var1, int var2, int var3, int var4, int var5) {
		int var6 = 0;

		for(int var7 = -var5; var7 <= var5; ++var7) {
			for(int var8 = -var5; var8 <= var5; ++var8) {
				for(int var9 = -var5; var9 <= var5; ++var9) {
					if(var7 * var7 + var8 * var8 + var9 * var9 <= var5 * var5) {
						long var10 = toBlockKey(var2 + var7, var3 + var8, var4 + var9);
						if(var0.playerBuiltBlocks.contains(Long.valueOf(var10)) && var1.getBlockId(var2 + var7, var3 + var8, var4 + var9) != 0) {
							++var6;
						}
					}
				}
			}
		}

		return var6;
	}

	public static ChunkCoordinates getSiegeBreakTarget(EntityMob var0, Entity var1) {
		ChunkCoordinates var2 = getDensePlayerBuildTarget(var0.worldObj, var0, var1);
		if(var2 != null && var0 instanceof EntityCreature) {
			((EntityCreature)var0).setPathToEntity(var0.worldObj.getEntityPathToXYZ(var0, var2.x, var2.y, var2.z, 24.0F));
		}

		double var3 = var1 != null ? var1.posX : var0.posX + (double)(-MathHelper.sin(var0.rotationYaw * (float)Math.PI / 180.0F));
		double var5 = var1 != null ? var1.posY + (double)var1.getEyeHeight() : var0.posY;
		double var7 = var1 != null ? var1.posZ : var0.posZ + (double)(MathHelper.cos(var0.rotationYaw * (float)Math.PI / 180.0F));
		double var9 = var3 - var0.posX;
		double var11 = var5 - var0.posY;
		double var13 = var7 - var0.posZ;
		double var15 = Math.sqrt(var9 * var9 + var11 * var11 + var13 * var13);
		if(var15 < 1.0E-4D) {
			return null;
		}

		var9 /= var15;
		var11 /= var15;
		var13 /= var15;

		for(int var17 = 1; var17 <= 3; ++var17) {
			int var18 = MathHelper.floor_double(var0.posX + var9 * (double)var17);
			int var19 = MathHelper.floor_double(var0.posY + var11 * (double)var17);
			int var20 = MathHelper.floor_double(var0.posZ + var13 * (double)var17);
			int var21 = var0.worldObj.getBlockId(var18, var19, var20);
			if(isBreakable(var21)) {
				return new ChunkCoordinates(var18, var19, var20);
			}
		}

		return null;
	}

	public static boolean isBreakable(int var0) {
		if(var0 <= 0) {
			return false;
		}

		if(var0 == Block.bedrock.blockID || var0 == Block.obsidian.blockID) {
			return false;
		}

		Block var1 = Block.blocksList[var0];
		return var1 != null && var1.blockHardness >= 0.0F;
	}

	private static long toBlockKey(int var0, int var1, int var2) {
		return ((long)(var0 & 67108863) << 38) | ((long)(var2 & 67108863) << 12) | (long)(var1 & 4095);
	}

	private static int[] fromBlockKey(long var0) {
		int var2 = (int)(var0 >> 38);
		int var3 = (int)(var0 << 26 >> 38);
		int var4 = (int)(var0 << 52 >> 52);
		if(var2 >= 33554432) {
			var2 -= 67108864;
		}

		if(var3 >= 33554432) {
			var3 -= 67108864;
		}

		if(var4 >= 2048) {
			var4 -= 4096;
		}

		return new int[]{var2, var4, var3};
	}
}
