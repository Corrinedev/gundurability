package mod.cdv.gdb.repair;

import com.tacz.guns.api.TimelessAPI;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.Util;
import mod.cdv.gdb.init.ModItems;
import mod.cdv.gdb.resource.PartDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Locale;

public class PartItem extends Item {

    public PartItem() {
        super(new Properties());
    }

    public static @Nullable PartDefinition getPartData(ItemStack itemStack) {
        return DataLookup.getPartData(itemStack);
    }
    @Override
    public int getBarWidth(ItemStack pStack) {
        return Math.round(12.0F - Util.getOrSetTag(pStack, "PartDamage", 1.0f) * 13.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return Util.getOrSetTag(pStack, "PartDamage", 0.0f) < 1.0f;
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return super.getRenderPropertiesInternal();
    }

    public static float getPartPercentage(ItemStack gunStack, PartDefinition.PartType partType) {
        if(!gunStack.getOrCreateTag().contains("PartData")) return 0.0f;

        CompoundTag partData = gunStack.getOrCreateTag().getCompound("PartData");
        return partData.getFloat(partType.name().toLowerCase(Locale.ROOT));
    }

    public static void setPercentForPart(ItemStack gunStack, PartDefinition.PartType partType) {
        if(!gunStack.getOrCreateTag().contains("PartData")) {
            CompoundTag defaultTag = new CompoundTag();
            gunStack.getOrCreateTag().put("PartData", defaultTag);
        }

        CompoundTag partData = gunStack.getTag().getCompound("PartData");


    }

    public static ItemStack getPartItem(ItemStack gunStack, PartDefinition.PartType partType) {
        if(!gunStack.getOrCreateTag().contains("PartData")) return ItemStack.EMPTY;

        ItemStack base = ModItems.PART_ITEM.getDefaultInstance();
        base.getOrCreateTag().putString("PartId", PartDefinition.getPartIdForGun(partType, gunStack).toString());
        base.getTag().putFloat("PartDamage", getPartPercentage(gunStack, partType));
        return base;
    }

    @Override
    public Component getName(ItemStack pStack) {
        PartDefinition def = DataLookup.getPartData(pStack);
        if(def == null) return Component.literal("Invalid Part");
        return switch (def.target()) {
            case BARREL -> Component.translatable(TimelessAPI.getCommonAmmoIndex(def.ammoItem()).get().getPojo().getName()).append(Component.literal(" Barrel"));
            case TRIGGER -> Component.literal(def.tabType().name() + " Trigger");
            case BOLT -> Component.translatable(TimelessAPI.getCommonAmmoIndex(def.ammoItem()).get().getPojo().getName()).append(Component.literal(" " + def.tabType().name() + " Bolt"));
        };
    }
}
