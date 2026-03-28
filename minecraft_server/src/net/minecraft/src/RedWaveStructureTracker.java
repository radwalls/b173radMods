package net.minecraft.src;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RedWaveStructureTracker {
	private final Set trackedBlocks = new HashSet();
	private final Map chunkScores = new HashMap();

	public void onPlayerPlacedBlock(int var1, int var2, int var3) {
		long var4 = packBlock(var1, var2, var3);
		if(this.trackedBlocks.add(Long.valueOf(var4))) {
			long var6 = packChunk(var1 >> 4, var3 >> 4);
			Integer var8 = (Integer)this.chunkScores.get(Long.valueOf(var6));
			this.chunkScores.put(Long.valueOf(var6), Integer.valueOf(var8 == null ? 1 : var8.intValue() + 1));
		}
	}

	public void onPlayerRemovedBlock(int var1, int var2, int var3) {
		long var4 = packBlock(var1, var2, var3);
		if(this.trackedBlocks.remove(Long.valueOf(var4))) {
			long var6 = packChunk(var1 >> 4, var3 >> 4);
			Integer var8 = (Integer)this.chunkScores.get(Long.valueOf(var6));
			if(var8 != null) {
				if(var8.intValue() <= 1) {
					this.chunkScores.remove(Long.valueOf(var6));
				} else {
					this.chunkScores.put(Long.valueOf(var6), Integer.valueOf(var8.intValue() - 1));
				}
			}
		}
	}

	public ChunkCoordinates findBestStructureTargetNear(EntityPlayer var1, int var2) {
		if(var1 == null || this.chunkScores.isEmpty()) {
			return null;
		}

		int var3 = MathHelper.floor_double(var1.posX) >> 4;
		int var4 = MathHelper.floor_double(var1.posZ) >> 4;
		int var5 = Math.max(1, var2 >> 4);
		int var6 = 0;
		long var7 = 0L;
		boolean var9 = false;
		Iterator var10 = this.chunkScores.entrySet().iterator();

		while(var10.hasNext()) {
			Map.Entry var11 = (Map.Entry)var10.next();
			long var12 = ((Long)var11.getKey()).longValue();
			int var14 = unpackChunkX(var12);
			int var15 = unpackChunkZ(var12);
			if(Math.abs(var14 - var3) <= var5 && Math.abs(var15 - var4) <= var5) {
				int var16 = ((Integer)var11.getValue()).intValue();
				if(var16 > 3 && (!var9 || var16 > var6)) {
					var6 = var16;
					var7 = var12;
					var9 = true;
				}
			}
		}

		if(!var9) {
			return null;
		}

		int var17 = (unpackChunkX(var7) << 4) + 8;
		int var18 = (unpackChunkZ(var7) << 4) + 8;
		return new ChunkCoordinates(var17, 0, var18);
	}

	private static long packBlock(int var0, int var1, int var2) {
		return ((long)var0 & 67108863L) << 38 | ((long)var2 & 67108863L) << 12 | (long)(var1 & 4095);
	}

	private static long packChunk(int var0, int var1) {
		return ((long)var0 & 4294967295L) << 32 | (long)var1 & 4294967295L;
	}

	private static int unpackChunkX(long var0) {
		return (int)(var0 >> 32);
	}

	private static int unpackChunkZ(long var0) {
		return (int)var0;
	}
}
