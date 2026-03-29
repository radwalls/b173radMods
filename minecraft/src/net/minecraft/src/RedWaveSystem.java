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

		if((var0.ticksExisted + var0.entityId) % 10 == 0) {
			EntityPlayer var1 = var0.worldObj.getClosestPlayerToEntity(var0, -1.0D);
			if(var1 != null) {
				if(var0.getDistanceSqToEntity(var1) < 4096.0D && var0.canEntityBeSeen(var1)) {
					var0.setTarget(var1);
					var0.setPathToEntity(var0.worldObj.getPathToEntity(var0, var1, 64.0F));
				} else {
					ChunkPosition var2 = findNearestBuildBlock(var0.worldObj, var0);
					if(var2 != null) {
						var0.setPathToEntity(var0.worldObj.getEntityPathToXYZ(var0, var2.x, var2.y, var2.z, 48.0F));
					}
				}
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
		if(var0.redWaveCreeper) {
			return;
		}

		World var1 = var0.worldObj;
		ChunkPosition var2 = findContactBuildBlock(var0);
		if(var2 == null) {
			resetBlockBreakProgress(var0);
			return;
		}

		int var3 = var1.getBlockId(var2.x, var2.y, var2.z);
		if(var3 <= 0 || var3 == Block.bedrock.blockID) {
			resetBlockBreakProgress(var0);
			return;
		}

		int var4 = getClusterStrength(var1, var2.x, var2.y, var2.z, 4);
		if(var4 < 3) {
			resetBlockBreakProgress(var0);
			return;
		}

		if(var0.redWaveBreakBlockX != var2.x || var0.redWaveBreakBlockY != var2.y || var0.redWaveBreakBlockZ != var2.z) {
			var0.redWaveBreakBlockX = var2.x;
			var0.redWaveBreakBlockY = var2.y;
			var0.redWaveBreakBlockZ = var2.z;
			var0.redWaveBreakProgress = 0;
		}

		if(!canDamageBlockNow(var0, var2)) {
			return;
		}

		++var0.redWaveBreakProgress;
		if(var0.redWaveBreakProgress >= getRequiredHits(var0, var3)) {
			var0.redWaveBreakProgress = 0;
			var1.setBlockWithNotify(var2.x, var2.y, var2.z, 0);
		}
	}

	private static ChunkPosition findNearestBuildBlock(World var0, EntityMob var1) {
		RedWaveState var2 = getState(var0);
		ChunkPosition var3 = null;
		double var4 = 2304.0D;
		Iterator var6 = var2.playerBuiltBlocks.iterator();

		while(var6.hasNext()) {
			Long var7 = (Long)var6.next();
			int var8 = unpackX(var7.longValue());
			int var9 = unpackY(var7.longValue());
			int var10 = unpackZ(var7.longValue());
			double var11 = (double)var8 + 0.5D - var1.posX;
			double var13 = (double)var9 + 0.5D - var1.posY;
			double var15 = (double)var10 + 0.5D - var1.posZ;
			double var17 = var11 * var11 + var13 * var13 + var15 * var15;
			if(var17 < var4) {
				var3 = new ChunkPosition(var8, var9, var10);
				var4 = var17;
			}
		}

		return var3;
	}

	private static ChunkPosition findContactBuildBlock(EntityMob var0) {
		World var1 = var0.worldObj;
		RedWaveState var2 = getState(var1);
		AxisAlignedBB var3 = var0.boundingBox.expand(0.2D, 0.1D, 0.2D);
		int var4 = MathHelper.floor_double(var3.minX);
		int var5 = MathHelper.floor_double(var3.maxX);
		int var6 = MathHelper.floor_double(var3.minY);
		int var7 = MathHelper.floor_double(var3.maxY);
		int var8 = MathHelper.floor_double(var3.minZ);
		int var9 = MathHelper.floor_double(var3.maxZ);
		ChunkPosition var10 = null;
		double var11 = 9999.0D;

		for(int var13 = var4; var13 <= var5; ++var13) {
			for(int var14 = var6; var14 <= var7; ++var14) {
				for(int var15 = var8; var15 <= var9; ++var15) {
					int var16 = var1.getBlockId(var13, var14, var15);
					if(var16 <= 0 || !var2.playerBuiltBlocks.contains(Long.valueOf(pack(var13, var14, var15)))) {
						continue;
					}

					AxisAlignedBB var17 = Block.blocksList[var16].getCollisionBoundingBoxFromPool(var1, var13, var14, var15);
					if(var17 == null || !var17.intersectsWith(var3)) {
						continue;
					}

					double var18 = ((double)var13 + 0.5D - var0.posX) * ((double)var13 + 0.5D - var0.posX) + ((double)var14 + 0.5D - var0.posY) * ((double)var14 + 0.5D - var0.posY) + ((double)var15 + 0.5D - var0.posZ) * ((double)var15 + 0.5D - var0.posZ);
					if(var18 < var11) {
						var11 = var18;
						var10 = new ChunkPosition(var13, var14, var15);
					}
				}
			}
		}

		return var10;
	}

	private static boolean canDamageBlockNow(EntityMob var0, ChunkPosition var1) {
		if(var0 instanceof EntitySkeleton) {
			Entity var2 = var0.getTarget();
			if(!(var2 instanceof EntityPlayer) || var0.attackTime > 0) {
				return false;
			}

			Vec3D var3 = Vec3D.createVector(var0.posX, var0.posY + (double)var0.getEyeHeight(), var0.posZ);
			Vec3D var4 = Vec3D.createVector(var2.posX, var2.posY + (double)var2.getEyeHeight(), var2.posZ);
			MovingObjectPosition var5 = var0.worldObj.rayTraceBlocks(var3, var4);
			return var5 != null && var5.blockX == var1.x && var5.blockY == var1.y && var5.blockZ == var1.z;
		}

		return var0.isCollidedHorizontally;
	}

	private static int getRequiredHits(EntityMob var0, int var1) {
		Block var2 = Block.blocksList[var1];
		Material var3 = var2 == null ? Material.rock : var2.blockMaterial;
		int var4;
		if(var3 == Material.wood) {
			var4 = 5;
		} else if(var1 == Block.cobblestone.blockID || var3 == Material.rock) {
			var4 = 10;
		} else {
			float var5 = var2 == null ? 1.0F : Math.max(0.5F, var2.blockHardness);
			var4 = 6 + MathHelper.floor_float(var5 * 3.0F);
		}

		return var0 instanceof EntityZombie || var0 instanceof EntitySpider || var0 instanceof EntitySkeleton ? var4 : var4 + 2;
	}

	private static void resetBlockBreakProgress(EntityMob var0) {
		var0.redWaveBreakProgress = 0;
		var0.redWaveBreakBlockX = Integer.MIN_VALUE;
		var0.redWaveBreakBlockY = Integer.MIN_VALUE;
		var0.redWaveBreakBlockZ = Integer.MIN_VALUE;
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

	private static int unpackX(long var0) {
		return (int)(var0 >> 38);
	}

	private static int unpackY(long var0) {
		return (int)(var0 >> 26 & 4095L);
	}

	private static int unpackZ(long var0) {
		int var2 = (int)(var0 << 38 >> 38);
		return var2;
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
