package mod.cdv.gdb.mixin;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.AnimationPlan;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.input.InspectKey;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.network.NetworkHandler;
import mod.cdv.gdb.network.StartJamWorkerPacket;
import mod.cdv.gdb.resource.GunModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(value = InspectKey.class, remap = false)
public class InspectKeybindHook {
    @Inject(method = "onInspectPress", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/client/gameplay/IClientPlayerGunOperator;inspect()V"), cancellable = true)
    private static void keyPress(InputEvent.Key event, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        ItemStack mainHandItem = player.getMainHandItem();
        GunModifier modifiers = DataLookup.getModifiers(mainHandItem);
        if(modifiers != null && mainHandItem.getItem() instanceof ModernKineticGunItem iGun && mainHandItem.getTag().getBoolean("Jammed")) {
            gundurability$unjam(player, mainHandItem, iGun, modifiers);
            ci.cancel();
        }
    }

    @Inject(method = "onInspectControllerPress", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/client/gameplay/IClientPlayerGunOperator;inspect()V"), cancellable = true)
    private static void controllerPress(boolean isPress, CallbackInfoReturnable<Boolean> cir) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        ItemStack mainHandItem = player.getMainHandItem();
        GunModifier modifiers = DataLookup.getModifiers(mainHandItem);
        if(modifiers != null && mainHandItem.getItem() instanceof ModernKineticGunItem iGun && mainHandItem.getTag().getBoolean("Jammed")) {
            gundurability$unjam(player, mainHandItem, iGun, modifiers);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static void gundurability$unjam(Player player, ItemStack mainHandItem, IGun iGun, GunModifier modifiers) {
        GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).map(ClientGunIndex::getGunData).orElse(null);
        if (gunData != null) {
            TimelessAPI.getGunDisplay(mainHandItem).ifPresent((gunIndex) -> {
                Bolt boltType = gunData.getBolt();
                boolean noAmmo;
                if (boltType == Bolt.OPEN_BOLT) {
                    noAmmo = iGun.getCurrentAmmoCount(mainHandItem) <= 0;
                } else {
                    noAmmo = !iGun.hasBulletInBarrel(mainHandItem);
                }

                SoundPlayManager.stopPlayGunSound();

                LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = gunIndex.getAnimationStateMachine();
                if (animationStateMachine != null) {
                    if (animationStateMachine.getAnimationController().containPrototype("unjam") && animationStateMachine.getContext() != null) {
                        SoundPlayManager.playClientSound(player, gunIndex.getSounds("unjam"), 1.0f, 1.0f, GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
                        animationStateMachine.trigger("unjam");
                    } else {
                        SoundPlayManager.playInspectSound(player, gunIndex, noAmmo);
                        animationStateMachine.trigger("inspect");
                        int length = (int) (((AnimationControllerAccessor) animationStateMachine.getAnimationController()).getPrototypes().get("inspect").getMaxEndTimeS() * 1000);
                        NetworkHandler.sendToServer(new StartJamWorkerPacket(length));
                    }
                }


            });
        }
    }
}
