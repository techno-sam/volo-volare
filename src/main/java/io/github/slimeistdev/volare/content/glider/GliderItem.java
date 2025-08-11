package io.github.slimeistdev.volare.content.glider;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class GliderItem extends Item {
	private final EntityType<? extends GliderEntity> gliderEntityType;

	public GliderItem(EntityType<? extends GliderEntity> gliderEntityType, Settings settings) {
		super(settings);
		this.gliderEntityType = gliderEntityType;
	}

	public static Function<Settings, GliderItem> create(EntityType<? extends GliderEntity> gliderEntityType) {
		return settings -> new GliderItem(gliderEntityType, settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		HitResult hit = raycast(world, user, RaycastContext.FluidHandling.NONE);

		if (hit.getType() != HitResult.Type.BLOCK)
			return ActionResult.PASS;

		GliderEntity glider = this.createEntity(world, hit, stack, user);
		if (glider == null)
			return ActionResult.FAIL;

		glider.initRotation(0, user.getYaw(), 0);

		if (!world.isSpaceEmpty(glider, glider.getBoundingBox(), true))
			return ActionResult.FAIL;

		if (!world.isClient) {
			world.spawnEntity(glider);
			world.emitGameEvent(user, GameEvent.ENTITY_PLACE, hit.getPos());
			stack.decrementUnlessCreative(1, user);
		}

		user.incrementStat(Stats.USED.getOrCreateStat(this));
		return ActionResult.SUCCESS;
	}

	@Nullable
	private GliderEntity createEntity(World world, HitResult hitResult, ItemStack stack, PlayerEntity player) {
		GliderEntity glider = this.gliderEntityType.create(world, SpawnReason.SPAWN_ITEM_USE);
		if (glider != null) {
			Vec3d vec3d = hitResult.getPos();
			glider.initPosition(vec3d.x, vec3d.y, vec3d.z);
			if (world instanceof ServerWorld serverWorld) {
				EntityType.copier(serverWorld, stack, player).accept(glider);
			}
		}

		return glider;
	}
}
