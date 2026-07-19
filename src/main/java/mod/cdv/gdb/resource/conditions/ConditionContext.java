package mod.cdv.gdb.resource.conditions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ConditionContext(
        Player player,
        ItemStack gunStack
) {
}
