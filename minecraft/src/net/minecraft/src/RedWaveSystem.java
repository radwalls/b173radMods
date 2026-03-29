package net.minecraft.src;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class RedWaveSystem {
	private static final int WAVE_INTERVAL_TICKS = 48000;
	private static final int EDGE_MIN_RADIUS = 80;
	private static final int EDGE_MAX_RADIUS = 150;
	private static final String[] DIRECTIONS = new String[]{"NORTH", "SOUTH", "EAST", "WEST"};
	private boolean triggerWaveAfterNextSleep = true;
	private long nextWaveWorldTime = -1L;
	private int completedWaves = 0;

	public void tick(World var1) {
		if(var1.multiplayerWorld || var1.playerEntities.isEmpty()) {
			return;
		}

		long var2 = var1.getWorldInfo().getWorldTime();
		if(!this.triggerWaveAfterNextSleep && this.nextWaveWorldTime > 0L && var2 >= this.nextWaveWorldTime) {
			this.startWave(var1, false);
		}
	}

	public void onPlayerWokeUp(World var1, EntityPlayer var2) {
		if(var1.multiplayerWorld) {
			return;
		}

		if(this.triggerWaveAfterNextSleep) {
			this.startWave(var1, true);
		}
	}

	private void startWave(World var1, boolean var2) {
		EntityPlayer var3 = this.getPrimaryTarget(var1);
		if(var3 == null) {
			return;
		}

		int var4;
		if(var2) {
			var4 = 80 + var1.rand.nextInt(41);
		} else {
			var4 = 18 + this.completedWaves * 10 + var1.rand.nextInt(6);
		}

		LinkedHashSet var5 = new LinkedHashSet();
		int var6 = this.spawnWaveMobs(var1, var3, var4, var5);
		if(var6 <= 0) {
			return;
		}

		this.triggerWaveAfterNextSleep = false;
		++this.completedWaves;
		long var7 = var1.getWorldInfo().getWorldTime();
		this.nextWaveWorldTime = var7 + (long)WAVE_INTERVAL_TICKS;
		this.broadcastWaveMessage(var1, var6, var5);
		var1.playSoundAtEntity(var3, "ambient.cave.cave", 1.4F, 0.5F + var1.rand.nextFloat() * 0.2F);
	}

	private EntityPlayer getPrimaryTarget(World var1) {
		if(var1.playerEntities.isEmpty()) {
			return null;
		} else {
			return (EntityPlayer)var1.playerEntities.get(0);
		}
	}

	private int spawnWaveMobs(World var1, EntityPlayer var2, int var3, Set var4) {
		int var5 = 0;
		int var6 = Math.max(4, var3 / 4);

		for(int var7 = 0; var7 < 4; ++var7) {
			int var8 = var7 == 3 ? var3 - var5 : var6;
			for(int var9 = 0; var9 < var8; ++var9) {
				int var10 = EDGE_MIN_RADIUS + var1.rand.nextInt(EDGE_MAX_RADIUS - EDGE_MIN_RADIUS + 1);
				int var11 = MathHelper.floor_double(var2.posX);
				int var12 = MathHelper.floor_double(var2.posZ);
				if(var7 == 0) {
					var12 -= var10;
					var11 += var1.rand.nextInt(31) - 15;
				} else if(var7 == 1) {
					var12 += var10;
					var11 += var1.rand.nextInt(31) - 15;
				} else if(var7 == 2) {
					var11 += var10;
					var12 += var1.rand.nextInt(31) - 15;
				} else {
					var11 -= var10;
					var12 += var1.rand.nextInt(31) - 15;
				}

				int var13 = var1.getHeightValue(var11, var12);
				if(var13 <= 0) {
					continue;
				}

				EntityMob var14 = this.createWaveMob(var1);
				if(var14 == null) {
					continue;
				}

				var14.setPosition((double)var11 + 0.5D, (double)var13, (double)var12 + 0.5D);
				if(!var14.getCanSpawnHere()) {
					var14.setPosition((double)var11 + 0.5D, (double)(var13 + 2), (double)var12 + 0.5D);
				}

				if(!var14.getCanSpawnHere()) {
					continue;
				}

				var14.rotationYaw = var1.rand.nextFloat() * 360.0F;
				var14.setRedWaveMob(true);
				var14.setTarget(var2);
				var14.setPathToEntity(var1.getPathToEntity(var14, var2, 96.0F));
				var1.entityJoinedWorld(var14);
				++var5;
				var4.add(DIRECTIONS[var7]);
			}
		}

		return var5;
	}

	private EntityMob createWaveMob(World var1) {
		int var2 = var1.rand.nextInt(100);
		if(var2 < 30) {
			return new EntityZombie(var1);
		} else if(var2 < 55) {
			return new EntitySkeleton(var1);
		} else if(var2 < 80) {
			return new EntitySpider(var1);
		} else {
			EntityCreeper var3 = new EntityCreeper(var1);
			var3.setRedWaveCreeper(true);
			return var3;
		}
	}

	private void broadcastWaveMessage(World var1, int var2, Set var3) {
		String var4 = this.buildDirectionList(var3);
		Iterator var5 = var1.playerEntities.iterator();

		while(var5.hasNext()) {
			EntityPlayer var6 = (EntityPlayer)var5.next();
			var6.addChatMessage("HEROBRINE: RED WAVE HAS STARTED. SURVIVE THE RED WAVES...");
			var6.addChatMessage("HEROBRINE: Spawned " + var2 + " mobs from the edges: " + var4);
		}
	}

	private String buildDirectionList(Set var1) {
		StringBuffer var2 = new StringBuffer();
		Iterator var3 = var1.iterator();

		while(var3.hasNext()) {
			if(var2.length() > 0) {
				var2.append(", ");
			}

			var2.append((String)var3.next());
		}

		if(var2.length() == 0) {
			return "NONE";
		} else {
			return var2.toString();
		}
	}
}
