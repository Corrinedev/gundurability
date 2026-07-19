package mod.cdv.gdb;

import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.AttachmentPropertyEvent;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.api.resource.ResourceManager;
import com.tacz.guns.item.AttachmentItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.*;
import mod.cdv.gdb.init.ModItems;
import mod.cdv.gdb.init.ModTabs;
import mod.cdv.gdb.network.NetworkHandler;
import mod.cdv.gdb.network.SyncDamageNBTPacket;
import mod.cdv.gdb.network.SyncGunModifiersPacket;
import mod.cdv.gdb.network.SyncJammedPacket;
import mod.cdv.gdb.resource.GunModifier;
import mod.cdv.gdb.resource.ResourceLoader;
import mod.cdv.gdb.resource.StatModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import static mod.cdv.gdb.init.ModTabs.TAB;

@Mod(GunDurability.MODID)
public class GunDurability {
    public static final String MODID = "gundb";
    public static final Logger LOGGER = Logger.getLogger(MODID);
    public static final HashMap<LivingEntity, TimedWork<LivingEntity>> jamWorker = new HashMap<>();

    public GunDurability() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        //modEventBus.register(new ModItems());
        //modEventBus.register(new ModTabs());
        MinecraftForge.EVENT_BUS.addListener(GunDurability::addReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(GunDurability::fireEvent);
        MinecraftForge.EVENT_BUS.addListener(GunDurability::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(GunDurability::onEquipmentSwap);
        MinecraftForge.EVENT_BUS.addListener(GunDurability::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(GunDurability::anvilChangeEvent);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, GunDurability::attachmentPropertyEvent);
        NetworkHandler.register();
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ArrayList<GunModifier> modifiers = DataLookup.getAllModifiers();
            if (!modifiers.isEmpty()) {
                if(ServerLifecycleHooks.getCurrentServer() != null)
                    NetworkHandler.sendToClient(new SyncGunModifiersPacket(modifiers), player);
            }
        }
    }

