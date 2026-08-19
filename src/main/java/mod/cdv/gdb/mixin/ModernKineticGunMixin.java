package mod.cdv.gdb.mixin;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.Util;
import mod.cdv.gdb.network.NetworkHandler;
import mod.cdv.gdb.network.SyncDamageNBTPacket;
import mod.cdv.gdb.resource.GunModifier;
import mod.cdv.gdb.resource.PartDefinition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mod.cdv.gdb.repair.PartItem.*;

@Mixin(value = ModernKineticGunItem.class, remap = false)
public class ModernKineticGunMixin extends Item {
    public ModernKineticGunMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return Util.getOrSetTag(stack, "Damage", 0) >= 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        var modifiers = DataLookup.getModifiers(pStack);
        return modifiers != null && modifiers.repairItem().test(pRepairCandidate);
    }

    //@Override
    //public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
    //    PartDefinition partData = getPartData(pOther);
    //    System.out.println(partData);
    //    if(partData != null)
    //        System.out.println(partData.isApplicable(pSlot.getItem()));
    //    if(partData != null && partData.isApplicable(pSlot.getItem())) {
    //        ItemStack gunStack = pSlot.getItem();
    //        ItemStack oldPart = getPartItem(gunStack, partData.target());
    //        setPercentForPart(gunStack, partData.target());
    //        pAccess.set(oldPart);
//
    //        System.out.println("slot set = " + partData);
    //    }
    //    return super.overrideOtherStackedOnMe(pStack, pOther, pSlot, pAction, pPlayer, pAccess);
    //}

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

    @Inject(method = "defaultReloadFinishing", at = @At("HEAD"))
    private void finishReload(ModernKineticGunScriptAPI api, boolean isTactical, CallbackInfo ci) {
        if (api.getShooter() instanceof Player plr) {
            ItemStack item = api.getItemStack();
            ModernKineticGunItem gun = (ModernKineticGunItem) item.getItem();
            AttachmentType type = AttachmentType.EXTENDED_MAG;
            ItemStack stack = gun.getAttachment(item, type);
            if (stack.isEmpty()) return;
            GunModifier attachmentModifier = DataLookup.getModifiers(stack);
            if (attachmentModifier != null) {
                int dmg = stack.getDamageValue() + 1;
                stack.setDamageValue(dmg);
                if (api.getShooter() instanceof ServerPlayer sv)
                    NetworkHandler.sendToClient(new SyncDamageNBTPacket(stack, dmg), sv);
                if (stack.getMaxDamage() <= stack.getDamageValue()) {
                    if (!stack.isEmpty() && plr.getInventory().add(stack)) {
                        gun.unloadAttachment(item, type);
                        AttachmentPropertyManager.postChangeEvent(plr, item);
                        gun.dropAllAmmo(plr, item);
                        plr.inventoryMenu.broadcastChanges();
                        plr.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 2.5f, 0.6f);
                    }
                }
            }
        }
    }
    @Inject(method = "startReload", at = @At("HEAD"))
    private void startReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, CallbackInfoReturnable<Boolean> cir) {
        if (shooter instanceof Player plr) {
            ModernKineticGunItem gun = (ModernKineticGunItem) gunItem.getItem();
            AttachmentType type = AttachmentType.EXTENDED_MAG;
            ItemStack stack = gun.getAttachment(gunItem, type);
            if (stack.isEmpty()) return;
            GunModifier attachmentModifier = DataLookup.getModifiers(stack);
            if (attachmentModifier != null) {
                if (stack.getMaxDamage() <= stack.getDamageValue()) {
                    if (!stack.isEmpty() && plr.getInventory().add(stack)) {
                        gun.unloadAttachment(gunItem, type);
                        AttachmentPropertyManager.postChangeEvent(plr, gunItem);
                        gun.dropAllAmmo(plr, gunItem);
                        plr.inventoryMenu.broadcastChanges();
                        plr.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 2.5f, 0.6f);
                    }
                }
            }
        }
    }
}
