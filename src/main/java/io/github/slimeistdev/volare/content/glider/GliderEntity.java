package io.github.slimeistdev.volare.content.glider;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.compat.area_lib.AreaLibProxy;
import io.github.slimeistdev.volare.config.VolareServerConfig;
import io.github.slimeistdev.volare.content.glider.components.GliderParticlesComponent;
import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import io.github.slimeistdev.volare.infrastructure.QuatPositionInterpolator;
import io.github.slimeistdev.volare.network.VolarePackets;
import io.github.slimeistdev.volare.network.c2s.UpdateGliderC2SPacket;
import io.github.slimeistdev.volare.network.s2c.SetGliderPhysicsS2CPacket;
import io.github.slimeistdev.volare.registry.VolareDataComponentTypes;
import io.github.slimeistdev.volare.registry.VolareSoundEvents;
import io.github.slimeistdev.volare.registry.VolareTags;
import io.github.slimeistdev.volare.util.LerpedFloat;
import io.github.slimeistdev.volare.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreakingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE;

public class GliderEntity extends VehicleEntity implements QuatEntity {
	private static final EntityAttributeModifier SCALE_MODIFIER = new EntityAttributeModifier(
		Volare.id("glider_scale"),
		(1 / 4.0) - 1,
		EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);
	private static final EntityAttributeModifier CAMERA_DISTANCE_MODIFIER = new EntityAttributeModifier(
		Volare.id("glider_camera_distance"),
		4.0 - 1,
		EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);


