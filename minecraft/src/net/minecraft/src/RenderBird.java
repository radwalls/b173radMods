package net.minecraft.src;

public class RenderBird extends RenderChicken {
	private final int birdTint;

	public RenderBird(ModelBase var1, float var2, int var3) {
		super(var1, var2);
		this.birdTint = var3;
	}

	protected int getColorMultiplier(EntityLiving var1, float var2, float var3) {
		return this.birdTint;
	}
}
