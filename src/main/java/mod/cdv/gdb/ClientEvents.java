package mod.cdv.gdb;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import mod.cdv.gdb.resource.GunModifier;
import net.minecraftforge.api.distmarker.Dist;
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
}
