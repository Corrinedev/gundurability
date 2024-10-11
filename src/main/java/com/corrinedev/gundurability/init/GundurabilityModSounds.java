
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.corrinedev.gundurability.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.corrinedev.gundurability.GundurabilityMod;

public class GundurabilityModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GundurabilityMod.MODID);
	public static final RegistryObject<SoundEvent> JAMSFX = REGISTRY.register("jamsfx", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("gundurability", "jamsfx")));
}
