package net.minecraft.src;

import java.awt.image.BufferedImage;

public final class SpyglassIconTexture {
	private static int textureId = -1;

	private SpyglassIconTexture() {
	}

	public static int getTextureId(RenderEngine var0) {
		if(textureId < 0) {
			textureId = var0.allocateAndSetupTexture(createImage());
		}

		return textureId;
	}

	private static BufferedImage createImage() {
		BufferedImage var0 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		int[] var1 = new int[]{0x00000000, 0xFF2D2A5A, 0xFF3E3A7B, 0xFF1A183A, 0xFFB58A31, 0xFF8D6C26, 0xFFE4BA55, 0xFF6B5420, 0xFFE7E2B8, 0xFF7FB4CF, 0xFF9FD5EC};

		for(int var2 = 0; var2 < 16; ++var2) {
			for(int var3 = 0; var3 < 16; ++var3) {
				var0.setRGB(var3, var2, var1[0]);
			}
		}

		for(int var4 = 0; var4 < 12; ++var4) {
			int var5 = 2 + var4;
			int var6 = 13 - var4;
			drawPixel(var0, var5, var6, var1[4]);
			drawPixel(var0, var5 - 1, var6 + 1, var1[4]);
			drawPixel(var0, var5 + 1, var6 - 1, var1[4]);
			drawPixel(var0, var5, var6 + 1, var1[6]);
			drawPixel(var0, var5 - 1, var6 + 2, var1[6]);
			drawPixel(var0, var5, var6 - 1, var1[5]);
			drawPixel(var0, var5 + 1, var6 - 2, var1[5]);
		}

		for(int var7 = 0; var7 < 4; ++var7) {
			drawPixel(var0, 12 + var7, 2 + var7, var1[1]);
			drawPixel(var0, 11 + var7, 3 + var7, var1[2]);
			drawPixel(var0, 12 + var7, 1 + var7, var1[3]);
		}

		drawPixel(var0, 14, 4, var1[9]);
		drawPixel(var0, 15, 3, var1[9]);
		drawPixel(var0, 15, 4, var1[10]);
		drawPixel(var0, 13, 5, var1[10]);
		drawPixel(var0, 14, 5, var1[8]);

		for(int var8 = 0; var8 < 3; ++var8) {
			drawPixel(var0, var8, 15 - var8, var1[7]);
		}

		return var0;
	}

	private static void drawPixel(BufferedImage var0, int var1, int var2, int var3) {
		if(var1 >= 0 && var1 < 16 && var2 >= 0 && var2 < 16) {
			var0.setRGB(var1, var2, var3);
		}
	}
}
