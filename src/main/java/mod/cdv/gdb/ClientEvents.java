package mod.cdv.gdb;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.cdv.gdb.resource.GunModifier;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void shootEvent(GunShootEvent event) {
        var item = event.getGunItemStack();
        if (event.getLogicalSide().isServer()) return;
        GunModifier modifiers = DataLookup.getModifiers(item);
        if(modifiers == null) return;
        //Jam cancel
        if(item.getTag() != null && item.getTag().contains("Jammed") && item.getTag().getBoolean("Jammed")) {
            event.setCanceled(true);
            TimelessAPI.getGunDisplay(event.getGunItemStack()).ifPresent(display -> SoundPlayManager.playDryFireSound(event.getShooter(), display));
            return;
        }
        //Zero durability cancel
        if (item.getDamageValue() == item.getMaxDamage()) {
            if(!modifiers.preventFiring()) {
                event.setCanceled(true);
                TimelessAPI.getGunDisplay(event.getGunItemStack()).ifPresent(display -> SoundPlayManager.playDryFireSound(event.getShooter(), display));
            }
            return;
        }
        AttachmentPropertyManager.postChangeEvent(event.getShooter(), item);
    }

    @SubscribeEvent
    public static void animateTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START) return;

        var player = Minecraft.getInstance().player;
        if(player == null) return;
        var mainHandItem = player.getMainHandItem();
        GunModifier modifiers = DataLookup.getModifiers(mainHandItem);
        if(modifiers == null) return;
        var iGun = IGun.getIGunOrNull(mainHandItem);
        if(modifiers.jam() && Util.getOrSetTag(mainHandItem, "Jammed", false)) {
            GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).map(ClientGunIndex::getGunData).orElse(null);
            if (gunData != null) {
                TimelessAPI.getGunDisplay(mainHandItem).ifPresent((gunIndex) -> {
                    LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = gunIndex.getAnimationStateMachine();
                    if (animationStateMachine != null) {
                        if (animationStateMachine.getAnimationController().containPrototype("unjam_idle")) {
                            animationStateMachine.trigger("unjam_idle");
                        }
                    }
                });
            }
        }
    }
}
