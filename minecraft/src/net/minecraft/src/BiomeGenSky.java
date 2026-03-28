package net.minecraft.src;

public class BiomeGenSky extends BiomeGenBase {
	public BiomeGenSky() {
		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 5000));
	}

	public int getSkyColorByTemp(float var1) {
		return 12632319;
	}
}
