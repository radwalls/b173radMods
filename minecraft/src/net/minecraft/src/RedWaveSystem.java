package net.minecraft.src;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class RedWaveSystem {
	private static final Map worldStates = new HashMap();

	private RedWaveSystem() {
	}

	private static RedWaveState getState(World var0) {
		RedWaveState var1 = (RedWaveState)worldStates.get(var0);
		if(var1 == null) {
			var1 = new RedWaveState();
			worldStates.put(var0, var1);
		}

		return var1;
	}

	public static void onWorldTick(World var0) {
		if(var0.multiplayerWorld || var0.playerEntities.isEmpty() || var0.difficultySetting <= 0) {
			return;
		}

		RedWaveState var1 = getState(var0);
		if(var1.waitForSleepTrigger) {
			return;
		}

		long var2 = var0.getWorldInfo().getWorldTime();
		if(var2 % 20L != 0L) {
			return;
		}

		long var4 = var2 / 24000L;
		if(var4 >= var1.nextWaveDay) {
			EntityPlayer var6 = (EntityPlayer)var0.playerEntities.get(0);
			startWave(var0, var6, false);
		}
	}

	public static void onPlayerWokeAfterNight(World var0, EntityPlayer var1) {
		if(var0.multiplayerWorld || var1 == null || var0.difficultySetting <= 0) {
			return;
		}

		RedWaveState var2 = getState(var0);
		if(var2.waitForSleepTrigger) {
			startWave(var0, var1, true);
		}
	}

	public static void markPlayerBuiltBlock(World var0, int var1, int var2, int var3) {
		RedWaveState var4 = getState(var0);
		var4.playerBuiltBlocks.add(Long.valueOf(pack(var1, var2, var3)));
		if(var4.playerBuiltBlocks.size() > 32000) {
			Iterator var5 = var4.playerBuiltBlocks.iterator();
			int var6 = 8000;

			while(var6 > 0 && var5.hasNext()) {
				var5.next();
				var5.remove();
				--var6;
			}
		}
	}

	public static boolean isRedWaveMob(EntityMob var0) {
		return var0 != null && var0.redWaveMob;
	}

	public static boolean isRedWaveCreeper(EntityCreeper var0) {
		return var0 != null && var0.redWaveCreeper;
	}

	public static int getRedWaveColorMultiplier(EntityLiving var0) {
		if(var0 instanceof EntityMob && ((EntityMob)var0).redWaveMob) {
			return -1291911168;
		} else {
			return 0;
		}
	}

	public static void handleSiegeBehavior(EntityMob var0) {
		if(!isRedWaveMob(var0) || var0.worldObj.multiplayerWorld || var0.worldObj.difficultySetting <= 0) {
			return;
		}

		EntityPlayer var1 = var0.worldObj.getClosestPlayerToEntity(var0, 128.0D);
		if(var1 == null) {
			return;
		}

		int var2 = 20;
		if(var0 instanceof EntityCreeper && isRedWaveCreeper((EntityCreeper)var0)) {
			var2 = 6;
		}

		if(var0.ticksExisted % var2 == 0) {
			ChunkPosition var3 = findBestSiegeTarget(var0.worldObj, var0, var1);
			if(var3 != null) {
				breakBlock(var0.worldObj, var3.x, var3.y, var3.z);
			}
		}

		if(var0.ticksExisted % 15 == 0) {
			var0.setTarget(var1);
			PathEntity var4 = var0.worldObj.getPathToEntity(var0, var1, 64.0F);
			if(var4 != null) {
				var0.setPathToEntity(var4);
			}
		}
	}

	private static ChunkPosition findBestSiegeTarget(World var0, EntityMob var1, EntityPlayer var2) {
		ChunkPosition var3 = findDensePlayerBuiltBlock(var0, var2, 18, 6);
		if(var3 != null) {
			return var3;
		} else {
			int var4 = MathHelper.floor_double(var1.posX);
			int var5 = MathHelper.floor_double(var1.posY + (double)var1.getEyeHeight());
			int var6 = MathHelper.floor_double(var1.posZ);
			double var7 = var2.posX - var1.posX;
			double var9 = var2.posY + (double)var2.getEyeHeight() - (var1.posY + (double)var1.getEyeHeight());
			double var11 = var2.posZ - var1.posZ;
			double var13 = Math.sqrt(var7 * var7 + var9 * var9 + var11 * var11);
			if(var13 < 0.001D) {
				return null;
			} else {
				double var15 = var7 / var13;
				double var17 = var9 / var13;
				double var19 = var11 / var13;

				for(int var21 = 1; var21 < 8; ++var21) {
					int var22 = MathHelper.floor_double((double)var4 + var15 * (double)var21);
					int var23 = MathHelper.floor_double((double)var5 + var17 * (double)var21);
					int var24 = MathHelper.floor_double((double)var6 + var19 * (double)var21);
					int var25 = var0.getBlockId(var22, var23, var24);
					if(var25 != 0 && canSiegeBreak(var25)) {
						return new ChunkPosition(var22, var23, var24);
					}
				}

				return null;
			}
		}
	}

	private static ChunkPosition findDensePlayerBuiltBlock(World var0, EntityPlayer var1, int var2, int var3) {
		RedWaveState var4 = getState(var0);
		int var5 = MathHelper.floor_double(var1.posX);
		int var6 = MathHelper.floor_double(var1.posY);
		int var7 = MathHelper.floor_double(var1.posZ);
		ChunkPosition var8 = null;
		int var9 = 0;
		int var10 = 4;

		for(int var11 = -var2; var11 <= var2; var11 += var10) {
			for(int var12 = -var2; var12 <= var2; var12 += var10) {
				int var13 = var5 + var11;
				int var14 = var7 + var12;
				int var15 = var0.findTopSolidBlock(var13, var14);
				if(var15 >= 1) {
					int var16 = var15 - 1;
					int var17 = countPlayerBuiltNearby(var4, var13, var6, var14, var3);
					if(var17 >= 12 && (var8 == null || var17 > var9)) {
						var9 = var17;
						var8 = new ChunkPosition(var13, var16, var14);
					}
				}
			}
		}

		return var8;
	}

	private static int countPlayerBuiltNearby(RedWaveState var0, int var1, int var2, int var3, int var4) {
		int var5 = 0;

		for(int var6 = -var4; var6 <= var4; ++var6) {
			for(int var7 = -2; var7 <= 4; ++var7) {
				for(int var8 = -var4; var8 <= var4; ++var8) {
					if(var0.playerBuiltBlocks.contains(Long.valueOf(pack(var1 + var6, var2 + var7, var3 + var8)))) {
						++var5;
					}
				}
			}
		}

		return var5;
	}

	private static boolean canSiegeBreak(int var0) {
		if(var0 == Block.bedrock.blockID || var0 == Block.obsidian.blockID) {
			return false;
		} else {
			return Block.blocksList[var0] != null && Block.blocksList[var0].blockHardness >= 0.0F;
		}
	}

	private static void breakBlock(World var0, int var1, int var2, int var3) {
		int var4 = var0.getBlockId(var1, var2, var3);
		if(var4 != 0 && canSiegeBreak(var4)) {
			var0.playAuxSFX(2001, var1, var2, var3, var4);
			var0.setBlockWithNotify(var1, var2, var3, 0);
		}
	}

	private static void startWave(World var0, EntityPlayer var1, boolean var2) {
		RedWaveState var3 = getState(var0);
		Random var4 = var0.rand;
		int var5;
		if(var2 && var3.waveNumber == 0) {
			var5 = 80 + var4.nextInt(41);
		} else {
			var5 = 16 + var3.waveNumber * 8 + var4.nextInt(9);
		}

		int var6 = spawnWaveMobs(var0, var1, var5, var3);
		var3.waveNumber++;
		var3.waitForSleepTrigger = false;
		var3.nextWaveDay = var0.getWorldInfo().getWorldTime() / 24000L + 2L;
		var1.addChatMessage("HEROBRINE: RED WAVE HAS STARTED. SURVIVE THE RED WAVES...");
		var1.addChatMessage("HEROBRINE: Spawned " + var6 + " mobs from the edges: " + var3.lastDirectionsText);
		var0.playSoundAtEntity(var1, "ambient.cave.cave", 1.0F, 0.65F);
	}

	private static int spawnWaveMobs(World var0, EntityPlayer var1, int var2, RedWaveState var3) {
		String[] var4 = new String[]{"NORTH", "SOUTH", "EAST", "WEST"};
		boolean[] var5 = new boolean[4];
		int var6 = 0;

		for(int var7 = 0; var7 < var2; ++var7) {
			int var8 = var7 % 4;
			if(spawnOne(var0, var1, var8, var7)) {
				var5[var8] = true;
				++var6;
			}
		}

		StringBuffer var10 = new StringBuffer();

		for(int var9 = 0; var9 < 4; ++var9) {
			if(var5[var9]) {
				if(var10.length() > 0) {
					var10.append(", ");
				}

				var10.append(var4[var9]);
			}
		}

		var3.lastDirectionsText = var10.toString();
		return var6;
	}

	private static boolean spawnOne(World var0, EntityPlayer var1, int var2, int var3) {
		Random var4 = var0.rand;
		double var5 = var1.posX;
		double var7 = var1.posZ;
		int var9 = 80 + var4.nextInt(71);
		int var10 = var4.nextInt(37) - 18;
		int var11 = MathHelper.floor_double(var5);
		int var12 = MathHelper.floor_double(var7);
		if(var2 == 0) {
			var12 -= var9;
			var11 += var10;
		} else if(var2 == 1) {
			var12 += var9;
			var11 += var10;
		} else if(var2 == 2) {
			var11 += var9;
			var12 += var10;
		} else {
			var11 -= var9;
			var12 += var10;
		}

		int var13 = var0.findTopSolidBlock(var11, var12);
		if(var13 < 1) {
			return false;
		} else {
			EntityMob var14 = createWaveMob(var0);
			var14.redWaveMob = true;
			if(var14 instanceof EntityCreeper) {
				var14.redWaveCreeper = true;
			}

			var14.setLocationAndAngles((double)var11 + 0.5D, (double)var13, (double)var12 + 0.5D, var4.nextFloat() * 360.0F, 0.0F);
			var14.setTarget(var1);
			PathEntity var15 = var0.getPathToEntity(var14, var1, 96.0F);
			if(var15 != null) {
				var14.setPathToEntity(var15);
			}

			var0.entityJoinedWorld(var14);
			return true;
		}
	}

	private static EntityMob createWaveMob(World var0) {
		switch(var0.rand.nextInt(4)) {
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

	private static long pack(int var0, int var1, int var2) {
		return ((long)var0 & 33554431L) << 38 | ((long)var2 & 33554431L) << 12 | (long)(var1 & 4095);
	}

	private static final class RedWaveState {
		public int waveNumber;
		public boolean waitForSleepTrigger = true;
		public long nextWaveDay;
		public String lastDirectionsText = "NONE";
		public final Set playerBuiltBlocks = new HashSet();
	}
}
