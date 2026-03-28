package net.minecraft.src;

public class BiomeGenSwamp extends BiomeGenBase {
	public BiomeGenSwamp() {
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySeagull.class, 9));
	}
}
