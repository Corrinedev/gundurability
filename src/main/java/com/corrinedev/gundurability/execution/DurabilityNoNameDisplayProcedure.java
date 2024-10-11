package com.corrinedev.gundurability.execution;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import com.corrinedev.gundurability.init.GundurabilityModMobEffects;

public class DurabilityNoNameDisplayProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(GundurabilityModMobEffects.CHECKINGDURABILITY.get());
	}
}
