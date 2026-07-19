package mod.cdv.gdb.resource;

import com.mojang.datafixers.util.Either;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;

public record GunModifier(
        boolean enabled,
        StatModifier[] modifiers,
        Ingredient repairItem,
        Either<Either<GunTabType, AttachmentType>, ResourceLocation> target,
        int maxDurability,
        int repairCost,
        float xpCost,
        boolean preventFiring,
        boolean jam,
        float jamChance,
        float jamThreshold
) {
    public static GunModifier createDisabled(Either<Either<GunTabType, AttachmentType>, ResourceLocation> target) {
        return new GunModifier(target);
    }

    // Default constructor (enabled=true)
    public GunModifier(
            StatModifier[] modifiers,
            Ingredient repairItem,
            Either<Either<GunTabType, AttachmentType>, ResourceLocation> target,
            int maxDurability,
            int repairCost,
            float xpCost,
            boolean preventFiring,
            boolean jam,
            float jamChance,
            float jamThreshold
    ) {
        this(true, modifiers, repairItem, target, maxDurability, repairCost, xpCost, preventFiring, jam, jamChance, jamThreshold);
    }

    //Disabled GunModifier
    private GunModifier(Either<Either<GunTabType, AttachmentType>, ResourceLocation> target) {
        this(false, new StatModifier[0], Ingredient.of(), target, 0, 0, 0, false, false, 0, 0);
    }
    @Override
    public String toString() {
        return "GunModifier{" +
                "modifiers=" + Arrays.toString(modifiers) +
                ", repairItem=" + repairItem +
                ", target=" + target +
                ", maxDurability=" + maxDurability +
                ", repairCost=" + repairCost +
                ", xpCost=" + xpCost +
                ", jam=" + jam +
                ", jamChance=" + jamChance +
                ", jamThreshold=" + jamThreshold +
                '}';
    }
}
