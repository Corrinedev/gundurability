package mod.cdv.gdb.resource;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.cdv.gdb.DataLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public record PartDefinition(
        PartType target,
        @Nullable ResourceLocation ammoItem,
        @Nullable GunTabType tabType
) {
    public boolean isApplicable(ItemStack gunStack) {
        if(!gunStack.is(ModItems.MODERN_KINETIC_GUN.get())) return false;
        ResourceLocation gunId = ModItems.MODERN_KINETIC_GUN.get().getGunId(gunStack);
        CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElseThrow();

        return switch (target) {
            case BARREL -> gunIndex.getGunData().getAmmoId().equals(ammoItem);
            case TRIGGER -> gunIndex.getType().equals(tabType.name().toLowerCase(Locale.ROOT));
            case BOLT -> gunIndex.getGunData().getAmmoId().equals(ammoItem) && gunIndex.getType().equals(tabType.name().toLowerCase(Locale.ROOT));
        };
    }

    public static PartDefinition getPartForGun(PartType type, ItemStack gunStack) {
        if(!gunStack.is(ModItems.MODERN_KINETIC_GUN.get())) return null;
        ResourceLocation gunId = ModItems.MODERN_KINETIC_GUN.get().getGunId(gunStack);
        CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElseThrow();

        return switch (type) {
            case BARREL -> DataLookup.getPartData(ResourceLocation.fromNamespaceAndPath("barrel", gunIndex.getGunData().getAmmoId().getPath()));
            case TRIGGER -> DataLookup.getPartData(ResourceLocation.fromNamespaceAndPath("trigger", gunIndex.getType().toLowerCase(Locale.ROOT)));
            case BOLT -> DataLookup.getPartData(ResourceLocation.fromNamespaceAndPath("bolt", gunIndex.getGunData().getAmmoId().getPath() + "_" + gunIndex.getType().toLowerCase(Locale.ROOT)));
        };
    }

    public static ResourceLocation getPartIdForGun(PartType type, ItemStack gunStack) {
        if(!gunStack.is(ModItems.MODERN_KINETIC_GUN.get())) return null;
        ResourceLocation gunId = ModItems.MODERN_KINETIC_GUN.get().getGunId(gunStack);
        CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElseThrow();

        return switch (type) {
            case BARREL -> ResourceLocation.fromNamespaceAndPath("barrel", gunIndex.getGunData().getAmmoId().getPath());
            case TRIGGER -> ResourceLocation.fromNamespaceAndPath("trigger", gunIndex.getType().toLowerCase(Locale.ROOT));
            case BOLT -> ResourceLocation.fromNamespaceAndPath("bolt", gunIndex.getGunData().getAmmoId().getPath() + "_" + gunIndex.getType().toLowerCase(Locale.ROOT));
        };
    }

    public static PartDefinition barrel(
            ResourceLocation ammoItem
    ) {
        return new PartDefinition(PartType.BARREL, ammoItem, null);
    }

    public static PartDefinition trigger(
            GunTabType tabType
    ) {
        return new PartDefinition(PartType.TRIGGER, null, tabType);
    }

    public static PartDefinition bolt(
            ResourceLocation ammoItem,
            GunTabType tabType
    ) {
        return new PartDefinition(PartType.BOLT, ammoItem, tabType);
    }

    public enum PartType {
        BARREL,
        TRIGGER,
        BOLT
    }
}
