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
	private static final int PLAYER_AGGRO_RANGE = 28;
	private static final int BUILD_AGGRO_RANGE = 96;

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

		if((var0.ticksExisted + var0.entityId) % 10 == 0) {
			EntityPlayer var1 = var0.worldObj.getClosestPlayerToEntity(var0, -1.0D);
			if(var1 != null && var0.getDistanceToEntity(var1) <= (float)PLAYER_AGGRO_RANGE) {
				var0.setTarget(var1);
				var0.setPathToEntity(var0.worldObj.getPathToEntity(var0, var1, 64.0F));
			} else {
				var0.setTarget((Entity)null);
				seekNearestBuildCluster(var0);
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
		int var2 = var0.rand.nextInt(10);
		switch(var2) {
		case 0:
			return new EntityZombie(var0);
		case 1:
		case 2:
		case 3:
			return new EntitySkeleton(var0);
		case 4:
		case 5:
		case 6:
			return new EntitySpider(var0);
		default:
			return var1 % 8 == 0 ? new EntityCreeper(var0) : new EntityZombie(var0);
		}
	}

	private static void markAsWaveMob(EntityMob var0, int var1) {
		var0.redWaveMob = true;
		var0.redWaveLevel = var1;
		if(var0 instanceof EntityCreeper) {
			var0.redWaveCreeper = true;
			var0.moveSpeed = 0.82F;
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
		World var1 = var0.worldObj;
		long var2 = selectAttackableBlock(var0);
		if(var2 == -1L) {
			var0.redWaveBreakProgress = 0;
			return;
		}

		int var3 = unpackX(var2);
		int var4 = unpackY(var2);
		int var5 = unpackZ(var2);
		int var6 = var1.getBlockId(var3, var4, var5);
		if(var6 <= 0 || var6 == Block.bedrock.blockID) {
			var0.redWaveBreakProgress = 0;
			return;
		}

		if(!isPlayerBuiltBlock(var1, var3, var4, var5)) {
			var0.redWaveBreakProgress = 0;
			return;
		}

		if(!isValidAttackContact(var0, var3, var4, var5)) {
			var0.redWaveBreakProgress = 0;
			return;
		}

		++var0.redWaveBreakProgress;
		int var7 = getRequiredHits(var0, var6);
		if(var0.redWaveBreakProgress >= var7) {
			var0.redWaveBreakProgress = 0;
			var1.setBlockWithNotify(var3, var4, var5, 0);
		}
	}

	private static long selectAttackableBlock(EntityMob var0) {
		Entity var1 = var0.getTarget();
		if(var1 instanceof EntityPlayer) {
			return getBlockBetweenMobAndTarget(var0, (EntityPlayer)var1);
		}

		return getFrontCollisionBlock(var0);
	}

	private static long getBlockBetweenMobAndTarget(EntityMob var0, EntityPlayer var1) {
		World var2 = var0.worldObj;
		Vec3D var3 = Vec3D.createVector(var0.posX, var0.posY + (double)var0.getEyeHeight(), var0.posZ);
		Vec3D var4 = Vec3D.createVector(var1.posX, var1.posY + (double)var1.getEyeHeight(), var1.posZ);
		MovingObjectPosition var5 = var2.rayTraceBlocks(var3, var4);
		return var5 == null ? -1L : pack(var5.blockX, var5.blockY, var5.blockZ);
	}

	private static long getFrontCollisionBlock(EntityMob var0) {
		if(!var0.isCollidedHorizontally) {
			return -1L;
		}

		int var1 = MathHelper.floor_double(var0.posX + var0.motionX * 2.0D);
		int var2 = MathHelper.floor_double(var0.posY + 0.2D);
		int var3 = MathHelper.floor_double(var0.posZ + var0.motionZ * 2.0D);
		return pack(var1, var2, var3);
	}

	private static boolean isValidAttackContact(EntityMob var0, int var1, int var2, int var3) {
		double var4 = (double)var1 + 0.5D;
		double var6 = (double)var2 + 0.5D;
		double var8 = (double)var3 + 0.5D;
		double var10 = var0.getDistanceSq(var4, var6, var8);
		if(var0 instanceof EntitySkeleton) {
			return var10 <= 225.0D;
		}

		return var10 <= 4.0D && var0.isCollidedHorizontally;
	}

	private static int getRequiredHits(EntityMob var0, int var1) {
		if(var0.redWaveCreeper) {
			return 1;
		}

		Block var2 = Block.blocksList[var1];
		if(var2 == null) {
			return 6;
		}

		Material var3 = var2.blockMaterial;
		if(var3 == Material.wood || var1 == Block.planks.blockID || var1 == Block.wood.blockID) {
			return 5;
		}

		if(var3 == Material.rock || var1 == Block.cobblestone.blockID || var1 == Block.stone.blockID) {
			return 10;
		}

		return 7;
	}

	private static boolean isPlayerBuiltBlock(World var0, int var1, int var2, int var3) {
		RedWaveState var4 = getState(var0);
		return var4.playerBuiltBlocks.contains(Long.valueOf(pack(var1, var2, var3)));
	}

	private static void seekNearestBuildCluster(EntityMob var0) {
		long var1 = findNearestPlayerBuiltBlock(var0.worldObj, var0, BUILD_AGGRO_RANGE);
		if(var1 == -1L) {
			return;
		}

		int var2 = unpackX(var1);
		int var3 = unpackY(var1);
		int var4 = unpackZ(var1);
		var0.setPathToEntity(var0.worldObj.getEntityPathToXYZ(var0, var2, var3, var4, 64.0F));
	}

	private static long findNearestPlayerBuiltBlock(World var0, EntityMob var1, int var2) {
		RedWaveState var3 = getState(var0);
		if(var3.playerBuiltBlocks.isEmpty()) {
			return -1L;
		}

		double var4 = (double)(var2 * var2);
		double var6 = Double.MAX_VALUE;
		long var8 = 0L;
		Iterator var10 = var3.playerBuiltBlocks.iterator();

		while(var10.hasNext()) {
			Long var11 = (Long)var10.next();
			int var12 = unpackX(var11.longValue());
			int var13 = unpackY(var11.longValue());
			int var14 = unpackZ(var11.longValue());
			double var15 = var1.getDistanceSq((double)var12 + 0.5D, (double)var13 + 0.5D, (double)var14 + 0.5D);
			if(var15 <= var4 && var15 < var6) {
				var6 = var15;
				var8 = var11.longValue();
			}
		}

		return var6 == Double.MAX_VALUE ? -1L : var8;
	}

	private static int unpackX(long var0) {
		int var2 = (int)(var0 >> 38);
		return var2 >= 16777216 ? var2 - 33554432 : var2;
	}

	private static int unpackY(long var0) {
		int var2 = (int)(var0 >> 26 & 4095L);
		return var2 >= 2048 ? var2 - 4096 : var2;
	}

	private static int unpackZ(long var0) {
		int var2 = (int)(var0 & 67108863L);
		return var2 >= 33554432 ? var2 - 67108864 : var2;
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
