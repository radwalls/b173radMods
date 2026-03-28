package net.minecraft.src;

public class EntityHawk extends EntityBird {
	public EntityHawk(World var1) {
		super(var1);
		this.texture = "/mob/chicken.png";
		this.health = 6;
	}

	protected String getLivingSound() {
		return "mob.chickenhurt";
	}
}