	protected static final TrackedData<Quaternionf> QUAT_SERVER = DataTracker.registerData(GliderEntity.class, TrackedDataHandlerRegistry.QUATERNION_F);
	protected static final TrackedData<List<ParticleEffect>> PARTICLES = DataTracker.registerData(GliderEntity.class, TrackedDataHandlerRegistry.PARTICLE_LIST);
	protected static final TrackedData<Integer> PITCH_CONTROL = DataTracker.registerData(GliderEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> YAW_CONTROL = DataTracker.registerData(GliderEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> THRUST_TICKS = DataTracker.registerData(GliderEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private final QuatPositionInterpolator interpolator = new QuatPositionInterpolator(this, 3);
	private final Supplier<Item> itemSupplier;

	private Quaternionf quatClient = new Quaternionf();
	private Quaternionf lastQuatClient = new Quaternionf();

	private float roll = 0.0f;
	private float lastRoll = 0.0f;

	private final RigidBody rigidBody;
	private final GliderWings wings;
	private final Vector3fc centerOfMass;
	private final Vector3fc centerOfPressure;
	private final Vector3fc wingtipOffset;

	private int pitchControl = 0;
	private int yawControl = 0;

	private final LerpedFloat aileronAngle = new LerpedFloat(5.0f);
	private final LerpedFloat elevatorAngle = new LerpedFloat(5.0f);
	private final LerpedFloat rudderAngle = new LerpedFloat(5.0f * 1.5f);

	private boolean wasLogicalSideForUpdatingMovement = false;

	private @Nullable Vector3f frozenMotion;
	private List<ParticleEffect> wingtipParticles = List.of();

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
		this.wingtipOffset = params.wingtipOffset();
		Volare.LOG.info(
			"GliderEntity created with center of mass at ({}, {}, {}) and center of pressure at ({}, {}, {}). Mass: {} kg",
			centerOfMass.x()*16, centerOfMass.y()*16, centerOfMass.z()*16,
			centerOfPressure.x()*16, centerOfPressure.y()*16, centerOfPressure.z()*16,
			rigidBody.mass
		);
	}

	public void initPosition(double x, double y, double z) {
		this.setPosition(x, y, z);
		this.lastX = x;
		this.lastY = y;
		this.lastZ = z;
	}

	public void initRotation(float pitch, float yaw, float roll) {
		this.setPitch(pitch);
		this.setYaw(yaw);
		this.roll = roll;
		this.refreshPositionAndAngles$Quat(new MathUtil.EulerAngles(yaw, pitch, roll).getQuat());
	}

	@Override
	protected void copyComponentsFrom(ComponentsAccess from) {
		this.copyComponentFrom(from, VolareDataComponentTypes.GLIDER_PARTICLES);
		this.copyComponentFrom(from, VolareDataComponentTypes.GLIDER_FROZEN_MOTION);
		super.copyComponentsFrom(from);
	}

	@Override
	protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
		if (type == VolareDataComponentTypes.GLIDER_PARTICLES) {
			this.setWingtipParticles(castComponentValue(VolareDataComponentTypes.GLIDER_PARTICLES, value).particles());
			return true;
		} else if (type == VolareDataComponentTypes.GLIDER_FROZEN_MOTION) {
			this.frozenMotion = castComponentValue(VolareDataComponentTypes.GLIDER_FROZEN_MOTION, value).frozenMotionMut();
			return true;
		}
		return super.setApplicableComponent(type, value);
	}

	private void setWingtipParticles(List<ParticleEffect> particles) {
		dataTracker.set(PARTICLES, particles);
		this.wingtipParticles = particles;
	}

	public void applyPhysicsSnapshot(PhysicsSnapshot snapshot) {
		snapshot.applyTo(rigidBody);
	}

	public PhysicsSnapshot createPhysicsSnapshot() {
		return new PhysicsSnapshot(rigidBody);
	}

	public void setControls(int pitchControl, int yawControl) {
		this.pitchControl = pitchControl;
		this.yawControl = yawControl;
		dataTracker.set(PITCH_CONTROL, pitchControl);
		dataTracker.set(YAW_CONTROL, yawControl);
	}

	public float getAileronAngle(float tickProgress) {
		return aileronAngle.get(tickProgress);
	}

	public float getElevatorAngle(float tickProgress) {
		return elevatorAngle.get(tickProgress);
	}

	public float getRudderAngle(float tickProgress) {
		return rudderAngle.get(tickProgress);
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
		builder.add(PARTICLES, List.of());
		builder.add(PITCH_CONTROL, 0);
		builder.add(YAW_CONTROL, 0);
		builder.add(THRUST_TICKS, 0);
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		super.onTrackedDataSet(data);

		if (getWorld().isClient) {
			if (PARTICLES.equals(data)) {
				this.wingtipParticles = dataTracker.get(PARTICLES);
			}

			if (!isLogicalSideForUpdatingMovement()) {
				if (QUAT_SERVER.equals(data)) {
					updateTrackedPositionAndAngles$Quat(new Quaternionf(getQuat()));
				} else if (PITCH_CONTROL.equals(data)) {
					this.pitchControl = dataTracker.get(PITCH_CONTROL);
				} else if (YAW_CONTROL.equals(data)) {
					this.yawControl = dataTracker.get(YAW_CONTROL);
				}
			}
		}
	}

	public void boostThrust() {
		var config = VolareServerConfig.get(getWorld());
		if (!config.isBoostEnabled())
			return;

		setThrustTicks(getThrustTicks() + config.boostTicks);
		getWorld().playSoundFromEntity(
			null,
			this,
			VolareSoundEvents.GLIDER_BOOST,
			getSoundCategory(),
			1.0f, 0.8F + 0.4F * this.random.nextFloat()
		);
	}

	private void setThrustTicks(int ticks) {
		dataTracker.set(THRUST_TICKS, ticks);
	}

	private int getThrustTicks() {
		return dataTracker.get(THRUST_TICKS);
	}

	@Override
	protected @NotNull Item asItem() {
		return itemSupplier.get();
	}

	@Override
	protected void killAndDropSelf(ServerWorld world, DamageSource damageSource) {
		this.kill(world);
		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = getPickBlockStack();
			itemStack.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
			this.dropStack(world, itemStack);
		}
	}

	@Override
	public @NotNull ItemStack getPickBlockStack() {
		ItemStack stack = new ItemStack(asItem());
		stack.set(VolareDataComponentTypes.GLIDER_PARTICLES, new GliderParticlesComponent(wingtipParticles));
		return stack;
	}

	@Override
	protected void readCustomData(ReadView view) {
		float roll = view.getFloat("Roll", 0.0f);
		refreshPositionAndAngles$Quat(new MathUtil.EulerAngles(getYaw(), getPitch(), roll).getQuat());

		frozenMotion = view.read("FrozenMotion", Codecs.VECTOR_3F).orElse(null);
		setWingtipParticles(view.read("WingtipParticles", ParticleTypes.TYPE_CODEC.listOf()).orElse(List.of()));

		setThrustTicks(view.getInt("ThrustTicks", 0));
	}

