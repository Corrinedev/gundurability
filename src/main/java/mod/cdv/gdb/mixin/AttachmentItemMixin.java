package mod.cdv.gdb.mixin;

import com.tacz.guns.item.AttachmentItem;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AttachmentItem.class)
public class AttachmentItemMixin extends Item {
    public AttachmentItemMixin(Properties pProperties) {super(pProperties);}

    @Override
    public boolean isRepairable(ItemStack stack) {
        return Util.getOrSetTag(stack, "Damage", 0) >= 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        var modifiers = DataLookup.getModifiers(pStack);
        return modifiers != null && modifiers.repairItem().test(pRepairCandidate);
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        var mods = DataLookup.getModifiers(stack);
        return enchantment.category.canEnchant(stack.getItem()) || (mods != null && (
                enchantment == Enchantments.UNBREAKING ||
                        enchantment == Enchantments.MENDING
        ));
    }

    @Override
    public boolean isDamageable(ItemStack pStack) {
        mod.cdv.gdb.resource.GunModifier modifiers = DataLookup.getModifiers(pStack);
        return modifiers != null;
    }

    @Override
    public int getMaxDamage(ItemStack pStack) {
        mod.cdv.gdb.resource.GunModifier modifiers = DataLookup.getModifiers(pStack);
        return modifiers != null ? modifiers.maxDurability() : 0;
    }

    @Override
    public float getXpRepairRatio(ItemStack stack) {
        return 2.0f;
    }
}
