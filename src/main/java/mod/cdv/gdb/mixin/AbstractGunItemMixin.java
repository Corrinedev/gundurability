package mod.cdv.gdb.mixin;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunItem;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractGunItem.class)
public class AbstractGunItemMixin extends Item {
    public AbstractGunItemMixin(Properties pProperties) {
        super(pProperties);
    }
}
