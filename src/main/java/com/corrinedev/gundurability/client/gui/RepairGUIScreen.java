package net.corrinedev.gundurability.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

import net.corrinedev.gundurability.world.inventory.RepairGUIMenu;
import net.corrinedev.gundurability.network.RepairGUIButtonMessage;
import net.corrinedev.gundurability.GundurabilityMod;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class RepairGUIScreen extends AbstractContainerScreen<RepairGUIMenu> {
	private final static HashMap<String, Object> guistate = RepairGUIMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	Button button_apply_spraybrush;

	public RepairGUIScreen(RepairGUIMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	private static final ResourceLocation texture = new ResourceLocation("gundurability:textures/screens/repair_gui.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
		if (mouseX > leftPos + 151 && mouseX < leftPos + 175 && mouseY > topPos + 9 && mouseY < topPos + 33)
			guiGraphics.renderTooltip(font, Component.translatable("gui.gundurability.repair_gui.tooltip_wd40"), mouseX, mouseY);
		if (mouseX > leftPos + 79 && mouseX < leftPos + 103 && mouseY > topPos + 63 && mouseY < topPos + 87)
			guiGraphics.renderTooltip(font, Component.translatable("gui.gundurability.repair_gui.tooltip_gun"), mouseX, mouseY);
		if (mouseX > leftPos + 115 && mouseX < leftPos + 139 && mouseY > topPos + 36 && mouseY < topPos + 60)
			guiGraphics.renderTooltip(font, Component.translatable("gui.gundurability.repair_gui.tooltip_barrel"), mouseX, mouseY);
		if (mouseX > leftPos + 79 && mouseX < leftPos + 103 && mouseY > topPos + 9 && mouseY < topPos + 33)
			guiGraphics.renderTooltip(font, Component.translatable("gui.gundurability.repair_gui.tooltip_bolt"), mouseX, mouseY);
		if (mouseX > leftPos + 43 && mouseX < leftPos + 67 && mouseY > topPos + 36 && mouseY < topPos + 60)
			guiGraphics.renderTooltip(font, Component.translatable("gui.gundurability.repair_gui.tooltip_spring"), mouseX, mouseY);
		if (mouseX > leftPos + 151 && mouseX < leftPos + 175 && mouseY > topPos + 63 && mouseY < topPos + 87)
			guiGraphics.renderTooltip(font, Component.translatable("gui.gundurability.repair_gui.tooltip_brush"), mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		button_apply_spraybrush = Button.builder(Component.translatable("gui.gundurability.repair_gui.button_apply_spraybrush"), e -> {
			if (true) {
				GundurabilityMod.PACKET_HANDLER.sendToServer(new RepairGUIButtonMessage(0, x, y, z));
				RepairGUIButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 34, this.topPos + 171, 114, 20).build();
		guistate.put("button:button_apply_spraybrush", button_apply_spraybrush);
		this.addRenderableWidget(button_apply_spraybrush);
	}
}
