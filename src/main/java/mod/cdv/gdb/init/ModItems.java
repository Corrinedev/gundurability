package mod.cdv.gdb.init;


import mod.cdv.gdb.repair.PartItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

public class ModItems {
    public static PartItem PART_ITEM;

    //@SubscribeEvent
    void dynamicRegister(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> helper.register("gun_part", PART_ITEM = new PartItem()));
    }
}
