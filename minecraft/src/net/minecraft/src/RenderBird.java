package net.minecraft.src;

public class RenderBird extends RenderChicken {
	public RenderBird(ModelBase var1, float var2) {
		super(var1, var2);
	}

	protected int getColorMultiplier(EntityLiving var1, float var2, float var3) {
		if(var1 instanceof EntitySeagull) {
			return 1719703280;
		} else if(var1 instanceof EntityCrow) {
			return 1721349138;
		} else {
			return var1 instanceof EntityHawk ? 1719305800 : 0;
		}
	}
}
