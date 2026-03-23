package mod.cdv.gdb.mixin;

import com.tacz.guns.client.input.InspectKey;
import mod.cdv.gdb.network.NetworkHandler;
import mod.cdv.gdb.network.StartJamWorkerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InspectKey.class, remap = false)
public class InspectKeybindHook {
    @Inject(method = "onInspectPress", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/client/gameplay/IClientPlayerGunOperator;inspect()V"))
    private static void keyPress(InputEvent.Key event, CallbackInfo ci) {
        gundurability$unjam();
    }

    @Inject(method = "onInspectControllerPress", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/client/gameplay/IClientPlayerGunOperator;inspect()V"))
    private static void controllerPress(boolean isPress, CallbackInfoReturnable<Boolean> cir) {
        gundurability$unjam();
    }

    @Unique
    private static void gundurability$unjam() {
        if(Minecraft.getInstance().player == null) return;
        ItemStack item = Minecraft.getInstance().player.getMainHandItem();
        if(item.getTag() != null && item.getTag().getBoolean("Jammed")) {
            NetworkHandler.sendToServer(new StartJamWorkerPacket());
        }
    }
}
