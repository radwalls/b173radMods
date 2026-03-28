package net.minecraft.src;

public class ItemPickaxe extends ItemTool {
	private static Block[] blocksEffectiveAgainst = new Block[]{Block.cobblestone, Block.stairDouble, Block.stairSingle, Block.stone, Block.sandStone, Block.cobblestoneMossy, Block.oreIron, Block.blockSteel, Block.oreCoal, Block.blockGold, Block.oreGold, Block.oreDiamond, Block.blockDiamond, Block.ice, Block.netherrack, Block.oreLapis, Block.blockLapis};

	protected ItemPickaxe(int var1, EnumToolMaterial var2) {
		super(var1, 2, var2, blocksEffectiveAgainst);
	}

	public boolean canHarvestBlock(Block var1) {
		return var1 == Block.obsidian ? this.toolMaterial.getHarvestLevel() == 3 : (var1 != Block.blockDiamond && var1 != Block.oreDiamond ? (var1 != Block.blockGold && var1 != Block.oreGold ? (var1 != Block.blockSteel && var1 != Block.oreIron ? (var1 != Block.blockLapis && var1 != Block.oreLapis ? (var1 != Block.oreRedstone && var1 != Block.oreRedstoneGlowing ? (var1.blockMaterial == Material.rock ? true : var1.blockMaterial == Material.iron) : this.toolMaterial.getHarvestLevel() >= 2) : this.toolMaterial.getHarvestLevel() >= 1) : this.toolMaterial.getHarvestLevel() >= 1) : this.toolMaterial.getHarvestLevel() >= 2) : this.toolMaterial.getHarvestLevel() >= 2);
	}

	public int getChargedMiningMaxTicks() {
		switch(this.toolMaterial) {
		case GOLD:
			return 12;
		case EMERALD:
			return 20;
		case IRON:
			return 24;
		case STONE:
			return 28;
		case WOOD:
		default:
			return 32;
		}
	}

	public float getChargedMiningDamageBonus(float var1) {
		float var2;
		switch(this.toolMaterial) {
		case GOLD:
			var2 = 0.45F;
			break;
		case EMERALD:
			var2 = 0.38F;
			break;
		case IRON:
			var2 = 0.30F;
			break;
		case STONE:
			var2 = 0.22F;
			break;
		case WOOD:
		default:
			var2 = 0.14F;
		}

		return var2 * var1;
	}
}
