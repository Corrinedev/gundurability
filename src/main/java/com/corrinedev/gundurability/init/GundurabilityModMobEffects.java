
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package com.corrinedev.gundurability.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import com.corrinedev.gundurability.potion.CheckingdurabilityMobEffect;
import com.corrinedev.gundurability.Gundurability;

public class GundurabilityModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Gundurability.MODID);
	public static final RegistryObject<MobEffect> CHECKINGDURABILITY = REGISTRY.register("checkingdurability", CheckingdurabilityMobEffect::new);
}
