package io.github.slimeistdev.volare.content.glider;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import io.github.slimeistdev.volare.infrastructure.QuatPositionInterpolator;
import io.github.slimeistdev.volare.network.VolarePackets;
import io.github.slimeistdev.volare.network.c2s.RotationC2SPacket;
import io.github.slimeistdev.volare.util.MathUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

public class GliderEntity extends VehicleEntity implements QuatEntity {
	private static final EntityAttributeModifier SCALE_MODIFIER = new EntityAttributeModifier(
		Volare.id("glider_scale"),
		0.125 - 1,
		EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);

	protected static final TrackedData<Quaternionf> QUAT_SERVER = DataTracker.registerData(GliderEntity.class, TrackedDataHandlerRegistry.QUATERNION_F);

	private final QuatPositionInterpolator interpolator = new QuatPositionInterpolator(this, 3);
	private final Supplier<Item> itemSupplier;

	private Quaternionf quatClient = new Quaternionf();
	private Quaternionf lastQuatClient = new Quaternionf();

	private final RigidBody rigidBody;
	private final GliderWings wings;
	private final Vector3fc centerOfMass;
	private final Vector3fc centerOfPressure;

	private int pitchControl = 0;
	private int rollControl = 0;

	public static EntityType.EntityFactory<GliderEntity> create(Supplier<Item> itemSupplier) {
		return (entityType, world) -> new GliderEntity(entityType, world, itemSupplier);
	}

	public GliderEntity(EntityType<?> entityType, World world, Supplier<Item> itemSupplier) {
		super(entityType, world);
		this.itemSupplier = itemSupplier;

		var params = GliderParams.create();
		this.rigidBody = params.body();
		this.wings = params.wings();
		this.centerOfMass = params.centerOfMass();
		this.centerOfPressure = params.centerOfPressure();
		Volare.LOG.info(
			"GliderEntity created with center of mass at ({}, {}, {}) and center of pressure at ({}, {}, {}). Mass: {} kg",
			centerOfMass.x()*16, centerOfMass.y()*16, centerOfMass.z()*16,
			centerOfPressure.x()*16, centerOfPressure.y()*16, centerOfPressure.z()*16,
			rigidBody.mass
		);
	}

	public Vector3fc getCenterOfMass() {
		return centerOfMass;
	}

	public Vector3fc getCenterOfPressure() {
		return centerOfPressure;
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);

		builder.add(QUAT_SERVER, new Quaternionf());
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		super.onTrackedDataSet(data);

		if (QUAT_SERVER.equals(data) && getWorld().isClient && !isLogicalSideForUpdatingMovement()) {
			updateTrackedPositionAndAngles$Quat(new Quaternionf(getQuat()));
		}
	}

	@Override
	protected @NotNull Item asItem() {
		return itemSupplier.get();
	}

	@Override
	public @Nullable ItemStack getPickBlockStack() {
		return new ItemStack(asItem());
	}

	@Override
	protected void readCustomData(ReadView view) {
		float roll = view.getFloat("Roll", 0.0f);
		refreshPositionAndAngles$Quat(new MathUtil.EulerAngles(getYaw(), getPitch(), roll).getQuat());
	}

	@Override
	protected void writeCustomData(WriteView view) {
		view.putFloat("Roll", MathUtil.toEuler(getQuat()).roll());
	}

	@Override
	public void animateDamage(float yaw) {
		this.setDamageWobbleSide(-this.getDamageWobbleSide());
		this.setDamageWobbleTicks(10);
		this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0F);
	}

	@Override
	public boolean canHit() {
		return !this.isRemoved();
	}

	@Override
	public @Nullable PositionInterpolator getInterpolator() {
		return interpolator;
	}

	/*@Override
	public float getRoll() {
		return roll;
	}

	@Override
	public float getRoll(float tickProgress) {
		return tickProgress == 1.0f ? getRoll() : MathHelper.lerp(tickProgress, lastRoll, getRoll());
	}

	@Override
	public void setRoll(float roll) {
		if (!Float.isFinite(roll)) {
			Util.logErrorOrPause("Invalid entity rotation: " + roll + ", discarding.");
		} else {
			this.roll = roll;
		}
	}*/

	@Override
	public Quaternionfc getQuat() {
		return dataTracker.get(QUAT_SERVER);
	}

	@Override
	public void setQuat(Quaternionf quat) {
		dataTracker.set(QUAT_SERVER, quat);
	}

	@Override
	public Quaternionfc getQuatClient() {
		return quatClient;
	}

	@Override
	public Quaternionfc getQuatClient(float tickProgress) {
		return tickProgress == 1.0f ? getQuatClient() : lastQuatClient.slerp(getQuatClient(), tickProgress, new Quaternionf());
	}

	@Override
	public void setQuatClient(Quaternionf quatClient) {
		this.quatClient = quatClient;
	}

	/*TO-DO: must be called by (or equivalent)
						   [x] onEntityPositionSync(EntityPositionSyncS2CPacket)
						   [x] onEntity(EntityS2CPacket)
						   [~] setPosition > onEntityPosition(EntityPositionS2CPacket)
						 */
	/*@Override
	public void updateTrackedPositionAndAngles$Roll(float roll) {
		// vanilla method does exactly this (as long as there's an interpolator)
		this.interpolator.refreshPositionAndAngles$Roll(roll);
	}*/

	@Override
	public void updateTrackedPositionAndAngles$Quat(Quaternionf quat) {
		this.interpolator.refreshPositionAndAngles$Quat(quat);
	}

	//	/* TO-DO: must be called by (or equivalent)
