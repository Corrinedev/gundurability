package com.corrinedev.gundurability.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import com.corrinedev.gundurability.init.GundurabilityModMobEffects;

public class DurabilityOverlayDisplayOverlayIngameProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return !(entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(GundurabilityModMobEffects.CHECKINGDURABILITY.get()));
	}
}