	@Override
	protected void writeCustomData(WriteView view) {
		view.putFloat("Roll", MathUtil.toEuler(getQuat()).roll());

		if (frozenMotion != null) {
			view.put("FrozenMotion", Codecs.VECTOR_3F, frozenMotion);
		}

		if (!wingtipParticles.isEmpty()) {
			view.put("WingtipParticles", ParticleTypes.TYPE_CODEC.listOf(), wingtipParticles);
		}

		int thrustTicks = getThrustTicks();
		if (thrustTicks > 0) {
			view.putInt("ThrustTicks", thrustTicks);
		}
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
	public float getRoll(float tickProgress) {
		return MathHelper.lerp(tickProgress, lastRoll, roll);
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
		this.lastRoll = roll;
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

	protected void clampPassengerYaw(Entity passenger) {
		passenger.setBodyYaw(this.getYaw());
		float f = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
		float g = MathHelper.clamp(f, -105.0F, 105.0F);
		passenger.lastYaw += g - f;
		passenger.setYaw(passenger.getYaw() + g - f);
		passenger.setHeadYaw(passenger.getYaw());
	}

	@Override
	protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
		Vector3f passengerOffset = getUnrotatedPassengerAttachmentPos(passenger);
		passengerOffset.sub(passenger.getVehicleAttachmentPos(this).toVector3f());
		passengerOffset = rotateAttachmentPos(passengerOffset);
		positionUpdater.accept(passenger, passengerOffset.x + getX(), passengerOffset.y + getY(), passengerOffset.z + getZ());

		float yawVelocity = MathHelper.wrapDegrees(getYaw() - lastYaw);
		passenger.setYaw(passenger.getYaw() + yawVelocity);
		passenger.setHeadYaw(passenger.getHeadYaw() + yawVelocity);

		this.clampPassengerYaw(passenger);
	}

	protected Vector3f getUnrotatedPassengerAttachmentPos(Entity passenger) {
		float xOffset = 0;
		if (getPassengerList().size() > 1) {
			int i = getPassengerList().indexOf(passenger);
			if (i != 0) {
				int side = i % 2 == 0 ? -1 : 1;
				int distance = (i + 1) / 2;
				xOffset = side * distance * 5;
			}
		}

		return new Vector3f(xOffset, 2.5f, 3)
			.div(16f);
	}

	private Vector3f rotateAttachmentPos(Vector3f pos) {
		return rotateAttachmentPos(pos, pos);
	}

	private Vector3f rotateAttachmentPos(Vector3fc pos, Vector3f scratch) {
		var flippedCoM = new Vector3f(centerOfMass).mul(1, 1, 0);

		return rigidBody.directionToGlobal(pos.sub(flippedCoM, scratch), scratch)
			.mul(1, 1, -1)
			.add(flippedCoM);
	}

	@Override
	protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
		return new Vec3d(rotateAttachmentPos(getUnrotatedPassengerAttachmentPos(passenger)));
	}

	@Override
	public void onPassengerLookAround(Entity entity) {
		this.clampPassengerYaw(entity);
	}

