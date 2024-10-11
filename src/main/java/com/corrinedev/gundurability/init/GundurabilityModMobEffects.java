
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.corrinedev.gundurability.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import net.corrinedev.gundurability.potion.CheckingdurabilityMobEffect;
import net.corrinedev.gundurability.GundurabilityMod;

public class GundurabilityModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, GundurabilityMod.MODID);
	public static final RegistryObject<MobEffect> CHECKINGDURABILITY = REGISTRY.register("checkingdurability", () -> new CheckingdurabilityMobEffect());
}
