package net.corrinedev.gundurability.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.corrinedev.gundurability.init.GundurabilityModMobEffects;

public class DurabilityOnKeyPressedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getBoolean("Jammed") == false && entity.getPersistentData().getBoolean("checkingdurability") == false) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(GundurabilityModMobEffects.CHECKINGDURABILITY.get(), 40, 1, false, false));
		}
	}
}
