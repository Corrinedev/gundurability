package com.corrinedev.gundurability.execution;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import com.corrinedev.gundurability.init.GundurabilityModAttributes;
import com.corrinedev.gundurability.Gundurability;

public class InspectDurabilityOnKeyPressedProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if ((ForgeRegistries.ITEMS.getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString()).equals("tacz:modern_kinetic_gun")
				&& (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getBoolean("Jammed") == true) {
			Gundurability.queueServerWork((int) (((LivingEntity) entity).getAttribute(GundurabilityModAttributes.HANDLING.get()).getValue() * 20), () -> {
				if ((ForgeRegistries.ITEMS.getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString()).equals("tacz:modern_kinetic_gun")
						&& (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getBoolean("Jammed") == true) {
					(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("Jammed", false);
					//(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("GunCurrentAmmoCount",
					//		((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("SavedAmmo")));
					//(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("SavedAmmo", 0);
					if (entity instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(MutableComponent.create(Component.literal("Jam Cleared").getContents()).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.YELLOW), true);
				}
			});
		}
	}
}
