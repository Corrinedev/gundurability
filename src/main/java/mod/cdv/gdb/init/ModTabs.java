package mod.cdv.gdb.init;

import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.resource.PartDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Map;

public class ModTabs {
    public static CreativeModeTab TAB;

    //@SubscribeEvent
    void dynamicRegister(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> {
            TAB = CreativeModeTab.builder().icon(Items.IRON_INGOT::getDefaultInstance).displayItems(
                    (itemDisplayParameters, output) -> {
                        for (Map.Entry<ResourceLocation, PartDefinition> e : DataLookup.partData.entrySet()) {
                            CompoundTag tag = new CompoundTag();
                            tag.putString("id", "gundb:gun_part");
                            tag.putInt("Count", 1);
                            ItemStack item = ItemStack.of(tag);
                            item.getOrCreateTag().putString("PartId", e.getKey().toString());
                            output.accept(item);
                        }
                    }
            ).build();
            helper.register("part_tab", TAB);
        });
    }
}
