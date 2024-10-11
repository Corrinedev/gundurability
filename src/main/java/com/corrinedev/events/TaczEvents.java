package com.corrinedev.events;

import com.corrinedev.gundurability.Config;
import com.tacz.guns.api.event.common.GunReloadEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TaczEvents {
    @SubscribeEvent
    public static void onShootEvent(GunShootEvent event) {
         if(event.getLogicalSide().isServer()) {

                System.out.println("Shoot");
             if(event.getShooter().getMainHandItem().getTag().getBoolean("HasDurability") == true) {
                if(event.getShooter().getMainHandItem().getTag().getString("GunFireMode").equals("BURST")) {
                    event.getShooter().getMainHandItem().getTag().putInt("Durability", event.getShooter().getMainHandItem().getTag().getInt("Durability") - 3);
                } else {
                    event.getShooter().getMainHandItem().getTag().putInt("Durability", event.getShooter().getMainHandItem().getTag().getInt("Durability") - 1);
                }
             } else {
                 event.getShooter().getMainHandItem().getOrCreateTag().putBoolean("HasDurability", true);
             event.getShooter().getMainHandItem().getOrCreateTag().putInt("Durability", Config.MAXDURABILITY.get());
             }

             if(!event.getShooter().getMainHandItem().getTag().getBoolean("Jammed")) {



             }
         }
    }
}
