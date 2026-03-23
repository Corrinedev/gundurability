package mod.cdv.gdb.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.tacz.guns.item.ModernKineticGunItem;
import mod.cdv.gdb.DataLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    public int repairItemCountCost;

    public AnvilMenuMixin(@Nullable MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(pType, pContainerId, pPlayerInventory, pAccess);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V", shift = At.Shift.BEFORE))
    public void modifyRepairLogic(CallbackInfo ci, @Local(name = "itemstack1", ordinal = 1) ItemStack itemstack1) {
        var item = this.inputSlots.getItem(0);
        var modifiers = DataLookup.getModifiers(item);
        if(modifiers != null && item.getItem().isValidRepairItem(item, this.inputSlots.getItem(1))) {
            int itemCount = this.inputSlots.getItem(1).getCount();
            itemstack1.setDamageValue(item.getDamageValue());
            int l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / modifiers.repairCost());
            if (l2 <= 0) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                this.cost.set(0);
                return;
            }

            int i3;
            for(i3 = 0; l2 > 0 && i3 < itemCount; ++i3) {
                int j3 = itemstack1.getDamageValue() - l2;
                itemstack1.setDamageValue(j3);
                l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / modifiers.repairCost());
            }
            int xpCost = Math.round(modifiers.xpCost() * Math.min(itemCount, i3));
            cost.set(xpCost > 0 ? xpCost : 1);
            this.repairItemCountCost = i3;
        }

        if(modifiers != null && this.inputSlots.getItem(1).getItem() instanceof ModernKineticGunItem && !item.getTag().getString("GunId").equals(this.inputSlots.getItem(1).getTag().getString("GunId"))) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
        }
    }
}
