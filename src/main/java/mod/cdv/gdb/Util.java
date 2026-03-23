package mod.cdv.gdb;

import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.GunProperty;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import static com.tacz.guns.api.GunProperties.*;

public final class Util {
    public static <T> T getOrSetTag(ItemStack stack, String key, T defaultValue) {
        if(!stack.hasTag()) return defaultValue;
        if(!stack.getTag().contains(key)) {
            switch (defaultValue.getClass().getSimpleName()) {
                case "int", "Integer" -> stack.getTag().putInt(key, (Integer) defaultValue);
                case "boolean", "Boolean" -> stack.getTag().putBoolean(key, (Boolean) defaultValue);
                case "String" -> stack.getTag().putString(key, (String) defaultValue);
                case "CompoundTag", "Tag" -> stack.getTag().put(key, (Tag) defaultValue);
                default -> throw new IllegalStateException("Unexpected value: " + defaultValue.getClass().getSimpleName());
            }
        }
        return (T) switch (defaultValue.getClass().getSimpleName()) {
            case "int", "Integer" -> stack.getTag().getInt(key);
            case "boolean", "Boolean" -> stack.getTag().getBoolean(key);
            case "String" -> stack.getTag().getString(key);
            case "CompoundTag" -> stack.getTag().getCompound(key);
            default -> throw new IllegalStateException("Unexpected value: " + defaultValue.getClass().getSimpleName());
        };
    }

    public static GunProperty<?> getGunProperty(String prop) {
        return switch (prop.toLowerCase()) {
            case "ammospeed" -> AMMO_SPEED;
            case "weight" -> WEIGHT;
            case "damage" -> DAMAGE;
            case "ads" -> ADS_TIME;
            case "armorignore" -> ARMOR_IGNORE;
            case "inaccuracy" -> INACCURACY;
            case "range" -> EFFECTIVE_RANGE;
            case "speed" -> MOVE_SPEED;
            case "headshot" -> HEADSHOT_MULTIPLIER;
            case "knockback" -> KNOCKBACK;
            case "pierce" -> PIERCE;
            case "recoil" -> RECOIL;
            case "rpm" -> ROUNDS_PER_MINUTE;
            default -> throw new IllegalStateException("Unexpected value: " + prop);
        };
    }

    public static float remap(float value, float new_min, float new_max, float old_min, float old_max) {
        return new_min + (new_max - new_min) * ((value - old_min) / (old_max - old_min));
    }
}