	protected int getMaxPassengers() {
		return 5;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return getPassengerList().size() < getMaxPassengers();
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ActionResult superResult = super.interact(player, hand);
		if (superResult != ActionResult.PASS) return superResult;

		if (player.shouldCancelInteraction())
			return ActionResult.PASS;

		if (this.getWorld().isClient)
			return ActionResult.SUCCESS;

		if (player.startRiding(this))
			return ActionResult.SUCCESS;

		ItemStack stack = player.getStackInHand(hand);
		if (stack.isIn(VolareTags.THRUST_SOURCE)) {
			stack.decrementUnlessCreative(1, player);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);

		if (frozenMotion != null) {
			rigidBody.setVelocity(rigidBody.directionToGlobal(frozenMotion));
			rigidBody.setAngularVelocity(new Vector3f(0));
			frozenMotion = null;
		}

		if (passenger instanceof ServerPlayerEntity serverPlayer) {
			VolarePackets.PACKETS.sendTo(serverPlayer, new SetGliderPhysicsS2CPacket(this));
		}

		if (passenger instanceof LivingEntity living) {
			EntityAttributeInstance scale = living.getAttributeInstance(EntityAttributes.SCALE);
			EntityAttributeInstance cameraDistance = living.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE);
			if (scale != null && !scale.hasModifier(SCALE_MODIFIER.id())) {
				scale.addTemporaryModifier(SCALE_MODIFIER);
			}
			if (cameraDistance != null && !cameraDistance.hasModifier(CAMERA_DISTANCE_MODIFIER.id())) {
				cameraDistance.addTemporaryModifier(CAMERA_DISTANCE_MODIFIER);
			}
		}
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);

		if (passenger instanceof LivingEntity living) {
			EntityAttributeInstance scale = living.getAttributeInstance(EntityAttributes.SCALE);
			EntityAttributeInstance cameraDistance = living.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE);
			if (scale != null) {
				scale.removeModifier(SCALE_MODIFIER);
			}
			if (cameraDistance != null) {
				cameraDistance.removeModifier(CAMERA_DISTANCE_MODIFIER);
			}
		}

		setControls(0, 0);
	}

	public boolean isSmallerThanBoat(Entity entity) {
		return entity.getWidth() < this.getWidth();
	}

	protected void pickUpPassengers() {
		List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.2F, -0.01F, 0.2F), EntityPredicates.canBePushedBy(this));
		if (!list.isEmpty()) {
			boolean bl = !this.getWorld().isClient && !(this.getControllingPassenger() instanceof PlayerEntity);

			for (Entity entity : list) {
				if (!entity.hasPassenger(this)) {
					if (bl
						&& this.getPassengerList().size() < this.getMaxPassengers()
						&& !entity.hasVehicle()
						&& this.isSmallerThanBoat(entity)
						&& entity instanceof LivingEntity
						&& !(entity instanceof WaterCreatureEntity)
						&& !(entity instanceof PlayerEntity)
						&& !(entity instanceof CreakingEntity)) {
						entity.startRiding(this);
					} else {
						this.pushAwayFrom(entity);
					}
				}
			}
		}
	}

	@Override
	public void tick() {
		World world = getWorld();
		boolean isClient = world.isClient;

		if (!isClient && AreaLibProxy.isInFlak(world, this)) {
			explode();
			return;
		}

		// because Minecraft, in its infinite wisdom, decided that player-ridden entities should always :(
		if (!world.getTickManager().shouldTick())
			return;

		this.updateLastAngles();

		if (this.getDamageWobbleTicks() > 0) {
			this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
		}

		if (this.getDamageWobbleStrength() > 0.0F) {
			this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
		}

		super.tick();
		interpolator.tick();

		int thrustTicks = getThrustTicks() - 1;
		if (thrustTicks >= 0) {
			setThrustTicks(thrustTicks);
		}

		if (this.isLogicalSideForUpdatingMovement()) {
			if (!wasLogicalSideForUpdatingMovement) {
				var vel = rigidBody.getVelocity();
				this.setVelocity(vel.x(), vel.y(), -vel.z());
			}
			wasLogicalSideForUpdatingMovement = true;
			Quaternionfc quat;
			if (isClient) {
				this.updateControls();
				quat = getQuatClient();
			} else {
				quat = getQuat();
			}

			if (frozenMotion == null) {
				Quaternionf newQuat = this.physicsStep(quat);
				setQuat(newQuat);
				setQuatClient(newQuat);

				this.move(MovementType.SELF, getVelocity());
				this.tickBlockCollision();
			}

			if (isClient) {
				VolarePackets.PACKETS.send(new UpdateGliderC2SPacket(
					new Quaternionf(getQuatClient()),
					createPhysicsSnapshot(),
					pitchControl,
					yawControl
				));
			}
		} else {
			this.setVelocity(Vec3d.ZERO);
			if (wasLogicalSideForUpdatingMovement) {
				rigidBody.setVelocity(new Vector3f(0));
			}
			wasLogicalSideForUpdatingMovement = false;
		}

		// set pitch and yaw from quat
		Quaternionfc quat = isClient ? getQuatClient() : getQuat();
		var euler = MathUtil.toEuler(quat);
		setPitch(euler.pitch());
		setYaw(euler.yaw());
		roll = euler.roll();
		rigidBody.setOrientation(quat);

		if (isClient) {
			aileronAngle.setTarget(yawControl * 20.0f);
			elevatorAngle.setTarget(pitchControl * 40.0f);
			rudderAngle.setTarget(yawControl * 30.0f);

			aileronAngle.tick();
			elevatorAngle.tick();
			rudderAngle.tick();
		}

		if (isClient && !wingtipParticles.isEmpty()) {
			var lastPos = getLerpedPos(0.0f).toVector3f();
			var pos = getLerpedPos(1.0f).toVector3f();
			var vel = pos.sub(lastPos, lastPos);
			pos.sub(vel.mul(0.5f));
			float speed = vel.length();

			if (random.nextFloat() * (speed + 0.2) > 0.2f) {
				Vector3f rightWingtipOffset = rigidBody.directionToGlobal(new Vector3f(wingtipOffset).add(centerOfMass)).mul(1, 1, -1);
				Vector3f leftWingtipOffset = rigidBody.directionToGlobal(new Vector3f(wingtipOffset).mul(-1, 1, 1).add(centerOfMass)).mul(1, 1, -1);

				rightWingtipOffset.add(pos);
				leftWingtipOffset.add(pos);

				for (ParticleEffect particle : wingtipParticles) {
					float speedFactor = MathHelper.clamp(speed * 0.5f, 0.5f, 2.0f) * 0.3f;
					float oX = (random.nextFloat() - 0.5f) * speedFactor;
					float oY = (random.nextFloat() - 0.5f) * speedFactor;
					float oZ = (random.nextFloat() - 0.5f) * speedFactor;

					world.addParticleClient(particle, rightWingtipOffset.x + oX, rightWingtipOffset.y + oY, rightWingtipOffset.z + oZ, oX, oY, oZ);
					world.addParticleClient(particle, leftWingtipOffset.x + oX, leftWingtipOffset.y + oY, leftWingtipOffset.z + oZ, oX, oY, oZ);
				}
			}
		}

		pickUpPassengers();
	}

	protected void explode() {
		if (getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.createExplosion(
				this,
				null,
				new AdvancedExplosionBehavior(
					false,
					false,
					Optional.empty(),
					Optional.empty()
				),
				getX(), getY(), getZ(),
				4.0f,
				false,
				World.ExplosionSourceType.NONE
			);
			discard();
		}
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
		} else if (isOnGround()) {
			friction = 0.99F;
		} else {
			friction = 1.0F;
		}

		this.setVelocity(vel.multiply(friction));
	}

	private void updateControls() {
		pitchControl = 0;
		yawControl = 0;

		PlayerEntity player = getControllingPassenger();
		if (player == null) return;

		pitchControl = MathHelper.sign(player.forwardSpeed);
		yawControl = MathHelper.sign(player.sidewaysSpeed);
	}

	private static boolean isThermalSource(BlockState state) {
		return state.isIn(VolareTags.THERMAL_SOURCE)
			&& (!state.contains(CampfireBlock.LIT) || state.get(CampfireBlock.LIT));
	}

	private float getThermalSpeed() {
		var config = VolareServerConfig.get(getWorld());
		final int RANGE = config.thermalsHeight;
		final int FULL_STRENGTH_RANGE = Math.min(config.thermalsFullStrengthHeight, RANGE);
		final float STRENGTH = config.thermalsStrength;

		if (RANGE <= 0 || STRENGTH < 1e-6f)
			return 0;

		var world = getWorld();

		Float areaLibStrength = AreaLibProxy.getThermalStrength(world, this, STRENGTH);
		if (areaLibStrength != null) {
			return areaLibStrength;
		}

		var chunk = world.getWorldChunk(getBlockPos());
		int height = chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, getBlockX(), getBlockZ());
		int blockY = getBlockY();
		float y = (float) getY();

		// if we're below the heightmap, we have to search more
		if (y < height) {
			boolean found = false;

			height = blockY;

			int currentSection = Math.clamp(chunk.getSectionIndex(blockY), 0, chunk.getSectionArray().length - 1);
			int minSection = Math.clamp(chunk.getSectionIndex(blockY - RANGE), 0, chunk.getSectionArray().length - 1);

			int sectionX = getBlockX() & 15;
			int sectionZ = getBlockZ() & 15;
			int maxSectionY = blockY & 15;

			boolean anyThermalSources = false;
			for (int i = currentSection; i >= minSection; i--) {
				if (chunk.getSection(i).hasAny(GliderEntity::isThermalSource)) {
					anyThermalSources = true;
					break;
				}
			}

			if (!anyThermalSources)
				return 0;

			Predicate<BlockState> motionBlocking = Heightmap.Type.MOTION_BLOCKING.getBlockPredicate();

			Outer: for (int i = currentSection; i >= minSection; i--) {
				ChunkSection section = chunk.getSection(i);

				for (int sectionY = maxSectionY; sectionY >= 0; sectionY--) {
					BlockState state = section.getBlockState(sectionX, sectionY, sectionZ);

					if (isThermalSource(state)) {
						found = true;
						break Outer;
					}

					if (motionBlocking.test(state)) {
						break Outer;
					}

					height--;
				}

				maxSectionY = 15;
			}

			if (!found)
				return 0;
		} else {
			if (y - height > RANGE)
				return 0;

			BlockPos topBlock = new BlockPos(getBlockX(), height, getBlockZ());
			BlockState topState = chunk.getBlockState(topBlock);

			if (!isThermalSource(topState))
				return 0;
		}

		float delta = y - height;
		if (delta < FULL_STRENGTH_RANGE) {
			return STRENGTH;
		} else {
			delta /= (RANGE - FULL_STRENGTH_RANGE);
			return STRENGTH * (1 - (delta * delta));
		}
	}

	private Quaternionf physicsStep(Quaternionfc quat) {
		Vector3f scratch = new Vector3f();

		/* prepare */
		rigidBody.setVelocity(getVelocity().toVector3f().mul(1, 1, -1)); // negate z to convert to right-handed coordinates
		rigidBody.setOrientation(quat);

		/* simulate */
		if (getThrustTicks() > 0) {
			var config = VolareServerConfig.get(getWorld());
			if (config.isBoostEnabled()) {
				float forwardSpeed = rigidBody.directionToLocal(rigidBody.getVelocity()).z;
				float forwardThrust = Math.max(0.0f, config.boostMaxSpeed - forwardSpeed) * config.boostForce;
				rigidBody.applyForceAtPoint(
					new Vector3f(0.0f, 0.0f, forwardThrust),
					new Vector3f(0, 1.0f, 8.0f)
						.mul(1 / 16f)
						.add(centerOfMass)
				);
			}
		}

		// gravity
		rigidBody.applyForceAtCoM(rigidBody.directionToLocal(scratch.set(0, -9.8f * rigidBody.mass / 20.0f, 0)));

		// wings
		wings.applyControls(pitchControl, yawControl);
		float airDensity = isTouchingWater() ? 1.5159375f : 1.225f; // sea level standard atmosphere
		boolean wingForcesApplied = wings.applyForcesTo(rigidBody, 1.0f / 20.0f, airDensity, getThermalSpeed());

		rigidBody.endStep(1.0f / 20.0f);

		/* extract results */
		var vel = rigidBody.getVelocity();

		if (vel.lengthSquared() > 1000 * 1000) { // prevent physics explosions
			vel = new Vector3f(0);
			remove(RemovalReason.KILLED);
			Volare.LOG.warn("GliderEntity {} ({}) exploded due to excessive velocity: {}", this.getUuid(), this, vel);
		}

		float f = wingForcesApplied ? 1.0f : 0.9f; // if no wing forces, apply magic drag (TM)
		setVelocity(vel.x() * f, vel.y() * f, vel.z() * f * -1);
		applyDrag();

		if (isOnGround()) {
			var euler = MathUtil.toEuler(rigidBody.getOrientation());
			Vector3fc currentAngularVelocity = rigidBody.getAngularVelocity();
			Vector3f targetAngularVelocity = new Vector3f(
				MathHelper.clamp(euler.pitch() * -0.1f, -10f, 10f) * RADIANS_PER_DEGREE,
				0,
				MathHelper.clamp(euler.roll() * -0.1f, -10f, 10f) * RADIANS_PER_DEGREE
			);
			Vector3f newAngularVelocity = currentAngularVelocity.mul(0.8f, scratch)
				.add(targetAngularVelocity.mul(0.2f));

			boolean forceSet = false;
			float forcedPitch = euler.pitch();
			float forcedRoll = euler.roll();
			if (Math.abs(euler.pitch()) < 0.1f && Math.abs(newAngularVelocity.x) < 0.1f) {
				newAngularVelocity.x = 0;
				forcedPitch = 0;
				forceSet = true;
			}
			if (Math.abs(euler.roll()) < 0.1f && Math.abs(newAngularVelocity.z) < 0.1f) {
				newAngularVelocity.z = 0;
				forcedRoll = 0;
				forceSet = true;
			}
			if (forceSet) {
				rigidBody.setOrientation(new MathUtil.EulerAngles(
					euler.yaw(),
					forcedPitch,
					forcedRoll
				).getQuat());
			}

			rigidBody.setAngularVelocity(
				newAngularVelocity
			);
		}

		return new Quaternionf(rigidBody.getOrientation());
	}
}
