package mod.cdv.gdb;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.item.AttachmentItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import mod.cdv.gdb.resource.GunModifier;
import mod.cdv.gdb.resource.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public final class DataLookup {

    public static final ArrayList<GunModifier> gunModifiers = new ArrayList<>();
    public static final HashMap<ResourceLocation, GunModifier> modifierCache = new HashMap<>();
    public static final HashMap<ResourceLocation, PartDefinition> partData = new HashMap<>();

    public static Map<ResourceLocation, GunModifier> getAllModifiersCache() {
        return new HashMap<>(modifierCache); // Return a copy
    }

    public static void createPartData() {
        partData.clear();
        for (Map.Entry<ResourceLocation, CommonAmmoIndex> allCommonAmmoIndex : TimelessAPI.getAllCommonAmmoIndex()) {
            partData.put(ResourceLocation.fromNamespaceAndPath("barrel", allCommonAmmoIndex.getKey().getPath()), PartDefinition.barrel(allCommonAmmoIndex.getKey()));

            for (GunTabType value : GunTabType.values()) {
                partData.put(ResourceLocation.fromNamespaceAndPath("bolt", allCommonAmmoIndex.getKey().getPath() + "_" + value.name().toLowerCase(Locale.ROOT)), PartDefinition.bolt(allCommonAmmoIndex.getKey(), value));
            }
        }

        for (GunTabType value : GunTabType.values()) {
            partData.put(ResourceLocation.fromNamespaceAndPath("trigger", value.name().toLowerCase(Locale.ROOT)), PartDefinition.trigger(value));
        }
    }

    public static @Nullable PartDefinition getPartData(ItemStack itemStack) {
        String partId = itemStack.getTag().getString("PartId");
        return partData.get(ResourceLocation.parse(partId));
    }

    public static @Nullable PartDefinition getPartData(ResourceLocation partId) {
        return partData.get(partId);
    }

    public static ArrayList<GunModifier> getAllModifiers() {
        return new ArrayList<>(gunModifiers); // Return a copy
    }

    public static @Nullable GunModifier getModifiers(ItemStack item) {
        if(item.getItem() instanceof AttachmentItem) return getAttachmentModifiers(item);
        if (item.getItem() instanceof ModernKineticGunItem) return getGunIdModifiers(item);
        return null;
    }

    public static @Nullable GunModifier getGunIdModifiers(ItemStack item) {
        if(!(item.getItem() instanceof ModernKineticGunItem)) return null;
        if (item.getTag() == null) return null;
        if (!item.getTag().contains("GunId")) return null;

        ResourceLocation gunId = ResourceLocation.parse(item.getTag().getString("GunId"));
        if(modifierCache.containsKey(gunId)) {
            final GunModifier mod = modifierCache.get(gunId);
            if(mod.enabled()) return mod;
            return null;
        }
        var index = TimelessAPI.getCommonGunIndex(gunId);
        if(index.isEmpty()) return null;
        GunTabType category = GunTabType.valueOf(
                index.get().getType().toUpperCase()
        );
        GunModifier modifier = null;
        Optional<GunModifier> gunIdModifier = gunModifiers.stream()
                .filter(gunModifier ->
                        gunModifier.target().right().isPresent() && gunModifier.target().right().get().equals(gunId)
                ).findFirst();


        if(gunIdModifier.isPresent()) {
            modifier = gunIdModifier.get();
        } else {
            Optional<GunModifier> categoryModifier = gunModifiers.stream()
                    .filter(gunModifier ->
                            gunModifier.target().left().isPresent() &&
                                    gunModifier.target().left().get().left().isPresent() &&
                                    gunModifier.target().left().get().left().get() == category
                    ).findFirst();
            if(categoryModifier.isPresent()) {
                modifier = categoryModifier.get();
            }
        }
        if (modifier == null) return null;
        modifierCache.put(gunId, modifier);
        if(modifier.enabled()) return modifier;
        return null;
    }

    public static @Nullable GunModifier getAttachmentModifiers(ItemStack item) {
        if(!(item.getItem() instanceof AttachmentItem)) return null;
        if (item.getTag() == null) return null;
        if (!item.getTag().contains("AttachmentId")) return null;

        ResourceLocation gunId = ResourceLocation.parse(item.getTag().getString("AttachmentId"));
        if(modifierCache.containsKey(gunId)) {
            final GunModifier mod = modifierCache.get(gunId);
            if(mod.enabled()) return mod;
            return null;
        }
        var index = TimelessAPI.getCommonAttachmentIndex(gunId);
        if(index.isEmpty()) return null;
        AttachmentType category = index.get().getType();
        GunModifier modifier = null;
        Optional<GunModifier> gunIdModifier = gunModifiers.stream()
                .filter(gunModifier ->
                        gunModifier.target().right().isPresent() && gunModifier.target().right().get().equals(gunId)
                ).findFirst();


        if(gunIdModifier.isPresent()) {
            modifier = gunIdModifier.get();
        } else {
            Optional<GunModifier> categoryModifier = gunModifiers.stream()
                    .filter(gunModifier ->
                            gunModifier.target().left().isPresent() &&
                                    gunModifier.target().left().get().right().isPresent() &&
                                    gunModifier.target().left().get().right().get() == category
                    ).findFirst();
            if(categoryModifier.isPresent()) {
                modifier = categoryModifier.get();
            }
        }
        if (modifier == null) return null;
        modifierCache.put(gunId, modifier);
        if(modifier.enabled()) return modifier;
        return null;
    }

    public static void setModifiers(ArrayList<GunModifier> modifiers) {
        modifierCache.clear();
        gunModifiers.addAll(modifiers);
    }
}
