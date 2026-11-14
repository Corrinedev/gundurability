package mod.cdv.gdb.mixin;

import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.modifier.custom.RecoilModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import com.tacz.guns.resource.pojo.data.gun.GunRecoilKeyFrame;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.resource.GunModifier;
import mod.cdv.gdb.resource.StatModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static mod.cdv.gdb.GunDurability.applyModifier;

@Mixin(value = RecoilModifier.class, remap = false)
public class RecoilModifierMixin {

    @Shadow
    private static float getMaxInGunRecoilKeyFrame(GunRecoilKeyFrame[] frames) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "initCache", at = @At("HEAD"), cancellable = true)
    public void initCache(ItemStack gunItem, GunData gunData, CallbackInfoReturnable<CacheValue<ParameterizedCachePair<Float, Float>>> cir) {
        GunRecoil recoil = gunData.getRecoil();
        if(recoil != null) {
            var modifier = DataLookup.getModifiers(gunItem);
            HashMap<ItemStack, GunModifier> attachmentModifiers = new HashMap<>();
            ModernKineticGunItem gun = (ModernKineticGunItem) gunItem.getItem();

            for (var type : AttachmentType.values()) {
                ItemStack stack = gun.getAttachment(gunItem, type);
                if (stack.isEmpty()) continue;
                GunModifier attachmentModifier = DataLookup.getModifiers(stack);
                if (attachmentModifier != null)
                    attachmentModifiers.put(stack, attachmentModifier);
            }
            if (modifier != null || !attachmentModifiers.isEmpty()) {
                HashMap<GunProperty<?>, Float> combinedMultipliers = new HashMap<>();
                // For the base gun modifier (uses gun durability)
                if (modifier != null) {
                    float durabilityPercent = ((float) gunItem.getMaxDamage() - gunItem.getDamageValue()) / gunItem.getMaxDamage();
                    for (StatModifier mod : modifier.modifiers()) {
                        applyModifier(mod, durabilityPercent, combinedMultipliers);
                    }
                }

                // For each attachment (uses attachment's own durability)
                if (!attachmentModifiers.isEmpty()) {
                    for (Map.Entry<ItemStack, GunModifier> entry : attachmentModifiers.entrySet()) {
                        ItemStack attachmentStack = entry.getKey();
                        float durabilityPercentA = ((float) attachmentStack.getMaxDamage() - attachmentStack.getDamageValue()) / attachmentStack.getMaxDamage();
                        for (StatModifier mod : entry.getValue().modifiers()) {
                            applyModifier(mod, durabilityPercentA, combinedMultipliers);
                        }
                    }
                }

                var reductionFactor = combinedMultipliers.get(GunProperties.RECOIL);

                if (reductionFactor != null) {
                    float pitch = getMaxInGunRecoilKeyFrame(recoil.getPitch());
                    float yaw = getMaxInGunRecoilKeyFrame(recoil.getYaw());
                    cir.setReturnValue(new CacheValue<>(ParameterizedCachePair.of(pitch * reductionFactor, yaw * reductionFactor)));
                }
            }
        }
    }
}
