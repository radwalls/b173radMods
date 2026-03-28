package net.minecraft.src;

import net.minecraft.client.Minecraft;

public class PlayerControllerSP extends PlayerController {
	private int field_1074_c = -1;
	private int field_1073_d = -1;
	private int field_1072_e = -1;
	private float curBlockDamage = 0.0F;
	private float prevBlockDamage = 0.0F;
	private float field_1069_h = 0.0F;
	private int blockHitWait = 0;
	private int lastMinedBlockX = -1;
	private int lastMinedBlockY = -1;
	private int lastMinedBlockZ = -1;
	private float lastMinedBlockDamage = 0.0F;

	public PlayerControllerSP(Minecraft var1) {
		super(var1);
	}

	public void flipPlayer(EntityPlayer var1) {
		var1.rotationYaw = -180.0F;
	}

	public boolean sendBlockRemoved(int var1, int var2, int var3, int var4) {
		int var5 = this.mc.theWorld.getBlockId(var1, var2, var3);
		int var6 = this.mc.theWorld.getBlockMetadata(var1, var2, var3);
		boolean var7 = super.sendBlockRemoved(var1, var2, var3, var4);
		ItemStack var8 = this.mc.thePlayer.getCurrentEquippedItem();
		boolean var9 = this.mc.thePlayer.canHarvestBlock(Block.blocksList[var5]);
		if(var8 != null) {
			var8.onDestroyBlock(var5, var1, var2, var3, this.mc.thePlayer);
			if(var8.stackSize == 0) {
				var8.func_1097_a(this.mc.thePlayer);
				this.mc.thePlayer.destroyCurrentEquippedItem();
			}
		}

		if(var7 && var9) {
			Block.blocksList[var5].harvestBlock(this.mc.theWorld, this.mc.thePlayer, var1, var2, var3, var6);
		}

		if(var7 && var1 == this.lastMinedBlockX && var2 == this.lastMinedBlockY && var3 == this.lastMinedBlockZ) {
			this.lastMinedBlockDamage = 0.0F;
		}

		return var7;
	}

	public void clickBlock(int var1, int var2, int var3, int var4) {
		this.mc.theWorld.onBlockHit(this.mc.thePlayer, var1, var2, var3, var4);
		int var5 = this.mc.theWorld.getBlockId(var1, var2, var3);
		if(var5 > 0 && this.curBlockDamage == 0.0F) {
			Block.blocksList[var5].onBlockClicked(this.mc.theWorld, var1, var2, var3, this.mc.thePlayer);
		}

		if(var5 > 0 && Block.blocksList[var5].blockStrength(this.mc.thePlayer) >= 1.0F) {
			this.sendBlockRemoved(var1, var2, var3, var4);
		}

	}

	public void resetBlockRemoving() {
		this.storeCurrentBlockDamage();
		this.curBlockDamage = 0.0F;
		this.blockHitWait = 0;
	}

	public void sendBlockRemoving(int var1, int var2, int var3, int var4) {
		if(this.blockHitWait > 0) {
			--this.blockHitWait;
		} else {
			if(var1 == this.field_1074_c && var2 == this.field_1073_d && var3 == this.field_1072_e) {
				int var5 = this.mc.theWorld.getBlockId(var1, var2, var3);
				if(var5 == 0) {
					return;
				}

				Block var6 = Block.blocksList[var5];
				this.curBlockDamage += var6.blockStrength(this.mc.thePlayer);
				if(this.field_1069_h % 4.0F == 0.0F && var6 != null) {
					this.mc.sndManager.playSound(var6.stepSound.func_1145_d(), (float)var1 + 0.5F, (float)var2 + 0.5F, (float)var3 + 0.5F, (var6.stepSound.getVolume() + 1.0F) / 8.0F, var6.stepSound.getPitch() * 0.5F);
				}

				++this.field_1069_h;
				if(this.curBlockDamage >= 1.0F) {
					this.sendBlockRemoved(var1, var2, var3, var4);
					this.curBlockDamage = 0.0F;
					this.prevBlockDamage = 0.0F;
					this.field_1069_h = 0.0F;
					this.blockHitWait = 5;
				}
			} else {
				this.storeCurrentBlockDamage();
				this.curBlockDamage = 0.0F;
				this.prevBlockDamage = 0.0F;
				this.field_1069_h = 0.0F;
				this.field_1074_c = var1;
				this.field_1073_d = var2;
				this.field_1072_e = var3;
				this.curBlockDamage = this.getStoredBlockDamage(var1, var2, var3);
				int var5 = this.mc.theWorld.getBlockId(var1, var2, var3);
				if(var5 > 0) {
					this.curBlockDamage += this.consumeChargedMiningBonus();
					if(this.curBlockDamage > 0.95F) {
						this.curBlockDamage = 0.95F;
					}
				}

				this.prevBlockDamage = this.curBlockDamage;
			}

		}
	}

	public void setPartialTime(float var1) {
		float var2;
		if(this.curBlockDamage > 0.0F) {
			var2 = this.prevBlockDamage + (this.curBlockDamage - this.prevBlockDamage) * var1;
		} else {
			var2 = this.getStoredBlockDamageFromMouseOver();
		}

		if(var2 <= 0.0F) {
			this.mc.ingameGUI.damageGuiPartialTime = 0.0F;
			this.mc.renderGlobal.damagePartialTime = 0.0F;
		} else {
			this.mc.ingameGUI.damageGuiPartialTime = var2;
			this.mc.renderGlobal.damagePartialTime = var2;
		}

	}

	public float getBlockReachDistance() {
		return 4.0F;
	}

	public void func_717_a(World var1) {
		super.func_717_a(var1);
	}

	public void updateController() {
		this.prevBlockDamage = this.curBlockDamage;
		if((!this.mc.inGameHasFocus || this.blockHitWait > 0 || this.field_1074_c != this.lastMinedBlockX || this.field_1073_d != this.lastMinedBlockY || this.field_1072_e != this.lastMinedBlockZ || this.curBlockDamage <= 0.0F) && this.lastMinedBlockDamage > 0.0F) {
			this.lastMinedBlockDamage -= 0.01F;
			if(this.lastMinedBlockDamage < 0.0F) {
				this.lastMinedBlockDamage = 0.0F;
			}
		}

		this.mc.sndManager.playRandomMusicIfReady();
	}

	private void storeCurrentBlockDamage() {
		if(this.field_1074_c >= 0 && this.field_1073_d >= 0 && this.field_1072_e >= 0 && this.curBlockDamage > 0.0F) {
			this.lastMinedBlockX = this.field_1074_c;
			this.lastMinedBlockY = this.field_1073_d;
			this.lastMinedBlockZ = this.field_1072_e;
			this.lastMinedBlockDamage = this.curBlockDamage;
		}
	}

	private float getStoredBlockDamage(int var1, int var2, int var3) {
		return var1 == this.lastMinedBlockX && var2 == this.lastMinedBlockY && var3 == this.lastMinedBlockZ ? this.lastMinedBlockDamage : 0.0F;
	}

	private float getStoredBlockDamageFromMouseOver() {
		return this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE ? this.getStoredBlockDamage(this.mc.objectMouseOver.blockX, this.mc.objectMouseOver.blockY, this.mc.objectMouseOver.blockZ) : 0.0F;
	}
}
