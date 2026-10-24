package com.iseeyourhearts;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class IseeYourHeartsClient implements ClientModInitializer {
	private static boolean enabled = true;

	public static boolean isEnabled() {
		return enabled;
	}

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal("iseeyourhearts")
				.executes(ctx -> set(ctx, !enabled))
				.then(ClientCommandManager.literal("on").executes(ctx -> set(ctx, true)))
				.then(ClientCommandManager.literal("off").executes(ctx -> set(ctx, false)))));
	}

	private static int set(CommandContext<FabricClientCommandSource> ctx, boolean value) {
		enabled = value;
		ctx.getSource().sendFeedback(Text.literal("iseeyourhearts: " + (value ? "ON" : "OFF"))
			.formatted(value ? Formatting.GREEN : Formatting.RED));
		return 1;
	}

	/** Adds hearts (and saturation, when it is known) after the player's name. */
	public static Text decorate(PlayerEntity player, Text base) {
		MutableText out = Text.empty().append(base);
		out.append(Text.literal(" \u2665 " + fmt(player.getHealth() / 2.0f)).formatted(Formatting.RED));

		Float sat = getSaturation(player);
		if (sat != null) {
			out.append(Text.literal(" Sat " + fmt(sat)).formatted(Formatting.GOLD));
		}
		return out;
	}

	/**
	 * Vanilla servers only tell you your OWN saturation. Other players' saturation
	 * is only readable in singleplayer / when you are hosting the world.
	 */
	private static Float getSaturation(PlayerEntity player) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (player == mc.player) {
			return player.getHungerManager().getSaturationLevel();
		}
		IntegratedServer server = mc.getServer();
		if (server != null) {
			ServerPlayerEntity sp = server.getPlayerManager().getPlayer(player.getUuid());
			if (sp != null) {
				return sp.getHungerManager().getSaturationLevel();
			}
		}
		return null;
	}

	private static String fmt(float value) {
		return String.format("%.1f", value);
	}
}
