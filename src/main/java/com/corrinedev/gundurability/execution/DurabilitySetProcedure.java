package com.corrinedev.gundurability.execution;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;

public class DurabilitySetProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		((new Object() {
			public Entity getEntity() {
				try {
					return EntityArgument.getEntity(arguments, "player");
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
					return null;
				}
			}
		}.getEntity()) instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("Durability", (IntegerArgumentType.getInteger(arguments, "durability")));
	}
}
