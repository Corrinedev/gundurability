
package net.corrinedev.gundurability.client.screens;

import org.checkerframework.checker.units.qual.h;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;

import net.corrinedev.gundurability.procedures.YellowOverlayProcedure;
import net.corrinedev.gundurability.procedures.ReturnOverlayProcedure;
import net.corrinedev.gundurability.procedures.RedOverlayProcedure;
import net.corrinedev.gundurability.procedures.OrangeOverlayProcedure;
import net.corrinedev.gundurability.procedures.HoldingOverlayProcedure;
import net.corrinedev.gundurability.procedures.DurabilityOverlayDisplayOverlayIngameProcedure;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class DurabilityOverlayOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getWindow().getGuiScaledWidth();
		int h = event.getWindow().getGuiScaledHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		if (DurabilityOverlayDisplayOverlayIngameProcedure.execute(entity)) {
			if (HoldingOverlayProcedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayProcedure.execute(entity), w - 97, h - 16, -13382656, false);
			if (YellowOverlayProcedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayProcedure.execute(entity), w - 97, h - 16, -154, false);
			if (OrangeOverlayProcedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayProcedure.execute(entity), w - 97, h - 16, -26317, false);
			if (RedOverlayProcedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayProcedure.execute(entity), w - 97, h - 16, -39322, false);
		}
	}
}