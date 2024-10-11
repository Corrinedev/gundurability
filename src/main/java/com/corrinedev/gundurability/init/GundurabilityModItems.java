
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.corrinedev.gundurability.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.corrinedev.gundurability.item.WD40Item;
import net.corrinedev.gundurability.item.RecoilSpringItem;
import net.corrinedev.gundurability.item.GunBoltItem;
import net.corrinedev.gundurability.item.GunBarrelItem;
import net.corrinedev.gundurability.item.BrassBrushItem;
import net.corrinedev.gundurability.GundurabilityMod;

public class GundurabilityModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, GundurabilityMod.MODID);
	public static final RegistryObject<Item> WD_40 = REGISTRY.register("wd_40", () -> new WD40Item());
	public static final RegistryObject<Item> GUN_BARREL = REGISTRY.register("gun_barrel", () -> new GunBarrelItem());
	public static final RegistryObject<Item> GUN_BOLT = REGISTRY.register("gun_bolt", () -> new GunBoltItem());
	public static final RegistryObject<Item> RECOIL_SPRING = REGISTRY.register("recoil_spring", () -> new RecoilSpringItem());
	public static final RegistryObject<Item> BRASS_BRUSH = REGISTRY.register("brass_brush", () -> new BrassBrushItem());
	public static final RegistryObject<Item> REPAIR_TABLE = block(GundurabilityModBlocks.REPAIR_TABLE);

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
