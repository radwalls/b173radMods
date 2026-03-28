package net.minecraft.src;

public class RenderWaveZombie extends RenderBiped {
	public RenderWaveZombie(ModelBiped var1, float var2) {
		super(var1, var2);
	}

	protected int getColorMultiplier(EntityLiving var1, float var2, float var3) {
		return var1 instanceof IWaveMob ? (200 << 24) + (255 << 16) + (32 << 8) + 32 : super.getColorMultiplier(var1, var2, var3);
	}
}
