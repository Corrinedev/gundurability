
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.corrinedev.gundurability.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

import net.corrinedev.gundurability.world.inventory.RepairGUIMenu;
import net.corrinedev.gundurability.GundurabilityMod;

public class GundurabilityModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, GundurabilityMod.MODID);
	public static final RegistryObject<MenuType<RepairGUIMenu>> REPAIR_GUI = REGISTRY.register("repair_gui", () -> IForgeMenuType.create(RepairGUIMenu::new));
}
