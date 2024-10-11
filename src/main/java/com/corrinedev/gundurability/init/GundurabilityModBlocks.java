
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.corrinedev.gundurability.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.corrinedev.gundurability.block.RepairTableBlock;
import net.corrinedev.gundurability.GundurabilityMod;

public class GundurabilityModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, GundurabilityMod.MODID);
	public static final RegistryObject<Block> REPAIR_TABLE = REGISTRY.register("repair_table", () -> new RepairTableBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
