package com.iseeyourhearts.mixin;

import com.iseeyourhearts.IseeYourHeartsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	// The nametag above a player's head is built from getDisplayName(),
	// so we tack the hearts/saturation onto the end of it (client side only).
	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
	private void iseeyourhearts$addStats(CallbackInfoReturnable<Text> cir) {
		if (!IseeYourHeartsClient.isEnabled()) return;
		PlayerEntity self = (PlayerEntity) (Object) this;
		if (!(self instanceof AbstractClientPlayerEntity)) return;
		if (self == MinecraftClient.getInstance().player) return;
		cir.setReturnValue(IseeYourHeartsClient.decorate(self, cir.getReturnValue()));
	}
}
