package com.corrinedev.events;

import com.corrinedev.gundurability.Config;
import com.corrinedev.gundurability.init.GundurabilityModGameRules;
import com.corrinedev.gundurability.init.GundurabilityModSounds;
import com.tacz.guns.api.event.common.GunShootEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TaczEvents {
    @SubscribeEvent
    public static void onShootEvent(GunShootEvent event) {

         if(event.getLogicalSide().isServer()) {

            //Biome modifier code start
             int biomeModifier = 0;
             if(event.getShooter().isUnderWater()) {
                biomeModifier = Config.WATERMODIFIER.get();
             } else if (event.getShooter().level().getBiome(event.getShooter().blockPosition()).containsTag(BiomeTags.IS_JUNGLE)) {
                 biomeModifier = Config.JUNGLEBIOMEMODIFIER.get();
             } else if (event.getShooter().level().getBiome(event.getShooter().blockPosition()).containsTag(BiomeTags.IS_BADLANDS)) {
                 biomeModifier = Config.DESERTBIOMEMODIFIER.get();
             } else if (event.getShooter().level().getBiome(event.getShooter().blockPosition()).containsTag(BiomeTags.IS_TAIGA)) {
                 biomeModifier = Config.COLDBIOMEMODIFIER.get();
             } else if (event.getShooter().level().getBiome(event.getShooter().blockPosition()) == Biomes.DESERT) {
                 biomeModifier = Config.DESERTBIOMEMODIFIER.get();
             } else if (event.getShooter().level().getBiome(event.getShooter().blockPosition()) == Biomes.SWAMP) {
                 biomeModifier = Config.SWAMPBIOMEMODIFIER.get();
             } else if (event.getShooter().level().getBiome(event.getShooter().blockPosition()) == Biomes.MANGROVE_SWAMP) {
                 biomeModifier = Config.SWAMPBIOMEMODIFIER.get();
             } else if (event.getShooter().level().getBiome(event.getShooter().blockPosition()) == Biomes.SNOWY_PLAINS) {
                 biomeModifier = Config.COLDBIOMEMODIFIER.get();
             }

             //Biome modifier code end

             if(event.getShooter().getMainHandItem().getTag().contains("HasDurability")) {
                 if(event.getShooter().getMainHandItem().getTag().getInt("Durability") != 0) {
                         if (Mth.nextInt(RandomSource.create(), 1, biomeModifier) == 1 && !(biomeModifier == 0)) {
                             if (event.getShooter().getMainHandItem().getTag().getString("GunFireMode").equals("BURST")) {
                                 event.getShooter().getMainHandItem().getTag().putInt("Durability", event.getShooter().getMainHandItem().getTag().getInt("Durability") - 6);
                             } else {
                                 event.getShooter().getMainHandItem().getTag().putInt("Durability", event.getShooter().getMainHandItem().getTag().getInt("Durability") - 2);
                             }
                         } else {
                             if (event.getShooter().getMainHandItem().getTag().getString("GunFireMode").equals("BURST")) {
                                 event.getShooter().getMainHandItem().getTag().putInt("Durability", event.getShooter().getMainHandItem().getTag().getInt("Durability") - 3);
                             } else {
                                 event.getShooter().getMainHandItem().getTag().putInt("Durability", event.getShooter().getMainHandItem().getTag().getInt("Durability") - 1);
                             }
                         }
                 } else {
                     if(Config.JAMCHANCE.get() != 1) {
                         event.getShooter().getMainHandItem().getOrCreateTag().putBoolean("Jammed", true);
                         event.getShooter().getMainHandItem().getOrCreateTag().putInt("SavedAmmo", event.getShooter().getMainHandItem().getTag().getInt("GunCurrentAmmoCount"));
                         event.getShooter().getMainHandItem().getOrCreateTag().putInt("GunCurrentAmmoCount", 0);
                         event.getShooter().getMainHandItem().getOrCreateTag().putBoolean("BulletInBarrel", false);
                         event.getShooter().playSound(GundurabilityModSounds.JAMSFX.get());
                         Player player = (Player) event.getShooter();
                         player.displayClientMessage(MutableComponent.create(Component.literal("Jammed!").getContents()).withStyle(ChatFormatting.RED), true);
                     }
                 }
             } else {
                 event.getShooter().getMainHandItem().getOrCreateTag().putBoolean("HasDurability", true);
                 event.getShooter().getMainHandItem().getOrCreateTag().putInt("Durability", Config.MAXDURABILITY.get());
             }

             if(!event.getShooter().getMainHandItem().getTag().getBoolean("Jammed") && Config.JAMCHANCE.get() != 1) {
                if(Mth.nextInt(RandomSource.create(), -1, event.getShooter().getMainHandItem().getTag().getInt("Durability") / Config.JAMCHANCE.get()) == 0) {

                    event.getShooter().getMainHandItem().getOrCreateTag().putBoolean("Jammed", true);
                    event.getShooter().getMainHandItem().getOrCreateTag().putInt("SavedAmmo", event.getShooter().getMainHandItem().getTag().getInt("GunCurrentAmmoCount"));
                    event.getShooter().getMainHandItem().getOrCreateTag().putInt("GunCurrentAmmoCount", 0);
                    event.getShooter().getMainHandItem().getOrCreateTag().putBoolean("BulletInBarrel", false);
                    event.getShooter().playSound(GundurabilityModSounds.JAMSFX.get());
                    Player player = (Player) event.getShooter();
                    player.displayClientMessage(MutableComponent.create(Component.literal("Jammed!").getContents()).withStyle(ChatFormatting.RED), true);

                }
             }
         }
        if (event.getShooter().getMainHandItem().getTag().getInt("Durability") <= 0) {
            if(event.getShooter().level().getGameRules().getBoolean(GundurabilityModGameRules.GUNBREAK)) {
                event.getShooter().getMainHandItem().setCount(0);
                Player player = (Player) event.getShooter();
                player.displayClientMessage(MutableComponent.create(Component.literal("Your gun broke!").getContents()).withStyle(ChatFormatting.YELLOW), true);
                event.getShooter().level().playSound(null, event.getShooter().blockPosition(), SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS, 1f, 1f);
            }
        }
    }
}
