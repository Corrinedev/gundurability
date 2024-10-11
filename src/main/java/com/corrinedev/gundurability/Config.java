package net.corrinedev.gundurability.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.ConfigValue<Double> MAXDURABILITY;
	public static final ForgeConfigSpec.ConfigValue<Boolean> GUNSBREAK;
	public static final ForgeConfigSpec.ConfigValue<Double> JAMCHANCE;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SHOTGUNLIST;
	static {
		BUILDER.push("maxdurability");
		MAXDURABILITY = BUILDER.comment("The durability the gun starts with and the max it can repair to").define("maxdurability", (double) 2000);
		BUILDER.pop();
		BUILDER.push("gunsbreak");
		GUNSBREAK = BUILDER.comment("Do guns break or not").define("gunsbreak", false);
		BUILDER.pop();
		BUILDER.push("jamchance");
		JAMCHANCE = BUILDER.comment("Set to zero to stop jamming entirely").define("jamchance", (double) 20);
		BUILDER.pop();
		BUILDER.push("shotgunlist");
		SHOTGUNLIST = BUILDER.comment("Add any shotguns here to prevent the bug where they take 10 times more durability").defineList("shotgunlist", List.of("tacz:aa12", "tacz:db_short", "tacz:db_long"), entry -> true);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
