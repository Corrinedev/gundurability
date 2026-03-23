package mod.cdv.gdb.resource;

import com.tacz.guns.api.GunProperty;

import java.util.ArrayList;
import java.util.Arrays;

public record StatModifier(
        GunProperty<?>[] types,
        float reductionThreshold,
        float maxReduction
) {
    @Override
    public String toString() {
        return "StatModifier{" +
                "types=" + Arrays.toString(types) +
                ", reductionThreshold=" + reductionThreshold +
                ", maxReduction=" + maxReduction +
                '}';
    }
}
