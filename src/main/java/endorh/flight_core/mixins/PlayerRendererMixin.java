package endorh.flight_core.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import endorh.flight_core.events.ApplyRotationsRenderPlayerEvent;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects {@link ApplyRotationsRenderPlayerEvent} on
 * {@link PlayerRenderer#setupRotations}
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin
  extends LivingRenderer<AbstractClientPlayerEntity,
  PlayerModel<AbstractClientPlayerEntity>> {
	/**
	 * Dummy constructor required by the Java compiler to inherit from superclass
	 * @param renderManager ignored
	 * @throws IllegalAccessException always
	 */
	private PlayerRendererMixin(EntityRendererManager renderManager) throws IllegalAccessException {
		super(renderManager, new PlayerModel<>(0F, false), 0F);
		throw new IllegalAccessException("Mixin dummy constructor shouldn't be called!");
	}
	
	/**
	 * Inject {@link ApplyRotationsRenderPlayerEvent} on
	 * {@link PlayerRenderer#setupRotations}. The event is cancellable.
	 * If cancelled, the {@code applyRotations} method call will be
	 * skipped. The super method call can be invoked through the
	 * provided lambda.
	 * @param player Player being rendered
	 * @param mStack Transformation matrix stack
	 * @param ageInTicks Age of the player in ticks, containing partialTicks
	 * @param rotationYaw Smoothed yaw between this tick and the next for this frame
	 * @param partialTicks Interpolation progress between this tick and the next for this frame
	 * @param callbackInfo Mixin {@link CallbackInfo}
	 */
	@Inject(method = "setupRotations*", at = @At("HEAD"), cancellable = true)
	protected void _flightcore_applyRotations(
	  AbstractClientPlayerEntity player, MatrixStack mStack,
	  float ageInTicks, float rotationYaw, float partialTicks,
	  CallbackInfo callbackInfo
	) {
		//noinspection ConstantConditions
		ApplyRotationsRenderPlayerEvent event =
		  new ApplyRotationsRenderPlayerEvent(
		    (PlayerRenderer)(LivingRenderer<AbstractClientPlayerEntity,
		    PlayerModel<AbstractClientPlayerEntity>>)this,
		    player, mStack, ageInTicks, rotationYaw, partialTicks,
		    (vec) -> super.setupRotations(
		      player, mStack, vec.x(), vec.y(), vec.z()));
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) callbackInfo.cancel();
	}
}
