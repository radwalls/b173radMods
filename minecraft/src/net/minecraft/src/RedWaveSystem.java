package net.minecraft.src;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class RedWaveSystem {
	private static final HashMap worldStates = new HashMap();
	private static final int WAVE_INTERVAL_DAYS = 2;

	private RedWaveSystem() {
	}

	private static RedWaveState getState(World var0) {
		Integer var1 = Integer.valueOf(System.identityHashCode(var0));
		RedWaveState var2 = (RedWaveState)worldStates.get(var1);
		if(var2 == null) {
			var2 = new RedWaveState();
			worldStates.put(var1, var2);
		}

		return var2;
	}

	public static void onWorldTick(World var0) {
		if(var0.multiplayerWorld || var0.playerEntities.size() == 0) {
			return;
		}

		RedWaveState var1 = getState(var0);
		if(var1.firstWavePendingSleep) {
			return;
		}

		long var2 = var0.getWorldTime() / 24000L;
		if(var2 - var1.lastWaveDay >= (long)WAVE_INTERVAL_DAYS) {
			EntityPlayer var4 = (EntityPlayer)var0.playerEntities.get(0);
			startWave(var0, var4, false);
		}
	}

	public static void onPlayerWake(EntityPlayer var0, boolean var1, boolean var2, boolean var3) {
		if(var0.worldObj.multiplayerWorld) {
			return;
		}

		RedWaveState var4 = getState(var0.worldObj);
		if(var4.firstWavePendingSleep && var3 && var0.isEntityAlive()) {
			startWave(var0.worldObj, var0, true);
			var4.firstWavePendingSleep = false;
		}
	}

	public static void onPlayerPlacedBlock(World var0, int var1, int var2, int var3) {
		if(var0.multiplayerWorld) {
			return;
		}

		RedWaveState var4 = getState(var0);
		var4.playerBuiltBlocks.add(Long.valueOf(pack(var1, var2, var3)));
	}

	public static void updateWaveMob(EntityMob var0) {
		if(!var0.redWaveMob || var0.worldObj.multiplayerWorld || !var0.isEntityAlive()) {
			return;
		}
		var0.entityAge = 0;

		if((var0.ticksExisted + var0.entityId) % 10 == 0) {
			EntityPlayer var1 = var0.worldObj.getClosestPlayerToEntity(var0, 128.0D);
			if(var1 != null) {
				var0.setTarget(var1);
				var0.setPathToEntity(var0.worldObj.getPathToEntity(var0, var1, 64.0F));
			}
		}

		tryBreakIntoStructures(var0);
	}

	public static boolean isRedWaveMob(EntityLiving var0) {
		return var0 instanceof EntityMob && ((EntityMob)var0).redWaveMob;
	}

	public static boolean isRedCreeper(EntityCreeper var0) {
		return var0.redWaveMob && var0.redWaveCreeper;
	}

	private static void startWave(World var0, EntityPlayer var1, boolean var2) {
		RedWaveState var3 = getState(var0);
		++var3.waveCount;
		long var4 = var0.getWorldTime() / 24000L;
		var3.lastWaveDay = var4;
		int var6;
		if(var2) {
			var6 = 80 + var0.rand.nextInt(41);
		} else {
			var6 = 16 + var3.waveCount * 8 + var0.rand.nextInt(8);
		}

		int var7 = spawnWaveMobs(var0, var1, var6, var3.waveCount);
		String var8 = buildDirectionList(var3.lastWaveDirections);
		sendToAllPlayers(var0, "\u00a74HEROBRINE: RED WAVE HAS STARTED. SURVIVE THE RED WAVES...");
		sendToAllPlayers(var0, "\u00a74HEROBRINE: Spawned " + var7 + " mobs from the edges: " + var8);
		var0.playSoundAtEntity(var1, "ambient.cave.cave", 1.2F, 0.5F);
	}

	private static int spawnWaveMobs(World var0, EntityPlayer var1, int var2, int var3) {
		RedWaveState var4 = getState(var0);
		var4.lastWaveDirections.clear();
		int var5 = 0;
		Direction[] var6 = Direction.values();

		for(int var7 = 0; var7 < var2; ++var7) {
			Direction var8 = var6[var7 % var6.length];
			EntityMob var9 = createWaveMob(var0, var7);
			if(var9 == null) {
				continue;
			}

			if(placeMobAtEdge(var0, var1, var9, var8)) {
				markAsWaveMob(var9, var3);
				var9.setTarget(var1);
				var0.entityJoinedWorld(var9);
				var4.lastWaveDirections.add(var8);
				++var5;
			}
		}

		return var5;
	}

	private static EntityMob createWaveMob(World var0, int var1) {
		int var2 = var0.rand.nextInt(100);
		if(var2 < 35) {
			return new EntityZombie(var0);
		} else if(var2 < 65) {
			return new EntitySkeleton(var0);
		} else if(var2 < 90) {
			return new EntitySpider(var0);
		} else {
			return new EntityCreeper(var0);
		}
	}

	private static void markAsWaveMob(EntityMob var0, int var1) {
		var0.redWaveMob = true;
		var0.redWaveLevel = var1;
		if(var0 instanceof EntityCreeper) {
			var0.redWaveCreeper = true;
			var0.moveSpeed = 0.95F;
		} else {
			var0.moveSpeed = Math.max(var0.moveSpeed, 0.35F);
		}
	}

	private static boolean placeMobAtEdge(World var0, EntityPlayer var1, EntityMob var2, Direction var3) {
		Random var4 = var0.rand;
		int var5 = 80 + var4.nextInt(71);
		int var6 = (int)Math.floor(var1.posX);
		int var7 = (int)Math.floor(var1.posZ);
		int var8 = var4.nextInt(49) - 24;
		int var9 = var6;
		int var10 = var7;
		switch(var3) {
		case NORTH:
			var10 -= var5;
			var9 += var8;
			break;
		case SOUTH:
			var10 += var5;
			var9 += var8;
			break;
		case EAST:
			var9 += var5;
			var10 += var8;
			break;
		case WEST:
			var9 -= var5;
			var10 += var8;
		}

		int var11 = var0.getHeightValue(var9, var10);
		if(var11 < 1) {
			var11 = 64;
		}

		var2.setLocationAndAngles((double)var9 + 0.5D, (double)var11, (double)var10 + 0.5D, var4.nextFloat() * 360.0F, 0.0F);
		return true;
	}

	private static void tryBreakIntoStructures(EntityMob var0) {
		Entity var1 = var0.getTarget();
		if(!(var1 instanceof EntityPlayer)) {
			return;
		}

		World var2 = var0.worldObj;
		int var3 = MathHelper.floor_double(var0.posX);
		int var4 = MathHelper.floor_double(var0.posY + 0.2D);
		int var5 = MathHelper.floor_double(var0.posZ);
		Vec3D var6 = Vec3D.createVector(var0.posX, var0.posY + (double)var0.getEyeHeight(), var0.posZ);
		Vec3D var7 = Vec3D.createVector(var1.posX, var1.posY + (double)var1.getEyeHeight(), var1.posZ);
		MovingObjectPosition var8 = var2.rayTraceBlocks(var6, var7);
		int var9 = var3;
		int var10 = var4;
		int var11 = var5;
		if(var8 != null) {
			var9 = var8.blockX;
			var10 = var8.blockY;
			var11 = var8.blockZ;
		} else if(var0.isCollidedHorizontally) {
			var9 = MathHelper.floor_double(var0.posX + var0.motionX * 2.0D);
			var11 = MathHelper.floor_double(var0.posZ + var0.motionZ * 2.0D);
		} else {
			return;
		}

		int var12 = var2.getBlockId(var9, var10, var11);
		if(var12 <= 0 || var12 == Block.bedrock.blockID) {
			return;
		}

		int var13 = getClusterStrength(var2, var9, var10, var11, 4);
		if(var13 < 10 && !var0.redWaveCreeper) {
			return;
		}

		++var0.redWaveBreakProgress;
		int var14 = var0.redWaveCreeper ? 12 : 55;
		if(var13 > 25) {
			var14 -= 8;
		}

		if(var0.redWaveBreakProgress >= var14) {
			var0.redWaveBreakProgress = 0;
			var2.setBlockWithNotify(var9, var10, var11, 0);
		}
	}

	private static int getClusterStrength(World var0, int var1, int var2, int var3, int var4) {
		RedWaveState var5 = getState(var0);
		int var6 = 0;

		for(int var7 = -var4; var7 <= var4; ++var7) {
			for(int var8 = -2; var8 <= 2; ++var8) {
				for(int var9 = -var4; var9 <= var4; ++var9) {
					if(var5.playerBuiltBlocks.contains(Long.valueOf(pack(var1 + var7, var2 + var8, var3 + var9)))) {
						++var6;
					}
				}
			}
		}

		return var6;
	}

	private static String buildDirectionList(EnumSet var0) {
		List var1 = new ArrayList();
		if(var0.contains(Direction.NORTH)) {
			var1.add("NORTH");
		}

		if(var0.contains(Direction.SOUTH)) {
			var1.add("SOUTH");
		}

		if(var0.contains(Direction.EAST)) {
			var1.add("EAST");
		}

		if(var0.contains(Direction.WEST)) {
			var1.add("WEST");
		}

		if(var1.isEmpty()) {
			return "NONE";
		}

		StringBuffer var2 = new StringBuffer();
		for(int var3 = 0; var3 < var1.size(); ++var3) {
			if(var3 > 0) {
				var2.append(", ");
			}

			var2.append((String)var1.get(var3));
		}

		return var2.toString();
	}

	private static void sendToAllPlayers(World var0, String var1) {
		Iterator var2 = var0.playerEntities.iterator();

		while(var2.hasNext()) {
			EntityPlayer var3 = (EntityPlayer)var2.next();
			var3.addChatMessage(var1);
		}
	}

	private static long pack(int var0, int var1, int var2) {
		long var3 = ((long)var0 & 33554431L) << 38;
		long var5 = ((long)var1 & 4095L) << 26;
		long var7 = (long)var2 & 67108863L;
		return var3 | var5 | var7;
	}

	private static class RedWaveState {
		public boolean firstWavePendingSleep = true;
		public int waveCount = 0;
		public long lastWaveDay = -9999L;
		public final EnumSet lastWaveDirections = EnumSet.noneOf(Direction.class);
		public final HashSet playerBuiltBlocks = new HashSet();
	}

	private static enum Direction {
		NORTH,
		SOUTH,
		EAST,
		WEST
	}
}
