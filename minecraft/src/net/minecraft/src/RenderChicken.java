package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public class RenderChicken extends RenderLiving {
	public RenderChicken(ModelBase var1, float var2) {
		super(var1, var2);
	}

	public void renderChicken(EntityChicken var1, double var2, double var4, double var6, float var8, float var9) {
		if(var1 instanceof EntitySeagull) {
			GL11.glColor4f(0.95F, 0.95F, 1.0F, 1.0F);
		} else if(var1 instanceof EntityCrow) {
			GL11.glColor4f(0.35F, 0.35F, 0.38F, 1.0F);
		} else if(var1 instanceof EntityHawk) {
			GL11.glColor4f(0.72F, 0.58F, 0.34F, 1.0F);
		}

		super.doRenderLiving(var1, var2, var4, var6, var8, var9);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	protected float getWingRotation(EntityChicken var1, float var2) {
		float var3 = var1.field_756_e + (var1.field_752_b - var1.field_756_e) * var2;
		float var4 = var1.field_757_d + (var1.destPos - var1.field_757_d) * var2;
		return (MathHelper.sin(var3) + 1.0F) * var4;
	}

	protected float func_170_d(EntityLiving var1, float var2) {
		return this.getWingRotation((EntityChicken)var1, var2);
	}

	public void doRenderLiving(EntityLiving var1, double var2, double var4, double var6, float var8, float var9) {
		this.renderChicken((EntityChicken)var1, var2, var4, var6, var8, var9);
	}

	public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9) {
		this.renderChicken((EntityChicken)var1, var2, var4, var6, var8, var9);
	}
}
