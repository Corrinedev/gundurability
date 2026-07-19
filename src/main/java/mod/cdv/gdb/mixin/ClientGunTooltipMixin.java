package mod.cdv.gdb.mixin;

import com.tacz.guns.client.tooltip.ClientGunTooltip;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.resource.GunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mod.cdv.gdb.GunDurability.getJamPossibility;

@Mixin(value = ClientGunTooltip.class, remap = true)
public abstract class ClientGunTooltipMixin {

    @Shadow
    @Final
    private ItemStack gun;

    @Inject(method = "getHeight()I", at = @At("RETURN"), cancellable = true)
    void getHeight(CallbackInfoReturnable<Integer> cir) {
        GunModifier modifier = DataLookup.getModifiers(gun);
        if(modifier != null && modifier.jam()) {
            cir.setReturnValue(cir.getReturnValue() + 10);
        }
    }

    @ModifyVariable(
            method = "renderText",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z", ordinal = 2),
            name = "yOffset"
    )
    private int renderJamInfoText(int yOffset, Font font, int pX, int pY, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        GunModifier modifier = DataLookup.getModifiers(gun);
        if (modifier != null && modifier.jam()) {
            float durabilityPercent = ((float) gun.getMaxDamage() - gun.getDamageValue()) / gun.getMaxDamage();
            float jamChance = getJamPossibility(durabilityPercent, modifier.jamChance(), modifier.jamThreshold()) * 100;
            if(jamChance < 0) jamChance = 0f;
            yOffset += 4;
            font.drawInBatch(Component.literal("Jam Chance: ").append(Component.literal(String.format("%.2f%%", jamChance)).withStyle(ChatFormatting.AQUA)), (float)pX, (float)yOffset, 7829367, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            yOffset += 6;
        }

        return yOffset;
    }
}
