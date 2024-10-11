package net.corrinedev.gundurability.init;

import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.corrinedev.gundurability.configuration.ConfigConfiguration;
import net.corrinedev.gundurability.GundurabilityMod;

@Mod.EventBusSubscriber(modid = GundurabilityMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GundurabilityModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigConfiguration.SPEC, "taczdurability.toml");
		});
	}
}
