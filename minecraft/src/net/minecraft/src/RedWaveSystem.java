package net.minecraft.src;

public class RedWaveSystem {
	private static final int PLAYER_TRACK_LIMIT = 16384;
	private static final int WAVE_INTERVAL_DAYS = 2;

	public static void onPlayersSlept(World var0) {
		WorldInfo var1 = var0.getWorldInfo();
		if(var1.getRedWaveCount() <= 0) {
			startWave(var0, true);
		}
	}

	public static void tick(World var0) {
		if(var0.multiplayerWorld || var0.playerEntities.isEmpty() || var0.difficultySetting <= 0) {
			return;
		}

		WorldInfo var1 = var0.getWorldInfo();
		if(var1.getRedWaveCount() <= 0) {
			return;
		}

		long var2 = var0.getWorldTime() / 24000L;
		if(var2 >= var1.getNextRedWaveDay() && var0.getWorldTime() % 24000L < 60L) {
			startWave(var0, false);
		}
	}

	private static void startWave(World var0, boolean var1) {
		if(var0.playerEntities.isEmpty()) {
			return;
		}

		WorldInfo var2 = var0.getWorldInfo();
		int var3 = var2.getRedWaveCount() + 1;
		var2.setRedWaveCount(var3);
		var2.setNextRedWaveDay(var0.getWorldTime() / 24000L + (long)WAVE_INTERVAL_DAYS);
		EntityPlayer var4 = (EntityPlayer)var0.playerEntities.get(var0.rand.nextInt(var0.playerEntities.size()));
		int var5 = 8 + var3 * 3;
		if(var1) {
			var5 += 4;
		}

		int var6 = 0;
		int var7 = 0;
		while(var6 < var5 && var7 < var5 * 20) {
			++var7;
			EntityMob var8 = createWaveMob(var0);
			if(var8 == null) {
				continue;
			}

			ChunkPosition var9 = getPerimeterSpawn(var0, var4);
			if(var9 == null) {
				continue;
			}

			var8.setLocationAndAngles((double)var9.x + 0.5D, (double)var9.y, (double)var9.z + 0.5D, var0.rand.nextFloat() * 360.0F, 0.0F);
			if(!var8.getCanSpawnHere()) {
				continue;
			}

			var8.enableRedWaveMob(var3);
			var8.playerToAttack = var4;
			var0.entityJoinedWorld(var8);
			++var6;
		}
	}

	private static EntityMob createWaveMob(World var0) {
		int var1 = var0.rand.nextInt(10);
		if(var1 < 3) {
			return new EntityZombie(var0);
		} else if(var1 < 5) {
			return new EntitySkeleton(var0);
		} else if(var1 < 8) {
			return new EntitySpider(var0);
		} else {
			return new EntityCreeper(var0);
		}
	}

	private static ChunkPosition getPerimeterSpawn(World var0, EntityPlayer var1) {
		int var2 = MathHelper.floor_double(var1.posX);
		int var3 = MathHelper.floor_double(var1.posZ);
		int var4 = 88 + var0.rand.nextInt(48);
		int var5 = var0.rand.nextInt(4);
		int var6 = var2;
		int var7 = var3;
		switch(var5) {
		case 0:
			var6 += var4;
			var7 += var0.rand.nextInt(64) - 32;
			break;
		case 1:
			var6 -= var4;
			var7 += var0.rand.nextInt(64) - 32;
			break;
		case 2:
			var7 += var4;
			var6 += var0.rand.nextInt(64) - 32;
			break;
		default:
			var7 -= var4;
			var6 += var0.rand.nextInt(64) - 32;
		}

		if(!var0.blockExists(var6, 64, var7)) {
			return null;
		}

		int var8 = var0.getHeightValue(var6, var7);
		if(var8 < 1) {
			var8 = 1;
		}

		return new ChunkPosition(var6, var8, var7);
	}

	public static long packPlacedPos(int var0, int var1, int var2) {
		long var3 = (long)(var0 + 33554432) & 67108863L;
		long var5 = (long)var1 & 255L;
		long var7 = (long)(var2 + 33554432) & 67108863L;
		return var3 << 38 | var7 << 12 | var5;
	}

	public static int unpackX(long var0) {
		return (int)((var0 >> 38 & 67108863L) - 33554432L);
	}

	public static int unpackY(long var0) {
		return (int)(var0 & 255L);
	}

	public static int unpackZ(long var0) {
		return (int)((var0 >> 12 & 67108863L) - 33554432L);
	}

	public static int getTrackedLimit() {
		return PLAYER_TRACK_LIMIT;
	}
}
