package net.minecraft.src;

import java.util.Random;
import net.minecraft.client.Minecraft;

public class HerobrineController {
	private final Random rand = new Random();
	private EntityOtherPlayerMP apparition;
	private int alignmentStage = 0;
	private int stageTicks = 0;
	private int alignmentCharges = 0;
	private int cooldownTicks = 600;
	private boolean wasOnGround = false;
	private int stillSneakTicks = 0;
	private float lastYaw = 0.0F;
	private float spunYaw = 0.0F;
	private int spunWindowTicks = 0;
	private boolean hasBeenSeen = false;
	private int lookedAwayTicks = 0;
	private int activeTicks = 0;

	public void update(Minecraft mc) {
		if(mc == null || mc.theWorld == null || mc.thePlayer == null || mc.isGamePaused) {
			return;
		}

		if(this.apparition != null && this.apparition.worldObj != mc.theWorld) {
			this.apparition = null;
			this.hasBeenSeen = false;
			this.lookedAwayTicks = 0;
			this.activeTicks = 0;
		}

		if(this.apparition != null) {
			this.updateApparition(mc);
		} else {
			this.updateAlignment(mc);
			if(this.cooldownTicks > 0) {
				--this.cooldownTicks;
			}

			if(this.alignmentCharges >= 3 && this.cooldownTicks <= 0 && this.rand.nextInt(90) == 0) {
				this.trySpawn(mc);
			}
		}
	}

	private void updateAlignment(Minecraft mc) {
		EntityPlayerSP player = mc.thePlayer;
		boolean jumped = !player.onGround && this.wasOnGround && player.motionY > 0.2D;
		this.wasOnGround = player.onGround;

		if(player.isSneaking() && Math.abs(player.motionX) + Math.abs(player.motionZ) < 0.02D) {
			++this.stillSneakTicks;
		} else {
			this.stillSneakTicks = 0;
		}

		float deltaYaw = player.rotationYaw - this.lastYaw;
		while(deltaYaw > 180.0F) {
			deltaYaw -= 360.0F;
		}

		while(deltaYaw < -180.0F) {
			deltaYaw += 360.0F;
		}

		this.lastYaw = player.rotationYaw;
		this.spunYaw += Math.abs(deltaYaw);
		++this.spunWindowTicks;
		if(this.spunWindowTicks > 40) {
			this.spunWindowTicks = 0;
			this.spunYaw = 0.0F;
		}

		++this.stageTicks;
		if(this.stageTicks > 200) {
			this.resetAlignment();
		}

		if(this.alignmentStage == 0 && jumped) {
			this.nextStage();
		} else if(this.alignmentStage == 1 && this.stillSneakTicks >= 20) {
			this.nextStage();
		} else if(this.alignmentStage == 2 && this.spunYaw >= 300.0F) {
			this.alignmentStage = 0;
			this.stageTicks = 0;
			this.stillSneakTicks = 0;
			this.spunYaw = 0.0F;
			this.spunWindowTicks = 0;
			++this.alignmentCharges;
		}
	}

	private void nextStage() {
		++this.alignmentStage;
		this.stageTicks = 0;
	}

	private void resetAlignment() {
		this.alignmentStage = 0;
		this.stageTicks = 0;
		this.stillSneakTicks = 0;
		this.spunYaw = 0.0F;
		this.spunWindowTicks = 0;
	}

	private void trySpawn(Minecraft mc) {
		EntityPlayerSP player = mc.thePlayer;
		double distance = 28.0D + this.rand.nextDouble() * 14.0D;
		float angle = player.rotationYaw + (float)(this.rand.nextInt(120) - 60);
		double radians = Math.toRadians((double)angle);
		double x = player.posX - Math.sin(radians) * distance;
		double z = player.posZ + Math.cos(radians) * distance;
		int y = MathHelper.floor_double(player.posY);
		int groundY = -1;

		for(int offset = 6; offset >= -8; --offset) {
			int testY = y + offset;
			if(testY > 1 && testY < 126 && mc.theWorld.isBlockOpaqueCube(MathHelper.floor_double(x), testY - 1, MathHelper.floor_double(z)) && !mc.theWorld.isBlockOpaqueCube(MathHelper.floor_double(x), testY, MathHelper.floor_double(z))) {
				groundY = testY;
				break;
			}
		}

		if(groundY < 0) {
			return;
		}

		this.apparition = new EntityOtherPlayerMP(mc.theWorld, "Herobrine");
		this.apparition.motionX = 0.0D;
		this.apparition.motionY = 0.0D;
		this.apparition.motionZ = 0.0D;
		this.apparition.setPosition(x, (double)groundY, z);
		this.apparition.rotationYaw = player.rotationYaw + 180.0F;
		this.apparition.rotationPitch = 0.0F;
		mc.theWorld.entityJoinedWorld(this.apparition);
		this.cooldownTicks = 1200 + this.rand.nextInt(1200);
		this.alignmentCharges = 0;
		this.hasBeenSeen = false;
		this.lookedAwayTicks = 0;
		this.activeTicks = 0;
	}

	private void updateApparition(Minecraft mc) {
		++this.activeTicks;
		if(this.activeTicks > 200) {
			this.despawn(mc);
			return;
		}

		EntityPlayerSP player = mc.thePlayer;
		double dx = this.apparition.posX - player.posX;
		double dz = this.apparition.posZ - player.posZ;
		this.apparition.rotationYaw = (float)(Math.atan2(-dx, dz) * 180.0D / Math.PI);
		boolean lookingAtIt = this.isLookingAt(player, this.apparition);
		if(lookingAtIt) {
			this.hasBeenSeen = true;
			this.lookedAwayTicks = 0;
		} else if(this.hasBeenSeen) {
			++this.lookedAwayTicks;
			if(this.lookedAwayTicks > 8) {
				this.despawn(mc);
			}
		}
	}

	private boolean isLookingAt(EntityPlayerSP player, Entity apparitionEntity) {
		Vec3D look = player.getLookVec();
		if(look == null) {
			return false;
		}

		double tx = apparitionEntity.posX - player.posX;
		double ty = apparitionEntity.posY + (double)apparitionEntity.getEyeHeight() - (player.posY + (double)player.getEyeHeight());
		double tz = apparitionEntity.posZ - player.posZ;
		double length = Math.sqrt(tx * tx + ty * ty + tz * tz);
		if(length < 1.0E-4D) {
			return true;
		}

		double dot = (look.xCoord * tx + look.yCoord * ty + look.zCoord * tz) / length;
		return dot > 0.985D;
	}

	private void despawn(Minecraft mc) {
		if(this.apparition != null && this.apparition.worldObj == mc.theWorld) {
			mc.theWorld.setEntityDead(this.apparition);
		}

		this.apparition = null;
		this.hasBeenSeen = false;
		this.lookedAwayTicks = 0;
		this.activeTicks = 0;
	}
}
