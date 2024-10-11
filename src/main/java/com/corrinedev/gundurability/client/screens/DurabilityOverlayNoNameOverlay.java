
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

import net.corrinedev.gundurability.procedures.YellowOverlay2Procedure;
import net.corrinedev.gundurability.procedures.ReturnOverlayNoNameProcedure;
import net.corrinedev.gundurability.procedures.RedOverlay2Procedure;
import net.corrinedev.gundurability.procedures.OrangeOverlay2Procedure;
import net.corrinedev.gundurability.procedures.GreenOverlay2Procedure;
import net.corrinedev.gundurability.procedures.DurabilityNoNameDisplayProcedure;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class DurabilityOverlayNoNameOverlay {
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
		if (DurabilityNoNameDisplayProcedure.execute(entity)) {
			if (GreenOverlay2Procedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayNoNameProcedure.execute(entity), w - 97, h - 16, -13382656, false);
			if (YellowOverlay2Procedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayNoNameProcedure.execute(entity), w - 97, h - 16, -154, false);
			if (OrangeOverlay2Procedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayNoNameProcedure.execute(entity), w - 97, h - 16, -26317, false);
			if (RedOverlay2Procedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnOverlayNoNameProcedure.execute(entity), w - 97, h - 16, -39322, false);
		}
	}
}