    public static void onEquipmentSwap(LivingEquipmentChangeEvent event) {
        if(event.getEntity() instanceof Player plr)
            jamWorker.remove(plr);
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof ModernKineticGunItem) {
            var item = event.getEntity().getMainHandItem();
            GunModifier modifiers = DataLookup.getModifiers(item);
            if (modifiers != null && jamWorker.get(event.getEntity()) != null) {
                jamWorker.get(event.getEntity()).poll(event.getEntity());
            }
        }
    }

    //public void onRecipesUpdated(RecipesUpdatedEvent event) {
    //    DataLookup.createPartData();

    //    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    //    CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters(
    //            server.getWorldData().enabledFeatures(),
    //            false,
    //            server.registryAccess()
    //    );
    //    TAB.buildContents(params);

    //}

    public static void addReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(ResourceLoader.INSTANCE);
    }

    public static final Random random = new Random();

    public static void fireEvent(GunFireEvent event) {
        var item = event.getGunItemStack();
        if (event.getLogicalSide().isClient()) return;
        if (event.getShooter() instanceof Player plr) {
            GunModifier modifiers = DataLookup.getModifiers(item);

            HashMap<ItemStack, GunModifier> attachmentModifiers = new HashMap<>();
            ModernKineticGunItem gun = (ModernKineticGunItem) item.getItem();
            for (var type : AttachmentType.values()) {
                ItemStack stack = gun.getAttachment(item, type);
                if (stack.isEmpty()) continue;
                GunModifier attachmentModifier = DataLookup.getModifiers(stack);
                if (attachmentModifier != null) {
                    if (stack.getMaxDamage() <= stack.getDamageValue()) {
                        IGun iGun = IGun.getIGunOrNull(item);
                        if (iGun != null) {
                            ItemStack attachmentItem = iGun.getAttachment(item, type);
                            if (!attachmentItem.isEmpty() && plr.getInventory().add(attachmentItem)) {
                                iGun.unloadAttachment(item, type);
                                AttachmentPropertyManager.postChangeEvent(plr, item);
                                if (type == AttachmentType.EXTENDED_MAG) {
                                    iGun.dropAllAmmo(plr, item);
                                }

                                plr.inventoryMenu.broadcastChanges();
                                plr.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 2.5f, 0.6f);
                            }
                        }
                        return;
                    }
                    attachmentModifiers.put(stack, attachmentModifier);
                }

            }

            if (modifiers == null && attachmentModifiers.isEmpty()) return;
            if (modifiers != null) {
                //Jam cancel
                if (item.getTag() != null && item.getTag().contains("Jammed") && item.getTag().getBoolean("Jammed")) {
                    event.setCanceled(true);
                    return;
                }
                //Zero durability cancel
                if (item.getDamageValue() == item.getMaxDamage()) {
                    if (!modifiers.preventFiring())
                        event.setCanceled(true);
                    return;
                }

                float durabilityPercent = ((float) item.getMaxDamage() - item.getDamageValue()) / item.getMaxDamage();
                if (modifiers.jam() && durabilityPercent <= modifiers.jamThreshold()) {

                    float chance = getJamPossibility(durabilityPercent, modifiers.jamChance(), modifiers.jamThreshold());
                    if (Math.random() < chance) {
                        item.getTag().putBoolean("Jammed", true);
                        if (event.getShooter() instanceof ServerPlayer)
                            NetworkHandler.sendToClient(new SyncJammedPacket(true), (ServerPlayer) plr);
                    }
                }
                AttachmentPropertyManager.postChangeEvent(event.getShooter(), item);
                int level = event.getGunItemStack().getEnchantmentLevel(Enchantments.UNBREAKING);
                //Cancel durability loss with Unbreaking
                if (level > 0 && random.nextInt(level + 1) > 0) {
                    return;
                }

                int dmg = event.getGunItemStack().getDamageValue() + 1;
                item.setDamageValue(dmg);
                if (event.getShooter() instanceof ServerPlayer sv)
                    NetworkHandler.sendToClient(new SyncDamageNBTPacket(item, dmg), sv);

                item.getTag().putInt("RepairCost", 0);
            }
            for (var mod : attachmentModifiers.entrySet()) {
                ItemStack aItem = mod.getKey();
                AttachmentType type = ((AttachmentItem)aItem.getItem()).getType(aItem);
                int dmg = switch (type) {
                    case MUZZLE, STOCK, GRIP -> 1;
                    case SCOPE -> IGunOperator.fromLivingEntity(event.getShooter()).getSynIsAiming() ? 1 : 0;
                    case LASER -> plr.isCrouching() || plr.isVisuallyCrawling() ? 1 : 0;
                    default -> 0;
                };
                int fdmg = aItem.getDamageValue() + dmg;
                aItem.setDamageValue(fdmg);
                if (event.getShooter() instanceof ServerPlayer sv)
                    NetworkHandler.sendToClient(new SyncDamageNBTPacket(aItem, fdmg), sv);
            }
        }
    }

    public static float getJamPossibility(float durabilityPercent, float jamChance, float jamThreshold) {
        return Util.remap(
                durabilityPercent,
                0.00f,                       // new_min: Maps to no reduction when at threshold
                jamChance,         // new_max: Maps to max reduction when broken
                jamThreshold,   // old_min: Start of degradation range
                0.0f                        // old_max: End of degradation range (broken)
        );
    }

    public static void attachmentPropertyEvent(AttachmentPropertyEvent event) {
        ItemStack item = event.getGunItem();
        var modifier = DataLookup.getModifiers(item);

        HashMap<ItemStack, GunModifier> attachmentModifiers = new HashMap<>();
        ModernKineticGunItem gun = (ModernKineticGunItem) item.getItem();
        for (var type : AttachmentType.values()) {
            ItemStack stack = gun.getAttachment(item, type);
            if(stack.isEmpty()) continue;
            GunModifier attachmentModifier = DataLookup.getModifiers(stack);
            if (attachmentModifier != null)
                attachmentModifiers.put(stack, attachmentModifier);
        }

        if (modifier == null && attachmentModifiers.isEmpty()) return;

        var cache = event.getCacheProperty();
        var gunId = ResourceLocation.parse(item.getTag().getString("GunId"));


        HashMap<GunProperty<?>, Float> combinedMultipliers = new HashMap<>();
        // For the base gun modifier (uses gun durability)
        if (modifier != null) {
            float durabilityPercent = ((float) item.getMaxDamage() - item.getDamageValue()) / item.getMaxDamage();
            for (StatModifier mod : modifier.modifiers()) {
                applyModifier(mod, durabilityPercent, combinedMultipliers);
            }
        }

        // For each attachment (uses attachment's own durability)
        for (Map.Entry<ItemStack, GunModifier> entry : attachmentModifiers.entrySet()) {
            ItemStack attachmentStack = entry.getKey();
            float durabilityPercent = ((float) attachmentStack.getMaxDamage() - attachmentStack.getDamageValue()) / attachmentStack.getMaxDamage();
            for (StatModifier mod : entry.getValue().modifiers()) {
                applyModifier(mod, durabilityPercent, combinedMultipliers);
            }
        }

        for (Map.Entry<GunProperty<?>, Float> entry : combinedMultipliers.entrySet()) {
            var reductionFactor = entry.getValue();
            var type = entry.getKey();
            switch (type.name()) {
                case "pierce", "rpm" -> {
                    GunProperty<Integer> intType = (GunProperty<Integer>) type;
                    float val = cache.getCache(intType);
                    cache.setCache(intType, Math.round(val * reductionFactor));
                }
                case "armor_ignore", "ammo_speed", "effective_range", "head_shot", "knockback",
                     "weight_modifier" -> {
                    GunProperty<Float> floatType = (GunProperty<Float>) type;
                    float val = cache.getCache(floatType);
                    cache.setCache(floatType, val * reductionFactor);
                }
                case "ads" -> {
                    GunProperty<Float> floatType = (GunProperty<Float>) type;
                    float val = cache.getCache(floatType);
                    cache.setCache(floatType, val / reductionFactor);
                }
                case "damage" -> {
                    GunProperty<LinkedList<ExtraDamage.DistanceDamagePair>> damageType =
                            (GunProperty<LinkedList<ExtraDamage.DistanceDamagePair>>) type;
                    var val = cache.getCache(damageType);
                    LinkedList<ExtraDamage.DistanceDamagePair> newList = new LinkedList<>();
                    for (ExtraDamage.DistanceDamagePair pair : val) {
                        newList.add(new ExtraDamage.DistanceDamagePair(
                                pair.getDistance() * reductionFactor,
                                pair.getDamage() * reductionFactor
                        ));
                    }
                    cache.setCache(damageType, newList);
                }
                case "inaccuracy" -> {
                    GunProperty<Map<InaccuracyType, Float>> inaccuracyType =
                            (GunProperty<Map<InaccuracyType, Float>>) type;
                    var val = cache.getCache(inaccuracyType);
                    for (Map.Entry<InaccuracyType, Float> inEntry : val.entrySet()) {
                        inEntry.setValue(inEntry.getValue() / reductionFactor);
                    }
                    cache.setCache(inaccuracyType, val);
                }
                case "move_speed" -> {
                    GunProperty<MoveSpeed> moveSpeedType = (GunProperty<MoveSpeed>) type;
                    var val = cache.getCache(moveSpeedType);
                    cache.setCache(moveSpeedType, new MoveSpeed(
                            val.getBaseMultiplier() * reductionFactor,
                            val.getAimMultiplier() * reductionFactor,
                            val.getReloadMultiplier() * reductionFactor
                    ));
                }
                case "recoil" -> {
                    var gunData = TimelessAPI.getCommonGunIndex(gunId).get().getGunData();
                    GunRecoil recoil = gunData.getRecoil();
                    var recoilType = (GunProperty<ParameterizedCachePair<Float, Float>>) type;

                    ParameterizedCachePair<Float, Float> value;
                    if (recoil == null) {
                        value = ParameterizedCachePair.of(0.0F, 0.0F);
                    } else {
                        float pitch = getMaxInGunRecoilKeyFrame(recoil.getPitch());
                        float yaw = getMaxInGunRecoilKeyFrame(recoil.getYaw());
                        value = ParameterizedCachePair.of(
                                pitch * reductionFactor,
                                yaw * reductionFactor
                        );
                    }
                    cache.setCache(recoilType, value);
                }
            }
        }
    }

    public static void anvilChangeEvent(AnvilUpdateEvent event) {
    }

    private static float getMaxInGunRecoilKeyFrame(GunRecoilKeyFrame[] frames) {
        if (frames.length == 0) {
            return 0.0F;
        } else {
            float[] value = frames[0].getValue();
            float leftValue = Math.abs(value[0]);
            float rightValue = Math.abs(value[1]);
            return Math.max(leftValue, rightValue);
        }
    }

    private static void applyModifier(StatModifier mod, float durabilityPercent, HashMap<GunProperty<?>, Float> combinedMultipliers) {
        if (durabilityPercent > mod.reductionThreshold()) return;
        float survivalFactor = Util.remap(durabilityPercent, 0.0f, mod.maxReduction(), mod.reductionThreshold(), 0.0f);
        survivalFactor = 1.0f - Math.max(0.0f, Math.min(survivalFactor, mod.maxReduction()));
        for (GunProperty<?> type : mod.types()) {
            combinedMultipliers.merge(type, survivalFactor, (a, b) -> a * b);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        }
    }
}
