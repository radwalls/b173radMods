package net.minecraft.src;

public class EntitySeagull extends EntityBird {
	public EntitySeagull(World var1) {
		super(var1);
		this.texture = "/mob/chicken.png";
		this.health = 4;
	}

	protected String getLivingSound() {
		return "mob.chicken";
	}
}