//	    [x] onEntityPositionSync(EntityPositionSyncS2CPacket)
//	 */
//	@Override
//	public void refreshPositionAndAngles(Vec3 vec3, float f, float g) {
//		super.refreshPositionAndAngles(vec3, f, g);
//	}
//
//	/* TO-DO: must be called by (or equivalent)
//	    [x] this.onSpawnPacket(EntitySpawnS2CPacket)
//	 */
//	@Override
//	public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
//		super.refreshPositionAndAngles(x, y, z, yaw, pitch);
//	}

	/*@Override
	public void refreshPositionAndAngles$Roll(float roll) {
		setRoll(roll);
		this.lastRoll = getRoll();
	}*/

	@Override
	public void refreshPositionAndAngles$Quat(Quaternionf quat) {
		setQuat(quat);
		setQuatClient(quat);
		this.lastQuatClient = new Quaternionf(getQuatClient());
	}

	@Override
	public @NotNull Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry serverEntity) {
		return new EntitySpawnS2CPacket(this, serverEntity, Float.floatToIntBits(MathUtil.toEuler(getQuat()).roll()));
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket clientboundAddEntityPacket) {
		super.onSpawnPacket(clientboundAddEntityPacket);
		float roll = Float.intBitsToFloat(clientboundAddEntityPacket.getEntityData());
		refreshPositionAndAngles$Quat(new MathUtil.EulerAngles(getYaw(), getPitch(), roll).getQuat());
	}

	@Override
	public void updateLastAngles() {
		super.updateLastAngles();
		this.lastQuatClient = new Quaternionf(getQuatClient());
	}

	@Override
	public void copyPositionAndRotation(Entity entity) {
		super.copyPositionAndRotation(entity);
		if (entity instanceof QuatEntity quatEntity) {
			refreshPositionAndAngles$Quat(new Quaternionf(quatEntity.getQuat()));
		} else {
			refreshPositionAndAngles$Quat(new MathUtil.EulerAngles(entity.getYaw(), entity.getPitch(), 0).getQuat());
		}
	}

	@Override
	public @Nullable PlayerEntity getControllingPassenger() {
		return getFirstPassenger() instanceof PlayerEntity player ? player : null;
	}

	protected void clampPassengerYaw(Entity entity) {
		entity.setBodyYaw(this.getYaw());
		float f = MathHelper.wrapDegrees(entity.getYaw() - this.getYaw());
		float g = MathHelper.clamp(f, -105.0F, 105.0F);
		entity.lastYaw += g - f;
		entity.setYaw(entity.getYaw() + g - f);
		entity.setHeadYaw(entity.getYaw());
	}

	@Override
	protected void updatePassengerPosition(Entity entity, Entity.PositionUpdater moveFunction) {
		super.updatePassengerPosition(entity, moveFunction);
		//entity.setYRot(entity.getYRot() + this.deltaRotation);
		//entity.setYHeadRot(entity.getYHeadRot() + this.deltaRotation);
		this.clampPassengerYaw(entity);
	}

	@Override
	public void onPassengerLookAround(Entity entity) {
		this.clampPassengerYaw(entity);
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ActionResult superResult = super.interact(player, hand);
		if (superResult != ActionResult.PASS) return superResult;

		if (player.shouldCancelInteraction())
			return ActionResult.PASS;

		if (this.getWorld().isClient)
			return ActionResult.SUCCESS;

		return player.startRiding(this)
			? ActionResult.SUCCESS
			: ActionResult.PASS;
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);

		if (passenger instanceof LivingEntity living && false) {
			EntityAttributeInstance scale = living.getAttributeInstance(EntityAttributes.SCALE);
			if (scale != null && !scale.hasModifier(SCALE_MODIFIER.id())) {
				scale.addTemporaryModifier(SCALE_MODIFIER);
			}
		}
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);

		if (passenger instanceof LivingEntity living) {
			EntityAttributeInstance scale = living.getAttributeInstance(EntityAttributes.SCALE);
			if (scale != null) {
				scale.removeModifier(SCALE_MODIFIER);
			}
		}
	}

	@Override
	public void tick() {
		this.updateLastAngles();

		if (this.getDamageWobbleTicks() > 0) {
			this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
		}

		if (this.getDamageWobbleStrength() > 0.0F) {
			this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
		}

		super.tick();
		interpolator.tick();

		if (this.isLogicalSideForUpdatingMovement()) {
			//this.applyGravity();
			//this.applyDrag();

			Quaternionfc quat;
			if (this.getWorld().isClient) {
				this.updateControls();
				quat = getQuatClient();
			} else {
				quat = getQuat();
			}

			Quaternionf newQuat = this.physicsStep(quat);
			setQuat(newQuat);
			setQuatClient(newQuat);

			this.move(MovementType.SELF, getVelocity());
			this.tickBlockCollision();

			if (this.getWorld().isClient) {
				VolarePackets.PACKETS.send(new RotationC2SPacket(getQuatClient()));
			}
		} else {
			this.setVelocity(Vec3d.ZERO);
		}

		// set pitch and yaw from quat
		Quaternionfc quat = this.getWorld().isClient ? getQuatClient() : getQuat();
		var euler = MathUtil.toEuler(quat);
		setPitch(euler.pitch());
		setYaw(euler.yaw());
	}

	@Override
	@SuppressWarnings("RedundantMethodOverride")
	protected double getGravity() {
		return 0.0;
	}

	private void applyDrag() {
		Vec3d vel = this.getVelocity();
		Vec3d pos = this.getPos();
		float friction;
		if (this.isTouchingWater()) {
			for (int i = 0; i < 4; i++) {
				float f = 0.25F;
				this.getWorld().addParticleClient(ParticleTypes.BUBBLE, pos.x - vel.x * f, pos.y - vel.y * f, pos.z - vel.z * f, vel.x, vel.y, vel.z);
			}

			friction = 0.8F;
		} else {
			friction = 0.99F;
		}

		this.setVelocity(vel.multiply(friction));
	}

	private void updateControls() {
		pitchControl = 0;
		rollControl = 0;

		PlayerEntity player = getControllingPassenger();
		if (player == null) return;

		pitchControl = MathHelper.sign(player.forwardSpeed);
		rollControl = MathHelper.sign(player.sidewaysSpeed);
	}

	private Quaternionf physicsStep(Quaternionfc quat) {
		/* prepare */
		rigidBody.setVelocity(getVelocity().toVector3f().mul(1, 1, -1)); // negate z to convert to right-handed coordinates
		rigidBody.setOrientation(quat);

		/* simulate */

		/*engine
		float forwardSpeed = rigidBody.directionToLocal(rigidBody.getVelocity()).z;
		float forwardThrust = Math.max(0.0f, 0.5f - forwardSpeed) * 0.2f;
		rigidBody.applyForceAtPoint(new Vector3f(0.0f, 0.0f, forwardThrust), new Vector3f(0, 1.0f, -8.0f).mul(1 / 16f).add(centerOfMass));*/

		rigidBody.applyForceAtCoM(rigidBody.directionToLocal(new Vector3f(0, -9.8f * rigidBody.mass / 20.0f, 0)));

		// wings
		wings.applyControls(pitchControl, rollControl);
		boolean wingForcesApplied = wings.applyForcesTo(rigidBody, 1.0f / 20.0f, (float) (getY() - 64));

		rigidBody.endStep(1.0f / 20.0f);

		/* extract results */
		var vel = rigidBody.getVelocity();

		if (vel.lengthSquared() > 100 * 100) {
			vel = new Vector3f(0);
			remove(RemovalReason.KILLED);
		}

		float f = wingForcesApplied ? 1.0f : 0.9f; // if no wing forces, apply magic drag (TM)
		setVelocity(vel.x() * f, vel.y() * f, vel.z() * f * -1);
		return new Quaternionf(rigidBody.getOrientation());
	}
}
