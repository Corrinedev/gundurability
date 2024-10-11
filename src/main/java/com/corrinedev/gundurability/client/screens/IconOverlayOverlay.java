
package net.corrinedev.gundurability.client.screens;

import org.checkerframework.checker.units.qual.h;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.corrinedev.gundurability.procedures.YellowIconProcedure;
import net.corrinedev.gundurability.procedures.RedIconProcedure;
import net.corrinedev.gundurability.procedures.OrangeIconProcedure;
import net.corrinedev.gundurability.procedures.JamIconProcedure;
import net.corrinedev.gundurability.procedures.IconOverlayDisplayOverlayIngameProcedure;
import net.corrinedev.gundurability.procedures.GreenIconProcedure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class IconOverlayOverlay {
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
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		if (IconOverlayDisplayOverlayIngameProcedure.execute(entity)) {
			if (GreenIconProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("gundurability:textures/screens/green.png"), w - 115, h - 25, 0, 0, 16, 16, 16, 16);
			}
			if (OrangeIconProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("gundurability:textures/screens/orange.png"), w - 115, h - 25, 0, 0, 16, 16, 16, 16);
			}
			if (RedIconProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("gundurability:textures/screens/reds.png"), w - 115, h - 25, 0, 0, 16, 16, 16, 16);
			}
			if (YellowIconProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("gundurability:textures/screens/yellow.png"), w - 115, h - 25, 0, 0, 16, 16, 16, 16);
			}
			if (JamIconProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("gundurability:textures/screens/jam.png"), w - 115, h - 25, 0, 0, 16, 16, 16, 16);
			}
		}
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}